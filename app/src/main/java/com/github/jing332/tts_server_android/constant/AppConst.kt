package com.github.jing332.tts_server_android.constant

import android.content.pm.PackageManager
import com.github.jing332.tts_server_android.App
import com.github.jing332.tts_server_android.app
import com.script.javascript.RhinoScriptEngine

@Suppress("DEPRECATION")
object AppConst {
    // JS引擎
    val SCRIPT_ENGINE: RhinoScriptEngine by lazy { RhinoScriptEngine() }

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