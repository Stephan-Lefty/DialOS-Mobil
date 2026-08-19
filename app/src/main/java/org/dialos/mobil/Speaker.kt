package org.dialos.mobil

import android.content.Context
import android.media.AudioAttributes
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
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

    private val handler = Handler(Looper.getMainLooper())
    private val callbacks = ConcurrentHashMap<String, () -> Unit>()
    private val counter = AtomicLong(0)
    private val pending = mutableListOf<Pair<String, () -> Unit>>()

    @Volatile
    private var ready = false

    private val tts = TextToSpeech(context.applicationContext) { status ->
        ready = status == TextToSpeech.SUCCESS
        if (ready) configure()
        handler.post {
            onInitialized(ready)
            if (ready) {
                val queued = synchronized(pending) { pending.toList().also { pending.clear() } }
                queued.forEach { (text, done) -> speak(text, done) }
            } else {
                // Ohne Sprachausgabe darf der Dialog nicht hängen bleiben.
                val queued = synchronized(pending) { pending.toList().also { pending.clear() } }
                queued.forEach { (_, done) -> done() }
            }
        }
    }

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
        if (!ready) {
            synchronized(pending) { pending += text to onDone }
            return
        }
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

    /** Bricht die laufende Ansage ab. Wartende Rückrufe werden verworfen. */
    fun stop() {
        callbacks.clear()
        if (ready) tts.stop()
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
