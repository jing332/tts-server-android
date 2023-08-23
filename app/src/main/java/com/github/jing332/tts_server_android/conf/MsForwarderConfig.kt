package com.github.jing332.tts_server_android.conf

import com.funny.data_saver.core.DataSaverPreferences
import com.funny.data_saver.core.mutableDataSaverStateOf
import com.github.jing332.tts_server_android.app

object MsForwarderConfig {
    private val pref = DataSaverPreferences(app.getSharedPreferences("server", 0))

    val port = mutableDataSaverStateOf(
        dataSaverInterface = pref,
        key = "port",
        initialValue = 1233
    )

    val isWakeLockEnabled = mutableDataSaverStateOf(
        dataSaverInterface = pref,
        key = "isWakeLockEnabled",
        initialValue = false
    )

    val token = mutableDataSaverStateOf(
        dataSaverInterface = pref,
        key = "token",
        initialValue = "",
    )
}