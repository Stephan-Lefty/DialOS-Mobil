package org.dialos.mobil

import android.content.Context
import androidx.core.content.edit

/** Einstellungen der App. Bewusst klein gehalten - alles hat einen sinnvollen Standard. */
class Prefs(context: Context) {

    private val sp = context.applicationContext
        .getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE)

    /** Nach einem Neustart des Telefons automatisch wieder einschalten. */
    var autostart: Boolean
        get() = sp.getBoolean(KEY_AUTOSTART, true)
        set(value) = sp.edit { putBoolean(KEY_AUTOSTART, value) }

    /** Vor dem Wählen "… anrufen?" fragen und auf "Ja" warten. */
    var confirmBeforeCall: Boolean
        get() = sp.getBoolean(KEY_CONFIRM, true)
        set(value) = sp.edit { putBoolean(KEY_CONFIRM, value) }

    /**
     * Auf das Aktivierungswort hören. Aus bedeutet: die Sprachsteuerung startet
     * nur über die Schaltfläche, die Kachel oder den Assistenten-Aufruf.
     */
    var hotwordEnabled: Boolean
        get() = sp.getBoolean(KEY_HOTWORD, true)
        set(value) = sp.edit { putBoolean(KEY_HOTWORD, value) }

    /**
     * Ob die Sprachsteuerung beim letzten Ausschalten des Telefons lief.
     * Verhindert, dass [BootReceiver] sie startet, obwohl der Nutzer sie
     * bewusst ausgeschaltet hatte.
     */
    var wasRunning: Boolean
        get() = sp.getBoolean(KEY_WAS_RUNNING, false)
        set(value) = sp.edit { putBoolean(KEY_WAS_RUNNING, value) }

    private companion object {
        const val FILE_NAME = "dialos_mobil"
        const val KEY_AUTOSTART = "autostart"
        const val KEY_CONFIRM = "confirm_before_call"
        const val KEY_HOTWORD = "hotword_enabled"
        const val KEY_WAS_RUNNING = "was_running"
    }
}
