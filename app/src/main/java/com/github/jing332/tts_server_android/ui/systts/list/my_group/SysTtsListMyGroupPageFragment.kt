package com.github.jing332.tts_server_android.ui.systts.list.my_group

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.AccessibilityDelegate
import android.view.ViewGroup
import android.view.accessibility.AccessibilityNodeInfo
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.drake.brv.BindingAdapter
import com.drake.brv.listener.DefaultItemTouchCallback
import com.drake.brv.listener.ItemDifferCallback
import com.drake.brv.utils.linear
import com.drake.brv.utils.setup
import com.drake.net.utils.withDefault
import com.github.jing332.tts_server_android.R
import com.github.jing332.tts_server_android.data.appDb
import com.github.jing332.tts_server_android.data.entities.systts.SystemTts
import com.github.jing332.tts_server_android.data.entities.systts.SystemTtsGroup
import com.github.jing332.tts_server_android.databinding.SysttsListMyGroupFragmentBinding
import com.github.jing332.tts_server_android.databinding.SysttsListMyGroupItemBinding
import com.github.jing332.tts_server_android.help.SysTtsConfig
import com.github.jing332.tts_server_android.service.systts.SystemTtsService
import com.github.jing332.tts_server_android.ui.custom.AppDialogs
import com.github.jing332.tts_server_android.ui.custom.MaterialTextInput
import com.github.jing332.tts_server_android.ui.systts.list.SysTtsListItemHelper
import com.github.jing332.tts_server_android.util.clickWithThrottle
import com.github.jing332.tts_server_android.util.setFadeAnim
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.conflate

@Suppress("CAST_NEVER_SUCCEEDS")
class SysTtsListMyGroupPageFragment : Fragment() {
    val binding: SysttsListMyGroupFragmentBinding by lazy {
        SysttsListMyGroupFragmentBinding.inflate(layoutInflater)
    }

    private val itemHelper = SysTtsListItemHelper(this, isGroupList = true)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val brv = binding.recyclerView.linear().setup {
            addType<RvGroupModel>(R.layout.systts_list_my_group_item)
            addType<SystemTts>(R.layout.systts_list_item)

            onCreate {
                // 分组Item
                getBindingOrNull<SysttsListMyGroupItemBinding>()?.apply {
                    itemView.accessibilityDelegate = object : AccessibilityDelegate() {
                        override fun onInitializeAccessibilityNodeInfo(
                            host: View,
                            info: AccessibilityNodeInfo
                        ) {
                            super.onInitializeAccessibilityNodeInfo(host, info)
                            val model = getModel<RvGroupModel>()
                            val enabledCount =
                                model.itemSublist?.filter { (it as SystemTts).isEnabled }?.size

                            info.text =
                                "${ivState.contentDescription}, ${tvName.text}, ${
                                    getString(
                                        R.string.systts_desc_list_count_info,
                                        model.itemSublist?.size,
                                        enabledCount
                                    )
                                }"
                        }
                    }
                    itemView.clickWithThrottle {
                        expandOrCollapse()
                        getModel<RvGroupModel>().let { model ->
                            val enabledCount =
                                model.itemSublist?.filter { (it as SystemTts).isEnabled }?.size
                            val speakText =
                                if (model.itemExpand) R.string.group_expanded else R.string.group_collapsed
                            itemView.announceForAccessibility(
                                getString(speakText) + ", ${
                                    getString(
                                        R.string.systts_desc_list_count_info,
                                        model.itemSublist?.size,
                                        enabledCount
                                    )
                                }"
                            )

                            if (model.itemExpand && model.itemSublist.isNullOrEmpty()) {
                                collapse(modelPosition)
                                MaterialAlertDialogBuilder(requireContext())
                                    .setTitle(R.string.msg_group_is_empty)
                                    .setMessage(getString(R.string.systts_group_empty_msg))
                                    .setFadeAnim()
                                    .show()
                            }

                            appDb.systemTtsDao.updateGroup(model.data.apply {
                                isExpanded = model.itemExpand
                            })
                        }
                    }
                    checkBox.clickWithThrottle {
                        val group = getModel<RvGroupModel>().data
                        if (!SysTtsConfig.isGroupMultipleEnabled)
                            appDb.systemTtsDao.setAllTtsEnabled(false)

                        appDb.systemTtsDao.setTtsEnabledInGroup(
                            groupId = group.id,
                            checkBox.isChecked
                        )
                        SystemTtsService.notifyUpdateConfig()
                    }
                    btnMore.clickWithThrottle { displayMoreMenu(btnMore, getModel()) }

                }

