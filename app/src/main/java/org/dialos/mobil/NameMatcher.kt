package org.dialos.mobil

import kotlin.math.max
import kotlin.math.min

/**
 * Vergleicht einen gesprochenen Namen mit den Namen aus dem Adressbuch.
 *
 * Reine Kotlin-Logik ohne Android-Abhängigkeit, damit sie sich testen lässt.
 * Drei Verfahren greifen ineinander, weil Spracherkennung bei Eigennamen
 * regelmäßig danebenliegt:
 *
 *  1. Wortweiser Vergleich - "Müller" soll "Anna Müller" finden.
 *  2. Levenshtein-Ähnlichkeit - fängt Verhörer wie "Mustermann"/"Musterman" ab.
 *  3. Kölner Phonetik - fängt gleich klingende Schreibweisen ab
 *     (Meier/Maier/Mayr/Meyer ergeben denselben Code).
 */
object NameMatcher {

    private val CTX_C_INITIAL = setOf('a', 'h', 'k', 'l', 'o', 'q', 'r', 'u', 'x')
    private val CTX_C_AFTER_SIBILANT = setOf('s', 'z')
    private val CTX_C_FOLLOWING = setOf('a', 'h', 'k', 'o', 'q', 'u', 'x')
    private val CTX_X_PRECEDING = setOf('c', 'k', 'q')

    /** Ab diesem Wert gilt ein Name als Treffer. */
    const val THRESHOLD = 0.62

    /** Ist der beste Treffer um mindestens so viel besser, wird nicht nachgefragt. */
    const val CLEAR_WINNER_MARGIN = 0.12

    fun normalize(input: String): String {
        val sb = StringBuilder(input.length)
        for (raw in input.lowercase()) {
            when (raw) {
                'ä' -> sb.append('a')
                'ö' -> sb.append('o')
                'ü' -> sb.append('u')
                'ß' -> sb.append("ss")
                'é', 'è', 'ê' -> sb.append('e')
                'á', 'à', 'â' -> sb.append('a')
                'ó', 'ò', 'ô' -> sb.append('o')
                'ú', 'ù', 'û' -> sb.append('u')
                'í', 'ì', 'î' -> sb.append('i')
                'ç' -> sb.append('c')
                'ñ' -> sb.append('n')
                else -> if (raw.isLetterOrDigit()) sb.append(raw) else sb.append(' ')
            }
        }
        return sb.toString().split(' ').filter { it.isNotEmpty() }.joinToString(" ")
    }

    /** 0.0 (nichts gemeinsam) bis 1.0 (identisch). */
    fun score(spoken: String, contactName: String): Double {
        val s = normalize(spoken)
        val n = normalize(contactName)
        if (s.isEmpty() || n.isEmpty()) return 0.0
        if (s == n) return 1.0

        val spokenTokens = s.split(' ')
        val nameTokens = n.split(' ')

        var best = ratio(s, n)

        // Der gesprochene Text ist vollständig im Namen enthalten - typisch,
        // wenn nur der Vorname oder nur der Nachname gesagt wird.
        if (spokenTokens.all { token -> nameTokens.any { it == token } }) {
            best = max(best, 0.95)
        }

        // Wortweise beste Übereinstimmung, gemittelt über die gesprochenen Wörter.
        val perToken = spokenTokens.map { token ->
            nameTokens.maxOfOrNull { name -> max(ratio(token, name), phoneticScore(token, name)) } ?: 0.0
        }
        if (perToken.isNotEmpty()) {
            best = max(best, perToken.average() * 0.97)
        }

        // Gleicher Klang über den ganzen Namen.
        best = max(best, phoneticScore(s, n) * 0.94)

        return min(best, 1.0)
    }

    private fun phoneticScore(a: String, b: String): Double {
        val ca = colognePhonetic(a)
        val cb = colognePhonetic(b)
        if (ca.isEmpty() || cb.isEmpty()) return 0.0
        return if (ca == cb) 1.0 else 0.0
    }

    /** Levenshtein-Ähnlichkeit, normiert auf 0.0 .. 1.0. */
    fun ratio(a: String, b: String): Double {
        if (a == b) return 1.0
        if (a.isEmpty() || b.isEmpty()) return 0.0
        val distance = levenshtein(a, b)
        return 1.0 - distance.toDouble() / max(a.length, b.length)
    }

    private fun levenshtein(a: String, b: String): Int {
        var previous = IntArray(b.length + 1) { it }
        var current = IntArray(b.length + 1)
        for (i in 1..a.length) {
            current[0] = i
            for (j in 1..b.length) {
                val substitution = previous[j - 1] + if (a[i - 1] == b[j - 1]) 0 else 1
                current[j] = minOf(current[j - 1] + 1, previous[j] + 1, substitution)
            }
            val swap = previous
            previous = current
            current = swap
        }
        return previous[b.length]
    }

    /**
     * Kölner Phonetik nach Hans Joachim Postel (1969) - das deutsche Gegenstück
     * zu Soundex. Gleich klingende Namen bekommen denselben Zifferncode.
     */
    fun colognePhonetic(input: String): String {
        val word = normalize(input).replace(" ", "")
        if (word.isEmpty()) return ""

        val codes = StringBuilder()
        for (i in word.indices) {
            val c = word[i]
            val prev = word.getOrNull(i - 1) ?: ' '
            val next = word.getOrNull(i + 1) ?: ' '
            val code = when (c) {
                'a', 'e', 'i', 'j', 'o', 'u', 'y' -> '0'
                'b' -> '1'
                'p' -> if (next == 'h') '3' else '1'
                'd', 't' -> if (next == 'c' || next == 's' || next == 'z') '8' else '2'
                'f', 'v', 'w' -> '3'
                'g', 'k', 'q' -> '4'
                'c' -> when {
                    i == 0 -> if (next in CTX_C_INITIAL) '4' else '8'
                    prev in CTX_C_AFTER_SIBILANT -> '8'
                    next in CTX_C_FOLLOWING -> '4'
                    else -> '8'
                }
                'x' -> if (prev in CTX_X_PRECEDING) '8' else '4'
                'l' -> '5'
                'm', 'n' -> '6'
                'r' -> '7'
                's', 'z' -> '8'
                'h' -> continue
                else -> continue
            }
            codes.append(code)
        }

        // Doppelte Codes zusammenfassen, danach alle Nullen außer der ersten streichen.
        val deduped = StringBuilder()
        for (c in codes) {
            if (deduped.isEmpty() || deduped.last() != c) deduped.append(c)
        }
        if (deduped.isEmpty()) return ""
        val head = deduped.first()
        val tail = deduped.drop(1).filter { it != '0' }
        return "$head$tail"
    }
}
