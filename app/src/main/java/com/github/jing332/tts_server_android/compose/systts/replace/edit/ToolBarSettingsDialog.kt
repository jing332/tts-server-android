package com.github.jing332.tts_server_android.compose.systts.replace.edit

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.github.jing332.tts_server_android.R
import com.github.jing332.tts_server_android.compose.widgets.AppDialog

@Composable
fun ToolBarSettingsDialog(
    onDismissRequest: () -> Unit,
    symbols: LinkedHashMap<String, String>,
    onSave: (LinkedHashMap<String, String>) -> Unit,
    onReset: () -> Unit
) {
    var text by remember(symbols) {
        mutableStateOf(symbols.map { "${it.key} = ${it.value}" }.joinToString("\n"))
    }

    AppDialog(onDismissRequest = onDismissRequest,
        title = { Text(stringResource(id = R.string.keyboard_toolbar_settings)) },
        content = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(stringResource(R.string.keyboard_toolbar_settings_tips))

                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = text,
                    onValueChange = { text = it }
                )
            }
        },
        buttons = {
            Row(Modifier.fillMaxWidth()) {
                TextButton(onClick = onReset) {
                    Text(stringResource(id = R.string.reset))
                }
                Spacer(modifier = Modifier.weight(1f))
                Row {
                    TextButton(onClick = onDismissRequest) {
                        Text(stringResource(id = R.string.cancel))
                    }

                    TextButton(onClick = {
                        onSave(
                            text.split("\n").associate {
                                val (key, value) = it.split(" = ")
                                key to value
                            } as LinkedHashMap<String, String>
                        )
                    }) {
                        Text(stringResource(id = R.string.save))
                    }
                }
            }
        }
    )
}