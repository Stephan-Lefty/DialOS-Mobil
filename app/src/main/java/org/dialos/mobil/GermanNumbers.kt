package org.dialos.mobil

/**
 * Wandelt gesprochene deutsche Zahlwörter in Ziffern um.
 *
 * Das Vosk-Modell liefert Wörter, keine Ziffern - "null eins sieben" kommt als
 * Text an. Beim Diktieren einer Rufnummer werden Ziffern meist einzeln
 * gesprochen, gelegentlich aber auch paarweise ("einundzwanzig"), deshalb wird
 * beides unterstützt.
 */
object GermanNumbers {

    private val units = mapOf(
        "null" to 0, "eins" to 1, "ein" to 1, "eine" to 1, "einer" to 1,
        "zwei" to 2, "zwo" to 2, "drei" to 3, "vier" to 4, "fünf" to 5,
        "fuenf" to 5, "funf" to 5, "sechs" to 6, "sieben" to 7, "acht" to 8, "neun" to 9
    )

    private val teens = mapOf(
        "zehn" to 10, "elf" to 11, "zwölf" to 12, "zwoelf" to 12,
        "dreizehn" to 13, "vierzehn" to 14, "fünfzehn" to 15, "fuenfzehn" to 15, "funfzehn" to 15,
        "sechzehn" to 16, "siebzehn" to 17, "achtzehn" to 18, "neunzehn" to 19
    )

    private val tens = mapOf(
        "zwanzig" to 20, "dreißig" to 30, "dreissig" to 30, "vierzig" to 40,
        "fünfzig" to 50, "fuenfzig" to 50, "funfzig" to 50, "sechzig" to 60, "siebzig" to 70,
        "achtzig" to 80, "neunzig" to 90
    )

    /** Wörter, die beim Diktieren vorkommen, aber keine Ziffer beisteuern. */
    private val filler = setOf(
        "und", "die", "der", "das", "nummer", "rufnummer", "telefonnummer",
        "vorwahl", "durchwahl", "bitte", "dann", "noch", "mal"
    )

    /**
     * Extrahiert die Ziffernfolge aus einem gesprochenen Satzstück.
     * Liefert einen leeren String, wenn nichts Zählbares dabei war.
     */
    /**
     * Was das Sprachmodell aus Zahlwörtern macht, wenn es danebenliegt.
     *
     * **Gemessen am 28.09.2026** (Motorola edge 50 neo): Zweimal dieselbe
     * gesprochene Folge „null eins sieben acht vier sechs", zweimal anders
     * erkannt – „null ein sieben acht vier sex" und „nur eins sieben acht
     * viel sechs". Statt sechs Ziffern kamen jeweils nur vier an.
     *
     * Diese Wörter gelten **nur im Zifferngespräch**, nie allein: „ein" und
     * „nur" sind Alltagswörter, und [toDigits] läuft seit 0.6.15 auch über
     * gesprochene Namen. Ein großzügiges „nur" → 0 würde aus „nur ein
     * Moment" eine Rufnummer machen. Die Absicherung steht in [toDigits]:
     * Ein Verhörer zählt erst, wenn direkt daneben ein sicheres Zahlwort
     * steht. Dann ist der Zusammenhang der Schutz und nicht das Wort.
     */
    private val verhoert = mapOf(
        "ein" to 1, "eint" to 1, "einz" to 1,
        "sex" to 6, "sechst" to 6,
        // Die gebeugten Formen kamen am 01.10.2026 dazu. Gemessen wurde
        // "nummer wir null null vielen neuen eins sieben sechs acht null"
        // für "null null vier neun ..." - "viel" und "neu" standen in der
        // Tabelle, "vielen" und "neuen" nicht. Die Vier und die Neun
        // fielen deshalb beide weg, und aus 004917680 wurde 0017680: eine
        // andere, völlig gültig klingende Nummer. Stille Auslassungen sind
        // die gefährlichste Fehlerart, die hier auftreten kann.
        // "viele" und "neue" kamen am 07.10.2026 dazu - zum dritten Mal
        // derselbe Fehlertyp: eine Beugungsform fehlt, während die
        // Nachbarformen längst in der Tabelle stehen ("viel", "vielen";
        // "neu", "neune", "neuen"). Wer hier etwas ergänzt, sollte die
        // ganze Formenreihe durchgehen und nicht nur das gemessene Wort.
        //
        // Eine Stamm- oder Ähnlichkeitsregel wäre der naheliegende
        // Ausweg und ist bewusst nicht gebaut: Der Stamm von "sieben" ist
        // "sie", und ein Pronomen als Ziffer zu lesen wäre schlimmer als
        // jede fehlende Beugungsform.
        "viel" to 4, "vielen" to 4, "viele" to 4, "fier" to 4,
        // "nun" ist der am häufigsten gemessene Verhörer für "null" - am
        // 30.09.2026 stand er zweimal im Protokoll ("nun alles sie beim auch
        // für sechs", "nun nun als sieben acht für sechs") und fehlte hier
        // trotzdem. Als Alltagswort ("nun ja", "was nun") wäre er ohne die
        // Nachbarschaftsregel gefährlich; mit ihr zählt er nur innerhalb
        // einer Ziffernfolge.
        "nur" to 0, "nul" to 0, "nuller" to 0, "nun" to 0,
        "zwo" to 2, "drai" to 3, "achte" to 8,
        // "neue" ist zugleich der Anfang von "neue nummer" - dem Befehl
        // zum Verwerfen. Das geht gut: Der CommandParser läuft vor der
        // Ziffernerkennung, und "nummer" ist kein Zahlwort und damit kein
        // Anker. "nein" fehlt hier bewusst, obwohl es am 07.10.2026 als
        // Verhörer für "neun" gemessen wurde: Es ist der wichtigste
        // Ablehnungsbefehl der ganzen App.
        "neu" to 9, "neune" to 9, "neuen" to 9, "neue" to 9,
        // "sieden" wurde am 07.10.2026 gemessen, als dieselbe Nummer einmal
        // langsam und einmal schnell gesprochen wurde. Langsam verstand Vosk
        // "sieben", schnell "sieden" - und weil der Verhörer fehlte, fiel die
        // Sieben ersatzlos weg: aus neun Ziffern wurden acht. Wieder eine
        // stille Auslassung, die eine gültig klingende Nummer hinterlässt.
        "siem" to 7, "sieb" to 7, "sieden" to 7
    )

