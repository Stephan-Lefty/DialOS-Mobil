package org.dialos.mobil

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

/**
 * Aus einem Testbericht vom 10.09.2026: Nach „Abbrechen“ in der
 * Kontaktauswahl reagierte die App auf keine Spracheingabe mehr.
 *
 * Der Grund war kein technischer Defekt, sondern die Bedeutung von
 * „Abbrechen“: Es beendete das ganze Gespräch, und danach hört die App nur
 * noch auf das Aktivierungswort – ohne das zu sagen. Diese Tests halten die
 * beiden Bedeutungen auseinander, damit sie nicht wieder verschmelzen.
 */
class AbbrechenTest {

    @Test
    fun `Abbrechen und Sprachsteuerung beenden sind zweierlei`() {
        assertEquals(Command.Cancel, CommandParser.parse("abbrechen"))
        assertEquals(Command.ShutDown, CommandParser.parse("Sprachsteuerung beenden"))
        assertNotEquals(
            CommandParser.parse("abbrechen"),
            CommandParser.parse("Sprachsteuerung beenden")
        )
    }

    @Test
    fun `weitere Wege zum Abbrechen`() {
        for (wort in listOf("abbruch", "stopp", "halt", "zurück", "lass es")) {
            assertEquals("„$wort“ sollte abbrechen", Command.Cancel, CommandParser.parse(wort))
        }
    }

    @Test
    fun `weitere Wege zum Beenden`() {
        for (satz in listOf("sprachsteuerung aus", "aufhören", "schlafen")) {
            assertEquals("„$satz“ sollte beenden", Command.ShutDown, CommandParser.parse(satz))
        }
    }

    /**
     * Die Obergrenze der Vorschläge ist der zweite Befund desselben
     * Berichts: Bei fünf Kontakten namens Hans wurden drei vorgelesen und
     * zwei stillschweigend verschwiegen.
     */
    @Test
    fun `es werden mehr als drei Vorschlaege zugelassen`() {
        assertEquals(6, ContactRepository.MAX_VORSCHLAEGE)
    }
}
