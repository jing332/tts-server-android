package com.github.jing332.tts_server_android.conf

import com.funny.data_saver.core.DataSaverPreferences
import com.funny.data_saver.core.mutableDataSaverStateOf
import com.github.jing332.tts_server_android.app

object PluginConfig {
    private val pref = DataSaverPreferences(app.getSharedPreferences("plugin", 0))

    val textParam = mutableDataSaverStateOf(pref, key = "sampleText", "示例文本。 Sample text.")
//    val isSaveRhinoLog = mutableDataSaverStateOf(pref, key = "isSaveRhinoLog", false)
}