package org.dialos.mobil

/** Warum der Dienst gerade startet. */
enum class StartCause {
    /** Der Nutzer hat eingeschaltet. Alles normal. */
    FRESH,

    /** Das Telefon wurde neu gestartet und der Autostart hat gegriffen. */
    AFTER_REBOOT,

    /**
     * Der Dienst lief und ist verschwunden, ohne dass ihn jemand beendet hat
     * und ohne dass das Telefon neu gestartet wurde. Also hat Android ihn
     * abgeräumt - meist wegen der Akku-Optimierung oder weil der Hersteller
     * Hintergrunddienste aggressiv beendet.
     */
    AFTER_INTERRUPTION
}

/**
 * Erkennt, ob die Sprachsteuerung zwischendurch abgeschossen wurde.
 *
 * Der Grund, warum es das gibt: Stirbt der Dienst, verstummt die App
 * einfach. Wer auf den Bildschirm sehen kann, bemerkt die fehlende
 * Benachrichtigung - wer blind ist, merkt es erst, wenn er die App braucht
 * und nichts passiert. Genau dieses stille Versagen ist bei dieser
 * Zielgruppe das teuerste Fehlerbild, deshalb sagt die App es jetzt an.
 *
 * Reine Kotlin-Logik ohne Android-Abhängigkeit, damit sie sich ohne Gerät
 * prüfen lässt.
 */
object InterruptionDetector {

    /**
     * @param wasRunning stand beim letzten Mal "eingeschaltet" in den
     *   Einstellungen? Beim geordneten Ausschalten wird das zurückgesetzt.
     * @param savedUptime Betriebszeit des Telefons beim letzten Dienststart
     *   (`SystemClock.elapsedRealtime`).
     * @param currentUptime Betriebszeit jetzt.
     * @param systemRestart hat Android den Dienst selbst neu gestartet?
     *   Das ist bei `START_STICKY` daran zu erkennen, dass `onStartCommand`
     *   ohne Intent aufgerufen wird - und es passiert nur, wenn der Dienst
     *   vorher abgeräumt wurde.
     */
    fun classify(
        wasRunning: Boolean,
        savedUptime: Long,
        currentUptime: Long,
        systemRestart: Boolean
    ): StartCause = when {
        // Android startet einen Dienst nur dann von sich aus neu, wenn es ihn
        // vorher beendet hat. Das ist der eindeutigste Fall und gilt auch
        // dann, wenn die Einstellungen etwas anderes nahelegen.
        systemRestart -> StartCause.AFTER_INTERRUPTION

        // Regulär eingeschaltet - vorher lief nichts.
        !wasRunning -> StartCause.FRESH

        // Die Betriebszeit zählt seit dem Einschalten des Telefons monoton
        // hoch. Ist sie kleiner als beim letzten Start, war ein Neustart
        // dazwischen. Das braucht keine Toleranz und geht auch nicht schief,
        // wenn die Uhr per Zeitserver korrigiert wird - anders als ein
        // Vergleich der Kalenderzeit.
        currentUptime < savedUptime -> StartCause.AFTER_REBOOT

        // Dieselbe Sitzung, der Dienst lief - und ist trotzdem weg.
        else -> StartCause.AFTER_INTERRUPTION
    }
}
