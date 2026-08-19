package org.dialos.mobil

import org.junit.Assert.assertEquals
import org.junit.Test

class GermanNumbersTest {

    @Test
    fun `einzeln gesprochene Ziffern`() {
        assertEquals("0179", GermanNumbers.toDigits("null eins sieben neun"))
    }

    @Test
    fun `fuehrende Null bleibt erhalten`() {
        assertEquals("089", GermanNumbers.toDigits("null acht neun"))
    }

    @Test
    fun `zusammengesetzte Zahlwoerter`() {
        assertEquals("21", GermanNumbers.toDigits("einundzwanzig"))
        assertEquals("47", GermanNumbers.toDigits("siebenundvierzig"))
    }

    @Test
    fun `Laendervorwahl mit Plus`() {
        assertEquals("+49", GermanNumbers.toDigits("plus neunundvierzig"))
    }

    @Test
    fun `doppel verdoppelt die naechste Ziffer`() {
        assertEquals("77", GermanNumbers.toDigits("doppel sieben"))
    }

    @Test
    fun `bereits erkannte Ziffern werden uebernommen`() {
        assertEquals("0176", GermanNumbers.toDigits("0176"))
    }

    @Test
    fun `Fuellwoerter liefern keine Ziffern`() {
        assertEquals("", GermanNumbers.toDigits("die nummer bitte"))
    }

    @Test
    fun `Ziffern werden einzeln vorgelesen`() {
        assertEquals("0 1 7 9", GermanNumbers.spellOut("0179"))
    }
}
