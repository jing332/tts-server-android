package com.github.jing332.tts_server_android.compose.systts.list.edit

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.github.jing332.tts_server_android.R
import com.github.jing332.tts_server_android.compose.widgets.AppDialog

@Composable
fun TagDataClearConfirmDialog(
    tagData: String,
    onDismissRequest: () -> Unit,
    onConfirm: () -> Unit
) {
    AppDialog(
        title = { Text(stringResource(id = R.string.tag_data_clear_warn)) },
        content = { Text(tagData, style = MaterialTheme.typography.bodySmall) },
        buttons = {
            TextButton(onClick = onDismissRequest) {
                Text(stringResource(id = R.string.cancel))
            }
            TextButton(onClick = onConfirm) {
                Text(
                    stringResource(id = R.string.delete),
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error
                )
            }
        },
        onDismissRequest = onDismissRequest
    )
}