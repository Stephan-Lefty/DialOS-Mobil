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

    /**
     * Aus einem Testbericht vom 05.09.2026: "Michelle anrufen" schlug
     * hartnäckig Michaels vor. Michelle, Michel und Michael haben denselben
     * Kölner Code (645) und bekamen deshalb alle 0,97 - bei Gleichstand
     * entschied die alphabetische Reihenfolge, und die drei Vorschlagsplätze
     * waren mit Michaels belegt.
     */
    @Test
    fun `gleich klingender Name schlaegt den exakten nicht`() {
        val gemeint = NameMatcher.score("michelle", "Michelle Weber")
        val klingtNur = NameMatcher.score("michelle", "Michael Schmidt")
        assertTrue(
            "exakter Treffer $gemeint muss über Klangtreffer $klingtNur liegen",
            gemeint > klingtNur
        )
        assertTrue(
            "der Abstand muss für einen klaren Sieger reichen",
            gemeint - klingtNur >= NameMatcher.CLEAR_WINNER_MARGIN
        )
    }

    @Test
    fun `gleich klingende Schreibweisen bleiben trotzdem Treffer`() {
        // Der Grund, warum die Kölner Phonetik überhaupt drin ist - das darf
        // die Abwertung nicht kaputt machen.
        for (variante in listOf("Maier", "Mayer", "Meyer", "Mayr")) {
            assertTrue(
                "$variante muss Meier finden",
                NameMatcher.score(variante, "Anna Meier") >= NameMatcher.THRESHOLD
            )
        }
    }
}
