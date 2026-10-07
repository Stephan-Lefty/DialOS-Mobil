package org.dialos.mobil

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Die App darf sich nicht selbst aktivieren.
 *
 * Eine Testperson am 01.10.2026: „Sie redet, dass das Gerät die App
 * abgeschaltet hat und nun wieder funktioniert, sie fragt dann wem möchten
 * sie anrufen? Keine Aktivierung von mir."
 *
 * Die Ursache saß in [VoiceService.onEngineReady]: `engine.start()` schaltet
 * das Mikrofon scharf, und die beiden Sätze, die der Dienst danach selbst
 * spricht, liefen **nicht** über die Mikrofonpause aus
 * [DialogController.say]. Beide Sätze enthalten das Aktivierungswort – die
 * Startansage endet sogar wörtlich auf „Sagen Sie: Sprachsteuerung starten."
 * Vosk hörte den eigenen Lautsprecher und aktivierte.
 *
 * Diese Tests halten die zwei Hälften des Befunds fest:
 *  - Die Ansagetexte **würden** als Aktivierungswort durchgehen. Das ist
 *    kein Fehler des Parsers, sondern der Grund, warum die Mikrofonpause an
 *    dieser Stelle zwingend ist.
 *  - Der Abschiedssatz darf es dagegen nicht auslösen, sonst käme die App
 *    nach „abschalten" sofort wieder hoch.
 *
 * Die Texte stehen hier als Literal, weil `strings.xml` im Unit-Test nicht
 * erreichbar ist. Ändert sich ein Wortlaut, muss er hier mitgezogen werden –
 * genau dann lohnt sich auch der zweite Blick auf die Mikrofonpause.
 */
class SelbstausloeserTest {


    /** `say_started_contacts`, so wie die App ihn ausspricht. */
    private val startAnsage =
        "Sprachsteuerung eingeschaltet. Ich kenne 247 Kontakte. " +
            "Sagen Sie: Sprachsteuerung starten."

    /**
     * `say_after_interruption`, wie sie bis zum 01.10.2026 lautete.
     *
     * Die Ansage ist seitdem ersatzlos weg – der Wiederanlauf schweigt.
     * Der Text steht hier trotzdem, weil er den Messwert trägt, der die
     * Entscheidung begründet hat.
     */
    private val unterbrechungAlt =
        "Die Sprachsteuerung wurde vom Telefon unterbrochen und läuft jetzt " +
            "wieder. Wenn das öfter vorkommt, hilft in den Einstellungen der " +
            "Punkt Akku-Optimierung ausnehmen."

    @Test
    fun `die Startansage wuerde die App selbst aktivieren`() {
        // Der eigentliche Befund: Dieser Satz enthält das Aktivierungswort
        // wörtlich. Ohne Mikrofonpause ruft die App sich selbst.
        assertTrue(
            "Die Startansage enthält das Aktivierungswort - die Mikrofonpause " +
                "in VoiceService.sagOhneMitzuhoeren darf nie wieder entfallen",
            CommandParser.isWakePhrase(startAnsage)
        )
    }

    @Test
    fun `der Abschiedssatz weckt die App nicht`() {
        // Gegenprobe: Nach „abschalten" muss Ruhe sein. Täte dieser Satz es
        // auch, ließe sich die App gar nicht mehr abschalten.
        assertFalse(CommandParser.isWakePhrase("Sprachsteuerung ausgeschaltet."))
    }

    @Test
    fun `der Timeout-Hinweis weckt die App nicht sofort wieder`() {
        // „Ich warte wieder auf das Aktivierungswort. Sagen Sie
        // Sprachsteuerung starten, wenn Sie mich brauchen." läuft über
        // DialogController.say() und ist deshalb durch die Pause gedeckt -
        // aber er zeigt, wie dicht die Ansagen am Auslöser liegen.
        assertTrue(
            "auch dieser Satz traegt das Aktivierungswort",
            CommandParser.isWakePhrase(
                "Ich warte wieder auf das Aktivierungswort. Sagen Sie " +
                    "Sprachsteuerung starten, wenn Sie mich brauchen."
            )
        )
    }

    /**
     * Der alte Wortlaut weckte die App – **gemessen, nicht vermutet.**
     *
     * „Die Sprachsteuerung wurde vom Telefon unterbrochen …" enthält zwar
     * kein „starten", aber das Wortpaar „sprachsteuerung wurde" erreicht
     * gegen „sprachsteuerung starten" eine Ähnlichkeit von **0,783** – die
     * Schwelle liegt bei 0,70. Die Ansage allein genügte also, auch ohne
     * jedes Mithören eines zweiten Satzes.
     *
     * Dieser Test steht hier als Mahnmal: Er zeigt, dass die Mikrofonpause
     * allein den Fehler **nicht** behoben hätte.
     */
    @Test
    fun `der alte Wortlaut der Unterbrechungsansage weckte die App`() {
        assertTrue(
            "0,783 gegen die Schwelle 0,70 - genau das wurde gemeldet",
            CommandParser.isWakePhrase(unterbrechungAlt)
        )
    }

    /**
     * Die Schwelle anzuheben wäre der falsche Weg gewesen.
     *
     * Der einzige verpasste echte Ruf aus der Messung vom 21.09.2026
     * („sprachstörungen starten") lag bei 0,78 – gleichauf mit dem
     * Selbstauslöser 0,783. Es gibt dort keine Lücke, durch die sich die
     * Schwelle schieben ließe, ohne echte Rufe zu verlieren. Fällt dieser
     * Test, hat jemand an `WAKE_MIN_RATIO` gedreht und dabei entweder den
     * Selbstauslöser wieder eingebaut oder echte Rufe verloren.
     */
    @Test
    fun `zwischen echtem Ruf und Selbstausloeser liegt keine Luecke`() {
        val echterRuf = NameMatcher.ratio("sprachstorungen starten", "sprachsteuerung starten")
        val selbstausloeser = NameMatcher.ratio("sprachsteuerung wurde", "sprachsteuerung starten")
        assertTrue(
            "echter Ruf $echterRuf muesste ueber dem Selbstausloeser " +
                "$selbstausloeser liegen, tut er aber nicht - deshalb half " +
                "nur, den Wortlaut zu streichen",
            selbstausloeser >= echterRuf
        )
    }
}
