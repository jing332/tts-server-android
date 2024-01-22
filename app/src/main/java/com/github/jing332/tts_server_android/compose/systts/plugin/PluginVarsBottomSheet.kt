package com.github.jing332.tts_server_android.compose.systts.plugin

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.github.jing332.tts_server_android.compose.widgets.AppBottomSheet
import com.github.jing332.tts_server_android.data.entities.plugin.Plugin

@Composable
internal fun PluginVarsBottomSheet(
    onDismissRequest: () -> Unit,
    plugin: Plugin,
    onPluginChange: (Plugin) -> Unit
) {
    AppBottomSheet(onDismissRequest = onDismissRequest) {
        Text(
            text = plugin.name,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth(),
            textAlign = TextAlign.Center
        )
        plugin.defVars.forEach {
            val key = it.key
            val hint = it.value["hint"] ?: ""
            val label = it.value["label"] ?: ""
            val value = plugin.userVars.getOrDefault(key, "")

            OutlinedTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 4.dp, start = 8.dp, end = 8.dp),
                value = value,
                onValueChange = {
                    onPluginChange(
                        plugin.copy(
                            userVars = plugin.userVars.toMutableMap().apply {
                                this[key] = it
                                if (it.isBlank()) this.remove(key)
                            }
                        )
                    )
                },
                label = { Text(label) },
                placeholder = { Text(hint) },
            )
        }
    }
}