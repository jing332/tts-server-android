package com.github.jing332.tts_server_android.compose.plugin

import android.os.Bundle
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.github.jing332.tts_server_android.R
import com.github.jing332.tts_server_android.compose.LocalNavController
import com.github.jing332.tts_server_android.compose.ShadowReorderableItem
import com.github.jing332.tts_server_android.compose.navigateSingleTop
import com.github.jing332.tts_server_android.data.appDb
import org.burnoutcrew.reorderable.detectReorderAfterLongPress
import org.burnoutcrew.reorderable.rememberReorderableLazyListState
import org.burnoutcrew.reorderable.reorderable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PluginManagerScreen(onFinishActivity: () -> Unit) {
    val navController = LocalNavController.current
    Scaffold(Modifier.fillMaxSize(), topBar = {
        TopAppBar(
            title = { Text(stringResource(id = R.string.plugin_manager)) },
            navigationIcon = {
                IconButton(onClick = onFinishActivity) {
                    Icon(Icons.Default.ArrowBack, stringResource(id = R.string.nav_back))
                }
            },
            actions = {
                IconButton(onClick = { /*TODO*/ }) {
                    Icon(Icons.Default.Add, stringResource(id = R.string.add_config))
                }

                IconButton(onClick = {

                }) {
                    Icon(Icons.Default.MoreVert, stringResource(id = R.string.more_options))
                }
            }
        )
    }) { paddingValues ->
        val flowAll = remember { appDb.pluginDao.flowAll() }
        val list by flowAll.collectAsState(initial = emptyList())

        LaunchedEffect(Unit) {
            appDb.pluginDao.all.forEachIndexed { index, plugin ->
                appDb.pluginDao.update(plugin.copy(order = index))
            }
        }

        val reorderState = rememberReorderableLazyListState(onMove = { from, to ->
            val fromItem = list[from.index]
            val toItem = list[to.index]
            appDb.pluginDao.update(
                fromItem.copy(order = toItem.order),
                toItem.copy(order = fromItem.order)
            )
        })

        LazyColumn(
            state = reorderState.listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .reorderable(reorderState)
        ) {
            itemsIndexed(list, key = { _, item -> item.id }) { _, item ->
                val desc = remember { "${item.author} - v${item.version}" }
                ShadowReorderableItem(reorderableState = reorderState, key = item.id) {
                    Item(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp, horizontal = 8.dp)
                            .detectReorderAfterLongPress(reorderState),
                        name = item.name,
                        desc = desc,
                        isEnabled = item.isEnabled,
                        onEnabledChange = {
                        },
                        onEdit = {
                            navController.navigateSingleTop(
                                NavRoutes.PluginEdit.id,
                                Bundle().apply {
                                    putParcelable(NavRoutes.PluginEdit.KEY_DATA, item)
                                }
                            )
                        },
                        onSetVars = {

                        },
                        onDelete = {

                        }
                    )
                }
            }
        }
    }
}

@Composable
internal fun Item(
    modifier: Modifier,
    name: String,
    desc: String,
    isEnabled: Boolean,
    onEnabledChange: (Boolean) -> Unit,
    onEdit: () -> Unit,
    onSetVars: () -> Unit,
    onDelete: () -> Unit,
) {
    ElevatedCard(modifier) {
        Row(modifier = Modifier.padding(4.dp), verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = isEnabled, onCheckedChange = onEnabledChange)
            Column(Modifier.weight(1f)) {
                Text(text = name, style = MaterialTheme.typography.titleMedium)
                Text(text = desc, style = MaterialTheme.typography.bodyMedium)
            }
            Row {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, stringResource(id = R.string.edit))
                }

                var showOptions by remember { mutableStateOf(false) }
                IconButton(onClick = { showOptions = true }) {
                    Icon(Icons.Default.MoreVert, stringResource(id = R.string.more_options))
                    DropdownMenu(
                        expanded = showOptions,
                        onDismissRequest = { showOptions = false }) {
                        DropdownMenuItem(
                            text = { Text(stringResource(id = R.string.plugin_set_vars)) },
                            onClick = onSetVars
                        )

                        HorizontalDivider()

                        DropdownMenuItem(
                            text = {
                                Text(
                                    stringResource(id = R.string.delete),
                                    color = MaterialTheme.colorScheme.error
                                )
                            },
                            onClick = onDelete
                        )
                    }
                }

            }
        }
    }
}

@Preview
@Composable
fun PreviewPluginManager() {
    MaterialTheme {
        PluginManagerScreen(onFinishActivity = {})
    }
}