package com.github.jing332.tts_server_android.compose.systts.plugin

import android.os.Bundle
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Input
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Output
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.github.jing332.tts_server_android.R
import com.github.jing332.tts_server_android.compose.LocalNavController
import com.github.jing332.tts_server_android.compose.ShadowReorderableItem
import com.github.jing332.tts_server_android.compose.nav.systts.ConfigDeleteDialog
import com.github.jing332.tts_server_android.compose.navigateSingleTop
import com.github.jing332.tts_server_android.constant.AppConst
import com.github.jing332.tts_server_android.data.appDb
import com.github.jing332.tts_server_android.data.entities.plugin.Plugin
import kotlinx.serialization.encodeToString
import org.burnoutcrew.reorderable.detectReorderAfterLongPress
import org.burnoutcrew.reorderable.rememberReorderableLazyListState
import org.burnoutcrew.reorderable.reorderable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PluginManagerScreen(onFinishActivity: () -> Unit) {
    var showImportConfig by remember { mutableStateOf(false) }
    if (showImportConfig) {
        PluginImportBottomSheet(onDismissRequest = { showImportConfig = false })
    }

    var showExportConfig by remember { mutableStateOf<List<Plugin>?>(null) }
    if (showExportConfig != null) {
        val pluginList = showExportConfig!!
        PluginExportBottomSheet(
            fileName = if (pluginList.size == 1) "ttsrv-plugin-${pluginList[0].name}.json" else "ttsrv-plugins.json",
            onDismissRequest = { showExportConfig = null }) { isExportVars ->
            if (isExportVars) {
                AppConst.jsonBuilder.encodeToString(pluginList)
            } else {
                AppConst.jsonBuilder.encodeToString(pluginList.map { it.copy(userVars = mutableMapOf()) })
            }
        }
    }

    var showDeleteDialog by remember { mutableStateOf<Plugin?>(null) }
    if (showDeleteDialog != null) {
        val plugin = showDeleteDialog!!
        ConfigDeleteDialog(onDismissRequest = { showDeleteDialog = null }, name = plugin.name) {
            appDb.pluginDao.delete(plugin)
            showDeleteDialog = null
        }
    }

    var showVarsSettings by remember { mutableStateOf<Plugin?>(null) }
    if (showVarsSettings != null) {
        var plugin by remember { mutableStateOf(showVarsSettings!!) }
        if (plugin.defVars.isEmpty()) {
            showVarsSettings = null
        }
        PluginVarsBottomSheet(onDismissRequest = {
            appDb.pluginDao.update(plugin)
            showVarsSettings = null
        }, plugin = plugin) {
            plugin = it
        }
    }

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
                IconButton(onClick = {
                    navController.navigate(NavRoutes.PluginEdit.id)
                }) {
                    Icon(Icons.Default.Add, stringResource(id = R.string.add_config))
                }

                var showOptions by remember { mutableStateOf(false) }
                IconButton(onClick = {
                    showOptions = true
                }) {
                    Icon(Icons.Default.MoreVert, stringResource(id = R.string.more_options))

                    DropdownMenu(
                        expanded = showOptions,
                        onDismissRequest = { showOptions = false }) {
                        DropdownMenuItem(
                            text = { Text(stringResource(id = R.string.import_config)) },
                            onClick = {
                                showOptions = false
                                showImportConfig = true
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Input, null)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(id = R.string.export_config)) },
                            onClick = {
                                showOptions = false
                                showExportConfig = appDb.pluginDao.allEnabled
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Output, null)
                            }
                        )
                    }
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
                toItem.copy(order = fromItem.order),
                fromItem.copy(order = toItem.order),
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
                        hasDefVars = item.defVars.isNotEmpty(),
                        needSetVars = item.defVars.isNotEmpty() && item.userVars.isEmpty(),
                        name = item.name,
                        desc = desc,
                        isEnabled = item.isEnabled,
                        onEnabledChange = {
                            appDb.pluginDao.update(item.copy(isEnabled = it))
                        },
                        onEdit = {
                            navController.navigateSingleTop(
                                NavRoutes.PluginEdit.id,
                                Bundle().apply {
                                    putParcelable(NavRoutes.PluginEdit.KEY_DATA, item)
                                }
                            )
                        },
                        onSetVars = { showVarsSettings = item },
                        onDelete = { showDeleteDialog = item },
                        onExport = {
                            showExportConfig = listOf(item)
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun Item(
    modifier: Modifier,
    hasDefVars: Boolean,
    needSetVars: Boolean,
    name: String,
    desc: String,
    isEnabled: Boolean,
    onEnabledChange: (Boolean) -> Unit,
    onEdit: () -> Unit,
    onSetVars: () -> Unit,
    onExport: () -> Unit,
    onDelete: () -> Unit,
) {
    ElevatedCard(modifier = modifier, onClick = {
        if (hasDefVars) onSetVars()
    }) {
        Box(modifier = Modifier.padding(4.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
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

                            if (hasDefVars)
                                DropdownMenuItem(
                                    text = { Text(stringResource(id = R.string.plugin_set_vars)) },
                                    onClick = {
                                        showOptions = false
                                        onSetVars()
                                    },
                                    leadingIcon = {
                                        Icon(Icons.Default.EditNote, null)
                                    }
                                )

                            DropdownMenuItem(
                                text = { Text(stringResource(id = R.string.export_config)) },
                                onClick = {
                                    showOptions = false
                                    onExport()
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.Output, null)
                                }
                            )

                            HorizontalDivider()

                            DropdownMenuItem(
                                text = {
                                    Text(
                                        stringResource(id = R.string.delete),
                                        color = MaterialTheme.colorScheme.error
                                    )
                                },
                                onClick = {
                                    showOptions = false
                                    onDelete()
                                },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.DeleteForever,
                                        null,
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            )
                        }
                    }

                }
            }

            if (needSetVars)
                Text(
                    text = stringResource(id = R.string.systts_plugin_please_set_vars),
                    modifier = Modifier.align(Alignment.Center),
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
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