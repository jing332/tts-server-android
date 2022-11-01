package com.github.jing332.tts_server_android.service

import android.content.Intent
import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import androidx.annotation.RequiresApi

/* 快捷开关(Android 7+) */
@RequiresApi(Build.VERSION_CODES.N)
class QSTileService : TileService() {
    override fun startActivity(intent: Intent?) {
        super.startActivity(intent)
    }

    override fun onStartListening() {
        super.onStartListening()
        if (TtsIntentService.instance?.isRunning == true) {
            qsTile.state = Tile.STATE_ACTIVE
        } else {
            qsTile.state = Tile.STATE_INACTIVE
        }
        qsTile.updateTile()
    }

    override fun onClick() {
        super.onClick()
        if (qsTile.state == Tile.STATE_ACTIVE) { /* 关闭 */
            if (TtsIntentService.instance?.isRunning == true) {
                TtsIntentService.instance?.closeServer()
            }
            qsTile.state = Tile.STATE_INACTIVE
        } else {/* 打开 */
            val i = Intent(this.applicationContext, TtsIntentService::class.java)
            startService(i)
            qsTile.state = Tile.STATE_ACTIVE
        }
        qsTile.updateTile()
    }
}