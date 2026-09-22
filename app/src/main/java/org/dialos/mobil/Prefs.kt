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
     * Beim Öffnen der App die Sprachsteuerung gleich einschalten.
     *
     * Der Grund ist ein Weg, der ganz ohne Berührung auskommt: „Hey Google,
     * öffne DialOS Mobil" startet die App - bisher blieb die
     * Sprachsteuerung dabei aus, und man musste doch wieder den Knopf
     * treffen. Mit dieser Einstellung wird daraus ein vollständiger
     * Sprachweg. Aus dem Test am 12.09.2026: „Das sollte für Blinde und
     * sehbeeinträchtigte Personen auch mit Sprache möglich sein."
     *
     * Standardmäßig aus, weil es sonst jedes versehentliche Öffnen der App
     * in eine laufende Spracherkennung verwandelt.
     */
    var activateOnOpen: Boolean
        get() = sp.getBoolean(KEY_ACTIVATE_ON_OPEN, false)
        set(value) = sp.edit { putBoolean(KEY_ACTIVATE_ON_OPEN, value) }

    /** Kontraststarke Darstellung (schwarzer Grund, gelbe Schaltflächen). */
    var highContrast: Boolean
        get() = sp.getBoolean(KEY_HIGH_CONTRAST, false)
        set(value) = sp.edit { putBoolean(KEY_HIGH_CONTRAST, value) }

    /** Sprachausgabe auf voller Lautstärke statt der voreingestellten 60 %. */
    var loudMode: Boolean
        get() = sp.getBoolean(KEY_LOUD, false)
        set(value) = sp.edit { putBoolean(KEY_LOUD, value) }

    /** Gewünschte Lautstärke in Prozent. */
    val volumePercent: Int
        get() = if (loudMode) VolumeController.LOUD_PERCENT else VolumeController.NORMAL_PERCENT

    /**
     * Sprechgeschwindigkeit der Ansagen. 1.0 ist Androids Normaltempo.
     *
     * Als feste Stufen statt als Schieberegler: Ein Regler ist für zittrige
     * Hände und ohne Sicht kaum zu treffen, ein Knopf, der weiterschaltet,
     * schon. Wer viel mit Sprachausgabe arbeitet, will es oft deutlich
     * schneller, als Sehende erwarten - deshalb reicht die Reihe bis 1.6.
     */
    var speechRate: Float
        get() = sp.getFloat(KEY_SPEECH_RATE, 1.0f)
        set(value) = sp.edit { putFloat(KEY_SPEECH_RATE, value) }

    /**
     * Name der gewählten Sprachausgabe-Stimme, oder null für die Standardstimme
     * des Telefons. Der Name stammt von Android und ist geräteabhängig -
     * existiert er nicht mehr, greift wieder die Standardstimme.
     */
    var voiceName: String?
        get() = sp.getString(KEY_VOICE, null)
        set(value) = sp.edit { putString(KEY_VOICE, value) }


    /**
     * Ob die Sprachsteuerung beim letzten Ausschalten des Telefons lief.
     * Verhindert, dass [BootReceiver] sie startet, obwohl der Nutzer sie
     * bewusst ausgeschaltet hatte.
     */
    var wasRunning: Boolean
        get() = sp.getBoolean(KEY_WAS_RUNNING, false)
        set(value) = sp.edit { putBoolean(KEY_WAS_RUNNING, value) }

    /**
     * Betriebszeit des Telefons beim letzten Start des Dienstes
     * (`SystemClock.elapsedRealtime`). Zusammen mit [wasRunning] erkennt
     * [InterruptionDetector] daran, ob der Dienst abgeschossen wurde.
     */
    var lastUptime: Long
        get() = sp.getLong(KEY_LAST_UPTIME, 0L)
        set(value) = sp.edit { putLong(KEY_LAST_UPTIME, value) }

    /**
     * Wie oft die Sprachsteuerung unbemerkt beendet wurde, und wann zuletzt.
     *
     * Steht in "Infos & Einstellungen", damit sich die Frage "beendet sich
     * die App immer wieder?" ohne Kabel, ohne Protokoll und ohne Play
     * Console beantworten lässt - auch von jemandem, der nur zuhören kann.
     */
    var interruptions: Int
        get() = sp.getInt(KEY_INTERRUPTIONS, 0)
        set(value) = sp.edit { putInt(KEY_INTERRUPTIONS, value) }

    /** Zeitpunkt der letzten Unterbrechung, oder 0. */
    var lastInterruptionAt: Long
        get() = sp.getLong(KEY_LAST_INTERRUPTION, 0L)
        set(value) = sp.edit { putLong(KEY_LAST_INTERRUPTION, value) }

    /**
     * Wie viele Kontakte der Dienst zuletzt gelesen hat, oder -1 für "noch
     * nie gelesen".
     *
     * Der Dienst schreibt die Zahl, die Einstellungsseite liest sie. Sie
     * könnte das Adressbuch auch selbst abfragen, aber das dauert und
     * gehört nicht in den Hauptthread - und vor allem soll dort stehen, was
     * der **Dienst** tatsächlich kennt, nicht was eine zweite Abfrage
     * gerade findet. Genau diese Unterscheidung ist der Zweck der Anzeige:
     * Eine Testperson meldete am 22.09.2026 "findet meine Kontakte nicht"
     * und hatte keine Möglichkeit nachzusehen, ob die App ihr Adressbuch
     * überhaupt kennt.
     */
    var lastContactCount: Int
        get() = sp.getInt(KEY_CONTACT_COUNT, -1)
        set(value) = sp.edit { putInt(KEY_CONTACT_COUNT, value) }

    companion object {
        /** Die wählbaren Sprechgeschwindigkeiten, in dieser Reihenfolge. */
        val SPEECH_RATES = listOf(0.8f, 1.0f, 1.3f, 1.6f)

        private const val FILE_NAME = "dialos_mobil"
        private const val KEY_AUTOSTART = "autostart"
        private const val KEY_CONFIRM = "confirm_before_call"
        private const val KEY_HOTWORD = "hotword_enabled"
        private const val KEY_WAS_RUNNING = "was_running"
        private const val KEY_HIGH_CONTRAST = "high_contrast"
        private const val KEY_LOUD = "loud_mode"
        private const val KEY_SPEECH_RATE = "speech_rate"
        private const val KEY_VOICE = "voice_name"
        private const val KEY_LAST_UPTIME = "last_uptime"
        private const val KEY_INTERRUPTIONS = "interruptions"
        private const val KEY_LAST_INTERRUPTION = "last_interruption_at"
        private const val KEY_ACTIVATE_ON_OPEN = "activate_on_open"
        private const val KEY_CONTACT_COUNT = "last_contact_count"
    }
}
