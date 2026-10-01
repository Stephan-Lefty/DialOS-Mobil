package org.dialos.mobil

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Eine Nummer im selben Satz wie der Befehl: „wähle die Nummer null eins …"
 *
 * Stephans Vorschlag vom 28.09.2026, am 01.10.2026 am Gerät gemessen. Die
 * Ausgangslage war, dass eine am Stück gesprochene Ziffernfolge von Vosk
 * gar nicht verlässlich erkannt wurde – vier Versuche am 30.09., zwei davon
 * unbrauchbar („nun alles sie beim auch für sechs").
 *
 * Der Messlauf vom 01.10., dieselbe Folge zweimal gesprochen:
 *
 * | Variante | Vosk hörte | Ziffern |
 * |---|---|---|
 * | ohne Einleitung | `null eines sie wenn acht vier sechs` | `0846` |
 * | mit Einleitung | `wer die nummer null eins sieben acht vier sex` | `017846` |
 *
 * Die Einleitung rettet die Erkennung – vermutlich, weil der Vorlauf dem
 * Erkenner Zeit zum Einschwingen gibt. Bis dahin zerfiel regelmäßig die
 * erste Ziffer: „null" wurde zu „nun".
 *
 * `DialogController` lässt sich ohne Android-Context nicht testen. Geprüft
 * wird deshalb der Baustein, auf dem die Übernahme beruht – dass aus dem
 * vollen Satz die richtige Nummer herausfällt und dass die Schwelle von
 * sechs Ziffern die Befehlswörter aussperrt.
 */
class NummerMitEinleitungTest {

    private fun ziffern(gesprochen: String) =
        GermanNumbers.toDigits(NameMatcher.normalize(gesprochen))

    @Test
    fun `der gemessene Satz ergibt die richtige Nummer`() {
        // Wörtlich aus dem Protokoll vom 01.10.2026, 14:38:27 Uhr.
        // "wer" statt "wähle" ist der tatsächliche Verhörer - der Satz muss
        // auch so noch tragen, sonst hilft die Einleitung im Alltag nicht.
        assertEquals("017846", ziffern("wer die nummer null eins sieben acht vier sex"))
    }

    @Test
    fun `ohne Einleitung blieben nur vier Ziffern uebrig`() {
        // Derselbe Sprecher, dieselbe Folge, wenige Sekunden vorher.
        // Unter MIN_RUFNUMMER_ZIFFERN - zu Recht verworfen.
        val d = ziffern("null eines sie wenn acht vier sechs")
        assertEquals("0846", d)
        assertTrue("muss unter der Schwelle bleiben", d.count { it.isDigit() } < 6)
    }

    @Test
    fun `die Befehlswoerter allein ergeben keine Nummer`() {
        // Der Schutz der Sechs-Ziffern-Schwelle: Wer nur den Befehl sagt,
        // soll ins Diktat kommen und nicht in eine Scheinnummer laufen.
        listOf(
            "nummer wahlen",
            "wahle die nummer",
            "ich mochte eine nummer wahlen",
            "nummer eins"
        ).forEach {
            val d = ziffern(it)
            assertTrue(
                "'$it' ergab '$d' - das darf keine Nummer werden",
                d.count { c -> c.isDigit() } < 6
            )
        }
    }

    @Test
    fun `Auslandsnummer mit null null`() {
        assertEquals(
            "004917680",
            ziffern("wahle die nummer null null vier neun eins sieben sechs acht null")
        )
    }

    @Test
    fun `Auslandsnummer mit plus`() {
        assertEquals(
            "+4917680",
            ziffern("wahle die nummer plus vier neun eins sieben sechs acht null")
        )
    }

    /**
     * Warum „null null" der sicherere Weg ist.
     *
     * „plus" hat keinen Verhörer-Schutz: Wird es falsch gehört, fällt es
     * ersatzlos weg und übrig bleibt eine Nummer, die gültig klingt. „null"
     * dagegen fängt „nun", „nul" und „nuller" ab. Dieser Test hält den
     * Unterschied fest, damit er bei einer späteren Änderung nicht
     * unbemerkt verschwindet.
     */
    @Test
    fun `ein verhoertes plus faellt ersatzlos weg`() {
        assertEquals("49", ziffern("wahle die nummer blues vier neun"))
        assertEquals("49", ziffern("wahle die nummer plu vier neun"))
        // Gegenprobe: ein verhörtes "null" wird aufgefangen.
        assertEquals("0049", ziffern("wahle die nummer nun nun vier neun"))
    }

    /**
     * Gegenprobe zu „nun", das am 01.10.2026 als Verhörer für „null"
     * nachgetragen wurde. Es ist ein häufiges Alltagswort, und ohne die
     * Ankerregel würde jeder Satz damit Ziffern erzeugen.
     */
    @Test
    fun `nun bleibt in Alltagssaetzen ein Wort`() {
        listOf(
            "nun ja",
            "was nun",
            "nun gut dann eben nicht",
            "und nun"
        ).forEach {
            assertEquals("'$it' darf keine Ziffer ergeben", "", ziffern(it))
        }
    }

    /**
     * Die Ankerregel allein trägt nicht – das ist wichtig zu wissen.
     *
     * „nur ein moment" ergibt `01`, weil „ein" ein echtes Zahlwort ist und
     * damit selbst als Anker taugt; „nur" grenzt daran und wird zur Null.
     * Das war schon vor der Ausbreitung so und ist unverändert.
     *
     * Was solche Sätze davon abhält, zu einer Rufnummer zu werden, ist
     * allein die Schwelle von sechs Ziffern. Wer sie je senken will, muss
     * diesen Test hier gelesen haben.
     */
    @Test
    fun `Alltagssaetze bleiben unter der Schwelle statt leer zu sein`() {
        val d = ziffern("nur ein moment")
        assertEquals("01", d)
        assertTrue("der eigentliche Schutz", d.count { it.isDigit() } < 6)
    }

    @Test
    fun `die Ausbreitung rettet zwei Verhoerer nebeneinander`() {
        // Vor der Ausbreitung fiel hier die erste Null weg, weil sie keinen
        // unmittelbar sicheren Nachbarn hatte - aus 0049 wurde 049.
        assertEquals("0049", ziffern("nun nun vier neun"))
    }

    /**
     * Der Messlauf vom 01.10.2026, 14:56:00 Uhr – die Auslandsnummer.
     *
     * Gesprochen: „nummer wählen null null vier neun eins sieben sechs
     * acht null". Vosk hörte „vielen" und „neuen"; beide fehlten in der
     * Verhörer-Tabelle, obwohl „viel" und „neu" darin standen. Die Vier
     * und die Neun fielen weg, und aus 004917680 wurde 0017680 – eine
     * andere, völlig gültig klingende Nummer.
     *
     * Stille Auslassungen sind die gefährlichste Fehlerart hier: Der
     * Nutzer hört eine plausible Nummer und bestätigt sie.
     */
    @Test
    fun `die gebeugten Verhoerer fressen die Vorwahl nicht mehr`() {
        assertEquals(
            "004917680",
            ziffern("nummer wir null null vielen neuen eins sieben sechs acht null")
        )
    }

    @Test
    fun `vielen Dank bleibt ein Dank`() {
        // Gegenprobe zu "vielen": ohne Anker daneben keine Ziffer.
        assertEquals("", ziffern("vielen dank"))
        assertEquals("", ziffern("vielen dank fuer alles"))
    }

    @Test
    fun `Sprachsteuerung stoppen beendet die App`() {
        // Aus dem Messlauf: Stephan sagte das naheliegende Gegenwort zum
        // Aktivierungswort und bekam "habe ich in den Kontakten nicht
        // gefunden". "stopp" und "stop" standen in der Liste, "stoppen"
        // nicht - ein Unterschied, den kein Nutzer macht.
        assertEquals(Command.ShutDown, CommandParser.parse("sprachsteuerung stoppen"))
        assertEquals(Command.ShutDown, CommandParser.parse("sprachsteuerung stopp"))
        assertEquals(Command.ShutDown, CommandParser.parse("sprachsteuerung anhalten"))
    }

    @Test
    fun `stopp allein bricht weiterhin nur den Schritt ab`() {
        // Die Trennung ist gewollt und hat einen eigenen Test in
        // AbbrechenTest. Hier steht sie als Wächter gegen den naheliegenden
        // Fehler, beim Nachtragen von "stoppen" gleich "stopp" mitzunehmen.
        assertEquals(Command.Cancel, CommandParser.parse("stopp"))
        assertEquals(Command.Cancel, CommandParser.parse("halt"))
    }

    @Test
    fun `neue Nummer wirft die bisherige weg`() {
        // 14:55:31 im Messlauf: "neue nummer" -> "Das war keine Ziffer."
        assertEquals(Command.Clear, CommandParser.parse("neue nummer"))
        assertEquals(Command.Clear, CommandParser.parse("nummer löschen"))
    }

    /**
     * Die Nachbarn von „neue nummer" sind belegt – und sollen es bleiben.
     *
     * Beim ersten Anlauf hatte ich „andere nummer", „noch mal" und
     * „nochmal" gleich mit in die Löschen-Liste geschrieben. Alle drei
     * waren schon vergeben: „andere" ist ein Nein (und tut im
     * Bestätigungsschritt genau das Richtige), „noch mal" ist
     * Wiederholen. Drei bestehende Tests sind daran gescheitert, zu Recht.
     */
    @Test
    fun `aehnliche Formulierungen behalten ihre Bedeutung`() {
        assertEquals(Command.No, CommandParser.parse("andere nummer"))
        assertEquals(Command.Repeat, CommandParser.parse("noch mal"))
        assertEquals(Command.Repeat, CommandParser.parse("nochmal"))
    }

    @Test
    fun `das Plus wird beim Vorlesen ausgesprochen`() {
        // Sonst hört ein blinder Nutzer keinen Unterschied zwischen
        // "+49 17 680" und "49 17 680".
        assertEquals("plus 4 9 1 7 6 8 0", GermanNumbers.spellOut("+4917680"))
        assertEquals("0 1 7 8 4 6", GermanNumbers.spellOut("017846"))
    }
}
