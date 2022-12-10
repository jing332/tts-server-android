package com.github.jing332.tts_server_android.ui.systts.list

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.PopupMenu
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.viewModelScope
import com.drake.brv.listener.ItemDifferCallback
import com.drake.brv.utils.linear
import com.drake.brv.utils.setup
import com.drake.net.utils.withMain
import com.github.jing332.tts_server_android.R
import com.github.jing332.tts_server_android.constant.KeyConst
import com.github.jing332.tts_server_android.constant.ReadAloudTarget
import com.github.jing332.tts_server_android.data.appDb
import com.github.jing332.tts_server_android.data.entities.SysTts
import com.github.jing332.tts_server_android.databinding.SysttsListGroupFragmentBinding
import com.github.jing332.tts_server_android.databinding.SysttsListItemBinding
import com.github.jing332.tts_server_android.help.SysTtsConfig
import com.github.jing332.tts_server_android.model.tts.MsTTS
import com.github.jing332.tts_server_android.service.systts.SystemTtsService
import com.github.jing332.tts_server_android.ui.systts.edit.HttpTtsEditActivity
import com.github.jing332.tts_server_android.ui.systts.edit.MsTtsEditActivity
import com.github.jing332.tts_server_android.util.*
import kotlinx.coroutines.flow.conflate
import kotlin.system.measureTimeMillis

@Suppress("DEPRECATION")
class SysTtsListGroupFragment : Fragment() {
    companion object {
        private const val ARG_RA_TARGET = "ARG_RA_TARGET"

        fun newInstance(raTarget: Int = -1): SysTtsListGroupFragment {
            return SysTtsListGroupFragment().apply {
                arguments = Bundle().apply { putInt(ARG_RA_TARGET, raTarget) }
            }
        }
    }

    private val vm: SysTtsListGroupViewModel by viewModels()
    private val binding: SysttsListGroupFragmentBinding by lazy {
        SysttsListGroupFragmentBinding.inflate(layoutInflater)
    }

    // EditActivity的返回值
    private val startForResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            val data = result.data?.getParcelableExtra<SysTts>(KeyConst.KEY_DATA)
            data?.let {
                if (result.resultCode == KeyConst.RESULT_ADD) appDb.sysTtsDao.insert(data)
                else appDb.sysTtsDao.update(data)

                notifyTtsUpdate(data.isEnabled)
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return binding.root
    }

    @Suppress("UNCHECKED_CAST")
    @SuppressLint("RestrictedApi", "SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState != null) return

        val brv = binding.rv.linear().setup {
            addType<SysTts>(R.layout.systts_list_item)
            onCreate {
                getBinding<SysttsListItemBinding>().apply {
                    checkBoxSwitch.setOnClickListener { view ->
                        onSwitchClick(view, models as List<SysTts>, modelPosition)
                    }
                    btnEdit.setOnClickListener { measureTimeMillis { edit(getModel()) } }
                    btnDelete.setOnClickListener { delete(getModel()) }
                }
                itemView.setOnClickListener { displayQuickEditDialog(getModel()) }
                itemView.setOnLongClickListener { displayItemPopupMenu(itemView, getModel()) }
            }

            onBind {
                val model = getModel<SysTts>()
                val binding = getBinding<SysttsListItemBinding>()
                binding.apply {
                    tvDescription.text = Html.fromHtml(model.tts?.getDescription())
                    tvRaTarget.visibility =
                        if (model.readAloudTarget == ReadAloudTarget.ALL) View.INVISIBLE else View.VISIBLE
                }
            }

            itemDifferCallback = object : ItemDifferCallback {
                override fun areItemsTheSame(oldItem: Any, newItem: Any): Boolean {
                    return (oldItem as SysTts).id == (newItem as SysTts).id
                }

                override fun getChangePayload(oldItem: Any, newItem: Any) = true
            }
        }

        val raTarget = arguments?.getInt(ARG_RA_TARGET)

        // 监听数据
        vm.viewModelScope.runOnIO {
            appDb.sysTtsDao.flowAll().conflate().collect { list ->
                val filteredList =
                    if (raTarget == -1) list else list.filter { it.readAloudTarget == raTarget }
                val handledList = filteredList.sortedBy { it.tts?.getType() }

                if (brv.models == null) withMain { brv.models = handledList }
                else brv.setDifferModels(handledList)
            }
        }
    }

