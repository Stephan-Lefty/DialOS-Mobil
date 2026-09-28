package org.dialos.mobil

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Aus einem Testbericht vom 27.09.2026: „Ich wollte die MA40 anrufen, dieser
 * Kontakt steht so in meinem Telefon, diese hat die App nicht gefunden […]
 * Beidemale war es still in meiner Umgebung."
 *
 * Der letzte Satz ist der wichtigste: Die Testperson hat die naheliegende
 * Erklärung – Umgebungslärm – selbst ausgeschlossen. Nachgemessen war es
 * auch keine: „ma vierzig" gegen den Kontakt „MA40" ergibt 0,412 bei einer
 * Schwelle von 0,62. Das konnte nie funktionieren, bei keiner Lautstärke.
 *
 * Die Ursache ist eine Lücke zwischen zwei Schreibweisen derselben Sache: Im
 * Adressbuch steht die Ziffer, gesprochen wird das Zahlwort. Das trifft weit
 * mehr als diesen einen Fall – Behördenstellen, Buslinien, Zimmernummern,
 * „Werkstatt 2".
 */
class ZahlenImNamenTest {

    /** Der gemeldete Fall selbst. */
    @Test
    fun `MA40 wird gefunden, wie auch immer es ankommt`() {
        for (gesprochen in listOf("ma vierzig", "ma 40", "ma40", "m a vierzig")) {
            val wert = NameMatcher.score(gesprochen, "MA40")
            assertTrue(
                "„$gesprochen“ gegen MA40 ergab $wert, nötig sind ${NameMatcher.THRESHOLD}",
                wert >= NameMatcher.THRESHOLD
            )
        }
    }

    /**
     * Die zweite Umformung: Das Sprachmodell zerlegt unbekannte Namen in
     * bekannte Wörter. Gemessen lag „he li" gegen „Heli" wortweise bei 0,800
     * und zusammengezogen bei 1,000 – der Gewinn zeigt sich erst bei den
     * Fällen, die knapp darunter lagen.
     */
    @Test
    fun `zerlegte Namen werden wieder zusammengesetzt`() {
        for ((gesprochen, kontakt) in listOf(
            "he li" to "Heli",
            "lud wig" to "Ludwig",
            "an na" to "Anna",
            "mia michaela" to "Michaela"
        )) {
            val wert = NameMatcher.score(gesprochen, kontakt)
            assertTrue(
                "„$gesprochen“ gegen $kontakt ergab $wert",
                wert >= NameMatcher.THRESHOLD
            )
        }
    }

    /**
     * Die Gegenprobe, und der Grund für das Maximum über alle Varianten:
     * Jede Umformung für sich kann schaden. Zusammengezogen fällt „ma
     * vierzig" auf 0,222 und „Hans Peter" von 1,000 auf 0,900. Würde eine
     * Variante die andere ersetzen, wäre der eine Fall um den Preis des
     * anderen behoben.
     */
    @Test
    fun `bisherige Treffer bleiben unveraendert gut`() {
        assertEquals(1.0, NameMatcher.score("Hans Peter", "Hans Peter"), 0.001)
        assertEquals(1.0, NameMatcher.score("Anna Müller", "Anna Müller"), 0.001)
        assertTrue(NameMatcher.score("Müller", "Anna Müller") >= 0.9)
        assertTrue(NameMatcher.score("Meier", "Mayer") >= NameMatcher.THRESHOLD)
    }

    /** Und es dürfen keine neuen Falschtreffer entstehen. */
    @Test
    fun `fremde Namen treffen weiterhin nicht`() {
        for ((gesprochen, kontakt) in listOf(
            "anna" to "Hans",
            "eins zwei" to "Heli",
            "guten tag" to "Gudrun",
            "ma vierzig" to "Peter Schmidt"
        )) {
            val wert = NameMatcher.score(gesprochen, kontakt)
            assertTrue(
                "„$gesprochen“ sollte $kontakt NICHT treffen, ergab aber $wert",
                wert < NameMatcher.THRESHOLD
            )
        }
    }

    /**
     * Der Preis der Erweiterung, festgehalten statt verschwiegen.
     *
     * Weil das Maximum über mehr Varianten gebildet wird, steigen Werte – und
     * damit kommen gelegentlich mehr Kontakte über die Schwelle. „ma vierzig"
     * trifft jetzt auch einen Kontakt „Ute Vierzig" (gemessen 0,754, vorher
     * darunter). Das ist **kein** Fehler: Das Zahlwort steht dort wörtlich im
     * Namen, ein Nachname allein genügt seit jeher für einen Treffer, und bei
     * mehreren Treffern fragt die App ohnehin nach. Schlimmer wäre der
     * umgekehrte Fehler – ein Kontakt, den man per Sprache gar nicht erreicht.
     *
     * Der Test steht hier, damit diese Nebenwirkung beim nächsten Mal nicht
     * für einen Regress gehalten wird.
     */
    @Test
    fun `gleiches Zahlwort im Namen trifft mit - das ist gewollt`() {
        assertTrue(NameMatcher.score("ma vierzig", "Ute Vierzig") >= NameMatcher.THRESHOLD)
    }
}
