package com.github.jing332.tts_server_android.util

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.graphics.drawable.Icon
import android.net.Uri
import android.os.Build
import android.util.Log
import android.widget.Toast
import com.drake.net.Net
import com.github.jing332.tts_server_android.App
import com.github.jing332.tts_server_android.BuildConfig
import com.github.jing332.tts_server_android.R
import com.github.jing332.tts_server_android.bean.GithubReleaseApiBean
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import okhttp3.Response
import java.math.BigDecimal


object MyTools {
    const val TAG = "MyTools"
    private const val GITHUB_RELEASES_LATEST_URL =
        "https://api.github.com/repos/jing332/tts-server-android/releases/latest"
    private val json by lazy { App.jsonBuilder }

    /*从Github检查更新*/
    @OptIn(DelicateCoroutinesApi::class)
    fun checkUpdate(ctx: Context, isFromUser: Boolean = true) {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val resp: Response = Net.get(GITHUB_RELEASES_LATEST_URL).execute()
                val jsonStr = resp.body?.string()
                checkVersionFromJson(ctx, jsonStr!!, isFromUser)
            } catch (e: Exception) {
                e.printStackTrace()
                if (isFromUser) ctx.toast(R.string.check_update_failed)
            }
        }
    }

    private fun checkVersionFromJson(ctx: Context, s: String, isFromUser: Boolean) {
        val cpuAbi = Build.SUPPORTED_ABIS[0]
        val bean = json.decodeFromString<GithubReleaseApiBean>(s)
        // 最大的为全量apk
        val apkUniversalUrl = bean.assets.sortedByDescending { it.size }[0].browserDownloadUrl
        // 根据CPU ABI判断精简版apk
        val liteApk =
            bean.assets.find { it.name.endsWith("${cpuAbi}.apk") }?.browserDownloadUrl ?: ""
        val apkUrl = liteApk.ifBlank { apkUniversalUrl }

        val tag = bean.tagName
        val body = bean.body

        val remoteVersion = BigDecimal(tag.split("_")[1].trim())
        val appVersion = BigDecimal(BuildConfig.VERSION_NAME.split("_")[1].trim())
        Log.d(TAG, "appVersionName: $appVersion, versionName: $remoteVersion")

        if (remoteVersion > appVersion) /* 需要更新 */
            runOnUI { downLoadAndInstall(ctx, body, apkUrl, tag) }
        else if (isFromUser)
            ctx.toast(R.string.current_is_last_version)
    }

    private fun downLoadAndInstall(
        ctx: Context,
        body: String,
        downloadUrl: String,
        tag: String
    ) {
        MaterialAlertDialogBuilder(ctx)
            .setTitle(ctx.getString(R.string.new_version_available, tag))
            .setMessage(body)
            .setNeutralButton(
                "Github"
            ) { _, _ -> startDownload(ctx, downloadUrl) }
            .setNegativeButton(
                "GhProxy"
            ) { _, _ -> startDownload(ctx, "https://ghproxy.com/$downloadUrl") }
            .setPositiveButton("FastGit") { _, _ ->
                startDownload(ctx, downloadUrl.replace("github.com", "download.fastgit.org"))
            }
            .show()
    }

    private fun startDownload(context: Context, url: String) {
        ClipboardUtils.copyText(url)
        context.toast(R.string.copied)
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse(url)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        context.startActivity(intent)
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
                val pendingIntentFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    PendingIntent.FLAG_IMMUTABLE
                } else {
                    0
                }
                val successCallback = PendingIntent.getBroadcast(
                    ctx, 0, pinnedShortcutCallbackIntent, pendingIntentFlags
                )
                shortcutManager.requestPinShortcut(
                    pinShortcutInfo, successCallback.intentSender
                )
            }
        }
    }

}
