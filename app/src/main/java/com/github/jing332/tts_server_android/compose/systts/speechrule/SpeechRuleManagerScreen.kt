package com.github.jing332.tts_server_android.compose.systts.speechrule

import android.content.Intent
import android.os.Bundle
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AppShortcut
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.dp
import com.github.jing332.tts_server_android.R
import com.github.jing332.tts_server_android.compose.LocalNavController
import com.github.jing332.tts_server_android.compose.ShadowReorderableItem
import com.github.jing332.tts_server_android.compose.navigate
import com.github.jing332.tts_server_android.compose.systts.ConfigDeleteDialog
import com.github.jing332.tts_server_android.compose.systts.plugin.PluginManagerActivity
import com.github.jing332.tts_server_android.compose.widgets.LazyListIndexStateSaver
import com.github.jing332.tts_server_android.data.appDb
import com.github.jing332.tts_server_android.data.entities.SpeechRule
import com.github.jing332.tts_server_android.utils.MyTools
import kotlinx.coroutines.flow.conflate
import org.burnoutcrew.reorderable.detectReorderAfterLongPress
import org.burnoutcrew.reorderable.rememberReorderableLazyListState
import org.burnoutcrew.reorderable.reorderable
import java.util.Collections

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpeechRuleManagerScreen(finish: () -> Unit) {
    val navController = LocalNavController.current
    val context = LocalContext.current

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
            showDeleteDialog = null
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
                                onClick = {
                                    showOptions = false
                                    showImportSheet = true
                                },
                                leadingIcon = { Icon(Icons.Default.Input, null) }
                            )

                            DropdownMenuItem(
                                text = { Text(text = stringResource(id = R.string.export_config)) },
                                onClick = {
                                    showOptions = false
                                    showExportSheet = appDb.speechRuleDao.allEnabled
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.Output, null)
                                }
                            )

                            DropdownMenuItem(
                                text = { Text(stringResource(id = R.string.desktop_shortcut)) },
                                onClick = {
                                    showOptions = false
                                    MyTools.addShortcut(
                                        context,
                                        context.getString(R.string.speech_rule_manager),
                                        "speech_rule",
                                        R.drawable.ic_shortcut_speech_rule,
                                        Intent(context, SpeechRuleManagerActivity::class.java)
                                    )
                                },
                                leadingIcon = { Icon(Icons.Default.AppShortcut, null) }
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

        val flowAll = remember { appDb.speechRuleDao.flowAll().conflate() }
        val list by flowAll.collectAsState(initial = emptyList())

        val listState = remember { LazyListState() }
        LazyListIndexStateSaver(
            models = list,
            listState = listState,
        )

        val reorderState =
            rememberReorderableLazyListState(listState = listState, onMove = { from, to ->
                val mutList = list.toMutableList()
                Collections.swap(mutList, from.index, to.index)
                mutList.forEachIndexed { index, speechRule ->
                    if (speechRule.order != index)
                        appDb.speechRuleDao.update(speechRule.copy(order = index))
                }
            })
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
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
    val context = LocalContext.current
    ElevatedCard(modifier = modifier, onClick = onClick) {
        Box(modifier = Modifier.padding(4.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = isEnabled,
                    onCheckedChange = onEnabledChange,
                    modifier = Modifier.semantics {
                        role = Role.Switch
                        context
                            .getString(
                                if (isEnabled) R.string.rule_enabled_desc else R.string.rule_disabled_desc,
                                name
                            )
                            .let {
                                contentDescription = it
                                stateDescription = it
                            }
                    }
                )
                Column(Modifier.weight(1f)) {
                    Text(text = name, style = MaterialTheme.typography.titleMedium)
                    Text(text = desc, style = MaterialTheme.typography.bodyMedium)
                }
                Row {
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, stringResource(id = R.string.edit_desc, name))
                    }

                    var showOptions by remember { mutableStateOf(false) }
                    IconButton(onClick = { showOptions = true }) {
                        Icon(
                            Icons.Default.MoreVert,
                            stringResource(id = R.string.more_options_desc, name)
                        )
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