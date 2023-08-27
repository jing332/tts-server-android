package com.github.jing332.tts_server_android.compose.codeeditor

import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.github.jing332.tts_server_android.R
import com.github.jing332.tts_server_android.compose.widgets.AppDialog

@Composable
fun EditorThemeSettingsDialog(onDismissRequest: () -> Unit) {
    AppDialog(title = { Text(stringResource(id = R.string.theme)) },
        content = {

        }, buttons = {
            TextButton(onClick = {
                onDismissRequest()
            }) {
                Text(stringResource(id = R.string.close))
            }
        }) {
        onDismissRequest()
    }
}