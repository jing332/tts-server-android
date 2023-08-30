package com.github.jing332.tts_server_android.conf

import com.funny.data_saver.core.DataSaverPreferences
import com.funny.data_saver.core.mutableDataSaverStateOf
import com.github.jing332.tts_server_android.app
import com.github.jing332.tts_server_android.utils.FileUtils.readAllText

object DirectUploadConfig {
    private val pref = DataSaverPreferences(app.getSharedPreferences("direct_link_upload", 0))

    val code = mutableDataSaverStateOf(
        pref,
        key = "code",
        app.assets.open("defaultData/direct_link_upload.js").readAllText()
    )
}