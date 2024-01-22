package com.github.jing332.tts_server_android.compose.systts

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.github.jing332.tts_server_android.R
import com.github.jing332.tts_server_android.compose.systts.directlink.LinkUploadSelectionDialog
import com.github.jing332.tts_server_android.compose.widgets.AppBottomSheet
import com.github.jing332.tts_server_android.ui.AppActivityResultContracts
import com.github.jing332.tts_server_android.ui.FilePickerActivity
import com.github.jing332.tts_server_android.ui.view.BigTextView
import com.github.jing332.tts_server_android.utils.ClipboardUtils
import com.github.jing332.tts_server_android.utils.toast


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfigExportBottomSheet(
    json: String,
    fileName: String = "config.json",
    content: @Composable ColumnScope.() -> Unit = {},
    onDismissRequest: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val fileSaver =
        rememberLauncherForActivityResult(AppActivityResultContracts.filePickerActivity()) {
        }

    var showSelectUploadTargetDialog by remember { mutableStateOf(false) }
    if (showSelectUploadTargetDialog)
        LinkUploadSelectionDialog(
            onDismissRequest = { showSelectUploadTargetDialog = false },
            json = json
        )

    AppBottomSheet(onDismissRequest = onDismissRequest) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
        ) {
            content()
            Row(Modifier.align(Alignment.CenterHorizontally)) {
                TextButton(
                    onClick = {
                        ClipboardUtils.copyText(json)
                        context.toast(R.string.copied)
                    }
                ) {
                    Text(stringResource(id = R.string.copy))
                }

                TextButton(
                    onClick = {
                        showSelectUploadTargetDialog = true
                    }
                ) {
                    Text(stringResource(id = R.string.upload_to_url))
                }

                TextButton(
                    onClick = {
                        fileSaver.launch(
                            FilePickerActivity.RequestSaveFile(
                                fileName = fileName,
                                fileMime = "application/json",
                                fileBytes = json.toByteArray()
                            )
                        )
                    }) {
                    Text(stringResource(id = R.string.save_as_file))
                }
            }

            var tv by remember {
                mutableStateOf<BigTextView?>(null)
            }

            AndroidView(modifier = Modifier.verticalScroll(rememberScrollState()), factory = {
                tv = BigTextView(it)

                tv!!
            }, update = {
                it.setText(json)
            })

            LaunchedEffect(key1 = json) {
                tv?.setText(json)
            }
        }
    }
}