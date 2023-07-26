package com.github.jing332.tts_server_android.ui.systts.list

import android.os.Bundle
import android.speech.tts.TextToSpeechService
import android.view.View
import androidx.appcompat.widget.PopupMenu
import androidx.core.view.MenuCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import by.kirich1409.viewbindingdelegate.viewBinding
import com.drake.brv.BindingAdapter
import com.drake.brv.listener.DefaultItemTouchCallback
import com.drake.brv.listener.ItemDifferCallback
import com.drake.brv.utils.linear
import com.drake.brv.utils.setup
import com.drake.net.utils.withDefault
import com.drake.net.utils.withMain
import com.github.jing332.tts_server_android.R
import com.github.jing332.tts_server_android.constant.AppConst
import com.github.jing332.tts_server_android.data.appDb
import com.github.jing332.tts_server_android.data.entities.systts.AudioParams
import com.github.jing332.tts_server_android.data.entities.systts.GroupWithSystemTts
import com.github.jing332.tts_server_android.data.entities.systts.SystemTtsGroup
import com.github.jing332.tts_server_android.databinding.SysttsListGroupFragmentBinding
import com.github.jing332.tts_server_android.help.config.SysTtsConfig
import com.github.jing332.tts_server_android.service.systts.SystemTtsService
import com.github.jing332.tts_server_android.ui.base.group.GroupListHelper
import com.github.jing332.tts_server_android.ui.systts.AudioParamsSettingsView
import com.github.jing332.tts_server_android.ui.systts.BrvItemTouchHelper
import com.github.jing332.tts_server_android.ui.systts.ConfigExportBottomSheetFragment
import com.github.jing332.tts_server_android.ui.view.AppDialogs
import com.github.jing332.tts_server_android.utils.toast
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.conflate
import kotlinx.serialization.encodeToString

class ListGroupPageFragment : Fragment(R.layout.systts_list_group_fragment) {
    private val binding by viewBinding(SysttsListGroupFragmentBinding::bind)

    private lateinit var brv: BindingAdapter
    private val itemHelper = SysTtsListItemHelper(this, hasGroup = true)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        brv = binding.recyclerView.linear().setup {
            addType<GroupModel>(R.layout.base_list_group_item)
            addType<ItemModel>(R.layout.systts_list_item)

            val groupHelper = GroupListHelper<GroupModel>(requireContext())
            groupHelper.callback = object : GroupListHelper.Callback<GroupModel> {
                override fun onGroupClick(v: View, model: GroupModel) {
                    val isExpanded = !model.itemExpand
                    appDb.systemTtsDao.updateGroup(model.data.copy(isExpanded = isExpanded))
                }

                override fun onCheckedChange(v: MaterialCheckBox, model: GroupModel) {
                    if (!SysTtsConfig.isGroupMultipleEnabled)
                        appDb.systemTtsDao.setAllTtsEnabled(false)

                    appDb.systemTtsDao.setTtsEnabledInGroup(
                        groupId = model.data.id,
                        v.isChecked
                    )
                    SystemTtsService.notifyUpdateConfig()
                }

                override fun onGroupMoveButtonClick(v: View, model: GroupModel) {
                    PopupMenu(requireContext(), v).apply {
                        this.setForceShowIcon(true)
                        MenuCompat.setGroupDividerEnabled(menu, true)
                        menuInflater.inflate(R.menu.systts_list_group_more, menu)
                        setOnMenuItemClickListener { item ->
                            when (item.itemId) {
                                R.id.menu_rename_group -> displayGroupRename(model.data)
                                R.id.menu_export -> displayGroupExport(model)
                                R.id.menu_audio_params -> displayGroupAudioParams(model.data)

                                R.id.menu_remove_group -> removeGroup(model.data)
                            }
                            true
                        }
                        show()
                    }
                }
            }

            onCreate {
                groupHelper.initGroup(this@setup, this@onCreate)
                itemHelper.init(this@setup, this@onCreate)
            }

            itemDifferCallback = object : ItemDifferCallback {
                override fun areItemsTheSame(oldItem: Any, newItem: Any): Boolean {
                    if (oldItem is GroupModel && newItem is GroupModel)
                        return oldItem.data.id == newItem.data.id
                    if (oldItem is ItemModel && newItem is ItemModel)
                        return oldItem.data.id == newItem.data.id

                    return false
                }

                override fun getChangePayload(oldItem: Any, newItem: Any) = true
            }

            itemTouchHelper = ItemTouchHelper(object : DefaultItemTouchCallback() {
                override fun isLongPressDragEnabled() = false

                override fun onSelectedChanged(
                    viewHolder: RecyclerView.ViewHolder?,
                    actionState: Int
                ) {
                    viewHolder?.also { getModelOrNull<GroupModel>(it.layoutPosition) }?.let {
                        if (actionState == ItemTouchHelper.ACTION_STATE_DRAG)
                            (viewHolder as BindingAdapter.BindingViewHolder).collapse()
                    }

                    super.onSelectedChanged(viewHolder, actionState)
                }

                override fun onMove(
                    recyclerView: RecyclerView,
                    source: RecyclerView.ViewHolder,
                    target: RecyclerView.ViewHolder
                ): Boolean {
                    if (BrvItemTouchHelper.onMove<GroupModel>(recyclerView, source, target))
                        return super.onMove(recyclerView, source, target)

                    return false
                }

                override fun onDrag(
                    source: BindingAdapter.BindingViewHolder,
                    target: BindingAdapter.BindingViewHolder
                ) {
                    if (source.getModel<Any>() is GroupModel) { // 分组拖拽
                        models?.filterIsInstance<GroupModel>()?.let { models ->
                            models.forEachIndexed { index, value ->
                                appDb.systemTtsDao.updateGroup(value.data.apply { order = index })
                            }
                        }
                    } else { // Item拖拽
                        // 查找到组model
                        models?.getOrNull(source.findParentPosition())
                            ?.run { this as GroupModel }
                            ?.let { groupModel ->
                                val listInGroup =
                                    models?.filterIsInstance<ItemModel>()
                                        ?.filter { groupModel.data.id == it.data.groupId }
                                listInGroup?.forEachIndexed { index, v ->
                                    appDb.systemTtsDao.updateTts(v.data.copy(order = index))
                                }
                            }
                    }

                }
            })
        }

