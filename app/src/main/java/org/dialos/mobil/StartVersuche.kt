package org.dialos.mobil

/**
 * Zählt die Startversuche der Sprachausgabe und sagt, ob noch einer lohnt.
 *
 * Eigene Klasse, weil sie der einzige Teil dieser Reparatur ist, der sich
 * ohne Gerät prüfen lässt - [Speaker] hängt an Androids `TextToSpeech` und
 * ist damit im Unit-Test nicht erreichbar. Die Entscheidung "noch einmal
 * versuchen oder aufgeben" ist aber genau die Stelle, an der sich ein
 * Denkfehler einnistet: Wer zu oft neu startet, lässt eine blinde Nutzerin
 * auf eine App warten, die nicht wiederkommt; wer gar nicht neu startet,
 * lässt sie dauerhaft stumm.
 *
 * Der Anlass steht in TODO.md unter "Die App kann verstummen, ohne es zu
 * merken" (27.10.2026).
 */
class StartVersuche(private val hoechstens: Int = HOECHSTENS) {

    init {
        require(hoechstens >= 1) { "Mindestens ein Versuch muss erlaubt sein" }
    }

    var gescheiterte = 0
        private set

    /** True, solange noch ein Versuch im Rahmen liegt. */
    fun darfNochmal(): Boolean = gescheiterte < hoechstens

    /** Nach einem gescheiterten Start aufrufen. */
    fun gescheitert() {
        if (gescheiterte < Int.MAX_VALUE) gescheiterte++
    }

    /**
     * Nach einem geglückten Start aufrufen.
     *
     * Setzt den Zähler zurück: Eine Sprachausgabe, die später erneut
     * wegbricht (etwa weil die Engine im Hintergrund aktualisiert wurde),
     * bekommt wieder volle Versuche. Ohne diesen Schritt wäre die App nach
     * drei über Wochen verteilten Aussetzern dauerhaft stumm.
     */
    fun geglueckt() {
        gescheiterte = 0
    }

    /** True, wenn aufgegeben wurde - dann hilft nur noch der Nutzer. */
    fun aufgegeben(): Boolean = !darfNochmal()

    private companion object {
        /**
         * Drei Versuche. Der häufigste beobachtete Grund für einen
         * gescheiterten Start ist ein Zeitproblem - die Sprach-Engine wird
         * gerade vom Play Store aktualisiert, während der Dienst hochkommt.
         * Das ist nach Sekunden vorbei. Hält es länger an, liegt es an
         * etwas, das ein vierter Versuch auch nicht behebt.
         */
        const val HOECHSTENS = 3
    }
}
