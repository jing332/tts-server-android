package com.github.jing332.tts_server_android.compose.systts.directlink


import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.drake.net.utils.withIO
import com.github.jing332.tts_server_android.R
import com.github.jing332.tts_server_android.compose.widgets.AppSelectionDialog
import com.github.jing332.tts_server_android.model.rhino.direct_link_upload.DirectUploadEngine
import com.github.jing332.tts_server_android.model.rhino.direct_link_upload.DirectUploadFunction
import com.github.jing332.tts_server_android.ui.view.AppDialogs.displayErrorDialog
import com.github.jing332.tts_server_android.utils.ClipboardUtils
import com.github.jing332.tts_server_android.utils.longToast
import kotlinx.coroutines.launch

@Composable
fun LinkUploadSelectionDialog(onDismissRequest: () -> Unit, json: String) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val targetList = remember {
        try {
            DirectUploadEngine(context = context).obtainFunctionList()
        } catch (e: Exception) {
            context.displayErrorDialog(e, context.getString(R.string.upload_to_url))
            null
        }
    }

    var loading by remember { mutableStateOf(false) }
    if (targetList != null)
        AppSelectionDialog(
            isLoading = loading,
            onDismissRequest = onDismissRequest,
            title = { Text(stringResource(id = R.string.choose_an_upload_target)) },
            value = Any(),
            values = targetList,
            entries = targetList.map { it.funcName },
            onClick = { value, _ ->
                scope.launch {
                    runCatching {
                        loading = true
                        val url =
                            withIO { (value as DirectUploadFunction).invoke(json) }
                                ?: throw Exception("url is null")
                        ClipboardUtils.copyText("TTS Server", url)
                        context.longToast(R.string.copied_url)
                        loading = false
                    }.onFailure {
                        loading = false
                        context.displayErrorDialog(it)
                        return@launch
                    }

                    onDismissRequest()
                }
            },
            buttons = {
                TextButton(onClick = onDismissRequest) {
                    Text(stringResource(id = R.string.cancel))
                }
            },
        )

}