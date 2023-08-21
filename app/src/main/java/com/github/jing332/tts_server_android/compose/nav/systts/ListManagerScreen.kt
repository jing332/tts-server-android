package com.github.jing332.tts_server_android.compose.nav.systts

import android.content.Intent
import android.view.HapticFeedbackConstants
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CopyAll
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import com.github.jing332.tts_server_android.R
import com.github.jing332.tts_server_android.compose.activity
import com.github.jing332.tts_server_android.constant.AppConst
import com.github.jing332.tts_server_android.data.appDb
import com.github.jing332.tts_server_android.data.entities.systts.AudioParams
import com.github.jing332.tts_server_android.data.entities.systts.GroupWithSystemTts
import com.github.jing332.tts_server_android.data.entities.systts.SystemTts
import com.github.jing332.tts_server_android.data.entities.systts.SystemTtsGroup
import com.github.jing332.tts_server_android.help.config.AppConfig
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
import com.ireward.htmlcompose.HtmlText
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import org.burnoutcrew.reorderable.ReorderableItem
import org.burnoutcrew.reorderable.ReorderableLazyListState
import org.burnoutcrew.reorderable.detectReorder
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
        AlertDialog(
            onDismissRequest = { deleteTts = null },
            title = { Text(stringResource(id = R.string.delete)) },
            text = { Text(stringResource(id = R.string.is_confirm_delete) + "\n${deleteTts!!.displayName}") },
            confirmButton = {
                TextButton(onClick = { appDb.systemTtsDao.deleteTts(deleteTts!!) }) {
                    Text(
                        stringResource(id = R.string.delete),
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { deleteTts = null }) {
                    Text(stringResource(id = R.string.cancel))
                }
            }
        )
    }

    /*
        LaunchedEffect(Unit) {
            scope.launch {
                appDb.systemTtsDao.getFlowAllGroupWithTts().conflate().collect { list ->
                    updateModels(list)
                }
            }

            // 监听朗读规则变化
            scope.launch {
                appDb.speechRule.flowAll().collect {
                    updateModels(appDb.systemTtsDao.getAllGroupWithTts())
                }
            }
        }*/
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

    LaunchedEffect(models) {
        println("models")
    }

    LazyColumn(
        Modifier
            .fillMaxSize()
            .reorderable(state = reorderState),
        state = reorderState.listState
    ) {
        models.forEachIndexed { index, groupWithSystemTts ->
            val g = groupWithSystemTts.group
            val isAllEnabled = groupWithSystemTts.list.all { it.isEnabled }
            stickyHeader(key = g.id) {
                Group(
                    g.name,
                    isExpanded = g.isExpanded,
                    isChecked = isAllEnabled,
                    onCheckedChange = {
                        appDb.systemTtsDao.updateTts(*groupWithSystemTts.list.map { tts ->
                            tts.copy(isEnabled = it)
                        }.toTypedArray())
                    },
                    onClick = {
                        appDb.systemTtsDao.updateGroup(g.copy(isExpanded = !g.isExpanded))
                    }
                )
            }

            if (g.isExpanded) {
                itemsIndexed(groupWithSystemTts.list.sortedBy { it.order },
                    key = { _, v -> v.id }) { index, item ->
                    ReorderableItem(
//                        modifier = Modifier.animateItemPlacement(),
                        reorderableState = reorderState, key = item.id
                    ) { isDragging ->
                        println("isDragging=$isDragging")
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Group(
    name: String,
    isExpanded: Boolean,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .padding(vertical = 8.dp)
            .fillMaxWidth()
            .clickable(
                onClickLabel = stringResource(
                    id = if (isExpanded) R.string.desc_collapse_group
                    else R.string.desc_expand_group, name
                )
            ) { onClick() },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painterResource(id = if (isExpanded) R.drawable.ic_arrow_expand else R.drawable.ic_arrow_collapse),
            contentDescription = stringResource(id = if (isExpanded) R.string.group_expanded else R.string.group_collapsed)
        )
        Row(
            modifier = Modifier.padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                name,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .weight(1f)
            )

            Checkbox(
                modifier = Modifier
                    .align(Alignment.CenterVertically),
                checked = isChecked,
                onCheckedChange = onCheckedChange,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Item(
    modifier: Modifier,
    name: String,
    desc: String,
    params: String,
    reorderState: ReorderableLazyListState,

    enabled: Boolean,
    onEnabledChange: (Boolean) -> Unit,
    onClick: () -> Unit,

    onCopy: () -> Unit,
    onDelete: () -> Unit,
    onEdit: () -> Unit,
    onAudition: () -> Unit,
) {
    ElevatedCard(
        modifier = modifier,
        onClick = onClick,
    ) {
        ConstraintLayout(
            Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
        ) {
            val (checkRef,
                nameRef,
                contentRef,
                buttonsRef) = createRefs()
            Row(Modifier
                .constrainAs(checkRef) {
                    start.linkTo(parent.start)
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)
                }
                .detectReorder(reorderState)) {
                Checkbox(
                    checked = enabled,
                    onCheckedChange = onEnabledChange,
                )
            }
            Text(
                name,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier
                    .constrainAs(nameRef) {
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                        top.linkTo(parent.top)
                    }
                    .padding(bottom = 4.dp),
                maxLines = 1,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold
            )

            Column(
                Modifier
                    .constrainAs(contentRef) {
                        start.linkTo(checkRef.end)
                        top.linkTo(nameRef.bottom)
                        bottom.linkTo(parent.bottom)
//                        end.linkTo(buttonsRef.start)
                    }
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.Start
            ) {
                HtmlText(
                    text = desc,
                    style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onBackground),
                )

                HtmlText(
                    text = params,
                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onBackground),
                )
            }

            Row(modifier = Modifier.constrainAs(buttonsRef) {
                top.linkTo(parent.top)
                bottom.linkTo(parent.bottom)
                end.linkTo(parent.end)
            }) {
                IconButton(
                    modifier = Modifier,
                    onClick = {
                        if (AppConfig.isSwapListenAndEditButton)
                            onEdit()
                        else
                            onAudition()
                    }) {
                    if (AppConfig.isSwapListenAndEditButton)
                        Icon(Icons.Default.Headphones, stringResource(id = R.string.audition))
                    else
                        Icon(Icons.Default.Edit, stringResource(id = R.string.edit))
                }

                var showOptions by remember { mutableStateOf(false) }
                IconButton(
                    modifier = Modifier,
                    onClick = { showOptions = true }) {
                    Icon(Icons.Default.MoreVert, stringResource(id = R.string.more_options))

                    DropdownMenu(
                        expanded = showOptions,
                        onDismissRequest = { showOptions = false }) {

                        DropdownMenuItem(
                            text = { Text(stringResource(id = R.string.copy)) },
                            onClick = onCopy,
                            leadingIcon = {
                                Icon(Icons.Default.CopyAll, null)
                            }
                        )
                        Divider()
                        DropdownMenuItem(
                            text = { Text(stringResource(id = R.string.delete)) },
                            onClick = onDelete,
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