    fun toDigits(spoken: String): String {
        val out = StringBuilder()
        val tokens = spoken.lowercase()
            .replace('-', ' ')
            .split(' ', '\t', '\n')
            .map { it.trim { c -> !c.isLetterOrDigit() && c != '+' } }
            .filter { it.isNotEmpty() }

        var i = 0
        while (i < tokens.size) {
            val token = tokens[i]
            when {
                token == "plus" || token == "+" -> out.append('+')

                // Bereits als Ziffern erkannt (z. B. "0177")
                token.all { it.isDigit() } -> out.append(token)

                token == "doppel" || token == "zweimal" -> {
                    val next = tokens.getOrNull(i + 1)?.let { parseWord(it) }
                    if (next != null && next in 0..9) {
                        out.append(next).append(next)
                        i++
                    }
                }

                token in filler -> Unit

                // Ein sicheres Zahlwort - immer gueltig.
                parseWord(token) != null ->
                    out.append(formatNumber(parseWord(token)!!))

                // Ein Verhoerer zaehlt nur zwischen sicheren Ziffern.
                verhoert.containsKey(token) && hatSicherenNachbarn(tokens, i) ->
                    out.append(formatNumber(verhoert.getValue(token)))

                else -> Unit
            }
            i++
        }
        return out.toString()
    }

    /**
     * Steht [index] innerhalb einer Ziffernfolge?
     *
     * Das ist die ganze Absicherung für [verhoert]: Mitten in einer
     * Ziffernfolge ist „sex" mit Sicherheit eine Sechs, allein stehend ist
     * es irgendein Wort.
     *
     * Die Sicherheit breitet sich von echten Zahlwörtern aus. Ein Verhörer,
     * der an eine bereits gesicherte Stelle grenzt, gilt selbst als
     * gesichert, und von dort geht es weiter. Zwei Verhörer nebeneinander
     * waren sonst nicht zu retten – genau der Fall „null null" am Anfang
     * einer Auslandsnummer, den Vosk gern als „nun nun" hört. Nach der
     * reinen Nachbarschaftsprüfung fiel davon die erste Null weg, und aus
     * 0049 wurde 049.
     *
     * Die Kette braucht einen echten Anker: Ohne ein zweifelsfrei
     * erkanntes Zahlwort breitet sich gar nichts aus.
     *
     * Das reicht als Schutz aber **nicht** aus, und das sollte man wissen.
     * „nur ein moment" ergibt `01`, weil „ein" ein richtiges Zahlwort ist
     * und selbst ankert. Was solche Sätze davon abhält, zu einer Rufnummer
     * zu werden, ist allein die Schwelle von sechs Ziffern in
     * [DialogController]. Wer sie senken will, muss hier anfangen.
     */
    private fun hatSicherenNachbarn(tokens: List<String>, index: Int): Boolean =
        index in gesicherteStellen(tokens)