    // 警告 多语音选项未开启
    private val checkMultiVoiceDialog by lazy {
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.warning)
            .setCancelable(false)
            .setPositiveButton(android.R.string.ok) { _, _ -> }
            .setMessage(R.string.systts_please_check_multi_voice_option).create()
            .apply { window?.setWindowAnimations(R.style.dialogFadeStyle) }
    }

    private fun onSwitchClick(view: View?, list: List<SysTts>, position: Int) {
        val checkBox = view as CheckBox
        // 检测是否开启多语音
        if (vm.onCheckBoxChanged(list, position, checkBox.isChecked))
            notifyTtsUpdate()
        else {
            checkBox.isChecked = false
            checkMultiVoiceDialog.show()
        }
    }

    private fun displayQuickEditDialog(data: SysTts) {
        // 修改数据要clone，不然对比时数据相同导致UI不更新
        data.clone<SysTts>()?.let { clonedData ->
            clonedData.tts?.onDescriptionClick(requireContext(), view) {
                it?.let {
                    appDb.sysTtsDao.update(clonedData)
                    notifyTtsUpdate(clonedData.isEnabled)
                }
            }
        }
    }

    private fun notifyTtsUpdate(isUpdate: Boolean = true) {
        if (isUpdate) SystemTtsService.notifyUpdateConfig()
    }

    private fun edit(data: SysTts) {
        val cls = when (data.tts) {
            is MsTTS -> MsTtsEditActivity::class.java
            else -> HttpTtsEditActivity::class.java
        }
        startForResult.launch(Intent(requireContext(), cls).apply {
            putExtra(
                KeyConst.KEY_DATA,
                data
            )
        })
    }

    private fun delete(data: SysTts) {
        AlertDialog.Builder(requireContext()).setTitle(R.string.is_confirm_delete)
            .setPositiveButton(R.string.delete) { _, _ ->
                appDb.sysTtsDao.delete(data)
                notifyTtsUpdate(data.isEnabled)
            }
            .setFadeAnim().show()
    }


    private fun displayItemPopupMenu(view: View, data: SysTts): Boolean {
        val popupMenu = PopupMenu(requireContext(), view)
        popupMenu.menuInflater.inflate(R.menu.menu_systts_list_item, popupMenu.menu)
        popupMenu.setOnMenuItemClickListener { item ->
            val target = when (item.itemId) {
                R.id.menu_setAsDialogue -> ReadAloudTarget.DIALOGUE
                R.id.menu_setAsAside -> ReadAloudTarget.ASIDE
                else -> ReadAloudTarget.ALL
            }

            if (data.isEnabled) {
                val isMultiVoice = SysTtsConfig.isMultiVoiceEnabled
                if (target == ReadAloudTarget.ALL) {
                    if (isMultiVoice) { // 开多语音 但想启用单语音
                        longToast(getString(R.string.off_multi_voice_use_global))
                        return@setOnMenuItemClickListener false
                    }
                } else if (!isMultiVoice) { // 未开启多语音
                    checkMultiVoiceDialog.show()
                    return@setOnMenuItemClickListener false
                } else { // 已开启多语音并且target为旁白或对话
                    if (target == ReadAloudTarget.ALL) data.isEnabled = false
                    appDb.sysTtsDao.update(data.copy(readAloudTarget = target))
                    notifyTtsUpdate()
                    return@setOnMenuItemClickListener false
                }
            }
            appDb.sysTtsDao.update(data.copy(readAloudTarget = target))

            false
        }
        popupMenu.show()

        return true
    }
}
