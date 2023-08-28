package com.github.jing332.tts_server_android.compose.codeeditor

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.github.jing332.tts_server_android.R
import com.github.jing332.tts_server_android.compose.widgets.AppDialog
import com.github.jing332.tts_server_android.compose.widgets.DenseOutlinedField
import com.github.jing332.tts_server_android.conf.CodeEditorConfig

@Composable
internal fun RemoteSyncSettings(onDismissRequest: () -> Unit) {
    AppDialog(title = { Text(stringResource(id = R.string.remote_sync_service)) }, content = {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                stringResource(id = R.string.remote_sync_service_description),
                modifier = Modifier.padding(8.dp)
            )

            var port by remember { CodeEditorConfig.remoteSyncPort }
            DenseOutlinedField(value = port.toString(), onValueChange = {
                try {
                    port = it.toInt()
                } catch (_: NumberFormatException) {
                }
            })

        }
    }, onDismissRequest = onDismissRequest)
}