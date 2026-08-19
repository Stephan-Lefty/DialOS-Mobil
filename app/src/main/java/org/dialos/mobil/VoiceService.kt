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
import android.telephony.PhoneNumberUtils
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
    private lateinit var simRepository: SimRepository

    private var activateWhenReady = false

    /** Wann der Wählvorgang angestoßen wurde - für die Anlaufzeit unten. */
    private var callStartedAt = 0L

    /** Hat das Telefon den Anruf überhaupt angenommen? */
    private var callWasEstablished = false

    /**
     * Beobachtet den Anruf.
     *
     * Zwei Phasen, und die erste hat lange gefehlt: Ein Anruf braucht ein
     * paar Sekunden, bis das Telefon den Audio-Modus umstellt - wählen,
     * klingeln. Wer sofort prüft, ob der Modus normal ist, hält jeden
     * frisch gestarteten Anruf für längst beendet und räumt auf. Deshalb
     * gilt erst nach [CALL_SETUP_GRACE_MS] als erwiesen, dass gar nichts
     * zustande kam - und das wird dann ausdrücklich angesagt, statt den
     * Nutzer im Unklaren zu lassen.
     */
    private val callWatcher = object : Runnable {
        override fun run() {
            val mode = getSystemService<AudioManager>()?.mode ?: AudioManager.MODE_NORMAL
            val inCall = mode != AudioManager.MODE_NORMAL
            val elapsed = System.currentTimeMillis() - callStartedAt

            if (inCall) {
                if (!callWasEstablished) {
                    Log.i(TAG, "Anruf steht (nach ${elapsed} ms, Audio-Modus $mode)")
                    callWasEstablished = true
                }
                mainHandler.postDelayed(this, CALL_POLL_MS)
                return
            }

            if (callWasEstablished) {
                Log.i(TAG, "Gespräch beendet")
                dialog.onCallEnded()
                engine.setPaused(false)
                return
            }

            if (elapsed < CALL_SETUP_GRACE_MS) {
                mainHandler.postDelayed(this, CALL_POLL_MS)
                return
            }

            // Nach der Anlaufzeit immer noch kein Gespräch: Der Wählvorgang
            // ist gescheitert, ohne dass jemand eine Rückmeldung bekommen
            // hätte. Genau dieses stille Versagen ist für einen blinden
            // Nutzer das schlimmste Fehlerbild.
            Log.w(TAG, "Kein Anruf zustande gekommen (${elapsed} ms ohne Moduswechsel)")
            speaker.speak(getString(R.string.say_call_failed)) { dialog.goIdle() }
        }
    }

    override fun onCreate() {
        super.onCreate()
        prefs = Prefs(this)
        contacts = ContactRepository(this)
        speaker = Speaker(this)
        engine = VoiceEngine(this, this)
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
        scope.launch {
            contacts.reload()
            // Einmal beim Start festhalten, welche Karten erkannt wurden und
            // wie sie angesagt würden - ohne das lässt sich ein Fehler in der
            // Kartenwahl nur durch Sprechen finden.
            val cards = simRepository.activeSims()
            Log.i(TAG, "Karten erkannt: ${cards.size} – " +
                cards.joinToString { "Slot ${it.slotIndex + 1}: \"${it.label}\" (id=${it.subscriptionId})" })
        }
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
            DialogState.CALLING -> ServiceStatus.CALLING
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


    @SuppressLint("MissingPermission")
    override fun onPlaceCall(entry: PhoneEntry?, rawNumber: String, subscriptionId: Int?) {
        if (!hasPermission(Manifest.permission.CALL_PHONE)) {
            speaker.speak(getString(R.string.say_missing_call_permission)) { dialog.goIdle() }
            return
        }
        // Rufnummern stehen im Adressbuch oft mit Leerzeichen, Bindestrichen
        // oder Klammern ("+49 176 1234-5678"). Uri.fromParts kodiert den Teil
        // hinter "tel:" NICHT - eine solche Adresse ist ungültig, und Telecom
        // verwirft sie stillschweigend, ohne eine Ausnahme zu werfen.
        val number = PhoneNumberUtils.normalizeNumber(rawNumber).ifEmpty {
            rawNumber.filter { it.isDigit() || it in "+*#" }
        }
        if (number.isEmpty()) {
            Log.e(TAG, "Rufnummer nach dem Aufbereiten leer (roh: ${mask(rawNumber)})")
            speaker.speak(getString(R.string.say_call_failed)) { dialog.goIdle() }
            return
        }
        Log.i(TAG, "Wähle ${mask(number)} (roh: ${mask(rawNumber)}, Karte: $subscriptionId)")

        val uri = Uri.fromParts("tel", number, null)
        val telecom = getSystemService<TelecomManager>()
        val started = runCatching {
            // Über den Telecom-Dienst wählen statt per ACTION_CALL: eine
            // Activity aus einem Hintergrunddienst zu starten ist seit
            // Android 10 gesperrt, placeCall funktioniert dagegen zuverlässig.
            val extras = Bundle()
            // Ohne diesen Zusatz nimmt Android die voreingestellte Karte -
            // bei zwei Karten also womöglich nicht die, die der Nutzer
            // gerade gesagt hat.
            val account = subscriptionId?.let { simRepository.phoneAccountFor(it) }
            if (account != null) {
                extras.putParcelable(TelecomManager.EXTRA_PHONE_ACCOUNT_HANDLE, account)
                Log.i(TAG, "Telefonie-Zugang: ${account.id}")
            } else if (subscriptionId != null) {
                Log.w(TAG, "Kein Telefonie-Zugang zu Karte $subscriptionId gefunden – " +
                    "Anruf geht über die voreingestellte Karte")
            }
            checkNotNull(telecom).placeCall(uri, extras)
            Log.i(TAG, "placeCall zurückgekehrt, warte auf den Moduswechsel")
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
            callStartedAt = System.currentTimeMillis()
            callWasEstablished = false
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

    /** Rufnummern nur angedeutet protokollieren - das Protokoll ist lesbar. */
    private fun mask(number: String): String =
        if (number.length <= 4) "…" else number.take(3) + "…" + number.takeLast(2)

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

        /**
         * So lange darf ein Anruf brauchen, bis das Telefon den Audio-Modus
         * umstellt. Erst danach gilt als erwiesen, dass nichts zustande kam.
         */
        private const val CALL_SETUP_GRACE_MS = 12_000L
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
