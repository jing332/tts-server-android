package com.github.jing332.tts_server_android.compose.nav.systts

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FileOpen
import androidx.compose.material.icons.filled.Input
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.drake.net.Net
import com.drake.net.okhttp.trustSSLCertificate
import com.drake.net.utils.withMain
import com.github.jing332.tts_server_android.R
import com.github.jing332.tts_server_android.compose.widgets.RowToggleButtonGroup
import com.github.jing332.tts_server_android.ui.AppActivityResultContracts
import com.github.jing332.tts_server_android.ui.FilePickerActivity
import com.github.jing332.tts_server_android.utils.ClipboardUtils
import com.github.jing332.tts_server_android.utils.FileUtils.readAllText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.Response

class ImportSource {
    companion object {
        const val CLIPBOARD = 0
        const val FILE = 1
        const val URL = 2
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfigImportBottomSheet(onDismissRequest: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    suspend fun getConfig(
        src: Int,
        url: String? = null,
        uri: Uri? = null
    ): String {
        return when (src) {
            ImportSource.URL -> withContext(Dispatchers.IO) {
                val resp: Response = Net.get(url.toString()) {
                    setClient { trustSSLCertificate() }
                }.execute()
                val str = resp.body?.string()
                if (resp.isSuccessful && !str.isNullOrBlank()) {
                    return@withContext str
                } else {
                    throw Exception("GET $url failed: code=${resp.code}, message=${resp.message}, body=${str}")
                }
            }

            ImportSource.FILE -> withContext(Dispatchers.IO) {
                uri?.readAllText(context) ?: throw Exception("file uri is null!")
            }

            else -> withMain { ClipboardUtils.text.toString() }
        }
    }

    var source by remember { mutableIntStateOf(0) }
    var path by remember { mutableStateOf("") }
    var url by remember { mutableStateOf("") }
    ModalBottomSheet(onDismissRequest = onDismissRequest) {
        Column {
            Text(
                stringResource(id = R.string.import_config),
                modifier = Modifier.align(Alignment.CenterHorizontally),
                style = MaterialTheme.typography.titleLarge
            )

            Column(Modifier.fillMaxWidth()) {
                Text(
                    stringResource(id = R.string.source),
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    style = MaterialTheme.typography.titleMedium
                )

                RowToggleButtonGroup(
                    primarySelection = 0,
                    buttonCount = 3,
                    onButtonClick = { source = it },
                    buttonTexts = arrayOf(
                        R.string.clipboard, R.string.file, R.string.url_net
                    ).map { stringResource(id = it) }.toTypedArray(),
                    buttonIcons = arrayOf(
                        R.drawable.ic_baseline_select_all_24,
                        R.drawable.ic_baseline_insert_drive_file_24,
                        R.drawable.ic_web
                    ).map { painterResource(id = it) }.toTypedArray(),
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )

                when (source) {
                    1 -> { // File
                        val filePicker =
                            rememberLauncherForActivityResult(contract = AppActivityResultContracts.filePickerActivity()) {
                                it.second?.let { uri ->
                                    path = uri.toString()
                                }
                            }

                        OutlinedTextField(value = path, onValueChange = { path = it }, label = {
                            Text(stringResource(id = R.string.file))
                        }, trailingIcon = {
                            IconButton(onClick = {
                                filePicker.launch(FilePickerActivity.RequestSelectFile())
                            }) {
                                Icon(
                                    Icons.Default.FileOpen,
                                    stringResource(id = R.string.select_file)
                                )
                            }
                        })
                    }

                    2 -> {
                        OutlinedTextField(value = url, onValueChange = { url = it }, label = {
                            Text(stringResource(id = R.string.url_net))
                        })


                    }
                }
            }

            Box(Modifier.fillMaxWidth()) {
                TextButton(
                    modifier = Modifier.align(Alignment.CenterEnd),
                    onClick = {
                        scope.launch {
                            val jsonStr = getConfig(src = source, url = url, uri = Uri.parse(path))
                            println(jsonStr)
                        }
                    }) {
                    Row {
                        Icon(Icons.Default.Input, null)
                        Text(stringResource(id = R.string.import_config))
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun PreviewImportBottomSheet() {
    MaterialTheme {
        var show by remember { mutableStateOf(true) }
        if (show) {
            ConfigImportBottomSheet(onDismissRequest = { show = false })
        }
    }
}