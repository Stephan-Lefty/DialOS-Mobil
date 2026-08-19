package org.dialos.mobil

import android.service.quicksettings.Tile
import android.service.quicksettings.TileService

/**
 * Kachel in den Schnelleinstellungen: ein Tipp startet das Gespräch sofort,
 * ohne dass die App geöffnet oder das Aktivierungswort gesagt werden muss.
 */
class VoiceTileService : TileService() {

    override fun onStartListening() {
        super.onStartListening()
        refresh()
    }

    override fun onClick() {
        super.onClick()
        // activate() startet den Dienst mit, falls er noch nicht läuft.
        VoiceService.activate(this)
        refresh()
    }

    private fun refresh() {
        val tile = qsTile ?: return
        tile.state = if (VoiceService.isRunning) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
        tile.label = getString(R.string.tile_label)
        tile.updateTile()
    }
}
