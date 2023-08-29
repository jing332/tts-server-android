package com.github.jing332.tts_server_android.compose.systts.speechrule

import android.os.Bundle
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Edit
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
import androidx.compose.ui.unit.dp
import com.github.jing332.tts_server_android.R
import com.github.jing332.tts_server_android.compose.LocalNavController
import com.github.jing332.tts_server_android.compose.ShadowReorderableItem
import com.github.jing332.tts_server_android.compose.navigate
import com.github.jing332.tts_server_android.compose.systts.ConfigDeleteDialog
import com.github.jing332.tts_server_android.compose.widgets.LazyListIndexStateSaver
import com.github.jing332.tts_server_android.data.appDb
import com.github.jing332.tts_server_android.data.entities.SpeechRule
import org.burnoutcrew.reorderable.detectReorderAfterLongPress
import org.burnoutcrew.reorderable.rememberReorderableLazyListState
import org.burnoutcrew.reorderable.reorderable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpeechRuleManagerScreen(finish: () -> Unit) {
    val navController = LocalNavController.current

    var showImportSheet by remember { mutableStateOf(false) }
    if (showImportSheet)
        SpeechRuleImportBottomSheet { showImportSheet = false }

    var showExportSheet by remember { mutableStateOf<List<SpeechRule>?>(null) }
    if (showExportSheet != null)
        SpeechRuleExportBottomSheet(
            onDismissRequest = { showExportSheet = null },
            list = showExportSheet!!,
        )

    var showDeleteDialog by remember { mutableStateOf<SpeechRule?>(null) }
    if (showDeleteDialog != null)
        ConfigDeleteDialog(
            onDismissRequest = { showDeleteDialog = null },
            name = showDeleteDialog!!.name
        ) {
            appDb.speechRuleDao.delete(showDeleteDialog!!)
        }


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(id = R.string.speech_rule_manager)) },
                navigationIcon = {
                    IconButton(onClick = finish) {
                        Icon(Icons.Default.ArrowBack, stringResource(id = R.string.nav_back))
                    }
                },

                actions = {
                    IconButton(onClick = {
                        navController.navigate(NavRoutes.SpeechRuleEdit.id)
                    }) {
                        Icon(Icons.Default.Add, stringResource(id = R.string.add_config))
                    }

                    var showOptions by remember { mutableStateOf(false) }
                    IconButton(onClick = { showOptions = true }) {
                        Icon(Icons.Default.MoreVert, stringResource(id = R.string.more_options))

                        DropdownMenu(
                            expanded = showOptions,
                            onDismissRequest = { showOptions = false }) {
                            DropdownMenuItem(
                                text = { Text(stringResource(id = R.string.import_config)) },
                                onClick = { showImportSheet = true },
                                leadingIcon = { Icon(Icons.Default.Input, null) }
                            )

                            DropdownMenuItem(
                                text = { Text(text = stringResource(id = R.string.export_config)) },
                                onClick = { showExportSheet = appDb.speechRuleDao.allEnabled },
                                leadingIcon = {
                                    Icon(Icons.Default.Output, null)
                                }
                            )
                        }
                    }
                }

            )
        }
    ) { paddingValues ->
        LaunchedEffect(Unit) {
            appDb.speechRuleDao.all.forEachIndexed { index, speechRule ->
                appDb.speechRuleDao.update(speechRule.copy(order = index))
            }
        }

        val flowAll = remember { appDb.speechRuleDao.flowAll() }
        val list by flowAll.collectAsState(initial = emptyList())

        val listState = remember { LazyListState() }
        LazyListIndexStateSaver(
            models = list,
            listState = listState,
        )

        val reorderState =
            rememberReorderableLazyListState(listState = listState, onMove = { from, to ->
                val fromItem = list[from.index]
                val toItem = list[to.index]

                appDb.speechRuleDao.update(
                    toItem.copy(order = fromItem.order),
                    fromItem.copy(order = toItem.order)
                )
            })
        LazyColumn(
            modifier = Modifier
                .padding(paddingValues)
                .reorderable(reorderState),
            state = reorderState.listState,
        ) {
            itemsIndexed(list, key = { _, v -> v.id }) { index, item ->
                ShadowReorderableItem(reorderableState = reorderState, key = item.id) {
                    Item(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                            .detectReorderAfterLongPress(reorderState),
                        name = item.name,
                        desc = "${item.author} - v${item.version}",
                        isEnabled = item.isEnabled,
                        onEnabledChange = { appDb.speechRuleDao.update(item.copy(isEnabled = it)) },
                        onClick = {

                        },
                        onEdit = {
                            navController.navigate(NavRoutes.SpeechRuleEdit.id, Bundle().apply {
                                putParcelable(NavRoutes.SpeechRuleEdit.KEY_DATA, item)
                            })
                        },
                        onExport = { showExportSheet = listOf(item) },
                        onDelete = { showDeleteDialog = item }
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
    name: String,
    desc: String,
    isEnabled: Boolean,
    onEnabledChange: (Boolean) -> Unit,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onExport: () -> Unit,
    onDelete: () -> Unit,
) {
    ElevatedCard(modifier = modifier, onClick = onClick) {
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


        }
    }
}