package com.github.jing332.tts_server_android

import android.app.Application
import android.content.Context
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.chibatching.kotpref.Kotpref
import com.drake.brv.utils.BRV
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlin.properties.Delegates

val app: App by lazy { App.instance }

@Suppress("DEPRECATION")
class App : Application() {
    companion object {
        const val TAG = "App"
        var instance: App by Delegates.notNull()
        val context: Context by lazy { instance.applicationContext }
        val localBroadcast by lazy { LocalBroadcastManager.getInstance(context) }

        var isSysTtsLogEnabled = false
        var isServerLogEnabled = false

        @OptIn(ExperimentalSerializationApi::class)
        val jsonBuilder by lazy {
            Json {
                allowStructuredMapKeys = true
                ignoreUnknownKeys = true
                prettyPrint = true
                explicitNulls = false //忽略为null的字段
            }
        }

        val isCnLocale by lazy {
            val locale = context.resources.configuration.locale
            locale.language.endsWith("zh")
        }
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        CrashHandler(this)

        // SharedPreference
        Kotpref.init(this)

        // RecyclerView
        BRV.modelId = BR.m
    }
}