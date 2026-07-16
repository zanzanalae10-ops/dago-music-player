package com.dagomusic.core.services

import android.content.Intent
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import com.dagomusic.MainActivity

class MusicTileService : TileService() {

    override fun onStartListening() {
        super.onStartListening()
        val tile = qsTile
        if (tile != null) {
            tile.state = Tile.STATE_ACTIVE
            tile.label = "Dago Music"
            tile.updateTile()
        }
    }

    override fun onClick() {
        super.onClick()
        // Open the app when clicked
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        startActivityAndCollapse(intent)
    }
}
