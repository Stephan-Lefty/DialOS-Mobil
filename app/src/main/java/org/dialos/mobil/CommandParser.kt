package org.dialos.mobil

/** Was der Nutzer gesagt hat, in verwertbarer Form. */
sealed interface Command {
    /** "Max Mustermann anrufen" - der Name wurde bereits herausgelöst. */
    data class CallName(val name: String) : Command

    /** "Nummer wählen" - es folgt eine diktierte Rufnummer. */
    data object DialNumber : Command

    /** "Eins", "die Zweite" - Auswahl aus einer Vorschlagsliste (1-basiert). */
    data class Choice(val index: Int) : Command

    data object Yes : Command
    data object No : Command
    data object Cancel : Command
    data object Done : Command
    data object Help : Command
    data object Repeat : Command

    /** "Sprachsteuerung beenden" - die App soll ganz aufhören zuzuhören. */
    data object ShutDown : Command

    /** Nichts Bekanntes - der Rohtext bleibt für die Namenssuche erhalten. */
    data class Unknown(val text: String) : Command
}

/**
 * Übersetzt den Text der Spracherkennung in [Command]s.
 *
 * Bewusst großzügig: die Erkennung liefert oft leicht daneben liegende
 * Wortformen, und ein blinder Nutzer soll nicht raten müssen, welche
 * Formulierung "richtig" ist.
 */
object CommandParser {

    // Alle Muster liegen in normalisierter Form vor (klein, ohne Umlaute),
    // weil auch der erkannte Text vor dem Vergleich normalisiert wird.
    private fun words(vararg entries: String): Set<String> =
        entries.map { NameMatcher.normalize(it) }.toSet()

    private val yes = words(
        "ja", "jawohl", "jo", "genau", "richtig", "stimmt", "korrekt",
        "okay", "ok", "gerne", "bitte", "anrufen", "wählen"
    )
    private val no = words("nein", "nee", "ne", "nö", "falsch", "nicht", "anderer", "andere")
    private val cancel = words(
        "abbrechen", "abbruch", "stopp", "stop", "halt", "zurück",
        "vergiss es", "lass es", "nichts"
    )
    private val done = words("fertig", "ende", "das wars", "das war es", "abschicken", "los")
    private val help = words(
        "hilfe", "was kann ich sagen", "befehle", "welche befehle", "anleitung"
    )
    private val repeat = words("wiederholen", "wiederhole", "nochmal", "noch mal", "noch einmal", "was")
    private val shutdown = words(
        "sprachsteuerung beenden", "sprachsteuerung aus", "sprachsteuerung ausschalten",
        "beenden", "aufhören", "hör auf", "schlafen"
    )
    private val dialNumber = words(
        "nummer wählen", "nummer eingeben", "nummer sprechen", "nummer diktieren",
        "telefonnummer", "telefonnummer wählen", "rufnummer", "rufnummer wählen",
        "nummer", "ziffern"
    )

    /** "Ruf Anna an", "Anna anrufen", "wähle Anna", "telefoniere mit Anna" */
    private val callPrefix = Regex(
        "^(?:bitte\\s+)?(?:ruf|rufe|rufen sie|ruf mal|anrufen|anruf bei|wahle|wahl|wahlen sie|" +
            "telefoniere mit|telefonier mit|verbinde mich mit|verbinde mit|sprich mit)\\s+(.+)$"
    )
    private val callSuffix = Regex("^(.+?)\\s+(?:anrufen|anwahlen|wahlen|an)$")

    private val ordinals = mapOf(
        "eins" to 1, "erste" to 1, "der erste" to 1, "die erste" to 1, "ersten" to 1, "eine" to 1,
        "zwei" to 2, "zweite" to 2, "die zweite" to 2, "zweiten" to 2, "zwo" to 2,
        "drei" to 3, "dritte" to 3, "die dritte" to 3, "dritten" to 3,
        "vier" to 4, "vierte" to 4, "vierten" to 4
    )

    fun parse(rawText: String): Command {
        val text = NameMatcher.normalize(rawText)
        if (text.isEmpty()) return Command.Unknown("")

        // Mehrwortbefehle zuerst - "sprachsteuerung beenden" darf nicht als
        // "beenden" innerhalb eines Namens durchrutschen.
        if (text in shutdown) return Command.ShutDown
        if (text in dialNumber) return Command.DialNumber
        if (text in help) return Command.Help

        if (text in cancel) return Command.Cancel
        if (text in done) return Command.Done
        if (text in repeat) return Command.Repeat
        if (text in yes) return Command.Yes
        if (text in no) return Command.No
        ordinals[text]?.let { return Command.Choice(it) }

        // "nummer eins" / "die zweite nummer"
        Regex("^(?:die\\s+)?(?:nummer\\s+)?(\\w+)(?:\\s+nummer)?$").find(text)?.let { m ->
            ordinals[m.groupValues[1]]?.let { return Command.Choice(it) }
        }

        callPrefix.find(text)?.let { m ->
            val name = m.groupValues[1].removeSuffix(" an").trim()
            if (name.isNotEmpty()) return Command.CallName(name)
        }
        callSuffix.find(text)?.let { m ->
            val name = m.groupValues[1].trim()
            if (name.isNotEmpty() && name !in cancel) return Command.CallName(name)
        }

        if (dialNumber.any { text.contains(it) }) return Command.DialNumber

        return Command.Unknown(text)
    }

    /**
     * Erkennt das Aktivierungswort "Sprachsteuerung starten".
     *
     * Das Modell zerlegt das Kompositum je nach Aussprache unterschiedlich
     * ("sprach steuerung", "sprachsteuerung"), deshalb wird auf die beiden
     * tragenden Wortteile geprüft statt auf den exakten Wortlaut.
     */
    fun isWakePhrase(rawText: String): Boolean {
        val text = NameMatcher.normalize(rawText)
        if (text.isEmpty()) return false
        if (text.contains("sprachsteuerung starten")) return true
        if (text.contains("sprach steuerung starten")) return true

        val hasControl = text.contains("sprachsteuerung") ||
            (text.contains("sprach") && text.contains("steuerung")) ||
            text.contains("steuerung")
        val hasStart = text.contains("starten") || text.contains("start") || text.contains("starte")
        if (hasControl && hasStart) return true

        // Letzte Chance: ähnlich genug am Stück (Verhörer wie "sprachsteuerung startet")
        return text.split(' ').windowed(2, 1, partialWindows = true) { window ->
            NameMatcher.ratio(window.joinToString(" "), "sprachsteuerung starten")
        }.any { it >= 0.82 }
    }
}
