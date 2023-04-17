package com.github.jing332.tts_server_android

import android.app.Application
import android.content.Context
import com.chibatching.kotpref.Kotpref
import com.drake.brv.utils.BRV
import java.util.*
import kotlin.properties.Delegates


val app: App
    inline get() = App.instance

@Suppress("DEPRECATION")
class App : Application() {
    companion object {
        const val TAG = "App"
        var instance: App by Delegates.notNull()
        val context: Context by lazy { instance }
    }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(AppLocale.updateApplicationLocale(base!!))
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        CrashHandler(this)

        Kotpref.init(this)
        // RecyclerView
        BRV.modelId = BR.m
    }
}