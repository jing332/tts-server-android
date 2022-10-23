package com.github.jing332.tts_server_android.service

import android.content.Intent
import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import android.util.Log
import androidx.annotation.RequiresApi
import com.github.jing332.tts_server_android.utils.SharedPrefsUtils

/* 快捷开关 */
@RequiresApi(Build.VERSION_CODES.N)
class QSTileService : TileService() {
    override fun startActivity(intent: Intent?) {
        Log.e("TAG", intent.toString())
        super.startActivity(intent)
    }

    override fun onStartListening() {
        super.onStartListening()

        if (TtsIntentService.IsRunning) {
            qsTile.state = Tile.STATE_ACTIVE
        } else {
            qsTile.state = Tile.STATE_INACTIVE
        }
        qsTile.updateTile()
    }

    override fun onClick() {
        super.onClick()

        if (qsTile.state == Tile.STATE_ACTIVE) { /* 关闭 */
            if (TtsIntentService.IsRunning) {
                TtsIntentService.closeServer(this)
            }
            qsTile.state = Tile.STATE_INACTIVE
        } else {/* 打开 */
            val i = Intent(this.applicationContext, TtsIntentService::class.java)
            i.putExtra("isWakeLock", SharedPrefsUtils.getWakeLock(this))
            i.putExtra("token", SharedPrefsUtils.getToken(this))
            startService(i)
            qsTile.state = Tile.STATE_ACTIVE
        }
        qsTile.updateTile()

    }

}