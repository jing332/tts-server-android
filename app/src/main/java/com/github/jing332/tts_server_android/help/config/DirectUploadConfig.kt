package com.github.jing332.tts_server_android.help.config

import android.content.Context
import com.chibatching.kotpref.KotprefModel
import com.github.jing332.tts_server_android.utils.FileUtils.readAllText

object DirectUploadConfig : KotprefModel() {


    override val kotprefName: String = "direct_link_upload"
    var code by stringPref(
        context.assets.open("defaultData/direct_link_upload.js").readAllText()
    )

}