package com.github.jing332.tts_server_android.compose.backup

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.github.jing332.tts_server_android.R
import com.github.jing332.tts_server_android.app
import com.github.jing332.tts_server_android.compose.widgets.AppDialog
import com.github.jing332.tts_server_android.compose.widgets.LoadingContent
import com.github.jing332.tts_server_android.ui.AppActivityResultContracts
import com.github.jing332.tts_server_android.ui.FilePickerActivity
import com.github.jing332.tts_server_android.ui.view.AppDialogs.displayErrorDialog
import com.github.jing332.tts_server_android.utils.FileUtils.readBytes
import kotlinx.coroutines.launch

@Composable
internal fun RestoreDialog(onDismissRequest: () -> Unit, vm: BackupRestoreViewModel = viewModel()) {
    var isLoading by remember { mutableStateOf(true) }
    var needRestart by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val filePicker =
        rememberLauncherForActivityResult(contract = AppActivityResultContracts.filePickerActivity())
        {
            if (it.second == null) {
                onDismissRequest()
                return@rememberLauncherForActivityResult
            }
            scope.launch {
                runCatching {
                    needRestart = vm.restore(it.second!!.readBytes(context))
                    isLoading = false
                }.onFailure {
                    context.displayErrorDialog(it)
                }
            }
        }

    LaunchedEffect(Unit) {
        filePicker.launch(FilePickerActivity.RequestSelectFile(listOf("application/zip")))
    }

    AppDialog(
        onDismissRequest = onDismissRequest,
        title = { Text(stringResource(id = R.string.restore)) },
        content = {
            LoadingContent(
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                isLoading = isLoading
            ) {
                if (!isLoading)
                    if (needRestart)
                        Text(stringResource(id = R.string.restore_restart_msg))
                    else
                        Text(stringResource(id = R.string.restore_finished))
            }
        },
        buttons = {
            if (needRestart) {
                TextButton(onClick = onDismissRequest) {
                    Text(stringResource(id = R.string.cancel))
                }

                TextButton(onClick = {
                    app.restart()
                }) {
                    Text(stringResource(id = R.string.restart))
                }
            } else {
                TextButton(onClick = onDismissRequest) {
                    Text(stringResource(id = R.string.confirm))
                }
            }


        }
    )
}