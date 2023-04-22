package com.github.jing332.tts_server_android.service.forwarder.system

import android.content.Intent
import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import androidx.annotation.RequiresApi
import com.github.jing332.tts_server_android.service.forwarder.ForwarderServiceManager
import com.github.jing332.tts_server_android.service.forwarder.ForwarderServiceManager.startSysTtsForwarder

@RequiresApi(Build.VERSION_CODES.N)
class QSTileService : TileService() {
    override fun onStartListening() {
        super.onStartListening()
        if (SysTtsForwarderService.isRunning) {
            qsTile.state = Tile.STATE_ACTIVE
        } else {
            qsTile.state = Tile.STATE_INACTIVE
        }
        qsTile.updateTile()
    }

    override fun onClick() {
        super.onClick()
        if (qsTile.state == Tile.STATE_ACTIVE) { /* 关闭 */
            if (SysTtsForwarderService.isRunning) {
                ForwarderServiceManager.closeSysTtsForwarder()
            }
            qsTile.state = Tile.STATE_INACTIVE
        } else {/* 打开 */
            startSysTtsForwarder()
            qsTile.state = Tile.STATE_ACTIVE
        }
        qsTile.updateTile()
    }

}