package org.dialos.mobil

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class StartVersucheTest {

    @Test
    fun `am Anfang darf gestartet werden`() {
        val v = StartVersuche()
        assertTrue(v.darfNochmal())
        assertFalse(v.aufgegeben())
        assertEquals(0, v.gescheiterte)
    }

    @Test
    fun `nach drei Fehlschlaegen ist Schluss`() {
        val v = StartVersuche(hoechstens = 3)
        repeat(2) { v.gescheitert() }
        assertTrue("nach zwei Fehlschlaegen noch ein Versuch", v.darfNochmal())
        v.gescheitert()
        assertFalse("nach dem dritten nicht mehr", v.darfNochmal())
        assertTrue(v.aufgegeben())
    }

    @Test
    fun `ein geglueckter Start gibt die Versuche zurueck`() {
        // Der Fall, der sonst durchrutscht: drei ueber Wochen verteilte
        // Aussetzer wuerden die App sonst dauerhaft stumm schalten.
        val v = StartVersuche(hoechstens = 3)
        repeat(3) { v.gescheitert() }
        assertTrue(v.aufgegeben())

        v.geglueckt()

        assertTrue("nach einem geglueckten Start wieder frei", v.darfNochmal())
        assertEquals(0, v.gescheiterte)
    }

    @Test
    fun `ein einzelner Versuch ist erlaubt`() {
        val v = StartVersuche(hoechstens = 1)
        assertTrue(v.darfNochmal())
        v.gescheitert()
        assertFalse(v.darfNochmal())
    }

    @Test(expected = IllegalArgumentException::class)
    fun `null Versuche sind kein sinnvoller Wert`() {
        StartVersuche(hoechstens = 0)
    }

    @Test
    fun `wiederholtes Scheitern zaehlt nicht ins Unendliche hoch`() {
        val v = StartVersuche(hoechstens = 2)
        repeat(1000) { v.gescheitert() }
        assertTrue(v.aufgegeben())
        v.geglueckt()
        assertTrue("auch nach vielen Fehlschlaegen erholt es sich", v.darfNochmal())
    }
}
