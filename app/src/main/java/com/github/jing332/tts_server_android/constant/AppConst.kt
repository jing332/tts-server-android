package com.github.jing332.tts_server_android.constant

import android.content.pm.PackageManager
import com.github.jing332.tts_server_android.App

@Suppress("DEPRECATION")
object AppConst {
    val replaceRulesPath by lazy { App.context.filesDir.absolutePath + "/" + "/replace_rule.json" }

    val appInfo: AppInfo by lazy {
        val appInfo = AppInfo()
        App.context.packageManager.getPackageInfo(
            App.context.packageName,
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