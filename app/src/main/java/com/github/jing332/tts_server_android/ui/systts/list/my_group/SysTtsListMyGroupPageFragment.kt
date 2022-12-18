package com.github.jing332.tts_server_android.ui.systts.list.my_group

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
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
import com.github.jing332.tts_server_android.service.systts.SystemTtsService
import com.github.jing332.tts_server_android.ui.custom.MaterialTextInput
import com.github.jing332.tts_server_android.ui.systts.list.SysTtsListItemHelper
import com.github.jing332.tts_server_android.util.longToast
import com.github.jing332.tts_server_android.util.setFadeAnim
import com.github.jing332.tts_server_android.util.toast
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.launch

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
                    itemView.setOnClickListener {
                        expandOrCollapse()
                        getModelOrNull<RvGroupModel>(modelPosition)?.let {
                            if (it.itemExpand && it.itemSublist.isNullOrEmpty())
                                longToast(R.string.msg_group_is_empty)

                            appDb.systemTtsDao.updateGroup(it.data.apply {
                                isExpanded = it.itemExpand
                            })
                        }
                    }
                    checkBox.setOnClickListener { _ ->
                        (getModel<RvGroupModel>().itemSublist is List<*>).let { list ->
                            (list as List<SystemTts>).forEach {
                                if (it.isEnabled != checkBox.isChecked)
                                    appDb.systemTtsDao.updateTts(it.copy(isEnabled = checkBox.isChecked))
                            }
                            SystemTtsService.notifyUpdateConfig()
                        }
                    }

                    ivMore.setOnClickListener { displayMoreMenu(it, getModel()) }
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
        }

        lifecycleScope.launch() {
            appDb.systemTtsDao.flowAllGroupWithTts.conflate().collect { list ->
                val models = withDefault {
                    list.mapIndexed { i, v ->
                        val isEnabled = v.list.any { it.isEnabled }
                        RvGroupModel(
                            data = v.group,
                            itemGroupPosition = i,
                            isEnabled = isEnabled,
                            itemSublist = v.list,
                            itemExpand = v.group.isExpanded
                        )
                    }
                }

                if (brv.modelCount > 0)
                    withDefault { brv.setDifferModels(models) }
                else
                    brv.models = models
            }

        }

    }

    private fun displayMoreMenu(v: View, model: RvGroupModel) {
        PopupMenu(requireContext(), v).apply {
            menuInflater.inflate(R.menu.menu_systts_list_group_more, menu)
            setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.menu_add_config_from_existing -> addConfigFromExisting()
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
        AlertDialog.Builder(requireContext()).setTitle(R.string.edit_group_name)
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

    private fun addConfigFromExisting() {
        toast("😛 此功能计划中")
    }

    private fun deleteGroup(data: SystemTtsGroup) {
        AlertDialog.Builder(requireContext()).setTitle(R.string.is_confirm_delete)
            .setMessage(data.name)
            .setPositiveButton(R.string.delete) { _, _ ->
                appDb.systemTtsDao.deleteGroup(data)
            }
            .setFadeAnim().show()
    }
}