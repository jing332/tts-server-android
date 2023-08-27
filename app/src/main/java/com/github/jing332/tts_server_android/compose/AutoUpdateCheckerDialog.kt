package com.github.jing332.tts_server_android.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.drake.net.utils.withIO
import com.github.jing332.tts_server_android.R
import com.github.jing332.tts_server_android.model.updater.AppUpdateChecker
import com.github.jing332.tts_server_android.model.updater.UpdateResult
import com.github.jing332.tts_server_android.utils.longToast

@Composable
internal fun AutoUpdateCheckerDialog(showUpdateToast: Boolean = true) {
    val context = LocalContext.current
    var showDialog by remember { mutableStateOf<UpdateResult?>(null) }
    if (showDialog != null) {
        val ret = showDialog!!
        LaunchedEffect(ret) {
            if (showUpdateToast && ret.hasUpdate())
                context.longToast(R.string.new_version_available, ret.version)
        }
        AppUpdateDialog(
            onDismissRequest = { showDialog = null },
            version = ret.version,
            content = ret.content,
            downloadUrl = ret.downloadUrl,
        )
    }

    LaunchedEffect(Unit) {
        val result = try {
            withIO { AppUpdateChecker.checkUpdate() }
        } catch (e: Exception) {
            context.longToast(context.getString(R.string.check_update_failed) + "\n$e")
            null
        }
        if (result?.hasUpdate() == true) showDialog = result
    }
}