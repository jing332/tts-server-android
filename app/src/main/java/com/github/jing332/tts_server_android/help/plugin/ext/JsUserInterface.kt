package com.github.jing332.tts_server_android.help.plugin.ext

import com.github.jing332.tts_server_android.app
import com.github.jing332.tts_server_android.util.longToast
import com.github.jing332.tts_server_android.util.toast

interface JsUserInterface {
    fun toast(msg: String) = app.toast(msg)
    fun longToast(msg: String) = app.longToast(msg)

}