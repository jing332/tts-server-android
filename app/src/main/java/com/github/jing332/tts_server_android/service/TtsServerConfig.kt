package com.github.jing332.tts_server_android.service

import android.content.Context
import com.github.jing332.tts_server_android.utils.SharedPrefsUtils

class TtsServerConfig(ctx: Context) {
    var port: Int = 1233
    var token = ""
    var isWakeLock = false
    var isUseDnsEdge = false

    init {
        port = SharedPrefsUtils.getPort(ctx)
        token = SharedPrefsUtils.getToken(ctx)
        isWakeLock = SharedPrefsUtils.getWakeLock(ctx)
        isUseDnsEdge = SharedPrefsUtils.getUseDnsEdge(ctx)
    }

    fun writeConfig(ctx: Context){
        SharedPrefsUtils.setPort(ctx, port)
        SharedPrefsUtils.setToken(ctx, token)
        SharedPrefsUtils.setWakeLock(ctx,isWakeLock)
    }
}