package com.github.jing332.tts_server_android.constant

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import com.github.jing332.tts_server_android.App
import com.github.jing332.tts_server_android.app
import com.script.javascript.RhinoScriptEngine
import java.text.SimpleDateFormat

@SuppressLint("SimpleDateFormat")
@Suppress("DEPRECATION")
object AppConst {
    const val PACKET_NAME = "com.github.jing332.tts_server_android"

    // JS引擎
    val SCRIPT_ENGINE: RhinoScriptEngine by lazy { RhinoScriptEngine() }

    val timeFormat: SimpleDateFormat by lazy {
        SimpleDateFormat("HH:mm")
    }

    val dateFormat: SimpleDateFormat by lazy {
        SimpleDateFormat("yyyy/MM/dd HH:mm")
    }

    val fileNameFormat: SimpleDateFormat by lazy {
        SimpleDateFormat("yy-MM-dd-HH-mm-ss")
    }

    val appInfo: AppInfo by lazy {
        val appInfo = AppInfo()
        App.context.packageManager.getPackageInfo(
            app.packageName,
            PackageManager.GET_ACTIVITIES
        )
            ?.let {
                appInfo.versionName = it.versionName
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                    appInfo.versionCode = it.longVersionCode
                } else {
                    @Suppress("DEPRECATION")
                    appInfo.versionCode = it.versionCode.toLong()
                }
            }
        appInfo
    }

    data class AppInfo(
        var versionCode: Long = 0L,
        var versionName: String = ""
    )
}