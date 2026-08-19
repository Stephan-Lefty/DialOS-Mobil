package org.dialos.mobil

import android.Manifest
import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.media.AudioManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.telecom.TelecomManager
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.core.content.getSystemService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Hält die Spracherkennung am Leben, solange die Sprachsteuerung eingeschaltet
 * ist. Läuft als Vordergrunddienst, weil Android nur so dauerhaft Zugriff auf
 * das Mikrofon erlaubt.
 */
class VoiceService : Service(), VoiceEngine.Callbacks, DialogController.Listener {

    private val mainHandler = Handler(Looper.getMainLooper())
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private lateinit var prefs: Prefs
    private lateinit var contacts: ContactRepository
    private lateinit var speaker: Speaker
    private lateinit var engine: VoiceEngine
    private lateinit var dialog: DialogController
    private lateinit var sms: SmsSender
    private lateinit var simRepository: SimRepository

    private var activateWhenReady = false

    /** Prüft nach einem Anruf, ob das Gespräch beendet ist. */
    private val callWatcher = object : Runnable {
        override fun run() {
            val audioManager = getSystemService<AudioManager>()
            if (audioManager == null || audioManager.mode == AudioManager.MODE_NORMAL) {
                dialog.onCallEnded()
                engine.setPaused(false)
            } else {
                mainHandler.postDelayed(this, CALL_POLL_MS)
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        prefs = Prefs(this)
        contacts = ContactRepository(this)
        speaker = Speaker(this)
        engine = VoiceEngine(this, this)
        sms = SmsSender(this)
        simRepository = SimRepository(this)
        dialog = DialogController(this, speaker, contacts, simRepository, prefs, this)

        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP -> {
                stopEverything()
                return START_NOT_STICKY
            }

            ACTION_ACTIVATE -> activateWhenReady = true
        }

        if (!hasPermission(Manifest.permission.RECORD_AUDIO)) {
            publish(ServiceStatus.ERROR, getString(R.string.perm_needed))
            stopSelf()
            return START_NOT_STICKY
        }

        if (!startAsForegroundService()) return START_NOT_STICKY

        prefs.wasRunning = true

        if (engine.isListening) {
            if (activateWhenReady) {
                activateWhenReady = false
                dialog.activate()
            }
            return START_STICKY
        }

        publish(ServiceStatus.PREPARING)
        scope.launch { contacts.reload() }
        engine.prepare()
        return START_STICKY
    }

    override fun onDestroy() {
        mainHandler.removeCallbacks(callWatcher)
        dialog.shutdown()
        engine.shutdown()
        speaker.shutdown()
        scope.cancel()
        _status.value = ServiceState(ServiceStatus.OFF)
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    // -----------------------------------------------------------------------
    // VoiceEngine.Callbacks
    // -----------------------------------------------------------------------

    override fun onEngineReady() {
        Log.i(TAG, "Modell bereit, Erkennung startet (sofort aktivieren: $activateWhenReady)")
        if (!engine.start()) return
        publish(ServiceStatus.LISTENING)
        updateNotification(getString(R.string.status_listening))
        if (activateWhenReady) {
            activateWhenReady = false
            dialog.activate()
        } else {
            speaker.speak(getString(R.string.say_started))
        }
    }

    override fun onPhrase(text: String) = dialog.onPhrase(text)

    override fun onEngineError(message: String) {
        Log.e(TAG, "Engine-Fehler: $message")
        publish(ServiceStatus.ERROR, message)
        updateNotification(getString(R.string.status_error, message))
    }

    // -----------------------------------------------------------------------
    // DialogController.Listener
    // -----------------------------------------------------------------------

    override fun onDialogStateChanged(state: DialogState, spokenHint: String?) {
        Log.i(TAG, "Zustand: $state${spokenHint?.let { " – \"$it\"" }.orEmpty()}")
        val status = when (state) {
            DialogState.WAITING_FOR_WAKE -> ServiceStatus.LISTENING
            DialogState.CALLING, DialogState.SENDING -> ServiceStatus.CALLING
            else -> ServiceStatus.ACTIVE
        }
        publish(status, spokenHint)
        updateNotification(
            spokenHint ?: when (status) {
                ServiceStatus.LISTENING -> getString(R.string.status_listening)
                else -> getString(R.string.status_active)
            }
        )
    }

    override fun onPauseRecognition(paused: Boolean) = engine.setPaused(paused)

    override fun onSendMessage(
        entry: PhoneEntry?,
        rawNumber: String,
        text: String,
        subscriptionId: Int?
    ) {
        if (!hasPermission(Manifest.permission.SEND_SMS)) {
            speaker.speak(getString(R.string.say_missing_sms_permission)) { dialog.goIdle() }
            return
        }
        sms.send(rawNumber, text, subscriptionId) { success, error ->
            mainHandler.post { dialog.onMessageResult(success, error) }
        }
    }

    @SuppressLint("MissingPermission")
    override fun onPlaceCall(entry: PhoneEntry?, rawNumber: String, subscriptionId: Int?) {
        if (!hasPermission(Manifest.permission.CALL_PHONE)) {
            speaker.speak(getString(R.string.say_missing_call_permission)) { dialog.goIdle() }
            return
        }
        val uri = Uri.fromParts("tel", rawNumber, null)
        val telecom = getSystemService<TelecomManager>()
        val started = runCatching {
            // Über den Telecom-Dienst wählen statt per ACTION_CALL: eine
            // Activity aus einem Hintergrunddienst zu starten ist seit
            // Android 10 gesperrt, placeCall funktioniert dagegen zuverlässig.
            val extras = Bundle()
            // Ohne diesen Zusatz nimmt Android die voreingestellte Karte -
            // bei zwei Karten also womöglich nicht die, die der Nutzer
            // gerade gesagt hat.
            subscriptionId
                ?.let { simRepository.phoneAccountFor(it) }
                ?.let { extras.putParcelable(TelecomManager.EXTRA_PHONE_ACCOUNT_HANDLE, it) }
            checkNotNull(telecom).placeCall(uri, extras)
            true
        }.getOrElse { error ->
            Log.w(TAG, "placeCall fehlgeschlagen, versuche ACTION_CALL", error)
            runCatching {
                startActivity(
                    Intent(Intent.ACTION_CALL, uri).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                )
                true
            }.getOrElse { fallbackError ->
                Log.e(TAG, "Anruf konnte nicht gestartet werden", fallbackError)
                false
            }
        }

        if (started) {
            mainHandler.postDelayed(callWatcher, CALL_POLL_MS)
        } else {
            speaker.speak(getString(R.string.say_call_failed)) { dialog.goIdle() }
        }
    }

    // -----------------------------------------------------------------------
    // Vordergrunddienst und Benachrichtigung
    // -----------------------------------------------------------------------

    private fun startAsForegroundService(): Boolean {
        val notification = buildNotification(getString(R.string.status_loading))
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                startForeground(
                    NOTIFICATION_ID,
                    notification,
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE
                )
            } else {
                startForeground(NOTIFICATION_ID, notification)
            }
            true
        } catch (e: Exception) {
            // Android 14+ verbietet den Start eines Mikrofon-Dienstes aus dem
            // Hintergrund (z. B. direkt nach dem Neustart). Dann bleibt nur der
            // Hinweis, die Sprachsteuerung von Hand einzuschalten.
            Log.e(TAG, "Vordergrunddienst konnte nicht gestartet werden", e)
            publish(ServiceStatus.ERROR, getString(R.string.notif_boot_text))
            postTapToStartNotification()
            stopSelf()
            false
        }
    }

    private fun stopEverything() {
        prefs.wasRunning = false
        speaker.stop()
        speaker.speak(getString(R.string.say_stopped))
        engine.stop()
        publish(ServiceStatus.OFF)
        // Der Abschiedssatz soll noch zu hören sein.
        mainHandler.postDelayed({
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
        }, GOODBYE_DELAY_MS)
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            getString(R.string.notif_channel_name),
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = getString(R.string.notif_channel_desc)
            setShowBadge(false)
        }
        getSystemService<NotificationManager>()?.createNotificationChannel(channel)
    }

    private fun buildNotification(text: String): Notification {
        val open = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )
        val stop = PendingIntent.getService(
            this,
            1,
            Intent(this, VoiceService::class.java).setAction(ACTION_STOP),
            PendingIntent.FLAG_IMMUTABLE
        )
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_mic)
            .setContentTitle(getString(R.string.notif_title))
            .setContentText(text)
            .setContentIntent(open)
            .addAction(0, getString(R.string.notif_stop), stop)
            .setOngoing(true)
            .setSilent(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .build()
    }

    private fun updateNotification(text: String) {
        if (!hasPermission(Manifest.permission.POST_NOTIFICATIONS) &&
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
        ) return
        getSystemService<NotificationManager>()?.notify(NOTIFICATION_ID, buildNotification(text))
    }

    private fun postTapToStartNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            !hasPermission(Manifest.permission.POST_NOTIFICATIONS)
        ) return
        val open = PendingIntent.getActivity(
            this,
            2,
            Intent(this, MainActivity::class.java).setAction(MainActivity.ACTION_ACTIVATE),
            PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_mic)
            .setContentTitle(getString(R.string.notif_boot_title))
            .setContentText(getString(R.string.notif_boot_text))
            .setContentIntent(open)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()
        getSystemService<NotificationManager>()?.notify(BOOT_NOTIFICATION_ID, notification)
    }

    private fun hasPermission(permission: String): Boolean =
        ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED

    private fun publish(status: ServiceStatus, detail: String? = null) {
        _status.value = ServiceState(status, detail)
    }

    companion object {
        private const val TAG = "VoiceService"
        private const val CHANNEL_ID = "voice_control"
        private const val NOTIFICATION_ID = 1
        private const val BOOT_NOTIFICATION_ID = 2
        private const val CALL_POLL_MS = 2_000L
        private const val GOODBYE_DELAY_MS = 1_800L

        const val ACTION_START = "org.dialos.mobil.action.START"
        const val ACTION_STOP = "org.dialos.mobil.action.STOP"
        const val ACTION_ACTIVATE = "org.dialos.mobil.action.ACTIVATE"

        private val _status = MutableStateFlow(ServiceState(ServiceStatus.OFF))

        /** Aktueller Zustand für die Oberfläche und die Schnelleinstellungs-Kachel. */
        val status: StateFlow<ServiceState> = _status.asStateFlow()

        val isRunning: Boolean get() = _status.value.status != ServiceStatus.OFF

        fun start(context: Context) = send(context, ACTION_START)

        fun stop(context: Context) = send(context, ACTION_STOP)

        /** Startet den Dienst (falls nötig) und beginnt sofort das Gespräch. */
        fun activate(context: Context) = send(context, ACTION_ACTIVATE)

        private fun send(context: Context, action: String) {
            val intent = Intent(context, VoiceService::class.java).setAction(action)
            runCatching { context.startForegroundService(intent) }
                .onFailure { Log.e(TAG, "Dienst konnte nicht gestartet werden", it) }
        }
    }
}

enum class ServiceStatus { OFF, PREPARING, LISTENING, ACTIVE, CALLING, ERROR }

data class ServiceState(val status: ServiceStatus, val detail: String? = null)
