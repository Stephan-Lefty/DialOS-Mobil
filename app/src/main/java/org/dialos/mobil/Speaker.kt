package org.dialos.mobil

import android.content.Context
import android.media.AudioAttributes
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.speech.tts.Voice
import android.util.Log
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong

/**
 * Sprachausgabe.
 *
 * Wichtig für den Dialog: [speak] meldet über [onDone] zurück, wann der Satz
 * wirklich zu Ende gesprochen ist. Erst dann darf das Mikrofon wieder
 * zuhören - sonst erkennt die App ihre eigene Stimme.
 */
class Speaker(context: Context, private val onInitialized: (Boolean) -> Unit = {}) {

    private val app = context.applicationContext
    private val handler = Handler(Looper.getMainLooper())
    private val callbacks = ConcurrentHashMap<String, () -> Unit>()
    private val counter = AtomicLong(0)
    private val pending = mutableListOf<Pair<String, () -> Unit>>()
    private val prefs = Prefs(context)
    private val versuche = StartVersuche()

    /** Was zuletzt an die Sprachausgabe übergeben wurde - siehe [applyPreferences]. */
    private var appliedRate = Float.NaN
    private var appliedVoice: String? = null
    private var voiceEverApplied = false

    /**
     * Drei Zustände statt eines `ready`-Schalters.
     *
     * Der Unterschied zwischen "startet noch" und "gescheitert" ist der
     * ganze Punkt dieser Klasse: Im ersten Fall ist Warten richtig, im
     * zweiten ist Warten der Fehler. Vor dem 27.10.2026 gab es nur
     * `ready = false` für beides - mit der Folge, dass nach einem
     * gescheiterten Start jede Ansage in [pending] verschwand und ihr
     * `onDone` nie aufgerufen wurde. Der Dialog hängt seine Übergänge
     * daran und blieb stehen, während die App weiter zuhörte.
     */
    private enum class Zustand { STARTET, BEREIT, GESCHEITERT }

    @Volatile
    private var zustand = Zustand.STARTET

    private var tts: TextToSpeech = erzeugeTts()

    private fun erzeugeTts() = TextToSpeech(app) { status ->
        val geglueckt = status == TextToSpeech.SUCCESS
        zustand = if (geglueckt) Zustand.BEREIT else Zustand.GESCHEITERT
        if (geglueckt) {
            versuche.geglueckt()
            configure()
        } else {
            versuche.gescheitert()
            Log.w(TAG, "Sprachausgabe ließ sich nicht starten (Versuch " +
                "${versuche.gescheiterte}, Status $status)")
        }
        handler.post {
            onInitialized(geglueckt)
            val queued = synchronized(pending) { pending.toList().also { pending.clear() } }
            if (geglueckt) {
                queued.forEach { (text, done) -> speak(text, done) }
            } else {
                // Ohne Sprachausgabe darf der Dialog nicht hängen bleiben.
                queued.forEach { (_, done) -> handler.post(done) }
            }
        }
    }

    /**
     * Noch ein Startversuch, falls einer übrig ist.
     *
     * Der häufigste Grund für einen gescheiterten Start ist ein
     * Zeitproblem - die Sprach-Engine wird gerade vom Play Store
     * aktualisiert, während der Dienst hochkommt. Das ist nach Sekunden
     * vorbei, und ohne diesen zweiten Versuch bliebe die App bis zum
     * nächsten Einschalten stumm.
     *
     * Gibt zurück, ob ein Versuch gestartet wurde.
     */
    fun erneutStarten(): Boolean {
        if (zustand == Zustand.BEREIT || !versuche.darfNochmal()) return false
        Log.i(TAG, "Starte die Sprachausgabe erneut")
        runCatching { tts.shutdown() }
        zustand = Zustand.STARTET
        tts = erzeugeTts()
        return true
    }

    /** True, wenn die Sprachausgabe läuft. */
    val bereit: Boolean get() = zustand == Zustand.BEREIT

    /**
     * True, wenn alle Startversuche verbraucht sind.
     *
     * Dann hilft nur noch der Nutzer - und das muss ihm auf einem anderen
     * Weg gesagt werden als durch eine Ansage.
     */
    val aufgegeben: Boolean get() = zustand == Zustand.GESCHEITERT && versuche.aufgegeben()

