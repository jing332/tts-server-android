package com.github.jing332.tts_server_android.compose.systts.list

import android.os.Bundle
import androidx.annotation.StringRes
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.PlaylistAdd
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddCard
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.Javascript
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.drake.net.utils.withIO
import com.github.jing332.tts_server_android.R
import com.github.jing332.tts_server_android.compose.LocalDrawerState
import com.github.jing332.tts_server_android.compose.LocalNavController
import com.github.jing332.tts_server_android.compose.ShadowReorderableItem
import com.github.jing332.tts_server_android.compose.nav.NavRoutes
import com.github.jing332.tts_server_android.compose.nav.NavTopAppBar
import com.github.jing332.tts_server_android.compose.navigate
import com.github.jing332.tts_server_android.compose.systts.AuditionDialog
import com.github.jing332.tts_server_android.compose.systts.ConfigDeleteDialog
import com.github.jing332.tts_server_android.compose.systts.ConfigExportBottomSheet
import com.github.jing332.tts_server_android.compose.systts.list.edit.QuickEditBottomSheet
import com.github.jing332.tts_server_android.compose.systts.list.edit.TagDataClearConfirmDialog
import com.github.jing332.tts_server_android.compose.systts.sizeToToggleableState
import com.github.jing332.tts_server_android.compose.widgets.LazyListIndexStateSaver
import com.github.jing332.tts_server_android.compose.widgets.TextFieldDialog
import com.github.jing332.tts_server_android.constant.AppConst
import com.github.jing332.tts_server_android.constant.SpeechTarget
import com.github.jing332.tts_server_android.data.appDb
import com.github.jing332.tts_server_android.data.entities.AbstractListGroup
import com.github.jing332.tts_server_android.data.entities.systts.GroupWithSystemTts
import com.github.jing332.tts_server_android.data.entities.systts.SystemTts
import com.github.jing332.tts_server_android.data.entities.systts.SystemTtsGroup
import com.github.jing332.tts_server_android.model.rhino.speech_rule.SpeechRuleEngine
import com.github.jing332.tts_server_android.model.speech.tts.BgmTTS
import com.github.jing332.tts_server_android.model.speech.tts.LocalTTS
import com.github.jing332.tts_server_android.model.speech.tts.MsTTS
import com.github.jing332.tts_server_android.model.speech.tts.PluginTTS
import com.github.jing332.tts_server_android.service.systts.SystemTtsService
import com.github.jing332.tts_server_android.ui.view.AppDialogs.displayErrorDialog
import com.github.jing332.tts_server_android.utils.longToast
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import org.burnoutcrew.reorderable.detectReorderAfterLongPress
import org.burnoutcrew.reorderable.rememberReorderableLazyListState
import org.burnoutcrew.reorderable.reorderable


