package com.github.jing332.tts_server_android.compose

import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.github.jing332.tts_server_android.BuildConfig
import com.github.jing332.tts_server_android.R
import com.github.jing332.tts_server_android.compose.widgets.AppDialog

@Composable
fun AboutDialog(onDismissRequest: () -> Unit) {
    val context = LocalContext.current

    AppDialog(
        onDismissRequest = onDismissRequest,
        title = {
            Row {
                Image(
                    painter = painterResource(id = R.mipmap.ic_launcher_round),
                    contentDescription = "Logo",
                    modifier = Modifier.align(Alignment.CenterVertically)
                )
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

            Column {
                SelectionContainer {
                    Column {
                        Text("APP - ${BuildConfig.VERSION_NAME}(${BuildConfig.VERSION_CODE})")
                    }
                }
                HorizontalDivider(Modifier.padding(vertical = 8.dp))
                Text(
                    "Github - TTS Server",
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .clickable {
                            openUrl("https://github.com/jing332/tts-server-android")
                        }
                        .padding(vertical = 8.dp)
                        .fillMaxWidth()
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