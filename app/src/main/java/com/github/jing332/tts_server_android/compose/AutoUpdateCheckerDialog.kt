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
import kotlinx.coroutines.CancellationException

@Composable
internal fun AutoUpdateCheckerDialog(
    showUpdateToast: Boolean = true,
    fromAction: Boolean = false,
    dismiss: () -> Unit
) {
    val context = LocalContext.current
    var showDialog by remember { mutableStateOf<UpdateResult?>(null) }
    if (showDialog != null) {
        val ret = showDialog!!
        LaunchedEffect(ret) {
            if (showUpdateToast && ret.hasUpdate())
                context.longToast(R.string.new_version_available, ret.version)
        }
        AppUpdateDialog(
            onDismissRequest = {
                showDialog = null
                dismiss()
            },
            version = ret.version,
            content = ret.content,
            downloadUrl = ret.downloadUrl,
        )
    }

    var showActionDialog by remember { mutableStateOf<AppUpdateChecker.ActionResult?>(null) }
    if (showActionDialog != null) {
        val ret = showActionDialog!!
        AppUpdateActionDialog(
            onDismissRequest = {
                showActionDialog = null
                dismiss()
            },
            result = ret
        )
    }

    LaunchedEffect(Unit) {
        val result = try {
            withIO {
                if (fromAction) AppUpdateChecker.checkUpdateFromActions()
                else AppUpdateChecker.checkUpdate()
            }
        } catch (_: CancellationException) {
            null
        } catch (e: Exception) {
            e.printStackTrace()
            context.longToast(context.getString(R.string.check_update_failed) + "\n$e")
            null
        }

        if (result is AppUpdateChecker.ActionResult?) {
            if (result == null) {
                dismiss()
            } else {
                showActionDialog = result
            }
        } else if (result is UpdateResult?) {
            if (result?.hasUpdate() == true) showDialog = result
            else dismiss()
        }
    }
}