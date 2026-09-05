package org.dialos.mobil

/**
 * Welche der Nummern eines Kontakts gemeint ist.
 *
 * Bewusst nur diese drei: Sie decken ab, was Leute im Alltag sagen
 * ("privat", "mobil", "Arbeit"). Alles andere bleibt namenlos und wird über
 * die Reihenfolge der Vorschläge erreicht.
 */
enum class PhoneKind { MOBILE, HOME, WORK }

/** Was der Nutzer gesagt hat, in verwertbarer Form. */
sealed interface Command {
    /**
     * "Max Mustermann anrufen" - der Name wurde bereits herausgelöst.
     * [kind] ist gesetzt, wenn zusätzlich eine bestimmte Nummer genannt wurde
     * ("Max Mustermann privat anrufen").
     */
    data class CallName(val name: String, val kind: PhoneKind? = null) : Command

    /** "Privat" / "die Mobilnummer" - eine bestimmte Nummer des Kontakts. */
    data class PickKind(val kind: PhoneKind) : Command

    /** "Nummer wählen" - es folgt eine diktierte Rufnummer. */
    data object DialNumber : Command

    /** "Eins", "die Zweite" - Auswahl aus einer Vorschlagsliste (1-basiert). */
    data class Choice(val index: Int) : Command

    /** "Löschen" / "noch mal von vorn" - die diktierte Nummer wird verworfen. */
    data object Clear : Command

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
    private val clear = words(
        "löschen", "alles löschen", "verwerfen", "von vorn", "von vorne",
        "noch mal von vorn", "nochmal von vorn", "neu anfangen"
    )

    /**
     * Wörter, die einen Befehl begleiten, ohne ihn zu verändern.
     *
     * Ohne sie scheitert die häufigste Antwortform überhaupt: "ja bitte" und
     * "nein danke" wurden bis 0.6.2 nicht verstanden, weil der ganze Satz mit
     * den Wortlisten verglichen wurde statt Wort für Wort.
     */
    private val filler = words(
        "bitte", "danke", "mal", "doch", "schon", "eben", "dann", "also",
        "denn", "jetzt", "gleich", "die", "der", "das", "den", "nummer",
        "nummern", "anschluss", "sie", "ihn", "es"
    )

    /**
     * Die Nummerntypen, wie sie gesprochen werden. Großzügig gehalten, weil
     * jeder sie anders nennt - "privat", "zu Hause", "Festnetz" meinen
     * dasselbe.
     */
    private val kindWords: Map<PhoneKind, Set<String>> = mapOf(
        PhoneKind.MOBILE to words(
            "mobil", "mobile", "mobilnummer", "mobiltelefon", "handy",
            "handynummer", "mobilfunk", "natel"
        ),
        PhoneKind.HOME to words(
            "privat", "private", "privaten", "privatnummer", "zuhause",
            "daheim", "festnetz", "festnetznummer", "haus", "hause", "wohnung"
        ),
        PhoneKind.WORK to words(
            "arbeit", "arbeitsnummer", "büro", "buro", "geschäftlich",
            "geschäftliche", "dienstlich", "dienstliche", "firma", "job"
        )
    )

    /** Füllwörter, die vor einem Nummerntyp stehen können: "auf privat". */
    private val kindLeadIn = words("auf", "unter", "über", "am", "im", "in", "via", "per", "zu")

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
        if (text in clear) return Command.Clear
        if (text in done) return Command.Done
        if (text in repeat) return Command.Repeat
        if (text in yes) return Command.Yes
        if (text in no) return Command.No
        ordinals[text]?.let { return Command.Choice(it) }

        // Kurze Antworten Wort für Wort prüfen. Der Vergleich oben trifft nur
        // den exakten Wortlaut - gesprochen wird aber "ja bitte", "nein danke"
        // oder "nein, die private Nummer".
        val tokens = text.split(' ')
        if (tokens.size <= MAX_ANSWER_WORDS) {
            // Ein genannter Nummerntyp schlägt das "nein" davor: Wer "nein,
            // privat" sagt, will nicht abbrechen, sondern die andere Nummer.
            val kind = tokens.firstNotNullOfOrNull { kindOf(it) }
            if (kind != null &&
                tokens.all { it in no || it in filler || it in kindLeadIn || kindOf(it) != null }
            ) {
                return Command.PickKind(kind)
            }
            if (tokens.any { it in yes } && tokens.all { it in yes || it in filler }) {
                return Command.Yes
            }
            if (tokens.any { it in no } && tokens.all { it in no || it in filler }) {
                return Command.No
            }
        }

        // "nummer eins" / "die zweite nummer"
        Regex("^(?:die\\s+)?(?:nummer\\s+)?(\\w+)(?:\\s+nummer)?$").find(text)?.let { m ->
            ordinals[m.groupValues[1]]?.let { return Command.Choice(it) }
        }

        callPrefix.find(text)?.let { m ->
            val (name, kind) = splitKind(m.groupValues[1].removeSuffix(" an").trim())
            if (name.isNotEmpty()) return Command.CallName(name, kind)
        }
        callSuffix.find(text)?.let { m ->
            val (name, kind) = splitKind(m.groupValues[1].trim())
            if (name.isNotEmpty() && name !in cancel) return Command.CallName(name, kind)
        }

        if (dialNumber.any { text.contains(it) }) return Command.DialNumber

        return Command.Unknown(text)
    }

    /** Der Nummerntyp zu einem einzelnen Wort, oder null. */
    private fun kindOf(word: String): PhoneKind? =
        kindWords.entries.firstOrNull { word in it.value }?.key

    /**
     * Trennt einen angehängten Nummerntyp vom Namen ab:
     * "michaela privat" wird zu ("michaela", HOME).
     *
     * Nur am Ende und nur, wenn danach noch ein Name übrig bleibt - sonst
     * würde ein Kontakt namens "Privat" unauffindbar.
     */
    private fun splitKind(spoken: String): Pair<String, PhoneKind?> {
        var parts = spoken.split(' ').filter { it.isNotEmpty() }
        if (parts.size < 2) return spoken to null

        val kind = kindOf(parts.last()) ?: return spoken to null
        parts = parts.dropLast(1)
        // "michaela auf privat" - das Bindewort gehört auch nicht zum Namen.
        if (parts.size > 1 && parts.last() in kindLeadIn) parts = parts.dropLast(1)

        val name = parts.joinToString(" ")
        return if (name.isEmpty()) spoken to null else name to kind
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

    /** So viele Wörter darf eine Antwort haben, um noch als Ja/Nein zu gelten. */
    private const val MAX_ANSWER_WORDS = 4
}
