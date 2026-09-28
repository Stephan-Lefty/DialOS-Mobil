package org.dialos.mobil

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GermanNumbersTest {

    @Test
    fun `einzeln gesprochene Ziffern`() {
        assertEquals("0179", GermanNumbers.toDigits("null eins sieben neun"))
    }

    @Test
    fun `fuehrende Null bleibt erhalten`() {
        assertEquals("089", GermanNumbers.toDigits("null acht neun"))
    }

    @Test
    fun `zusammengesetzte Zahlwoerter`() {
        assertEquals("21", GermanNumbers.toDigits("einundzwanzig"))
        assertEquals("47", GermanNumbers.toDigits("siebenundvierzig"))
    }

    @Test
    fun `Laendervorwahl mit Plus`() {
        assertEquals("+49", GermanNumbers.toDigits("plus neunundvierzig"))
    }

    @Test
    fun `doppel verdoppelt die naechste Ziffer`() {
        assertEquals("77", GermanNumbers.toDigits("doppel sieben"))
    }

    @Test
    fun `bereits erkannte Ziffern werden uebernommen`() {
        assertEquals("0176", GermanNumbers.toDigits("0176"))
    }

    @Test
    fun `Fuellwoerter liefern keine Ziffern`() {
        assertEquals("", GermanNumbers.toDigits("die nummer bitte"))
    }

    @Test
    fun `Ziffern werden einzeln vorgelesen`() {
        assertEquals("0 1 7 9", GermanNumbers.spellOut("0179"))
    }

    @Test
    fun `Endziffern unterscheiden zwei gleich benannte Nummern`() {
        assertEquals("5 6 7 8", GermanNumbers.lastDigitsSpoken("017612345678"))
        assertEquals("4 3 2 1", GermanNumbers.lastDigitsSpoken("017687654321"))
    }

    @Test
    fun `Trennzeichen zaehlen nicht als Ziffer`() {
        // Aus dem Adressbuch kommen Nummern in jeder erdenklichen Schreibweise.
        assertEquals("5 6 7 8", GermanNumbers.lastDigitsSpoken("0176 / 1234-5678"))
        assertEquals("5 6 7 8", GermanNumbers.lastDigitsSpoken("+49 (176) 1234 5678"))
    }

    @Test
    fun `kurze Nummern liefern was da ist`() {
        assertEquals("1 1 0", GermanNumbers.lastDigitsSpoken("110"))
        assertEquals("", GermanNumbers.lastDigitsSpoken(""))
    }

    /**
     * Der Text kommt je nach Weg mit oder ohne Umlaut an: Beim Diktieren
     * reicht der Dienst den Rohtext durch, bei einer im Namenszustand
     * gesagten Rufnummer ist er schon durch `NameMatcher.normalize` gelaufen
     * (ü wird zu u). Am 28.09.2026 am Gerät aufgefallen - aus acht
     * gesprochenen Ziffern wurden sieben, die Fünf fehlte spurlos.
     *
     * Eine Rufnummer, die stillschweigend eine Ziffer verliert, ist
     * schlimmer als gar keine: Sie wird gewählt, nur eben falsch.
     */
    @Test
    fun `Zahlwoerter auch ohne Umlaut`() {
        assertEquals("5", GermanNumbers.toDigits("funf"))
        assertEquals("5", GermanNumbers.toDigits("fünf"))
        assertEquals("5", GermanNumbers.toDigits("fuenf"))
        assertEquals("01783456", GermanNumbers.toDigits("null eins sieben acht drei vier funf sechs"))
        assertEquals("01783456", GermanNumbers.toDigits("null eins sieben acht drei vier fünf sechs"))
    }

    /**
     * **Am Gerät gemessen** (28.09.2026): Zweimal dieselbe gesprochene Folge
     * „null eins sieben acht vier sechs", zweimal anders erkannt. „sex" für
     * „sechs" steht in keiner Wortliste der Welt – das hätte niemand
     * geraten, es musste gemessen werden.
     */
    @Test
    fun `verhoerte Zahlwoerter zwischen sicheren Ziffern`() {
        assertEquals("017846", GermanNumbers.toDigits("null ein sieben acht vier sex"))
        assertEquals("017846", GermanNumbers.toDigits("nur eins sieben acht viel sechs"))
        assertEquals("017846", GermanNumbers.toDigits("null eins sieben acht vier sechs"))
    }

    /**
     * Die Gegenprobe, und der Grund für die Nachbarschaftsregel: „ein",
     * „nur" und „viel" sind Alltagswörter. `toDigits` läuft seit 0.6.15 auch
     * über gesprochene Namen, deshalb darf ein Satz ohne Zifferngerüst keine
     * Rufnummer ergeben. Entscheidend ist der Abstand zu
     * `DialogController.MIN_RUFNUMMER_ZIFFERN` – sechs.
     */
    @Test
    fun `Alltagswoerter ergeben keine Rufnummer`() {
        for (satz in listOf("nur ein moment", "ein freund", "nur mal sehen",
                            "viel glueck", "nur so", "ein bisschen viel")) {
            val ziffern = GermanNumbers.toDigits(satz)
            assertTrue(
                "„$satz“ ergab $ziffern - das wäre als Rufnummer durchgegangen",
                ziffern.count { it.isDigit() } < 6
            )
        }
    }
}
