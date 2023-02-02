package com.github.jing332.tts_server_android.service.forwarder.system

import android.content.Intent
import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import androidx.annotation.RequiresApi

@RequiresApi(Build.VERSION_CODES.N)
class QSTileService : TileService() {
    override fun onStartListening() {
        super.onStartListening()
        if (SysTtsForwarderService.instance?.isRunning == true) {
            qsTile.state = Tile.STATE_ACTIVE
        } else {
            qsTile.state = Tile.STATE_INACTIVE
        }
        qsTile.updateTile()
    }

    override fun onClick() {
        super.onClick()
        if (qsTile.state == Tile.STATE_ACTIVE) { /* 关闭 */
            if (SysTtsForwarderService.instance?.isRunning == true) {
                SysTtsForwarderService.requestCloseServer()
            }
            qsTile.state = Tile.STATE_INACTIVE
        } else {/* 打开 */
            val i = Intent(this, SysTtsForwarderService::class.java)
            startService(i)
            qsTile.state = Tile.STATE_ACTIVE
        }
        qsTile.updateTile()
    }

}