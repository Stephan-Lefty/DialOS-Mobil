package org.dialos.mobil

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CommandParserTest {

    @Test
    fun `Aktivierungswort in verschiedenen Zerlegungen`() {
        assertTrue(CommandParser.isWakePhrase("sprachsteuerung starten"))
        assertTrue(CommandParser.isWakePhrase("sprach steuerung starten"))
        assertTrue(CommandParser.isWakePhrase("bitte sprachsteuerung starten"))
        assertTrue(CommandParser.isWakePhrase("Sprachsteuerung startet"))
    }

    @Test
    fun `beilaeufige Rede loest nicht aus`() {
        assertFalse(CommandParser.isWakePhrase("wie war das mit dem wetter"))
        assertFalse(CommandParser.isWakePhrase("ruf mal die anna an"))
    }

    @Test
    fun `Name mit vorangestelltem Verb`() {
        assertEquals(Command.CallName("max mustermann"), CommandParser.parse("ruf Max Mustermann an"))
        assertEquals(Command.CallName("anna"), CommandParser.parse("wähle Anna"))
    }

    @Test
    fun `Name mit nachgestelltem anrufen`() {
        assertEquals(Command.CallName("max mustermann"), CommandParser.parse("Max Mustermann anrufen"))
    }

    @Test
    fun `Nummernmodus`() {
        assertEquals(Command.DialNumber, CommandParser.parse("Nummer wählen"))
        assertEquals(Command.DialNumber, CommandParser.parse("telefonnummer"))
    }

    @Test
    fun `Bestaetigung und Abbruch`() {
        assertEquals(Command.Yes, CommandParser.parse("ja"))
        assertEquals(Command.No, CommandParser.parse("nein"))
        assertEquals(Command.Cancel, CommandParser.parse("abbrechen"))
        assertEquals(Command.Done, CommandParser.parse("fertig"))
        assertEquals(Command.ShutDown, CommandParser.parse("Sprachsteuerung beenden"))
    }

    @Test
    fun `Auswahl aus der Vorschlagsliste`() {
        assertEquals(Command.Choice(1), CommandParser.parse("eins"))
        assertEquals(Command.Choice(2), CommandParser.parse("die zweite"))
    }

    @Test
    fun `blosser Name bleibt als Rohtext erhalten`() {
        assertEquals(Command.Unknown("anna muller"), CommandParser.parse("Anna Müller"))
    }
}
