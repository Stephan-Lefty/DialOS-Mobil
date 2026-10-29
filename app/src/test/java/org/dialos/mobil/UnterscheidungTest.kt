package org.dialos.mobil

import org.dialos.mobil.Unterscheidung.Eingang
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class UnterscheidungTest {

    @Test
    fun `eindeutige Namen bekommen keinen Zusatz`() {
        val z = Unterscheidung.zusaetze(
            listOf(
                Eingang("Anna Berg", "Mobil", "01761234567"),
                Eingang("Bernd Klee", "Privat", "0301234567"),
            )
        )
        assertNull(z[0])
        assertNull(z[1])
    }

    @Test
    fun `der gemeldete Fall - gleicher Mensch, zwei Eintraege`() {
        // "X" und "X zuhause" sind streng genommen verschiedene Namen,
        // fuers Ohr aber derselbe Mensch. Gemeldet am 28.10.2026.
        val z = Unterscheidung.zusaetze(
            listOf(
                Eingang("Marco Muster", "Mobil", "01761234567"),
                Eingang("Marco Muster zuhause", "Privat", "0301234567"),
            )
        )
        assertEquals("Mobil", z[0]?.art)
        assertEquals("Privat", z[1]?.art)
        assertNull("die Art reicht, keine Ziffern noetig", z[0]?.endziffern)
        assertNull(z[1]?.endziffern)
    }

    @Test
    fun `gleiche Art - dann muessen die Endziffern her`() {
        val z = Unterscheidung.zusaetze(
            listOf(
                Eingang("Anna Berg", "Mobil", "0176 1234 4712"),
                Eingang("Anna Berg", "Mobil", "0176 9999 8830"),
            )
        )
        assertEquals("4 7 1 2", z[0]?.endziffern)
        assertEquals("8 8 3 0", z[1]?.endziffern)
    }

    @Test
    fun `ohne Art entscheiden allein die Endziffern`() {
        val z = Unterscheidung.zusaetze(
            listOf(
                Eingang("Anna Berg", null, "01761234712"),
                Eingang("Anna Berg", "", "03012348830"),
            )
        )
        assertNull(z[0]?.art)
        assertEquals("4 7 1 2", z[0]?.endziffern)
        assertEquals("8 8 3 0", z[1]?.endziffern)
    }

    @Test
    fun `Endziffern werden einzeln gesprochen, nicht als Zahl`() {
        // Ohne die Leerzeichen liest die Sprachausgabe "4712" als
        // "viertausendsiebenhundertzwoelf" - unbrauchbar zum Vergleichen.
        assertEquals("4 7 1 2", Unterscheidung.endziffern("+49 176 1234712"))
    }

    @Test
    fun `zu kurze Nummern liefern keine Endziffern`() {
        assertNull(Unterscheidung.endziffern("110"))
        assertNull(Unterscheidung.endziffern(null))
        assertNull(Unterscheidung.endziffern(""))
    }

    @Test
    fun `Umlaute und Gross-Klein stoeren den Vergleich nicht`() {
        val z = Unterscheidung.zusaetze(
            listOf(
                Eingang("Jürgen Groß", "Mobil", "01761234567"),
                Eingang("jürgen groß Büro", "Arbeit", "0301234567"),
            )
        )
        assertEquals("Mobil", z[0]?.art)
        assertEquals("Arbeit", z[1]?.art)
    }

    @Test
    fun `Grenze - umschriebene Umlaute gelten nicht als derselbe Name`() {
        // NameMatcher.normalize macht aus "ü" ein "u", nicht "ue". Damit
        // sind "Jürgen" und "Juergen" verschieden, und zwei Eintraege
        // derselben Person bleiben unerkannt. Das ist eine Eigenschaft der
        // Normalisierung, die ueberall in der App gilt - hier nur
        // festgehalten, damit niemand es fuer einen Fehler dieser Klasse
        // haelt. Beim Aendern waere die Namenssuche mit zu bedenken, nicht
        // nur diese Stelle.
        val z = Unterscheidung.zusaetze(
            listOf(
                Eingang("Jürgen Groß", "Mobil", "01761234567"),
                Eingang("Juergen Gross", "Mobil", "0301234567"),
            )
        )
        assertNull("wird heute NICHT als verwechselbar erkannt", z[0])
        assertNull(z[1])
    }

    @Test
    fun `drei Eintraege, nur zwei davon verwechselbar`() {
        val z = Unterscheidung.zusaetze(
            listOf(
                Eingang("Anna Berg", "Mobil", "01761111111"),
                Eingang("Anna Berg Arbeit", "Arbeit", "0302222222"),
                Eingang("Bernd Klee", "Privat", "0403333333"),
            )
        )
        assertEquals("Mobil", z[0]?.art)
        assertEquals("Arbeit", z[1]?.art)
        assertNull("der Dritte ist eindeutig und bleibt kurz", z[2])
    }

    @Test
    fun `ein einzelner Vorschlag braucht nie einen Zusatz`() {
        val z = Unterscheidung.zusaetze(listOf(Eingang("Anna Berg", "Mobil", "0176123")))
        assertNull(z[0])
    }
}
