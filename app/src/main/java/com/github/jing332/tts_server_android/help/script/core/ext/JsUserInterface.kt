package com.github.jing332.tts_server_android.help.script.core.ext

import android.view.View
import android.view.ViewGroup
import com.github.jing332.tts_server_android.app
import com.github.jing332.tts_server_android.util.dp
import com.github.jing332.tts_server_android.util.longToast
import com.github.jing332.tts_server_android.util.toast

interface JsUserInterface {
    fun toast(msg: String) = app.toast(msg)
    fun longToast(msg: String) = app.longToast(msg)

    fun setMargins(v: View, left: Int, top: Int, right: Int, bottom: Int) {
        (v.layoutParams as ViewGroup.MarginLayoutParams).setMargins(
            left.dp,
            top.dp,
            right.dp,
            bottom.dp
        )
    }
//
}