@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
internal fun ListManagerScreen(vm: ListManagerViewModel = viewModel()) {
    val navController = LocalNavController.current
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val drawerState = LocalDrawerState.current

    var showSortDialog by remember { mutableStateOf<List<SystemTts>?>(null) }
    if (showSortDialog != null) SortDialog(
        onDismissRequest = { showSortDialog = null },
        list = showSortDialog!!
    )

    var showQuickEdit by remember { mutableStateOf<SystemTts?>(null) }
    if (showQuickEdit != null) {
        QuickEditBottomSheet(onDismissRequest = {
            appDb.systemTtsDao.insertTts(showQuickEdit!!)
            if (showQuickEdit?.isEnabled == true) SystemTtsService.notifyUpdateConfig()
            showQuickEdit = null
        }, systts = showQuickEdit!!, onSysttsChange = {
            showQuickEdit = it
        })
    }

    fun navigateToEdit(systts: SystemTts) {
        navController.navigate(NavRoutes.TtsEdit.id, Bundle().apply {
            putParcelable(NavRoutes.TtsEdit.DATA, systts)
        })
    }

    // 长按Item拖拽提示
    var hasShownTip by rememberSaveable { mutableStateOf(false) }

    var showTagClearDialog by remember { mutableStateOf<SystemTts?>(null) }
    if (showTagClearDialog != null) {
        val systts = showTagClearDialog!!
        TagDataClearConfirmDialog(
            tagData = systts.speechRule.tagData.toString(),
            onDismissRequest = { showTagClearDialog = null },
            onConfirm = {
                systts.speechRule.target = SpeechTarget.ALL
                systts.speechRule.resetTag()
                appDb.systemTtsDao.updateTts(systts)
                if (systts.isEnabled) SystemTtsService.notifyUpdateConfig()
                showTagClearDialog = null
            }
        )
    }

    fun switchSpeechTarget(systts: SystemTts) {
        if (!hasShownTip) {
            hasShownTip = true
            context.longToast(R.string.systts_drag_tip_msg)
        }

        val model = systts.copy()
        if (model.speechRule.target == SpeechTarget.BGM) return

        if (model.speechRule.target == SpeechTarget.CUSTOM_TAG) appDb.speechRuleDao.getByRuleId(
            model.speechRule.tagRuleId
        )?.let { speechRule ->
            val keys = speechRule.tags.keys.toList()
            val idx = keys.indexOf(model.speechRule.tag)

            val nextIndex = (idx + 1)
            val newTag = keys.getOrNull(nextIndex)
            if (newTag == null) {
                if (model.speechRule.isTagDataEmpty()) {
                    model.speechRule.target = SpeechTarget.ALL
                    model.speechRule.resetTag()
                } else {
                    showTagClearDialog = model
                    return
                }
            } else {
                model.speechRule.tag = newTag
                runCatching {
                    model.speechRule.tagName =
                        SpeechRuleEngine.getTagName(context, speechRule, info = model.speechRule)
                }.onFailure {
                    model.speechRule.tagName = ""
                    context.displayErrorDialog(it)
                }

            }
        }
        else {
            appDb.speechRuleDao.getByRuleId(model.speechRule.tagRuleId)?.let {
                model.speechRule.target = SpeechTarget.CUSTOM_TAG
                model.speechRule.tag = it.tags.keys.first()
            }
        }

        appDb.systemTtsDao.updateTts(model)
        if (model.isEnabled) SystemTtsService.notifyUpdateConfig()
    }

    var deleteTts by remember { mutableStateOf<SystemTts?>(null) }
    if (deleteTts != null) {
        ConfigDeleteDialog(
            onDismissRequest = { deleteTts = null }, name = deleteTts?.displayName ?: ""
        ) {
            appDb.systemTtsDao.deleteTts(deleteTts!!)
            deleteTts = null
        }
    }

    var groupAudioParamsDialog by remember { mutableStateOf<SystemTtsGroup?>(null) }
    if (groupAudioParamsDialog != null) {
        GroupAudioParamsDialog(onDismissRequest = { groupAudioParamsDialog = null },
            params = groupAudioParamsDialog!!.audioParams,
            onConfirm = {
                appDb.systemTtsDao.updateGroup(
                    groupAudioParamsDialog!!.copy(audioParams = it)
                )

                groupAudioParamsDialog = null
            })
    }

    val models by vm.list.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()
    LazyListIndexStateSaver(models = models, listState = listState)

    val reorderState = rememberReorderableLazyListState(
        listState = listState, onMove = vm::reorder
    )

    LaunchedEffect(models) {
        println("update models: ${models.size}")
    }

    var addGroupDialog by remember { mutableStateOf(false) }
    if (addGroupDialog) {
        var name by remember { mutableStateOf("") }
        TextFieldDialog(title = stringResource(id = R.string.add_group),
            text = name,
            onTextChange = { name = it },
            onDismissRequest = { addGroupDialog = false }) {
            addGroupDialog = false
            appDb.systemTtsDao.insertGroup(SystemTtsGroup(name = name))
        }
    }

    var showGroupExportSheet by remember { mutableStateOf<List<GroupWithSystemTts>?>(null) }
    if (showGroupExportSheet != null) {
        val list = showGroupExportSheet!!
        ListExportBottomSheet(onDismissRequest = { showGroupExportSheet = null }, list = list)
    }

    var showExportSheet by remember { mutableStateOf<List<SystemTts>?>(null) }
    if (showExportSheet != null) {
        val jStr = remember { AppConst.jsonBuilder.encodeToString(showExportSheet!!) }
        ConfigExportBottomSheet(json = jStr) { showExportSheet = null }
    }

    var addPluginDialog by remember { mutableStateOf(false) }
    if (addPluginDialog) {
        PluginSelectionDialog(onDismissRequest = { addPluginDialog = false }) {
            navigateToEdit(SystemTts(tts = PluginTTS(pluginId = it.pluginId)))
        }
    }

    var showAuditionDialog by remember { mutableStateOf<SystemTts?>(null) }
    if (showAuditionDialog != null) AuditionDialog(systts = showAuditionDialog!!) {
        showAuditionDialog = null
    }

    var showOptions by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        topBar = {
            NavTopAppBar(drawerState = drawerState, title = {
                Text(stringResource(id = R.string.system_tts))
            }, actions = {
                var showAddMenu by remember { mutableStateOf(false) }
                IconButton(onClick = { showAddMenu = true }) {
                    Icon(Icons.Default.Add, stringResource(id = R.string.add_config))

                    DropdownMenu(expanded = showAddMenu,
                        onDismissRequest = { showAddMenu = false }) {

                        @Composable
                        fun MenuItem(
                            icon: @Composable () -> Unit,
                            @StringRes title: Int,
                            onClick: () -> Unit
                        ) {
                            DropdownMenuItem(text = {
                                Text(stringResource(id = title))
                            }, onClick = {
                                showAddMenu = false
                                onClick()
                            }, leadingIcon = icon)
                        }

                        MenuItem(
                            icon = { Icon(Icons.AutoMirrored.Default.PlaylistAdd, null) },
                            title = R.string.systts_add_internal_tts
                        ) {
                            navigateToEdit(SystemTts(tts = MsTTS()))
                        }

                        MenuItem(
                            icon = { Icon(Icons.Default.PhoneAndroid, null) },
                            title = R.string.add_local_tts
                        ) {
                            navigateToEdit(SystemTts(tts = LocalTTS()))
                        }

//                        MenuItem(
//                            icon = { Icon(Icons.Default.Http, null) },
//                            title = R.string.systts_add_custom_tts
//                        ) {
////                                startTtsEditor(HttpTtsEditActivity::class.java)
//                        }

                        MenuItem(
                            icon = { Icon(Icons.Default.Javascript, null) },
                            title = R.string.systts_add_plugin_tts
                        ) {
                            addPluginDialog = true
                        }

                        MenuItem(
                            icon = { Icon(Icons.Default.Audiotrack, null) },
                            title = R.string.add_bgm_tts
                        ) {
                            navigateToEdit(SystemTts(tts = BgmTTS()))
                        }

                        MenuItem(
                            icon = { Icon(Icons.Default.AddCard, null) },
                            title = R.string.add_group
                        ) {
                            addGroupDialog = true
                        }
                    }
                }

                IconButton(onClick = { showOptions = true }) {
                    Icon(Icons.Default.MoreVert, stringResource(id = R.string.more_options))
                    MenuMoreOptions(
                        expanded = showOptions,
                        onDismissRequest = { showOptions = false },
                        onExportAll = { showGroupExportSheet = models },
                    )
                }
            })
        },
    ) { paddingValues ->
        Box(Modifier.padding(top = paddingValues.calculateTopPadding())) {
            LazyColumn(
                Modifier
                    .fillMaxSize()
                    .reorderable(state = reorderState),
                state = listState
            ) {
                models.forEachIndexed { _, groupWithSystemTts ->
                    val g = groupWithSystemTts.group
                    val checkState =
                        groupWithSystemTts.list.filter { it.isEnabled }.size.sizeToToggleableState(
                            groupWithSystemTts.list.size
                        )
                    val key = "g_${g.id}"
                    stickyHeader(key = key) {
                        ShadowReorderableItem(reorderableState = reorderState, key = key) {
                            Group(modifier = Modifier.detectReorderAfterLongPress(reorderState),
                                name = g.name,
                                isExpanded = g.isExpanded,
                                toggleableState = checkState,
                                onToggleableStateChange = {
                                    vm.updateGroupEnable(groupWithSystemTts, it)
                                },
                                onClick = {
                                    appDb.systemTtsDao.updateGroup(g.copy(isExpanded = !g.isExpanded))
                                },
                                onDelete = {
                                    appDb.systemTtsDao.deleteTts(*groupWithSystemTts.list.toTypedArray())
                                    appDb.systemTtsDao.deleteGroup(g)
                                },
                                onRename = {
                                    appDb.systemTtsDao.updateGroup(g.copy(name = it))
                                },
                                onCopy = {
                                    scope.launch {
                                        val group = g.copy(id = System.currentTimeMillis(),
                                            name = it.ifBlank { context.getString(R.string.unnamed) })
                                        appDb.systemTtsDao.insertGroup(group)
                                        appDb.systemTtsDao.getTtsByGroup(g.id)
                                            .forEachIndexed { index, tts ->
                                                appDb.systemTtsDao.insertTts(
                                                    tts.copy(
                                                        id = System.currentTimeMillis() + index,
                                                        groupId = group.id
                                                    )
                                                )
                                            }
                                    }
                                },
                                onEditAudioParams = {
                                    groupAudioParamsDialog = g
                                },
                                onExport = {
                                    showGroupExportSheet = listOf(groupWithSystemTts)
                                },
                                onSort = {
                                    showSortDialog = groupWithSystemTts.list
                                }
                            )
                        }
                    }

                    if (g.isExpanded) {
                        itemsIndexed(groupWithSystemTts.list.sortedBy { it.order },
                            key = { _, v -> "${g.id}_${v.id}" }) { _, item ->
                            if (g.id == 1L) println(item.displayName + ", " + item.order)

                            ShadowReorderableItem(
                                reorderableState = reorderState,
                                key = "${g.id}_${item.id}"
                            ) {
                                Item(reorderState = reorderState,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    name = item.displayName ?: "",
                                    tagName = item.speechRule.tagName,
                                    type = item.tts.getType(),
                                    standby = item.speechRule.isStandby,
                                    enabled = item.isEnabled,
                                    onEnabledChange = {
                                        vm.updateTtsEnabled(item, it)
                                        if (it) SystemTtsService.notifyUpdateConfig()
                                    },
                                    desc = item.tts.getDescription(),
                                    params = item.tts.getBottomContent(),
                                    onClick = { showQuickEdit = item },
                                    onLongClick = { switchSpeechTarget(item) },
                                    onCopy = {
                                        navigateToEdit(item.copy(id = System.currentTimeMillis()))
                                    },
                                    onDelete = { deleteTts = item },
                                    onEdit = {
                                        navController.navigate(
                                            NavRoutes.TtsEdit.id,
                                            Bundle().apply {
                                                putParcelable(NavRoutes.TtsEdit.DATA, item)
                                            }
                                        )
                                    },
                                    onAudition = { showAuditionDialog = item },
                                    onExport = {
                                        showExportSheet =
                                            listOf(item.copy(groupId = AbstractListGroup.DEFAULT_GROUP_ID))
                                    }
                                )
                            }
                        }
                    }
                }

                item {
                    Spacer(Modifier.height(60.dp))
                }
            }


            LaunchedEffect(key1 = Unit) {
                withIO {
                    vm.checkListData(context)
                }
            }

        }
    }
}