package org.dialos.mobil

import android.Manifest
import android.annotation.SuppressLint
import android.app.ActivityManager
import android.app.AppOpsManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.database.ContentObserver
import android.media.AudioManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.SystemClock
import android.provider.ContactsContract
import android.provider.Settings
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
    private lateinit var volume: VolumeController

    private var activateWhenReady = false

    /** Beim nächsten „bereit“ ansagen, dass die App unterbrochen worden war. */
    private var announceInterruption = false

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
        volume = VolumeController(this)
        dialog = DialogController(this, speaker, contacts, simRepository, prefs, this)

        createNotificationChannel()
        beobachteAdressbuch()
    }

    /**
     * Auf Änderungen im Adressbuch horchen und die Kontakte neu einlesen.
     *
     * Bis 0.6.13 wurde das Adressbuch **einmal** beim Einschalten gelesen. Wer
     * danach einen Kontakt anlegte oder eine Nummer korrigierte, bekam „habe
     * ich in den Kontakten nicht gefunden" - ohne jeden Hinweis, dass die App
     * nur einen veralteten Stand kennt. Aufgefallen beim Test am 21.09.2026:
     * eine Nummer wurde geändert, und die App blieb bei der alten.
     *
     * Die Entprellung ist nötig, weil eine Kontosynchronisierung Dutzende
     * Einzeländerungen meldet; ohne sie läse die App das ganze Adressbuch
     * dutzendfach neu.
     */
    private fun beobachteAdressbuch() {
        if (!contacts.hasPermission()) return
        runCatching {
            contentResolver.registerContentObserver(
                ContactsContract.Contacts.CONTENT_URI, true, adressbuchBeobachter
            )
        }.onFailure { Log.w(TAG, "Adressbuch kann nicht beobachtet werden", it) }
    }

    private val adressbuchBeobachter = object : ContentObserver(Handler(Looper.getMainLooper())) {
        override fun onChange(selfChange: Boolean) {
            mainHandler.removeCallbacks(adressbuchNeuLesen)
            mainHandler.postDelayed(adressbuchNeuLesen, ADRESSBUCH_ENTPRELLUNG_MS)
        }
    }

    private val adressbuchNeuLesen = Runnable {
        scope.launch {
            contacts.reload()
            prefs.lastContactCount = contacts.anzahlKontakte ?: -1
            Log.i(TAG, "Adressbuch nach einer Änderung neu eingelesen: " +
                "${contacts.anzahlKontakte} Kontakte")
        }
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

        // Ab Android 12 entzieht das System einem Vordergrunddienst, der aus
        // dem Hintergrund gestartet wurde, den Mikrofonzugriff - ohne Fehler,
        // ohne Ausnahme. Der Dienst läuft dann weiter, die Benachrichtigung
        // steht, und Vosk wartet auf Audiodaten, die nie kommen. Die App
        // behauptet zuzuhören und ist taub.
        //
        // Genau das ist wochenlang unbemerkt passiert: Auf dem Testgerät lag
        // der letzte Mikrofonzugriff 15 Tage zurück, während die App
        // durchgehend "hört zu" anzeigte. Zwei Testpersonen meldeten, das
        // Aktivierungswort funktioniere nicht - es konnte gar nicht.
        //
        // Deshalb wird ein Hintergrundstart gar nicht erst versucht. Statt
        // stumm zu scheitern, bittet die App hörbar um einen Fingertipp.
        if (istHintergrundstart(intent)) {
            Log.w(TAG, "Start aus dem Hintergrund - ohne Mikrofon sinnlos, " +
                "stattdessen Benachrichtigung")
            publish(ServiceStatus.OFF)
            postTapToStartNotification()
            stopSelf()
            return START_NOT_STICKY
        }

        if (!startAsForegroundService()) return START_NOT_STICKY

        noteStartCause(
            systemRestart = intent == null,
            expected = intent?.getBooleanExtra(EXTRA_EXPECTED_RESTART, false) == true
        )
        prefs.wasRunning = true
        prefs.lastUptime = SystemClock.elapsedRealtime()

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
            prefs.lastContactCount = contacts.anzahlKontakte ?: -1
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
        mainHandler.removeCallbacks(adressbuchNeuLesen)
        runCatching { contentResolver.unregisterContentObserver(adressbuchBeobachter) }
        dialog.shutdown()
        engine.shutdown()
        speaker.shutdown()
        scope.cancel()
        _status.value = ServiceState(ServiceStatus.OFF)
        // Nicht über publish(): das ist hier schon abgebaut. Das Widget muss
        // trotzdem umschalten, sonst behauptet es weiter, die App laufe -
        // gerade beim Abschuss durch Android der irreführendste Fall.
        VoiceWidgetProvider.refresh(this)
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    // -----------------------------------------------------------------------
    // VoiceEngine.Callbacks
    // -----------------------------------------------------------------------

    override fun onEngineReady() {
        Log.i(TAG, "Modell bereit, Erkennung startet (sofort aktivieren: $activateWhenReady)")
        if (!engine.start()) return
        // Bevor die App zum ersten Mal spricht: hörbar sein. Ohne das
        // antwortet sie auf ein stumm gestelltes Telefon unhörbar - und
        // genau dann wird sie gebraucht, wenn niemand hinsieht.
        sorgeFuerHoerbarkeit()
        publish(ServiceStatus.LISTENING)
        updateNotification(getString(R.string.status_listening))
        if (activateWhenReady) {
            activateWhenReady = false
            dialog.activate()
        } else if (announceInterruption) {
            announceInterruption = false
            speaker.speak(getString(R.string.say_after_interruption))
        } else {
            speaker.speak(startAnsage())
        }
    }

    /**
     * Die Ansage beim Einschalten – mit der Zahl der gefundenen Kontakte.
     *
     * Aus einer Rückmeldung vom 22.09.2026: „sie findet nicht meine
     * Kontakte". Die App wusste in dem Moment genau, wie viele sie gelesen
     * hatte, und behielt es für sich. Wer den Bildschirm nicht sehen kann,
     * hat keinen anderen Weg, das nachzuprüfen – und sucht den Fehler
     * deshalb bei der Aussprache des Namens.
     *
     * Die Zahl kommt nur beim **Einschalten**, nicht bei jedem
     * Aktivierungswort. Sonst hörte man sie vor jedem Anruf, und aus einer
     * Diagnosehilfe würde eine Belästigung.
     *
     * Ist das Adressbuch noch nicht gelesen (`null`), bleibt es beim
     * bisherigen Satz: Eine falsche Null wäre schlimmer als keine Zahl.
     */
    private fun startAnsage(): String = when (val anzahl = contacts.anzahlKontakte) {
        null -> getString(R.string.say_started)
        0 -> getString(R.string.say_started_no_contacts)
        1 -> getString(R.string.say_started_one_contact)
        else -> getString(R.string.say_started_contacts, anzahl)
    }

    override fun onPhrase(text: String) {
        // Auch vor jedem Gesprächsbeginn per Aktivierungswort: Das Telefon
        // kann inzwischen stumm gestellt worden sein.
        if (dialog.state == DialogState.WAITING_FOR_WAKE) sorgeFuerHoerbarkeit()
        dialog.onPhrase(text)
    }

    /**
     * Stellt sicher, dass die Ansagen zu hören sind.
     *
     * Hebt die Lautstärke nur an, wenn sie unter der Voreinstellung liegt -
     * wer lauter gestellt hat, behält es. Scheitert es (aktives "Bitte nicht
     * stören"), bleibt nur das Protokoll: Eine Ansage darüber wäre genau so
     * unhörbar wie die, um die es geht.
     */
    private fun sorgeFuerHoerbarkeit() {
        val hoerbar = volume.ensureAudible(prefs.volumePercent)
        if (!hoerbar) {
            Log.w(TAG, "Lautstärke ließ sich nicht anheben - vermutlich " +
                "\"Bitte nicht stören\". Ansagen bleiben womöglich unhörbar.")
        }
    }

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


    /**
     * Ist das Telefon im Flugmodus?
     *
     * Bis 0.6.11 kannte die App den Flugmodus nicht. Sie sagte „Ich rufe
     * Max Mustermann an", der Wählvorgang scheiterte stumm, und erst nach
     * zwölf Sekunden meldete der Anrufwächter, es sei kein Gespräch
     * zustande gekommen - ohne zu sagen, warum. Die Auskunft ist sofort
     * verfügbar, also gehört sie auch sofort gesagt.
     *
     * Ausschalten kann die App den Flugmodus nicht: Das ist seit Android
     * 4.2 Systemapps vorbehalten.
     */
    private fun imFlugmodus(): Boolean = runCatching {
        Settings.Global.getInt(contentResolver, Settings.Global.AIRPLANE_MODE_ON, 0) != 0
    }.getOrDefault(false)

    /**
     * Zeigt eine Benachrichtigung, die zu den Flugmodus-Einstellungen führt.
     *
     * Den Flugmodus selbst ausschalten darf keine App - das ist seit Android
     * 4.2 Systemapps vorbehalten, und daran führt kein Weg vorbei. Was geht:
     * den Nutzer genau dorthin bringen, wo der Schalter sitzt, statt ihn
     * suchen zu lassen.
     *
     * Die Ansage führt hindurch, die Benachrichtigung ist der Türöffner.
     * Sie läuft über den lauten Kanal, damit sie nicht wieder nur für
     * Sehende existiert, und als Vollbild-Hinweis, damit sie bei gesperrtem
     * Bildschirm nicht untergeht.
     */
    private fun zeigeFlugmodusHinweis() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            !hasPermission(Manifest.permission.POST_NOTIFICATIONS)
        ) return

        val einstellungen = PendingIntent.getActivity(
            this, 3,
            Intent(Settings.ACTION_AIRPLANE_MODE_SETTINGS)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        getSystemService<NotificationManager>()?.notify(
            AIRPLANE_NOTIFICATION_ID,
            NotificationCompat.Builder(this, BOOT_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_mic)
                .setContentTitle(getString(R.string.notif_airplane_title))
                .setContentText(getString(R.string.notif_airplane_text))
                .setStyle(
                    NotificationCompat.BigTextStyle()
                        .bigText(getString(R.string.notif_airplane_text))
                )
                .setContentIntent(einstellungen)
                .addAction(0, getString(R.string.notif_airplane_action), einstellungen)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_ERROR)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .build()
        )
        Log.i(TAG, "Flugmodus-Hinweis angezeigt")
    }

    @SuppressLint("MissingPermission")
    override fun onPlaceCall(entry: PhoneEntry?, rawNumber: String, subscriptionId: Int?) {
        if (!hasPermission(Manifest.permission.CALL_PHONE)) {
            speaker.speak(getString(R.string.say_missing_call_permission)) { dialog.goIdle() }
            return
        }
        // Vor dem Wählen, nicht erst zwölf Sekunden danach: Im Flugmodus
        // kommt garantiert kein Gespräch zustande, und der Grund steht fest.
        if (imFlugmodus()) {
            Log.i(TAG, "Flugmodus aktiv - es wird nicht gewählt")
            publish(ServiceStatus.ERROR, getString(R.string.status_airplane_mode))
            zeigeFlugmodusHinweis()
            speaker.speak(getString(R.string.say_airplane_mode)) { dialog.goIdle() }
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

    /**
     * Stellt fest, ob die Sprachsteuerung zwischendurch abgeschossen wurde,
     * und sagt es an.
     *
     * Die Ansage ist der eigentliche Zweck: Stirbt der Dienst, verstummt die
     * App wortlos. Wer nicht auf den Bildschirm sehen kann, merkt das erst,
     * wenn er telefonieren will und nichts passiert. Der Zähler daneben
     * beantwortet die Frage "passiert das öfter?" ohne Kabel und ohne
     * Protokoll - sie kam aus dem Test und war sonst nicht zu klären.
     */
    private fun noteStartCause(systemRestart: Boolean, expected: Boolean) {
        val cause = InterruptionDetector.classify(
            wasRunning = prefs.wasRunning,
            savedUptime = prefs.lastUptime,
            currentUptime = SystemClock.elapsedRealtime(),
            systemRestart = systemRestart
        )
        Log.i(TAG, "Startgrund: $cause (Systemneustart: $systemRestart, erwartet: $expected)")
        if (cause != StartCause.AFTER_INTERRUPTION) return

        // Nach einem App-Update oder einem Neustart des Telefons war der
        // Dienst zwar weg, aber niemand hat ihn abgeschossen. Das als
        // Unterbrechung zu zählen würde den Zähler wertlos machen - er soll
        // ja gerade die Frage beantworten, ob das Gerät die App abräumt.
        if (expected) {
            Log.i(TAG, "Neustart war erwartet (Update oder Systemstart), zählt nicht")
            return
        }

        prefs.interruptions += 1
        prefs.lastInterruptionAt = System.currentTimeMillis()
        Log.w(TAG, "Dienst war unterbrochen (insgesamt ${prefs.interruptions}x)")

        // Erst ansagen, wenn die Erkennung wieder steht - sonst redet die App
        // in einen Zustand hinein, in dem sie noch nicht ansprechbar ist.
        announceInterruption = true
    }

    /**
     * Käme dieser Start ohne Mikrofonzugriff aus dem Hintergrund?
     *
     * Zwei Fälle, beide belegt:
     *  - `intent == null`: Android hat den Dienst nach einem Abschuss per
     *    `START_STICKY` selbst wiederbelebt.
     *  - Das Extra vom [BootReceiver]: Neustart des Telefons oder App-Update.
     *
     * Vor Android 12 gab es die Einschränkung nicht; dort darf der Start
     * weiterlaufen. Zusätzlich fragt die Prüfung das System, ob die
     * Mikrofon-Erlaubnis gerade tatsächlich gilt - sie steht auf
     * "foreground" und hängt damit am Prozesszustand.
     */
    private fun istHintergrundstart(intent: Intent?): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return false

        val ausDemHintergrund = intent == null ||
            intent.getBooleanExtra(EXTRA_EXPECTED_RESTART, false)
        if (!ausDemHintergrund) return false

        // Gegenprobe beim System. Sagt es "erlaubt", lassen wir den Start zu -
        // lieber einmal zu viel versucht als eine Bedienhilfe verweigert.
        val erlaubt = runCatching {
            val ops = getSystemService<AppOpsManager>() ?: return@runCatching true
            ops.unsafeCheckOpNoThrow(
                AppOpsManager.OPSTR_RECORD_AUDIO, android.os.Process.myUid(), packageName
            ) == AppOpsManager.MODE_ALLOWED
        }.getOrDefault(true)

        Log.i(TAG, "Hintergrundstart erkannt, Mikrofon laut System erlaubt: $erlaubt")
        return !erlaubt
    }

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
        // Ausdrücklich ausgeschaltet: Der nächste Start ist dann keine
        // Unterbrechung, sondern ein normaler Einschaltvorgang.
        prefs.wasRunning = false
        announceInterruption = false
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
        val manager = getSystemService<NotificationManager>() ?: return

        // Die Dauerbenachrichtigung ("hört zu") soll nicht stören: leise und
        // ohne Zähler am Symbol.
        manager.createNotificationChannel(
            NotificationChannel(
                CHANNEL_ID,
                getString(R.string.notif_channel_name),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = getString(R.string.notif_channel_desc)
                setShowBadge(false)
            }
        )

        // Die Aufforderung nach einem Neustart dagegen MUSS auffallen.
        // Sie lief bis 0.6.10 über denselben leisen Kanal - also lautlos,
        // ohne Einblendung. Wer nicht auf den Bildschirm sieht, erfuhr nie,
        // dass die Sprachsteuerung auf einen Fingertipp wartet, und hielt
        // die App für kaputt. Gemeldet aus dem Test am 12.09.2026.
        manager.createNotificationChannel(
            NotificationChannel(
                BOOT_CHANNEL_ID,
                getString(R.string.notif_boot_channel_name),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = getString(R.string.notif_boot_channel_desc)
                setShowBadge(true)
                enableVibration(true)
            }
        )
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
        val notification = NotificationCompat.Builder(this, BOOT_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_mic)
            .setContentTitle(getString(R.string.notif_boot_title))
            .setContentText(getString(R.string.notif_boot_text))
            .setContentIntent(open)
            .setAutoCancel(true)
            // Hoch und als Erinnerung eingestuft, damit Android sie
            // einblendet und hörbar macht: Sie ist keine Nebensache, sondern
            // der einzige Weg zurück zur Sprachsteuerung.
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
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
        // Das Widget zeigt denselben Zustand wie die Startseite. Bliebe es
        // stehen, behauptete es "ausgeschaltet", während die App zuhört -
        // eine Anzeige, die etwas anderes sagt als der Zustand, ist
        // schlimmer als gar keine.
        VoiceWidgetProvider.refresh(this)
    }

    companion object {
        private const val TAG = "VoiceService"
        private const val CHANNEL_ID = "voice_control"

        /** Eigener, lauter Kanal für die Aufforderung nach einem Neustart. */
        private const val BOOT_CHANNEL_ID = "voice_control_boot"
        private const val NOTIFICATION_ID = 1
        private const val BOOT_NOTIFICATION_ID = 2
        private const val AIRPLANE_NOTIFICATION_ID = 3
        private const val CALL_POLL_MS = 2_000L

        /**
         * Wartezeit nach der letzten Adressbuchänderung, bevor neu eingelesen
         * wird. Eine Kontosynchronisierung meldet Dutzende Einzeländerungen
         * kurz hintereinander - ohne diese Pause läse die App jedes Mal neu.
         */
        private const val ADRESSBUCH_ENTPRELLUNG_MS = 3_000L

        /**
         * So lange darf ein Anruf brauchen, bis das Telefon den Audio-Modus
         * umstellt. Erst danach gilt als erwiesen, dass nichts zustande kam.
         */
        private const val CALL_SETUP_GRACE_MS = 12_000L
        private const val GOODBYE_DELAY_MS = 1_800L

        const val ACTION_START = "org.dialos.mobil.action.START"

        /** Siehe [start] – unterdrückt das Zählen als Unterbrechung. */
        private const val EXTRA_EXPECTED_RESTART = "expected_restart"
        const val ACTION_STOP = "org.dialos.mobil.action.STOP"
        const val ACTION_ACTIVATE = "org.dialos.mobil.action.ACTIVATE"

        private val _status = MutableStateFlow(ServiceState(ServiceStatus.OFF))

        /** Aktueller Zustand für die Oberfläche und die Schnelleinstellungs-Kachel. */
        val status: StateFlow<ServiceState> = _status.asStateFlow()

        val isRunning: Boolean get() = _status.value.status != ServiceStatus.OFF

        /**
         * Gleicht den gemerkten Zustand mit der Wirklichkeit ab.
         *
         * [_status] lebt im Prozess. Räumt Android nur den Dienst ab und
         * lässt den Prozess stehen - das tun vor allem Xiaomi-Geräte -, dann
         * läuft [onDestroy] nicht zuverlässig durch und der Wert bleibt auf
         * "läuft" hängen. Die Startseite zeigt dann "Sprachsteuerung
         * ausschalten", obwohl nichts mehr läuft, und der große Knopf tut das
         * Gegenteil von dem, was daraufsteht. Genau das hat eine Testperson
         * am 09.09.2026 gemeldet.
         *
         * Deshalb wird beim Öffnen der App nicht dem Gedächtnis geglaubt,
         * sondern beim System nachgefragt. `getRunningServices` ist seit
         * API 26 auf die eigenen Dienste beschränkt - und genau die sind hier
         * gemeint.
         *
         * @return true, wenn der Zustand korrigiert werden musste.
         */
        @Suppress("DEPRECATION")
        fun syncStatus(context: Context): Boolean {
            if (_status.value.status == ServiceStatus.OFF) return false

            val manager = context.getSystemService<ActivityManager>() ?: return false
            val laeuft = runCatching {
                manager.getRunningServices(Int.MAX_VALUE).any {
                    it.service.className == VoiceService::class.java.name
                }
            }.getOrElse { return false }   // Im Zweifel nichts anfassen.

            if (laeuft) return false

            Log.w(TAG, "Dienst ist weg, gemerkter Zustand war ${_status.value.status}")
            _status.value = ServiceState(ServiceStatus.OFF)
            VoiceWidgetProvider.refresh(context)
            return true
        }

        /**
         * @param expected true, wenn der Neustart erklärbar ist – nach einem
         *   Neustart des Telefons oder einem App-Update. Dann war der Dienst
         *   zwar weg, aber nicht abgeschossen, und der Zähler in den
         *   Einstellungen darf nicht hochlaufen. Am Gerät nachgewiesen: Ein
         *   `adb install -r` löste sonst eine Unterbrechung aus.
         */
        fun start(context: Context, expected: Boolean = false) =
            send(context, ACTION_START, expected)

        fun stop(context: Context) = send(context, ACTION_STOP)

        /** Startet den Dienst (falls nötig) und beginnt sofort das Gespräch. */
        fun activate(context: Context) = send(context, ACTION_ACTIVATE)

        private fun send(context: Context, action: String, expected: Boolean = false) {
            val intent = Intent(context, VoiceService::class.java)
                .setAction(action)
                .putExtra(EXTRA_EXPECTED_RESTART, expected)
            runCatching { context.startForegroundService(intent) }
                .onFailure {
                    // Ab Android 12 verweigert das System den Start eines
                    // Vordergrunddienstes aus dem Hintergrund komplett
                    // (ForegroundServiceStartNotAllowedException). Bis 0.6.10
                    // stand hier nur eine Protokollzeile - die App war nach
                    // einem Neustart des Telefons oder einem Update schlicht
                    // aus, ohne dass jemand es erfuhr. Zwei Testpersonen
                    // haben genau das gemeldet.
                    Log.e(TAG, "Dienst konnte nicht gestartet werden", it)
                    zeigeTippAufforderung(context)
                }
        }

        /**
         * Bittet hörbar um einen Fingertipp, wenn der Dienst nicht von selbst
         * starten durfte.
         *
         * Läuft bewusst ohne Dienst-Instanz: In genau dem Fall, für den sie
         * gedacht ist, gibt es keine. Der Kanal ist derselbe laute wie bei
         * [postTapToStartNotification] - eine stumme Benachrichtigung wäre
         * für jemanden, der nicht auf den Bildschirm sieht, nicht vorhanden.
         */
        fun zeigeTippAufforderung(context: Context) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                ContextCompat.checkSelfPermission(
                    context, Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) return

            val manager = context.getSystemService<NotificationManager>() ?: return
            manager.createNotificationChannel(
                NotificationChannel(
                    BOOT_CHANNEL_ID,
                    context.getString(R.string.notif_boot_channel_name),
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = context.getString(R.string.notif_boot_channel_desc)
                    enableVibration(true)
                }
            )

            val open = PendingIntent.getActivity(
                context, 2,
                Intent(context, MainActivity::class.java)
                    .setAction(MainActivity.ACTION_ACTIVATE)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
            manager.notify(
                BOOT_NOTIFICATION_ID,
                NotificationCompat.Builder(context, BOOT_CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_mic)
                    .setContentTitle(context.getString(R.string.notif_boot_title))
                    .setContentText(context.getString(R.string.notif_boot_text))
                    .setContentIntent(open)
                    .setAutoCancel(true)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setCategory(NotificationCompat.CATEGORY_REMINDER)
                    .setDefaults(NotificationCompat.DEFAULT_ALL)
                    .build()
            )
            Log.i(TAG, "Aufforderung zum Antippen angezeigt")
        }
    }
}

enum class ServiceStatus { OFF, PREPARING, LISTENING, ACTIVE, CALLING, ERROR }

data class ServiceState(val status: ServiceStatus, val detail: String? = null)
