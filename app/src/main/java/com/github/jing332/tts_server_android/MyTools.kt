package com.github.jing332.tts_server_android

import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import okhttp3.OkHttpClient
import okhttp3.Request
import okio.IOException
import org.json.JSONObject
import java.math.BigDecimal


class MyTools {
    companion object {
        val TAG = "MyTools"

        /*从Github检查更新*/
        fun checkUpdate(act: Activity) {
            val client = OkHttpClient()
            val request = Request.Builder()
                .url("https://api.github.com/repos/jing332/tts-server-android/releases/latest")
                .get()
                .build()
            client.newCall(request).enqueue(object : okhttp3.Callback {
                override fun onFailure(call: okhttp3.Call, e: IOException) {
                    Log.d(MainActivity.TAG, "check update onFailure: ${e.message}")
                    act.runOnUiThread {
                        Toast.makeText(act, "检查更新失败 请检查网络", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                    try {
                        val s = response.body?.string()
                        act.runOnUiThread {
                            checkVersionFromJson(act, s.toString())
                        }
                    } catch (e: Exception) {
                        act.runOnUiThread {
                            Toast.makeText(act, "检查更新失败", Toast.LENGTH_SHORT).show()
                            e.printStackTrace()
                        }
                    }
                }
            })
        }

        fun checkVersionFromJson(ctx: Context, s: String) {
            val json = JSONObject(s)
            val tag: String = json.getString("tag_name")
            val downloadUrl: String =
                json.getJSONArray("assets").getJSONObject(0)
                    .getString("browser_download_url")
            val body: String = json.getString("body") /*本次更新内容*/
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
                ) { dialog: DialogInterface?, which: Int ->
                    val intent = Intent(Intent.ACTION_VIEW)
                    intent.data = Uri.parse(downloadUrl)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    ctx.startActivity(intent)
                }
                .setNegativeButton(
                    "Github加速"
                ) { dialog: DialogInterface?, which: Int ->
                    val intent = Intent(Intent.ACTION_VIEW)
                    intent.data = Uri.parse("https://ghproxy.com/$downloadUrl")
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    ctx.startActivity(intent)
                }
                .create().show()
        }
    }
}
