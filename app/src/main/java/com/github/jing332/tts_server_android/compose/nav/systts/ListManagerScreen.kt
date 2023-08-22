package com.github.jing332.tts_server_android.compose.nav.systts

import android.content.Intent
import android.view.HapticFeedbackConstants
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.unit.dp
import com.github.jing332.tts_server_android.R
import com.github.jing332.tts_server_android.compose.activity
import com.github.jing332.tts_server_android.constant.AppConst
import com.github.jing332.tts_server_android.data.appDb
import com.github.jing332.tts_server_android.data.entities.systts.AudioParams
import com.github.jing332.tts_server_android.data.entities.systts.GroupWithSystemTts
import com.github.jing332.tts_server_android.data.entities.systts.SystemTts
import com.github.jing332.tts_server_android.data.entities.systts.SystemTtsGroup
import com.github.jing332.tts_server_android.service.systts.SystemTtsService
import com.github.jing332.tts_server_android.ui.systts.AudioParamsSettingsView
import com.github.jing332.tts_server_android.ui.systts.ConfigExportBottomSheetFragment
import com.github.jing332.tts_server_android.ui.systts.edit.BaseTtsEditActivity
import com.github.jing332.tts_server_android.ui.systts.list.GroupModel
import com.github.jing332.tts_server_android.ui.systts.list.ItemModel
import com.github.jing332.tts_server_android.ui.view.AppDialogs
import com.github.jing332.tts_server_android.utils.clone
import com.github.jing332.tts_server_android.utils.toast
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import org.burnoutcrew.reorderable.ReorderableItem
import org.burnoutcrew.reorderable.rememberReorderableLazyListState
import org.burnoutcrew.reorderable.reorderable

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun ListManagerScreen() {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val activity = context.activity()
    val view = LocalView.current

    val systtsEditor =
        rememberLauncherForActivityResult(contract = ActivityResultContracts.StartActivityForResult()) { result ->
            @Suppress("DEPRECATION")
            result.data?.getParcelableExtra<SystemTts>(BaseTtsEditActivity.KEY_DATA)?.let {
                appDb.systemTtsDao.insertTts(it)
                if (it.isEnabled) SystemTtsService.notifyUpdateConfig()
            }
        }

    @Suppress("UNCHECKED_CAST")
    fun displayGroupExport(model: GroupModel) {
        val subList = (model.itemSublist as List<ItemModel>).map { it.data }
        val obj =
            GroupWithSystemTts(group = model.data, list = subList)
        val fragment = ConfigExportBottomSheetFragment(
            { AppConst.jsonBuilder.encodeToString(obj) },
            { "ttsrv-${model.name}.json" }
        )
        fragment.show(activity.supportFragmentManager, ConfigExportBottomSheetFragment.TAG)
    }

    fun displayGroupRename(data: SystemTtsGroup) {
        AppDialogs.displayInputDialog(
            context, context.getString(R.string.edit_group_name),
            context.getString(R.string.name), data.name
        ) {
            appDb.systemTtsDao.updateGroup(
                data.copy(name = it.ifEmpty { context.getString(R.string.unnamed) })
            )
        }
    }

    fun displayGroupAudioParams(data: SystemTtsGroup) {
        val audioParamsView = AudioParamsSettingsView(context).apply {
            setData(data.audioParams)
        }

        MaterialAlertDialogBuilder(context)
            .setTitle(R.string.audio_params_settings)
            .setView(audioParamsView)
            .setPositiveButton(R.string.close, null)
            .setNegativeButton(R.string.reset) { _, _ ->
                data.audioParams.reset(AudioParams.FOLLOW_GLOBAL_VALUE)
                context.toast(R.string.ok_reset)
            }
            .setOnDismissListener {
                appDb.systemTtsDao.updateGroup(data)
                SystemTtsService.notifyUpdateConfig()
            }
            .show()

    }

    fun displayCopyGroup(data: SystemTtsGroup) {
        AppDialogs.displayInputDialog(
            context,
            context.getString(R.string.copy_group),
            context.getString(R.string.name),
            data.name
        ) {
            scope.launch {
                val group = data.copy(id = System.currentTimeMillis(),
                    name = it.ifEmpty { context.getString(R.string.unnamed) }
                )
                appDb.systemTtsDao.insertGroup(group)
                appDb.systemTtsDao.getTtsByGroup(data.id).forEachIndexed { index, tts ->
                    appDb.systemTtsDao.insertTts(
                        tts.copy(
                            id = System.currentTimeMillis() + index,
                            groupId = group.id
                        )
                    )
                }
            }
        }
    }

    fun removeGroup(data: SystemTtsGroup) {
        AppDialogs.displayDeleteDialog(context, data.name) {
            appDb.systemTtsDao.deleteGroupAndTts(data)
        }
    }

    fun startTtsEditor(cls: Class<*>, data: SystemTts? = null, isCopy: Boolean = false) {
        val intent = Intent(context, cls)
        if (data != null) {
            intent.putExtra(
                BaseTtsEditActivity.KEY_DATA,
                data.clone<SystemTts>()!!
                    .run {
                        if (isCopy)
                            copy(
                                id = System.currentTimeMillis(),
                                displayName = "",
                                isEnabled = false
                            )
                        else this
                    }
            )
        }
        systtsEditor.launch(intent)
    }

    var deleteTts by remember { mutableStateOf<SystemTts?>(null) }
    if (deleteTts != null) {
        ConfigDeleteDialog(
            onDismissRequest = { deleteTts = null },
            name = deleteTts?.displayName ?: ""
        ) {
            appDb.systemTtsDao.deleteTts(deleteTts!!)
            deleteTts = null
        }
    }

    val flow = remember { appDb.systemTtsDao.getFlowAllGroupWithTts().conflate() }
    val models by flow.collectAsState(initial = emptyList())

    val reorderState = rememberReorderableLazyListState(onMove = { from, to ->
        val src =
            appDb.systemTtsDao.getTts(from.key as Long)
                ?: return@rememberReorderableLazyListState
        val target =
            appDb.systemTtsDao.getTts(to.key as Long)
                ?: return@rememberReorderableLazyListState

        val g = appDb.systemTtsDao.getGroup(src.groupId)
        val list =
            appDb.systemTtsDao.getTtsListByGroupId(src.groupId).sortedBy { it.order }
                .toMutableList()

        val srcIndex = list.indexOfFirst { it.id == src.id }
        if (srcIndex == -1) return@rememberReorderableLazyListState
        val targetIndex = list.indexOfFirst { it.id == target.id }
        if (targetIndex == -1) return@rememberReorderableLazyListState

        println("fromIndex; ${srcIndex}, toIndex $targetIndex")

        println(list.joinToString { it.displayName.toString() })
        list.removeAt(srcIndex)
        list.add(targetIndex, src)
        println(list.joinToString { it.displayName.toString() })
        list.forEachIndexed { index, systemTts ->
            appDb.systemTtsDao.updateTts(systemTts.copy(order = index))
        }
    })

    LazyColumn(
        Modifier
            .fillMaxSize()
            .reorderable(state = reorderState),
        state = reorderState.listState
    ) {
        models.forEachIndexed { index, groupWithSystemTts ->
            val g = groupWithSystemTts.group
            val checkState =
                when (groupWithSystemTts.list.filter { it.isEnabled }.size) {
                    0 -> ToggleableState.Off           // 全未选
                    groupWithSystemTts.list.size -> ToggleableState.On   // 全选
                    else -> ToggleableState.Indeterminate    // 部分选
                }
            stickyHeader(key = g.id) {
                Group(
                    g.name,
                    isExpanded = g.isExpanded,
                    toggleableState = checkState,
                    onCheckedChange = {
                        appDb.systemTtsDao.updateTts(*groupWithSystemTts.list.map { tts ->
                            tts.copy(isEnabled = checkState != ToggleableState.On)
                        }.toTypedArray())
                    },
                    onClick = {
                        appDb.systemTtsDao.updateGroup(g.copy(isExpanded = !g.isExpanded))
                    },
                    onDelete = {
                        appDb.systemTtsDao.deleteGroup(g)
                    },
                    onRename = {
                        appDb.systemTtsDao.updateGroup(g.copy(name = it))
                    }
                )
            }

            if (g.isExpanded) {
                itemsIndexed(groupWithSystemTts.list.sortedBy { it.order },
                    key = { _, v -> v.id }) { index, item ->
                    ReorderableItem(
                        reorderableState = reorderState, key = item.id
                    ) { isDragging ->
                        if (isDragging) {
                            view.isHapticFeedbackEnabled = true
                            view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                        }

                        val elevation =
                            animateDpAsState(if (isDragging) 24.dp else 0.dp, label = "")
                        Column(
                            modifier = Modifier
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                .shadow(elevation.value)
//                                .animateItemPlacement()
                        ) {
                            Item(
                                reorderState = reorderState,
                                modifier = Modifier.padding(),
                                name = item.displayName ?: "",
                                enabled = item.isEnabled,
                                onEnabledChange = {
                                    appDb.systemTtsDao.updateTts(item.copy(isEnabled = it))
                                },
                                desc = item.tts.getDescription(),
                                params = item.tts.getBottomContent(),
                                onClick = {
                                },
                                onCopy = {
                                    startTtsEditor(item.tts.getEditActivity(), item, isCopy = true)
                                },
                                onDelete = {
                                    deleteTts = item
                                },
                                onEdit = {
                                    startTtsEditor(item.tts.getEditActivity(), item)
                                },
                                onAudition = {
                                    context.toast("onAudition")
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
