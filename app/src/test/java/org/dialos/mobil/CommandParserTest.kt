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
    fun `Text verwerfen`() {
        assertEquals(Command.Clear, CommandParser.parse("löschen"))
        assertEquals(Command.Clear, CommandParser.parse("noch mal von vorn"))
    }

    @Test
    fun `blosser Name bleibt als Rohtext erhalten`() {
        assertEquals(Command.Unknown("anna muller"), CommandParser.parse("Anna Müller"))
    }

    /**
     * Aus einem Testbericht vom 05.09.2026: Bis 0.6.2 wurde der gesamte Satz
     * mit den Wortlisten verglichen, deshalb fiel die häufigste Antwortform
     * überhaupt durch.
     */
    @Test
    fun `hoefliche Antworten werden verstanden`() {
        assertEquals(Command.Yes, CommandParser.parse("ja bitte"))
        assertEquals(Command.Yes, CommandParser.parse("ja gerne"))
        assertEquals(Command.Yes, CommandParser.parse("ja genau"))
        assertEquals(Command.No, CommandParser.parse("nein danke"))
        assertEquals(Command.No, CommandParser.parse("nein, bitte nicht"))
        assertEquals(Command.No, CommandParser.parse("andere nummer"))
    }

    @Test
    fun `ein Name wird nicht zur Bestaetigung`() {
        // "anrufen" steht in der Ja-Liste - der Name davor muss trotzdem gewinnen.
        assertEquals(Command.CallName("michaela"), CommandParser.parse("Michaela anrufen"))
        assertEquals(Command.CallName("anna muller"), CommandParser.parse("Anna Müller anrufen"))
    }

    @Test
    fun `Nummerntyp im Aufruf wird abgetrennt`() {
        assertEquals(
            Command.CallName("michaela", PhoneKind.HOME),
            CommandParser.parse("Michaela privat anrufen")
        )
        assertEquals(
            Command.CallName("michaela", PhoneKind.MOBILE),
            CommandParser.parse("ruf Michaela mobil an")
        )
        assertEquals(
            Command.CallName("max mustermann", PhoneKind.WORK),
            CommandParser.parse("Max Mustermann auf Arbeit anrufen")
        )
    }

    @Test
    fun `Nummerntyp als Antwort auf die Rueckfrage`() {
        assertEquals(Command.PickKind(PhoneKind.HOME), CommandParser.parse("privat"))
        assertEquals(Command.PickKind(PhoneKind.HOME), CommandParser.parse("nein privat"))
        assertEquals(
            Command.PickKind(PhoneKind.HOME),
            CommandParser.parse("nein, die private Nummer")
        )
        assertEquals(Command.PickKind(PhoneKind.MOBILE), CommandParser.parse("das Handy"))
        assertEquals(Command.PickKind(PhoneKind.WORK), CommandParser.parse("geschäftlich"))
    }

    /**
     * Aus einem Testbericht vom 06.09.2026: Fehlte beim Diktieren eine
     * Ziffer, half nur "löschen" - und damit war die ganze Rufnummer weg.
     */
    @Test
    fun `letzte Ziffer zuruecknehmen`() {
        assertEquals(Command.Undo, CommandParser.parse("letzte Ziffer löschen"))
        assertEquals(Command.Undo, CommandParser.parse("eine zurück"))
        assertEquals(Command.Undo, CommandParser.parse("rückgängig"))
    }

    @Test
    fun `zurueck allein bleibt Abbruch`() {
        // "zurück" heißt abbrechen, "eine zurück" das Gegenteil - die beiden
        // dürfen sich nicht gegenseitig überschreiben.
        assertEquals(Command.Cancel, CommandParser.parse("zurück"))
    }

    @Test
    fun `ein Nummerntyp allein bleibt kein Name`() {
        // Ohne Namen davor darf "privat anrufen" keinen leeren Namen ergeben.
        val command = CommandParser.parse("privat anrufen")
        assertTrue(command !is Command.CallName || (command).name.isNotEmpty())
    }
}
