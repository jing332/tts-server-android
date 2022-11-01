package com.github.jing332.tts_server_android

import android.app.Application
import android.content.Context
import kotlin.properties.Delegates

class App : Application() {
    companion object {
        const val TAG = "App"
        var instance: App by Delegates.notNull()
        val context: Context by lazy { instance.applicationContext }
    }

    override fun onCreate() {
        super.onCreate()
        instance = this

        CrashHandler(this)
    }
}