package com.github.jing332.tts_server_android.model.updater

import android.os.Build
import android.util.Log
import com.github.jing332.tts_server_android.BuildConfig
import com.github.jing332.tts_server_android.utils.toNumberInt
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter


object AppUpdateChecker {
    const val TAG = "AppUpdateChecker"

    fun checkUpdate(): UpdateResult {
        val rel = Github.getLatestRelease()

        val latest = rel.tagName.toNumberInt()
        val current = BuildConfig.VERSION_NAME.toNumberInt()
        Log.i(TAG, "checkUpdate: current=$current, latest=$latest")

        if (current < latest) {
            val ass = getApkDownloadUrl(rel.assets)
            return UpdateResult(
                version = rel.tagName,
                content = rel.body,
                downloadUrl = ass.browserDownloadUrl
            ).apply {
                Log.i(TAG, "checkUpdate: hasUpdate=${this.downloadUrl}")
            }
        }

        return UpdateResult()
    }

    private fun getApkDownloadUrl(assets: List<Github.Release.Asset>): Github.Release.Asset {
        val abi = Build.SUPPORTED_ABIS[0]

        // 最大的为全量apk
        val apkUniversal = assets.sortedByDescending { it.size }[0]
        // 根据CPU ABI判断精简版apk
        val liteApk =
            assets.find { it.name.endsWith("${abi}.apk") }

        return liteApk ?: apkUniversal
    }

    private fun toTimestamp(str: String): Long {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")
        val dateTime = LocalDateTime.parse(str, formatter)

        // 将LocalDateTime转换为时间戳（秒）
        val instant = dateTime.toInstant(ZoneOffset.UTC)
        return instant.epochSecond
    }


    fun checkUpdateFromActions(path: String = ".github/workflows/test.yml"): ActionResult? {
        val workflowRuns = Github.getActions()
        val run = workflowRuns.workflowRuns.find { it.path == path }

        if (run != null && run.status == "completed" && run.conclusion == "success") {
            val actionTs = toTimestamp(run.createdAt)
            Log.i(
                TAG,
                "checkUpdateFromActions: actionTs=$actionTs, buildTs=${BuildConfig.BUILD_TIME}"
            )
            if (actionTs <= BuildConfig.BUILD_TIME) return null
            return ActionResult(
                url = run.htmlUrl,
                title = run.displayTitle,
                time = actionTs
            )
        }

        return null
    }

    data class ActionResult(
        val url: String,
        val title: String,
        val time: Long,
    )
}