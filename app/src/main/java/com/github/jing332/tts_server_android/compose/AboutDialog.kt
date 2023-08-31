package com.github.jing332.tts_server_android.compose

import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextAlign.Companion
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.github.jing332.tts_server_android.BuildConfig
import com.github.jing332.tts_server_android.R
import com.github.jing332.tts_server_android.compose.widgets.AppDialog
import com.github.jing332.tts_server_android.compose.widgets.AppLauncherIcon

@Composable
fun AboutDialog(onDismissRequest: () -> Unit) {
    val context = LocalContext.current

    AppDialog(
        onDismissRequest = onDismissRequest,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AppLauncherIcon(modifier = Modifier.size(64.dp))
                Text(
                    stringResource(id = R.string.app_name),
                    modifier = Modifier
                        .align(Alignment.CenterVertically)
                        .padding(start = 8.dp)
                )
            }
        },
        content = {
            fun openUrl(uri: String) {
                context.startActivity(
                    Intent(Intent.ACTION_VIEW).apply {
                        data = uri.toUri()
                    }
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                SelectionContainer {
                    Column(Modifier.padding(vertical = 4.dp)) {
                        Text("${BuildConfig.VERSION_NAME}(${BuildConfig.VERSION_CODE})")
                    }
                }
                HorizontalDivider(Modifier.padding(vertical = 8.dp))
                Text(
                    "Github - TTS Server",
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .clip(MaterialTheme.shapes.small)
                        .clickable {
                            openUrl("https://github.com/jing332/tts-server-android")
                        }
                        .padding(vertical = 8.dp)
                        .fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }
        },
        buttons = {
            TextButton(onClick = {
                onDismissRequest()
                context.startActivity(
                    Intent(context, LibrariesActivity::class.java).setAction(Intent.ACTION_VIEW)
                )
            }) {
                Text(text = stringResource(id = R.string.open_source_license))
            }

            TextButton(onClick = onDismissRequest) {
                Text(stringResource(id = R.string.confirm))
            }
        })
}