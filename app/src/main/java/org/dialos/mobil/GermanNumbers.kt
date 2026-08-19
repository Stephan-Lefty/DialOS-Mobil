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
        "fuenf" to 5, "sechs" to 6, "sieben" to 7, "acht" to 8, "neun" to 9
    )

    private val teens = mapOf(
        "zehn" to 10, "elf" to 11, "zwölf" to 12, "zwoelf" to 12,
        "dreizehn" to 13, "vierzehn" to 14, "fünfzehn" to 15, "fuenfzehn" to 15,
        "sechzehn" to 16, "siebzehn" to 17, "achtzehn" to 18, "neunzehn" to 19
    )

    private val tens = mapOf(
        "zwanzig" to 20, "dreißig" to 30, "dreissig" to 30, "vierzig" to 40,
        "fünfzig" to 50, "fuenfzig" to 50, "sechzig" to 60, "siebzig" to 70,
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

                else -> parseWord(token)?.let { out.append(formatNumber(it)) }
            }
            i++
        }
        return out.toString()
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

    /** "0179" -> "0 1 7 9", damit die Sprachausgabe jede Ziffer einzeln liest. */
    fun spellOut(digits: String): String = digits.toCharArray().joinToString(" ")
}
