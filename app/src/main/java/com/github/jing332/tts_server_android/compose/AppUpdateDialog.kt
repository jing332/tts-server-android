package com.github.jing332.tts_server_android.compose

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.github.jing332.tts_server_android.R
import com.github.jing332.tts_server_android.compose.widgets.AppDialog
import com.github.jing332.tts_server_android.compose.widgets.Markdown
import com.github.jing332.tts_server_android.constant.AppConst
import com.github.jing332.tts_server_android.model.updater.AppUpdateChecker
import com.github.jing332.tts_server_android.utils.ClipboardUtils

@Preview
@Composable
private fun PreviewAppUpdateDialog() {
    var show by remember { androidx.compose.runtime.mutableStateOf(true) }
    if (show)
        AppUpdateDialog(
            onDismissRequest = {
                show = false
            }, version = "1.0.0", content = "## 更新内容\n\n- 123", downloadUrl = "url"
        )

}

@Composable
fun AppUpdateActionDialog(onDismissRequest: () -> Unit, result: AppUpdateChecker.ActionResult) {
    val context = LocalContext.current
    fun openDownloadUrl(url: String) {
        ClipboardUtils.copyText("TTS Server", url)
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    }
    AppDialog(onDismissRequest = onDismissRequest, title = {
        Column {
            Text(
                stringResource(id = R.string.check_update) + " (Github Actions)",
                style = MaterialTheme.typography.titleLarge,
            )
            Text(
                text = AppConst.dateFormatSec.format(result.time * 1000),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
        }
    }, content = {
        Column(Modifier.padding(12.dp)) {
            Text(
                text = result.title,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    },
        buttons = {
            Row {
                TextButton(onClick = {
                    onDismissRequest()
                    openDownloadUrl(result.url)
                }) {
                    Text("Github")
                }

                TextButton(onClick = { onDismissRequest() }) {
                    Text(stringResource(id = R.string.cancel))
                }
            }
        }
    )
}

@Composable
fun AppUpdateDialog(
    onDismissRequest: () -> Unit,
    version: String,
    content: String,
    downloadUrl: String
) {
    val context = LocalContext.current
    fun openDownloadUrl(url: String) {
        ClipboardUtils.copyText("TTS Server", url)
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    }

    AppDialog(onDismissRequest = onDismissRequest,
        title = {
            Text(
                stringResource(id = R.string.check_update),
                style = MaterialTheme.typography.titleLarge,
            )
        },
        content = {
            Column {
                Text(
                    text = version,
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )

                val scrollState = rememberScrollState()
                Column(
                    Modifier
                        .padding(8.dp)
                        .verticalScroll(scrollState),
                    verticalArrangement = Arrangement.Center
                ) {
                    Markdown(
                        content = content,
                        modifier = Modifier
                            .padding(4.dp),
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                }
            }
        },
        buttons = {
            Row {
                TextButton(onClick = { openDownloadUrl(downloadUrl) }) {
                    Text("下载(Github)")
                }
                TextButton(onClick = { openDownloadUrl("https://ghproxy.com/${downloadUrl}") }) {
                    Text("下载(ghproxy加速)")
                }
            }
        }
    )
}