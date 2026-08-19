package org.dialos.mobil

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class NameMatcherTest {

    @Test
    fun `exakter Name trifft voll`() {
        assertEquals(1.0, NameMatcher.score("Max Mustermann", "Max Mustermann"), 0.001)
    }

    @Test
    fun `Nachname allein findet den Kontakt`() {
        assertTrue(NameMatcher.score("Mustermann", "Max Mustermann") >= NameMatcher.THRESHOLD)
    }

    @Test
    fun `Umlaute und Gross-Kleinschreibung stoeren nicht`() {
        assertTrue(NameMatcher.score("anna muller", "Anna Müller") >= NameMatcher.THRESHOLD)
    }

    @Test
    fun `gleich klingende Schreibweisen ergeben denselben Klangcode`() {
        val meier = NameMatcher.colognePhonetic("Meier")
        assertEquals(meier, NameMatcher.colognePhonetic("Maier"))
        assertEquals(meier, NameMatcher.colognePhonetic("Mayer"))
        assertEquals(meier, NameMatcher.colognePhonetic("Meyer"))
    }

    @Test
    fun `fremder Name bleibt unter der Schwelle`() {
        assertTrue(NameMatcher.score("Bäckerei", "Max Mustermann") < NameMatcher.THRESHOLD)
    }

    @Test
    fun `Verhoerer wird noch erkannt`() {
        assertTrue(NameMatcher.score("Musterman", "Max Mustermann") >= NameMatcher.THRESHOLD)
    }
}
