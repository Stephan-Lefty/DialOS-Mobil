package org.dialos.mobil

import org.dialos.mobil.StimmenWahl.Ergebnis
import org.junit.Assert.assertEquals
import org.junit.Test

class StimmenWahlTest {

    private val vorhanden = listOf("de-DE-language", "de-de-x-nfh-local")

    @Test
    fun `ohne Wunsch gilt die Voreinstellung`() {
        assertEquals(Ergebnis.STANDARD, StimmenWahl.entscheide(null, vorhanden))
        assertEquals(Ergebnis.STANDARD, StimmenWahl.entscheide(null, null))
        assertEquals(Ergebnis.STANDARD, StimmenWahl.entscheide(null, emptyList()))
    }

    @Test
    fun `die gewuenschte Stimme wird genommen, wenn sie da ist`() {
        assertEquals(
            Ergebnis.GEWUENSCHTE,
            StimmenWahl.entscheide("de-de-x-nfh-local", vorhanden)
        )
    }

    @Test
    fun `eine leere Liste heisst nicht, dass die Stimme weg ist`() {
        // Der gemeldete Fehler vom 27.10.2026: Die Engine kam gerade erst
        // hoch und lieferte noch nichts. Die App las das als "Stimme
        // geloescht" und warf die Einstellung weg.
        assertEquals(
            Ergebnis.SPAETER_NOCHMAL,
            StimmenWahl.entscheide("de-de-x-nfh-local", emptyList())
        )
        assertEquals(
            "null kommt vor, wenn tts.voices wirft",
            Ergebnis.SPAETER_NOCHMAL,
            StimmenWahl.entscheide("de-de-x-nfh-local", null)
        )
    }

    @Test
    fun `eine gefuellte Liste ohne die Stimme heisst, dass sie fehlt`() {
        assertEquals(
            Ergebnis.NICHT_VORHANDEN,
            StimmenWahl.entscheide("de-de-x-weggefallen", vorhanden)
        )
    }

    @Test
    fun `der Unterschied zwischen leer und gefuellt ist der ganze Punkt`() {
        val wunsch = "de-de-x-irgendeine"
        assertEquals(
            "leer: nichts ueber die Stimme bekannt",
            Ergebnis.SPAETER_NOCHMAL,
            StimmenWahl.entscheide(wunsch, emptyList())
        )
        assertEquals(
            "gefuellt: die Stimme ist wirklich nicht dabei",
            Ergebnis.NICHT_VORHANDEN,
            StimmenWahl.entscheide(wunsch, listOf("de-DE-language"))
        )
    }

    @Test
    fun `Namen werden genau verglichen, nicht ungefaehr`() {
        // Stimmennamen unterscheiden sich oft nur in einer Ziffer
        // (de-de-x-nfh-local gegen de-de-x-nfh-network). Ein
        // Teilstring-Vergleich wuerde hier die falsche Stimme setzen.
        assertEquals(
            Ergebnis.NICHT_VORHANDEN,
            StimmenWahl.entscheide("de-de-x-nfh", vorhanden)
        )
    }
}
