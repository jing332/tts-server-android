package com.github.jing332.tts_server_android.compose.systts.replace

import android.os.Bundle
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddCard
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Input
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Output
import androidx.compose.material.icons.filled.PlaylistAdd
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.github.jing332.tts_server_android.R
import com.github.jing332.tts_server_android.compose.LocalNavController
import com.github.jing332.tts_server_android.compose.ShadowReorderableItem
import com.github.jing332.tts_server_android.compose.navigate
import com.github.jing332.tts_server_android.compose.systts.sizeToToggleableState
import com.github.jing332.tts_server_android.compose.widgets.LazyListIndexStateSaver
import com.github.jing332.tts_server_android.compose.widgets.TextFieldDialog
import com.github.jing332.tts_server_android.data.appDb
import com.github.jing332.tts_server_android.data.entities.replace.GroupWithReplaceRule
import com.github.jing332.tts_server_android.data.entities.replace.ReplaceRule
import com.github.jing332.tts_server_android.data.entities.replace.ReplaceRuleGroup
import com.github.jing332.tts_server_android.service.systts.SystemTtsService
import org.burnoutcrew.reorderable.detectReorderAfterLongPress
import org.burnoutcrew.reorderable.rememberReorderableLazyListState
import org.burnoutcrew.reorderable.reorderable

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
internal fun ManagerScreen(vm: ManagerViewModel = viewModel(), finish: () -> Unit) {
    val navController = LocalNavController.current

    fun navigateToEdit(rule: ReplaceRule = ReplaceRule()) {
        navController.navigate(NavRoutes.Edit.id, Bundle().apply {
            putParcelable(NavRoutes.Edit.KEY_DATA, rule)
        })
    }

    var showImportSheet by remember { mutableStateOf(false) }
    if (showImportSheet)
        ReplaceRuleImportBottomSheet(onDismissRequest = { showImportSheet = false })

    var showExportSheet by remember { mutableStateOf<List<GroupWithReplaceRule>?>(null) }
    if (showExportSheet != null)
        ReplaceRuleExportBottomSheet(
            onDismissRequest = { showExportSheet = null },
            list = showExportSheet!!,
        )

    var showAddGroupDialog by remember { mutableStateOf(false) }
    if (showAddGroupDialog) {
        var text by remember { mutableStateOf("") }
        TextFieldDialog(
            title = stringResource(id = R.string.add_group),
            text = text,
            onTextChange = { text = it },
            onDismissRequest = { showAddGroupDialog = false }) {
            appDb.replaceRuleDao.insert(ReplaceRule(name = text))
        }
    }

    var showGroupEditDialog by remember { mutableStateOf<ReplaceRuleGroup?>(null) }
    if (showGroupEditDialog != null) {
        var group by remember { mutableStateOf(showGroupEditDialog!!) }
        GroupEditDialog(
            onDismissRequest = { showGroupEditDialog = null },
            group = group,
            onGroupChange = { group = it },
            onConfirm = { appDb.replaceRuleDao.updateGroup(group) }
        )
    }

    val models by vm.list.collectAsStateWithLifecycle()
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(id = R.string.replace_rule_manager)) },
                navigationIcon = {
                    IconButton(onClick = finish) {
                        Icon(Icons.Default.ArrowBack, stringResource(id = R.string.nav_back))
                    }
                },
                actions = {
                    var showAddOptions by remember { mutableStateOf(false) }
                    IconButton(onClick = { showAddOptions = true }) {
                        Icon(Icons.Default.Add, stringResource(id = R.string.add_config))
                        DropdownMenu(
                            expanded = showAddOptions,
                            onDismissRequest = { showAddOptions = false }) {
                            DropdownMenuItem(
                                text = { Text(stringResource(id = R.string.add_config)) },
                                onClick = {
                                    showAddOptions = false
                                    navigateToEdit()
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.PlaylistAdd, null)
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(id = R.string.add_group)) },
                                onClick = {
                                    showAddOptions = false
                                    showAddGroupDialog = true
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.AddCard, null)
                                }
                            )
                        }
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
                                text = { Text(stringResource(id = R.string.export_config)) },
                                onClick = {
                                    showOptions = false
                                    showExportSheet = models
                                },
                                leadingIcon = { Icon(Icons.Default.Output, null) }
                            )
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        val listState = rememberLazyListState()
        LazyListIndexStateSaver(
            models = models,
            listState = listState,
            onIndexUpdate = { index, offset ->
                listState.scrollToItem(index, offset)
            }
        )

        val reorderState =
            rememberReorderableLazyListState(listState = listState, onMove = { from, to ->
                val fromKey = from.key.toString()
                val toKey = to.key.toString()
                if (fromKey.startsWith("g_") && toKey.startsWith("g_")) {
                    val src = appDb.replaceRuleDao.getGroup(fromKey.substring(2).toLong())
                        ?: return@rememberReorderableLazyListState
                    val target = appDb.replaceRuleDao.getGroup(toKey.substring(2).toLong())
                        ?: return@rememberReorderableLazyListState

                    appDb.replaceRuleDao.updateGroup(
                        src.copy(order = target.order),
                        target.copy(order = src.order)
                    )
                } else {
                    val src = appDb.replaceRuleDao.get(fromKey.toLong())
                        ?: return@rememberReorderableLazyListState
                    val target = appDb.replaceRuleDao.get(toKey.toLong())
                        ?: return@rememberReorderableLazyListState

                    appDb.replaceRuleDao.update(
                        src.copy(order = target.order),
                        target.copy(order = src.order)
                    )
                }
            })
        LazyColumn(
            Modifier
                .padding(paddingValues)
                .reorderable(reorderState),
            state = listState,
        ) {
            models.forEachIndexed { _, groupWithRules ->
                val g = groupWithRules.group
                val toggleableState =
                    groupWithRules.list.filter { it.isEnabled }.size.sizeToToggleableState(
                        groupWithRules.list.size
                    )
                val key = "g_${g.id}"
                stickyHeader(key = key) {
                    ShadowReorderableItem(reorderableState = reorderState, key = key) {
                        Group(
                            modifier = Modifier.fillMaxWidth(),
                            name = g.name,
                            isExpanded = g.isExpanded,
                            toggleableState = toggleableState,
                            onToggleableStateChange = { enabled ->
                                groupWithRules.list.map {
                                    if (it.isEnabled != enabled)
                                        appDb.replaceRuleDao.update(it.copy(isEnabled = enabled))
                                }
                            },
                            onClick = { appDb.replaceRuleDao.updateGroup(g.copy(isExpanded = !g.isExpanded)) },
                            onEdit = { showGroupEditDialog = g },
                            onDeleteAction = { appDb.replaceRuleDao.delete(*groupWithRules.list.toTypedArray()) },
                            onExportAction = { showExportSheet = listOf(groupWithRules) }
                        )
                    }
                }

                if (g.isExpanded) {
                    items(groupWithRules.list.sortedBy { it.order }, key = { it.id }) { rule ->
                        ShadowReorderableItem(reorderableState = reorderState, key = rule.id) { _ ->
                            Item(
                                name = rule.name,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 2.dp, vertical = 4.dp)
                                    .detectReorderAfterLongPress(reorderState),
                                isChecked = rule.isEnabled,
                                onCheckedChange = { enabled ->
                                    appDb.replaceRuleDao.update(rule.copy(isEnabled = enabled))
                                    if (enabled) SystemTtsService.notifyUpdateConfig(isOnlyReplacer = true)
                                },
                                onClick = { },
                                onEdit = { navigateToEdit(rule) },
                                onDelete = {  appDb.replaceRuleDao.delete(rule) },
                            )
                        }
                    }
                }

            }
        }
    }
}