        lifecycleScope.launch {
            appDb.systemTtsDao.getFlowAllGroupWithTts().conflate().collect { list ->
                updateModels(list)
            }
        }

        // 监听朗读规则变化
        lifecycleScope.launch {
            appDb.speechRule.flowAll().collect {
                updateModels(appDb.systemTtsDao.getAllGroupWithTts())
            }
        }

    }

    private var mLastDataSet: List<GroupWithSystemTts>? = null
    private suspend fun updateModels(list: List<GroupWithSystemTts>? = mLastDataSet) {
        withDefault {
            mLastDataSet = list
            mLastDataSet?.let { dataSet ->
                val models = withDefault {
                    dataSet.mapIndexed { i, v ->
                        val checkState =
                            when (v.list.filter { it.isEnabled }.size) {
                                0 -> MaterialCheckBox.STATE_UNCHECKED           // 全未选
                                v.list.size -> MaterialCheckBox.STATE_CHECKED   // 全选
                                else -> MaterialCheckBox.STATE_INDETERMINATE    // 部分选
                            }

                        GroupModel(
                            data = v.group,
                            itemSublist = v.list.sortedBy { it.order }.map {
                                ItemModel(data = it.apply {
                                    tts.context = requireContext()
                                })
                            },
                        ).apply {
                            itemGroupPosition = i
                            checkedState = checkState
                            itemExpand = v.group.isExpanded
                        }
                    }
                }

                if (brv.models == null)
                    withMain { brv.models = models }
                else
                    brv.setDifferModels(models)

            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun displayGroupExport(model: GroupModel) {
        val subList = (model.itemSublist as List<ItemModel>).map { it.data }
        val obj =
            GroupWithSystemTts(group = model.data, list = subList)
        val fragment = ConfigExportBottomSheetFragment(
            { AppConst.jsonBuilder.encodeToString(obj) },
            { "ttsrv-${model.name}.json" }
        )
        fragment.show(requireActivity().supportFragmentManager, ConfigExportBottomSheetFragment.TAG)
    }

    private fun displayGroupRename(data: SystemTtsGroup) {
        AppDialogs.displayInputDialog(
            requireContext(), getString(R.string.edit_group_name),
            getString(R.string.name), data.name
        ) {
            appDb.systemTtsDao.updateGroup(
                data.copy(name = it.ifEmpty { getString(R.string.unnamed) })
            )
        }
    }

    private fun displayGroupAudioParams(data: SystemTtsGroup) {
        val audioParamsView = AudioParamsSettingsView(requireContext()).apply {
            setData(data.audioParams)
        }

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.audio_params_settings)
            .setView(audioParamsView)
            .setPositiveButton(R.string.close, null)
            .setNegativeButton(R.string.reset) { _, _ ->
                data.audioParams.reset(AudioParams.FOLLOW_GLOBAL_VALUE)
                requireContext().toast(R.string.ok_reset)
            }
            .setOnDismissListener {
                appDb.systemTtsDao.updateGroup(data)
                SystemTtsService.notifyUpdateConfig()
            }
            .show()

    }

    private fun removeGroup(data: SystemTtsGroup) {
        AppDialogs.displayDeleteDialog(requireContext(), data.name) {
            appDb.systemTtsDao.deleteGroupAndTts(data)
        }
    }
}