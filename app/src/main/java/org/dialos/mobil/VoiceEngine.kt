package org.dialos.mobil

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import org.json.JSONException
import org.json.JSONObject
import org.vosk.Model
import org.vosk.Recognizer
import org.vosk.android.RecognitionListener
import org.vosk.android.SpeechService
import org.vosk.android.StorageService
import java.io.IOException

/**
 * Offline-Spracherkennung mit Vosk - dieselbe Engine, die auch der
 * DialOS-Desktop verwendet. Es verlässt kein Ton das Gerät.
 *
 * Bewusst mit freiem Wortschatz statt mit einer festen Grammatik: die
 * Kontaktnamen sind vorher nicht bekannt, und eine zweite, grammatik-
 * beschränkte Erkennung nur für das Aktivierungswort würde beim Umschalten
 * das Mikrofon kurz freigeben - genau in dem Moment, in dem der Nutzer
 * weiterspricht.
 */
class VoiceEngine(private val context: Context, private val callbacks: Callbacks) {

    interface Callbacks {
        /** Ein abgeschlossener Satz wurde erkannt. */
        fun onPhrase(text: String)

        /** Das Modell ist geladen und die Erkennung kann starten. */
        fun onEngineReady()

        fun onEngineError(message: String)
    }

    private val handler = Handler(Looper.getMainLooper())
    private var model: Model? = null
    private var speechService: SpeechService? = null

    @Volatile
    var isListening: Boolean = false
        private set

    private val recognitionListener = object : RecognitionListener {
        override fun onPartialResult(hypothesis: String?) = Unit

        override fun onResult(hypothesis: String?) {
            extractText(hypothesis)?.let { callbacks.onPhrase(it) }
        }

        override fun onFinalResult(hypothesis: String?) {
            extractText(hypothesis)?.let { callbacks.onPhrase(it) }
        }

        override fun onError(exception: Exception?) {
            Log.e(TAG, "Erkennungsfehler", exception)
            callbacks.onEngineError(exception?.message ?: "Spracherkennung gestört")
        }

        override fun onTimeout() = Unit
    }

    /** Entpackt das Modell (nur beim ersten Start) und meldet sich über [Callbacks]. */
    fun prepare() {
        if (model != null) {
            callbacks.onEngineReady()
            return
        }
        StorageService.unpack(
            context,
            ASSET_MODEL_DIR,
            UNPACKED_DIR_NAME,
            { unpacked ->
                model = unpacked
                handler.post { callbacks.onEngineReady() }
            },
            { exception ->
                Log.e(TAG, "Modell konnte nicht entpackt werden", exception)
                handler.post {
                    callbacks.onEngineError(exception?.message ?: "Sprachmodell fehlt")
                }
            }
        )
    }

    /** Startet die Aufnahme. Muss nach [prepare] aufgerufen werden. */
    fun start(): Boolean {
        val loaded = model ?: return false
        if (speechService != null) return true
        return try {
            val recognizer = Recognizer(loaded, SAMPLE_RATE)
            speechService = SpeechService(recognizer, SAMPLE_RATE).also {
                it.startListening(recognitionListener)
            }
            isListening = true
            true
        } catch (e: IOException) {
            Log.e(TAG, "Mikrofon konnte nicht geöffnet werden", e)
            callbacks.onEngineError("Mikrofon nicht verfügbar")
            false
        } catch (e: SecurityException) {
            Log.e(TAG, "Keine Mikrofon-Berechtigung", e)
            callbacks.onEngineError("Keine Mikrofon-Berechtigung")
            false
        }
    }

    /**
     * Hält die Erkennung an, ohne das Mikrofon freizugeben - wird während der
     * eigenen Sprachausgabe genutzt, damit die App sich nicht selbst zuhört.
     */
    fun setPaused(paused: Boolean) {
        speechService?.setPause(paused)
    }

    fun stop() {
        speechService?.let {
            runCatching { it.stop() }
            runCatching { it.shutdown() }
        }
        speechService = null
        isListening = false
    }

    fun shutdown() {
        stop()
        runCatching { model?.close() }
        model = null
    }

    private fun extractText(hypothesis: String?): String? {
        if (hypothesis.isNullOrBlank()) return null
        return try {
            val text = JSONObject(hypothesis).optString("text").trim()
            text.ifEmpty { null }
        } catch (e: JSONException) {
            Log.w(TAG, "Unerwartete Antwort der Erkennung: $hypothesis", e)
            null
        }
    }

    private companion object {
        const val TAG = "VoiceEngine"
        const val SAMPLE_RATE = 16000.0f

        /** Ordner in den Assets (wird beim Build von Gradle befüllt). */
        const val ASSET_MODEL_DIR = "model-de"

        /** Zielordner im internen Speicher der App. */
        const val UNPACKED_DIR_NAME = "model"
    }
}