                // TTS Item
                itemHelper.init(this@setup, this@onCreate)
            }

            itemDifferCallback = object : ItemDifferCallback {
                override fun areItemsTheSame(oldItem: Any, newItem: Any): Boolean {
                    if (oldItem is RvGroupModel && newItem is RvGroupModel)
                        return oldItem.data.id == newItem.data.id
                    if (oldItem is SystemTts && newItem is SystemTts)
                        return oldItem.id == newItem.id

                    return false
                }

                override fun getChangePayload(oldItem: Any, newItem: Any) = true
            }

            itemTouchHelper = ItemTouchHelper(object : DefaultItemTouchCallback() {
                override fun onSelectedChanged(
                    viewHolder: RecyclerView.ViewHolder?,
                    actionState: Int
                ) {
                    if (actionState == ItemTouchHelper.ACTION_STATE_DRAG)
                        (viewHolder as BindingAdapter.BindingViewHolder).collapse()

                    super.onSelectedChanged(viewHolder, actionState)
                }

                override fun onDrag(
                    source: BindingAdapter.BindingViewHolder,
                    target: BindingAdapter.BindingViewHolder
                ) {
                    models?.filterIsInstance<RvGroupModel>()?.let { models ->
                        models.forEachIndexed { index, value ->
                            appDb.systemTtsDao.updateGroup(value.data.apply { order = index })
                        }
                    }
                }
            })
        }

        lifecycleScope.launch {
            var job: Job? = null
            appDb.systemTtsDao.flowAllGroupWithTts.conflate().collect { list ->
                val models = withDefault {
                    list.mapIndexed { i, v ->
                        val checkState =
                            when (v.list.filter { it.isEnabled }.size) {
                                0 -> MaterialCheckBox.STATE_UNCHECKED           // 全未选
                                v.list.size -> MaterialCheckBox.STATE_CHECKED   // 全选
                                else -> MaterialCheckBox.STATE_INDETERMINATE    // 部分选
                            }

                        RvGroupModel(
                            data = v.group,
                            itemGroupPosition = i,
                            checkedState = checkState,
                            itemSublist = v.list,
                            itemExpand = v.group.isExpanded
                        )
                    }
                }

                if (brv.models == null)
                    brv.models = models
                else {
                    job?.cancel()
                    job = null
                    job = launch { delay(100); brv.setDifferModels(models) }
                }

            }

        }

    }

    @SuppressLint("RestrictedApi")
    private fun displayMoreMenu(v: View, model: RvGroupModel) {
        PopupMenu(requireContext(), v).apply {
            this.setForceShowIcon(true)

            menuInflater.inflate(R.menu.menu_systts_list_group_more, menu)
            setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.menu_edit_group_name -> editGroupName(model.data)
                    R.id.menu_delete_group -> deleteGroup(model.data)
                }
                true
            }
            show()
        }
    }

    private fun editGroupName(data: SystemTtsGroup) {
        val et = MaterialTextInput(requireContext())
        et.inputEdit.setText(data.name)
        et.inputLayout.setHint(R.string.name)
        MaterialAlertDialogBuilder(requireContext()).setTitle(R.string.edit_group_name)
            .setView(et)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                appDb.systemTtsDao.updateGroup(
                    data.copy(
                        name = et.inputEdit.text.toString().ifEmpty { getString(R.string.unnamed) })
                )
            }
            .setFadeAnim()
            .show()
    }

    private fun deleteGroup(data: SystemTtsGroup) {
        AppDialogs.displayDeleteDialog(requireContext(), data.name) {
            appDb.systemTtsDao.deleteGroupAndTts(data)
        }
    }
}