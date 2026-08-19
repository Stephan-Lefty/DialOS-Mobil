package org.dialos.mobil

import android.content.Context
import android.media.AudioManager
import android.util.Log
import androidx.core.content.getSystemService
import kotlin.math.roundToInt

/**
 * Stellt die Lautstärke der Sprachausgabe.
 *
 * Bewusst der Medien-Kanal (`STREAM_MUSIC`) und nicht der Bedienungshilfen-
 * Kanal: den darf eine normale App nicht auf jedem Gerät verändern, und ein
 * Knopf, der manchmal nichts tut, ist schlimmer als keiner. Die
 * Sprachausgabe in [Speaker] ist aus demselben Grund auf denselben Kanal
 * gelegt - so wirkt der Knopf immer, und die Lautstärketasten des Telefons
 * regeln dieselbe Lautstärke.
 */
class VolumeController(context: Context) {

    private val audio = context.applicationContext.getSystemService<AudioManager>()

    /** Aktuelle Lautstärke in Prozent, oder null wenn nicht ermittelbar. */
    fun currentPercent(): Int? {
        val manager = audio ?: return null
        val max = manager.getStreamMaxVolume(STREAM)
        if (max <= 0) return null
        return (manager.getStreamVolume(STREAM) * 100f / max).roundToInt()
    }

    /**
     * Setzt die Lautstärke auf [percent] Prozent.
     * Liefert false, wenn Android das ablehnt - etwa bei aktivem
     * "Bitte nicht stören".
     */
    fun setPercent(percent: Int): Boolean {
        val manager = audio ?: return false
        return try {
            val max = manager.getStreamMaxVolume(STREAM)
            val target = (max * percent / 100f).roundToInt().coerceIn(1, max)
            manager.setStreamVolume(STREAM, target, 0)
            true
        } catch (e: SecurityException) {
            // "Bitte nicht stören" ist aktiv und sperrt Lautstärkeänderungen.
            Log.w(TAG, "Lautstärke darf nicht geändert werden", e)
            false
        }
    }

    companion object {
        const val STREAM = AudioManager.STREAM_MUSIC

        /**
         * Voreinstellung. Wird bei jedem Start der App wieder gesetzt, damit
         * die Ansagen nie unhörbar leise sind - das Telefon stand im Test bei
         * 27 %, und ein blinder Nutzer merkt das erst, wenn die App schweigt.
         */
        const val NORMAL_PERCENT = 50

        /** Umschaltbar für schwerhörige Nutzer oder laute Umgebung. */
        const val LOUD_PERCENT = 100
    }
}

private const val TAG = "VolumeController"
