package org.dialos.mobil

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Aus einem Testbericht vom 27.09.2026: „Die App hat ein Eigenleben. Sie
 * spricht ohne Aufforderung und gibt dann immer zur Antwort, das kann ich in
 * den Kontakten nicht finden. Auch wenn ich ihr sage, sie soll sich
 * abschalten […] versteht sie immer etwas anderes."
 *
 * Der zitierte Satz ist `say_not_found` – wortgleich. Das ist die
 * eigentliche Auskunft des Berichts: Diese Ansage kann nur fallen, wenn das
 * Adressbuch **gelesen** ist (sonst käme `say_no_contacts` und die App
 * würde aufhören). Es ist also kein Berechtigungsproblem, sondern die App
 * behandelt jede Äußerung im Raum als Namen und kommentiert sie.
 *
 * Der zweite Teil der Meldung ist hier nachrechenbar: „abschalten" stand in
 * keiner Wortliste, und Befehle wurden nur beim **exakten** Wortlaut des
 * ganzen Satzes erkannt – anders als „ja" und „nein", die Füllwörter schon
 * seit 0.6.3 dulden. Damit war der Ausweg schwerer zu treffen als der
 * Einstieg: Das Aktivierungswort wird mit Ähnlichkeitsmaß geprüft, das
 * Beenden verlangte Wort für Wort das Richtige. Wer es nicht traf, landete
 * in der Namenssuche – und hörte genau den Satz aus dem Bericht.
 */
class EigenlebenTest {

    /** Die Formulierung aus dem Bericht selbst. */
    @Test
    fun `sich abschalten beendet die Sprachsteuerung`() {
        for (satz in listOf(
            "sprachsteuerung abschalten",
            "app abschalten",
            "schalte dich ab",
            "abschalten",
            "ausschalten",
            "abstellen"
        )) {
            assertEquals("„$satz“ sollte beenden", Command.ShutDown, CommandParser.parse(satz))
        }
    }

    /**
     * Ein Verhörer, der gemessen vorliegt: Vosk hört „sprachstörungen" statt
     * „sprachsteuerung" (Realtest 21.09.2026). Beim Einschalten fängt
     * [CommandParser.isWakePhrase] das ab – beim Ausschalten muss es
     * genauso sein, sonst ist die Tür nur von außen zu öffnen.
     */
    @Test
    fun `verhoerte Sprachsteuerung beendet trotzdem`() {
        for (satz in listOf(
            "sprachstörungen beenden",
            "sprachstörung abschalten",
            "sprach steuerung beenden"
        )) {
            assertEquals("„$satz“ sollte beenden", Command.ShutDown, CommandParser.parse(satz))
        }
    }

    /**
     * Höflichkeit darf einen Befehl nicht entwerten. Bis 0.6.15 galt die
     * Füllwort-Duldung nur für Ja und Nein; „hilfe bitte" wurde als Name
     * gesucht – und das ist ausgerechnet der Satz, den jemand sagt, der
     * nicht weiterweiß.
     */
    @Test
    fun `Fuellwoerter entwerten keinen Befehl`() {
        assertEquals(Command.ShutDown, CommandParser.parse("bitte aufhören"))
        assertEquals(Command.ShutDown, CommandParser.parse("jetzt beenden"))
        assertEquals(Command.Cancel, CommandParser.parse("abbrechen bitte"))
        assertEquals(Command.Help, CommandParser.parse("hilfe bitte"))
        assertEquals(Command.Repeat, CommandParser.parse("mal wiederholen"))
        assertEquals(Command.Done, CommandParser.parse("dann fertig"))
    }

    /**
     * Die Gegenprobe. Die Duldung darf keine Namen auffressen: Wer einen
     * Kontakt „Ende" oder eine „Hilfe Hotline" im Adressbuch hat, muss ihn
     * weiter anrufen können, und ein blosser Name bleibt Rohtext.
     */
    @Test
    fun `Namen werden nicht zu Befehlen`() {
        assertEquals(Command.Unknown("anna muller"), CommandParser.parse("Anna Müller"))
        assertEquals(Command.Unknown("hilfe hotline"), CommandParser.parse("Hilfe Hotline"))
        assertEquals(Command.CallName("beate"), CommandParser.parse("Beate anrufen"))
        assertEquals(Command.CallName("anna ende"), CommandParser.parse("Anna Ende anrufen"))
    }
}
