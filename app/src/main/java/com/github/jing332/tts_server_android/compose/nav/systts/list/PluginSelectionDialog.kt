package com.github.jing332.tts_server_android.compose.nav.systts.list

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import com.github.jing332.tts_server_android.R
import com.github.jing332.tts_server_android.data.appDb
import com.github.jing332.tts_server_android.data.entities.plugin.Plugin

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PluginSelectionDialog(onDismissRequest: () -> Unit, onSelect: (Plugin) -> Unit) {
    AlertDialog(onDismissRequest = onDismissRequest,
        title = { Text(stringResource(id = R.string.select_plugin)) },
        text = {
            LazyColumn {
                items(appDb.pluginDao.allEnabled, { it.id }) {
                    Text(text = it.name, modifier = Modifier
                        .animateItemPlacement()
                        .clip(MaterialTheme.shapes.small)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = rememberRipple()
                        ) {
                            onSelect(it)
                        }
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismissRequest) {
                Text(stringResource(id = R.string.cancel))
            }
        }
    )


}