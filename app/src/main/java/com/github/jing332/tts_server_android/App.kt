package com.github.jing332.tts_server_android

import android.app.Application
import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.os.LocaleList
import android.util.Log
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
                explicitNulls = false //忽略为null的字段
            }
        }

        val isCnLocale: Boolean
            get() = context.resources.configuration.locale.language.endsWith("zh")

        val locale: Locale
            get() = context.resources.configuration.locale
    }

    fun updateLocale(locale: Locale) {
        Log.i(TAG, "updateLocale: $locale")
        val configuration: Configuration = resources.configuration
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
            configuration.setLocales(LocaleList(locale))
        else
            configuration.setLocale(locale)

        resources.updateConfiguration(configuration, resources.displayMetrics)
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