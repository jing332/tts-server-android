package com.github.jing332.tts_server_android.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.app.PendingIntent
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.graphics.drawable.Icon
import android.net.Uri
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.github.jing332.tts_server_android.bean.GithubReleaseApiBean
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import tts_server_lib.Tts_server_lib
import java.math.BigDecimal


object MyTools {
    const val TAG = "MyTools"
    private val json = Json { ignoreUnknownKeys = true }

    /*从Github检查更新*/
    @OptIn(DelicateCoroutinesApi::class)
    fun checkUpdate(act: Activity) {
        GlobalScope.launch {
            try {
                val data = Tts_server_lib.httpGet(
                    "https://api.github.com/repos/jing332/tts-server-android/releases/latest",
                    ""
                )
                act.runOnUiThread {
                    if (data == null)
                        Toast.makeText(act, "检查更新失败", Toast.LENGTH_SHORT).show()
                    else
                        checkVersionFromJson(act, data.decodeToString())
                }
            } catch (e: Exception) {
                act.runOnUiThread { Toast.makeText(act, "检查更新失败", Toast.LENGTH_SHORT).show() }
                e.printStackTrace()
            }

        }
    }

    @Suppress("DEPRECATION")
    private fun checkVersionFromJson(ctx: Context, s: String) {
        val bean = json.decodeFromString<GithubReleaseApiBean>(s)
        var apkUniversalUrl = ""
        var apkArmV8aUrl = ""
        var apkArmV7aUrl = ""
        bean.assets.forEach {
            if (it.name.contains("arm64-v8a")) {
                apkArmV8aUrl = it.browserDownloadUrl
            } else if (it.name.contains("armeabi-v7a")) {
                apkArmV7aUrl = it.browserDownloadUrl
            } else {
                apkUniversalUrl = it.browserDownloadUrl
            }
        }

        var downloadUrl = ""
        if (Build.SUPPORTED_ABIS.contains("arm64-v8a"))
            downloadUrl = apkArmV8aUrl
        else if (Build.SUPPORTED_ABIS.contains("armeabi-v7a")) {
            downloadUrl = apkArmV7aUrl
        }
        if (downloadUrl.isEmpty())
            downloadUrl = apkUniversalUrl

        val tag = bean.tagName
        val body = bean.body
        /* 远程版本号 */
        val versionName = BigDecimal(tag.split("_")[1].trim())
        val pi = ctx.packageManager.getPackageInfo(ctx.packageName, 0)
        val appVersionName = /* 本地版本号 */
            BigDecimal(pi.versionName.split("_").toTypedArray()[1].trim { it <= ' ' })
        Log.d(TAG, "appVersionName: $appVersionName, versionName: $versionName")
        if (appVersionName < versionName) {/* 需要更新 */
            downLoadAndInstall(ctx, body, downloadUrl, tag)
        } else {
            Toast.makeText(ctx, "当前已是最新版", Toast.LENGTH_SHORT).show()
        }
    }

    private fun downLoadAndInstall(
        ctx: Context,
        body: String,
        downloadUrl: String,
        tag: String
    ) {
        AlertDialog.Builder(ctx)
            .setTitle("有新版本")
            .setMessage("版本号: $tag\n\n$body")
            .setPositiveButton(
                "Github下载"
            ) { _: DialogInterface?, _: Int ->
                val intent = Intent(Intent.ACTION_VIEW)
                intent.data = Uri.parse(downloadUrl)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                ctx.startActivity(intent)
            }
            .setNegativeButton(
                "Github加速"
            ) { _: DialogInterface?, _: Int ->
                val intent = Intent(Intent.ACTION_VIEW)
                intent.data = Uri.parse("https://ghproxy.com/$downloadUrl")
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                ctx.startActivity(intent)
            }
            .create().show()
    }

    /* 添加快捷方式 */
    @SuppressLint("UnspecifiedImmutableFlag")
    @Suppress("DEPRECATION")
    fun addShortcut(
        ctx: Context,
        name: String,
        id: String,
        iconResId: Int,
        launcherIntent: Intent
    ) {
        if (Build.VERSION.SDK_INT < 26) { /* Android8.0 */
            Toast.makeText(ctx, "如失败 请手动授予权限", Toast.LENGTH_SHORT).show()
            val addShortcutIntent = Intent("com.android.launcher.action.INSTALL_SHORTCUT")
            // 不允许重复创建
            addShortcutIntent.putExtra("duplicate", false) // 经测试不是根据快捷方式的名字判断重复的
            addShortcutIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, name)
            addShortcutIntent.putExtra(
                Intent.EXTRA_SHORTCUT_ICON_RESOURCE,
                Intent.ShortcutIconResource.fromContext(
                    ctx, iconResId
                )
            )

            launcherIntent.action = Intent.ACTION_MAIN
            launcherIntent.addCategory(Intent.CATEGORY_LAUNCHER)
            addShortcutIntent
                .putExtra(Intent.EXTRA_SHORTCUT_INTENT, launcherIntent)

            // 发送广播
            ctx.sendBroadcast(addShortcutIntent)
        } else {
            val shortcutManager: ShortcutManager = ctx.getSystemService(ShortcutManager::class.java)
            if (shortcutManager.isRequestPinShortcutSupported) {
                launcherIntent.action = Intent.ACTION_VIEW
                val pinShortcutInfo = ShortcutInfo.Builder(ctx, id)
                    .setIcon(
                        Icon.createWithResource(ctx, iconResId)
                    )
                    .setIntent(launcherIntent)
                    .setShortLabel(name)
                    .build()
                val pinnedShortcutCallbackIntent = shortcutManager
                    .createShortcutResultIntent(pinShortcutInfo)
                //Get notified when a shortcut is pinned successfully//
                val successCallback = PendingIntent.getBroadcast(
                    ctx, 0, pinnedShortcutCallbackIntent, 0
                )
                shortcutManager.requestPinShortcut(
                    pinShortcutInfo, successCallback.intentSender
                )
            }
        }
    }

}
