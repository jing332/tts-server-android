package com.github.jing332.tts_server_android

import android.app.Application
import android.content.Context
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.chibatching.kotpref.Kotpref
import com.drake.brv.utils.BRV
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
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
        val localBroadcast by lazy { LocalBroadcastManager.getInstance(context) }

        var isSysTtsLogEnabled = false
        var isServerLogEnabled = false

        @OptIn(ExperimentalSerializationApi::class)
        val jsonBuilder by lazy {
            Json {
                allowStructuredMapKeys = true
                ignoreUnknownKeys = true
                prettyPrint = true
                isLenient = true
                explicitNulls = false //忽略为null的字段
                allowStructuredMapKeys = true
            }
        }

        val isCnLocale: Boolean
            get() = context.resources.configuration.locale.language.endsWith("zh")


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