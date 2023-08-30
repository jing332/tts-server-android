package com.github.jing332.tts_server_android.conf

import com.funny.data_saver.core.DataSaverPreferences
import com.funny.data_saver.core.mutableDataSaverStateOf
import com.github.jing332.tts_server_android.app

object MsTtsForwarderConfig {
    private val pref = DataSaverPreferences(app.getSharedPreferences("server", 0))

    val port = mutableDataSaverStateOf(pref, key = "port", 1233)
    val isWakeLockEnabled = mutableDataSaverStateOf(pref, key = "isWakeLockEnabled", false)
    val token = mutableDataSaverStateOf(pref, key = "token", "")
}