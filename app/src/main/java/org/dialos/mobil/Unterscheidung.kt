package org.dialos.mobil

/**
 * Macht gleich klingende Auswahlvorschläge unterscheidbar.
 *
 * **Der Anlass, 28.10.2026.** Aus der Praxis gemeldet: „Ich habe nach
 * meinem Namen gefragt und bekomme aus meinen Kontaktspeicherungen mehrere
 * Vorschläge, die ich aber nach Vorlesen nicht unterscheiden kann, weil die
 * zugehörige Nummer nicht mit angesagt wird. Da kommt beispielsweise
 * ‚Möchten Sie … oder … zuhause anrufen?' Aber welche Nummern stecken
 * dahinter? Festnetz oder Handy?"
 *
 * Die Auswahl war damit für einen blinden Nutzer nicht zu treffen - er
 * konnte nur raten. Und raten heißt hier: möglicherweise den falschen
 * Anschluss anrufen.
 *
 * **Warum nicht einfach immer die Nummer vorlesen?** Eine elfstellige
 * Nummer je Vorschlag macht eine Liste aus drei Treffern unzumutbar lang,
 * und sie wird laut gesprochen - womöglich in fremder Gegenwart. Deshalb
 * wird nur so viel gesagt, wie zum Unterscheiden nötig ist:
 *
 * 1. Der Name allein, wenn er eindeutig ist.
 * 2. Dazu die Art (Mobil, Privat, Arbeit), wenn zwei Vorschläge zu
 *    verwechseln sind.
 * 3. Dazu die letzten vier Ziffern, wenn auch die Art nicht reicht.
 *
 * **Verwechselbar heißt nicht gleich.** Im gemeldeten Fall hießen die
 * Einträge „X" und „X zuhause" - streng genommen verschiedene Namen, für
 * das Ohr aber derselbe Mensch. Deshalb gilt als verwechselbar, wenn ein
 * normalisierter Name mit dem anderen beginnt.
 */
object Unterscheidung {

    /** Ein Vorschlag, wie er aus dem Adressbuch kommt. */
    data class Eingang(
        val name: String,
        val art: String?,
        val nummer: String?,
    )

    /** Wie viele Endziffern genannt werden, wenn die Art nicht reicht. */
    const val ENDZIFFERN = 4

    /**
     * Gibt zu jedem Eingang den Zusatz zurück, der ihn unterscheidbar
     * macht - oder null, wenn der Name schon reicht.
     *
     * Die Reihenfolge entspricht der der Eingänge.
     */
    fun zusaetze(eintraege: List<Eingang>): List<Zusatz?> {
        val kerne = eintraege.map { NameMatcher.normalize(it.name).trim() }

        return eintraege.indices.map { i ->
            val verwechselbar = eintraege.indices.any { j ->
                j != i && verwechselbar(kerne[i], kerne[j])
            }
            if (!verwechselbar) return@map null

            // Reicht die Art, um diesen von den verwechselbaren zu trennen?
            val gleicheArt = eintraege.indices.any { j ->
                j != i && verwechselbar(kerne[i], kerne[j]) &&
                    artVergleichbar(eintraege[j].art) == artVergleichbar(eintraege[i].art)
            }
            Zusatz(
                art = eintraege[i].art?.takeIf { it.isNotBlank() },
                endziffern = if (gleicheArt) endziffern(eintraege[i].nummer) else null,
            ).takeIf { it.art != null || it.endziffern != null }
        }
    }

    /** Der Zusatz zu einem Namen. Beide Teile können fehlen. */
    data class Zusatz(val art: String?, val endziffern: String?)

    /**
     * Die letzten Ziffern, durch Leerzeichen getrennt.
     *
     * Das Trennen ist kein Schönheitsfehler: Ohne die Leerzeichen liest
     * eine Sprachausgabe „4712" als „viertausendsiebenhundertzwölf" vor -
     * eine Zahl, die niemand mit den Ziffern auf einem Zettel vergleichen
     * kann.
     */
    fun endziffern(nummer: String?): String? {
        val ziffern = nummer?.filter { it.isDigit() } ?: return null
        if (ziffern.length < ENDZIFFERN) return null
        return ziffern.takeLast(ENDZIFFERN).toList().joinToString(" ")
    }

    private fun verwechselbar(a: String, b: String): Boolean =
        a.isNotEmpty() && b.isNotEmpty() && (a.startsWith(b) || b.startsWith(a))

    private fun artVergleichbar(art: String?): String =
        art?.let { NameMatcher.normalize(it).trim() }.orEmpty()
}