    /**
     * Alle Stellen, die als Ziffer gelten dürfen – echte Zahlwörter plus
     * die von ihnen aus erreichbaren Verhörer.
     */
    private fun gesicherteStellen(tokens: List<String>): Set<Int> {
        val sicher = tokens.indices.filterTo(mutableSetOf()) {
            val t = tokens[it]
            t.all { c -> c.isDigit() } || parseWord(t) != null
        }
        if (sicher.isEmpty()) return emptySet()

        // Ausbreiten, bis nichts Neues mehr dazukommt. Die Schleife endet
        // spätestens nach tokens.size Durchläufen - jede Runde muss
        // mindestens eine Stelle hinzufügen, sonst bricht sie ab.
        var gewachsen = true
        while (gewachsen) {
            gewachsen = false
            tokens.indices.forEach { i ->
                if (i in sicher || !verhoert.containsKey(tokens[i])) return@forEach
                if ((i - 1) in sicher || (i + 1) in sicher) {
                    sicher += i
                    gewachsen = true
                }
            }
        }
        return sicher
    }

    /**
     * "null" wird zu "0", "einundzwanzig" zu "21", "dreihundert" zu "300".
     * Zahlen unter zehn behalten genau eine Stelle - so bleibt die führende
     * Null einer Vorwahl erhalten.
     */
    private fun formatNumber(value: Int): String = value.toString()

    /** Parst ein einzelnes deutsches Zahlwort (0-999). */
    fun parseWord(word: String): Int? {
        val w = word.lowercase()
        units[w]?.let { return it }
        teens[w]?.let { return it }
        tens[w]?.let { return it }

        // "einhundertzwanzig", "dreihundert"
        val hundredIndex = w.indexOf("hundert")
        if (hundredIndex >= 0) {
            val prefix = w.substring(0, hundredIndex)
            val suffix = w.substring(hundredIndex + "hundert".length)
            val factor = if (prefix.isEmpty()) 1 else parseWord(prefix) ?: return null
            val rest = if (suffix.isEmpty()) 0 else parseWord(suffix) ?: return null
            return factor * 100 + rest
        }

        // "einundzwanzig" = 1 + 20
        val undIndex = w.indexOf("und")
        if (undIndex > 0 && undIndex + 3 < w.length) {
            val left = units[w.substring(0, undIndex)]
            val right = tens[w.substring(undIndex + 3)]
            if (left != null && right != null) return right + left
        }
        return null
    }

    /**
     * "0179" -> "0 1 7 9", damit die Sprachausgabe jede Ziffer einzeln liest.
     *
     * Das führende Plus einer Auslandsnummer wird ausgeschrieben. Als
     * Sonderzeichen überlässt man es sonst der Sprachausgabe, ob sie es
     * ausspricht oder verschluckt - und wer den Bildschirm nicht sehen kann,
     * hörte zwischen "+49 176 ..." und "49 176 ..." womöglich keinen
     * Unterschied. Das ist keine Kleinigkeit: Das Wort "plus" hat beim
     * Erkennen keinen Verhörer-Schutz. Wird es als "blues" oder "plu"
     * gehört, fällt es ersatzlos weg, und übrig bleibt eine Nummer, die
     * gültig klingt. Die Vorlese-Bestätigung ist die einzige Stelle, an der
     * das auffallen kann - also muss sie es auch sagen.
     */
    fun spellOut(digits: String): String = digits.toCharArray()
        .joinToString(" ") { if (it == '+') "plus" else it.toString() }

    /**
     * Die letzten Ziffern einer Rufnummer, einzeln gesprochen.
     *
     * Für Kontakte mit mehreren gleich benannten Nummern: Wer zwei Handys hat,
     * hat im Adressbuch zweimal „Mobil" stehen, und die Rückfrage „Soll ich
     * Max Mustermann auf Mobil anrufen?" kommt dann zweimal wortgleich. Wer
     * den Bildschirm nicht sehen kann, hat keine Möglichkeit zu erkennen,
     * welche der beiden gemeint ist. Die Endziffern schließen diese Lücke -
     * die eigene Nummer erkennt man meist an ihnen.
     *
     * Trennzeichen und Vorwahlklammern werden vorher entfernt, sonst käme bei
     * „0171 / 23 45" die Klammer als vermeintliche Ziffer mit.
     */
    fun lastDigitsSpoken(number: String, count: Int = LAST_DIGITS): String =
        spellOut(number.filter { it.isDigit() }.takeLast(count))

    /** So viele Endziffern reichen zur Unterscheidung, ohne zur Merkaufgabe zu werden. */
    const val LAST_DIGITS = 4
}