    private fun configure() {
        val result = tts.setLanguage(Locale.GERMANY)
        if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
            Log.w(TAG, "Deutsche Sprachausgabe nicht verfügbar, nutze Standardsprache")
            tts.setLanguage(Locale.getDefault())
        }
        // Medien-Kanal, damit der Lautstärke-Knopf in der App und die
        // Lautstärketasten des Telefons dieselbe Lautstärke regeln wie das,
        // was der Nutzer hört. Siehe Begründung in [VolumeController].
        tts.setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                .build()
        )
        tts.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) = Unit

            override fun onDone(utteranceId: String?) = finish(utteranceId)

            @Suppress("OVERRIDE_DEPRECATION")
            override fun onError(utteranceId: String?) = finish(utteranceId)

            override fun onError(utteranceId: String?, errorCode: Int) = finish(utteranceId)

            override fun onStop(utteranceId: String?, interrupted: Boolean) = finish(utteranceId)
        })
    }

    private fun finish(utteranceId: String?) {
        val callback = utteranceId?.let { callbacks.remove(it) } ?: return
        handler.post(callback)
    }

    /** Spricht [text] und ruft danach [onDone] im Hauptthread auf. */
    fun speak(text: String, onDone: () -> Unit = {}) {
        if (text.isBlank()) {
            handler.post(onDone)
            return
        }
        when (zustand) {
            Zustand.STARTET -> {
                synchronized(pending) { pending += text to onDone }
                return
            }
            Zustand.GESCHEITERT -> {
                if (erneutStarten()) {
                    // Der neue Startversuch leert die Warteschlange, egal
                    // wie er ausgeht - hier darf eingereiht werden.
                    synchronized(pending) { pending += text to onDone }
                } else {
                    // Kein Versuch mehr übrig. Jetzt NICHT einreihen: Die
                    // Warteschlange wird nur beim Start geleert, und der
                    // kommt nicht wieder. Die Ansage ist verloren - aber
                    // der Dialog muss weiterlaufen, sonst steht die App
                    // zusätzlich still.
                    Log.w(TAG, "Stumm, Ansage verworfen: $text")
                    handler.post(onDone)
                }
                return
            }
            Zustand.BEREIT -> Unit
        }
        applyPreferences()
        val id = "dialos-" + counter.incrementAndGet()
        callbacks[id] = onDone
        val params = Bundle().apply {
            putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, id)
        }
        val result = tts.speak(text, TextToSpeech.QUEUE_FLUSH, params, id)
        if (result != TextToSpeech.SUCCESS) {
            Log.w(TAG, "Sprachausgabe fehlgeschlagen für: $text")
            finish(id)
        }
    }

    /**
     * Überträgt Tempo und Stimme aus den Einstellungen, sobald sie sich
     * geändert haben.
     *
     * Vor jeder Ansage statt nur beim Start, weil der Vordergrunddienst
     * tagelang läuft: Eine in den Einstellungen geänderte Stimme soll beim
     * nächsten Satz greifen und nicht erst nach einem Neustart der App.
     */
    private fun applyPreferences() {
        if (!bereit) return

        val rate = prefs.speechRate
        if (rate != appliedRate) {
            tts.setSpeechRate(rate)
            appliedRate = rate
        }

        val wanted = prefs.voiceName
        if (!voiceEverApplied || wanted != appliedVoice) {
            voiceEverApplied = true
            appliedVoice = wanted
            if (wanted != null) {
                // tts.voices wirft bei manchen Engines statt null zu liefern.
                val voice = runCatching { tts.voices }.getOrNull()
                    ?.firstOrNull { it.name == wanted }
                if (voice != null) {
                    tts.setVoice(voice)
                } else {
                    // Die Stimme ist weg (Engine gewechselt, Sprachpaket
                    // gelöscht). Lieber die Standardstimme als Schweigen.
                    Log.w(TAG, "Stimme $wanted nicht mehr vorhanden, nutze Standard")
                    prefs.voiceName = null
                    appliedVoice = null
                }
            }
        }
    }

    /**
     * Die auf diesem Gerät verfügbaren deutschen Stimmen, stabil sortiert.
     * Leer, wenn die Engine keine Auswahl anbietet.
     */
    fun germanVoices(): List<Voice> {
        if (!bereit) return emptyList()
        return runCatching { tts.voices }.getOrNull().orEmpty()
            .filter { it.locale.language == Locale.GERMAN.language && !it.isNetworkConnectionRequired }
            .sortedBy { it.name }
    }

    /** Bricht die laufende Ansage ab. Wartende Rückrufe werden verworfen. */
    fun stop() {
        callbacks.clear()
        if (bereit) tts.stop()
    }

    fun shutdown() {
        callbacks.clear()
        runCatching {
            tts.stop()
            tts.shutdown()
        }
    }

    private companion object {
        const val TAG = "Speaker"
    }
}
