package org.dialos.mobil

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Aus einem Testbericht vom 07.09.2026: "Die App beendet sich immer wieder
 * selbst." Ob das stimmte, war ohne Kabel nicht zu klären - ein vom System
 * beendeter Dienst ist kein Absturz und taucht in keiner Statistik auf.
 */
class InterruptionDetectorTest {

    @Test
    fun `regulaeres Einschalten ist keine Unterbrechung`() {
        assertEquals(
            StartCause.FRESH,
            InterruptionDetector.classify(
                wasRunning = false, savedUptime = 0, currentUptime = 5_000,
                systemRestart = false
            )
        )
    }

    @Test
    fun `nach einem Neustart des Telefons faengt die Betriebszeit von vorn an`() {
        assertEquals(
            StartCause.AFTER_REBOOT,
            InterruptionDetector.classify(
                wasRunning = true,
                savedUptime = 8 * 60 * 60 * 1000,   // acht Stunden gelaufen
                currentUptime = 12_000,             // gerade erst hochgefahren
                systemRestart = false
            )
        )
    }

    @Test
    fun `dieselbe Sitzung und trotzdem weg ist eine Unterbrechung`() {
        assertEquals(
            StartCause.AFTER_INTERRUPTION,
            InterruptionDetector.classify(
                wasRunning = true,
                savedUptime = 60_000,
                currentUptime = 3_600_000,          // eine Stunde später
                systemRestart = false
            )
        )
    }

    @Test
    fun `ein Neustart durch Android zaehlt immer als Unterbrechung`() {
        // START_STICKY ruft onStartCommand ohne Intent auf - und das tut
        // Android nur, wenn es den Dienst vorher selbst beendet hat.
        assertEquals(
            StartCause.AFTER_INTERRUPTION,
            InterruptionDetector.classify(
                wasRunning = false, savedUptime = 0, currentUptime = 0,
                systemRestart = true
            )
        )
    }

    @Test
    fun `gleiche Betriebszeit gilt noch als dieselbe Sitzung`() {
        // Grenzfall: Der Dienst wird sofort wieder gestartet, die Uhr des
        // Systems ist noch nicht weitergelaufen.
        assertEquals(
            StartCause.AFTER_INTERRUPTION,
            InterruptionDetector.classify(
                wasRunning = true, savedUptime = 42_000, currentUptime = 42_000,
                systemRestart = false
            )
        )
    }
}
