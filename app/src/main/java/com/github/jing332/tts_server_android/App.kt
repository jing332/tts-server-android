package com.github.jing332.tts_server_android

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.Process
import com.github.jing332.tts_server_android.model.hanlp.HanlpManager
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
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

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base.apply { AppLocale.setLocale(base) })
    }

    @SuppressLint("SdCardPath")
    @OptIn(DelicateCoroutinesApi::class)
    override fun onCreate() {
        super.onCreate()
        instance = this
        CrashHandler(this)

        GlobalScope.launch {
            HanlpManager.initDir(
                context.getExternalFilesDir("hanlp")?.absolutePath
                    ?: "/data/data/$packageName/files/hanlp"
            )
        }
    }

    @SuppressLint("UnspecifiedImmutableFlag")
    fun restart() {
        val intent = packageManager.getLaunchIntentForPackage(packageName)!!
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        //杀掉以前进程
        Process.killProcess(Process.myPid());
    }
}