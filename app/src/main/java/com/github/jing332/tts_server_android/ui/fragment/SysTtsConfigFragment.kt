package com.github.jing332.tts_server_android.ui.fragment

import android.annotation.SuppressLint
import android.content.*
import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.viewModelScope
import com.drake.brv.listener.ItemDifferCallback
import com.drake.brv.utils.linear
import com.drake.brv.utils.setup
import com.github.jing332.tts_server_android.App
import com.github.jing332.tts_server_android.R
import com.github.jing332.tts_server_android.constant.KeyConst.KEY_DATA
import com.github.jing332.tts_server_android.constant.KeyConst.RESULT_ADD
import com.github.jing332.tts_server_android.constant.ReadAloudTarget
import com.github.jing332.tts_server_android.data.CompatSysTtsConfig
import com.github.jing332.tts_server_android.data.appDb
import com.github.jing332.tts_server_android.data.entities.SysTts
import com.github.jing332.tts_server_android.databinding.FragmentTtsConfigBinding
import com.github.jing332.tts_server_android.databinding.ItemSysttsConfigBinding
import com.github.jing332.tts_server_android.help.SysTtsConfig
import com.github.jing332.tts_server_android.model.tts.MsTTS
import com.github.jing332.tts_server_android.ui.systts.edit.HttpTtsEditActivity
import com.github.jing332.tts_server_android.ui.systts.edit.MsTtsEditActivity
import com.github.jing332.tts_server_android.util.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch


@Suppress("DEPRECATION", "UNCHECKED_CAST")
class SysTtsConfigFragment : Fragment() {
    companion object {
        const val TAG = "TtsConfigFragment"
        const val ACTION_ON_CONFIG_CHANGED = "on_config_changed"
    }

    private val viewModel: SysTtsConfigViewModel by activityViewModels()
    private val binding: FragmentTtsConfigBinding by lazy {
        FragmentTtsConfigBinding.inflate(
            layoutInflater
        )
    }

    /* EditActivity的返回值 */
    private val startForResult =
        registerForActivityResult(StartActivityForResult()) { result: ActivityResult ->
            val data = result.data?.getParcelableExtra<SysTts>(KEY_DATA)
            data?.let {
                if (result.resultCode == RESULT_ADD) appDb.sysTtsDao.insert(data)
                else appDb.sysTtsDao.update(data)

                if (data.isEnabled) requireContext().sendBroadcast(Intent(ACTION_ON_CONFIG_CHANGED))
            }
        }

    fun startEditActivity() {
        val intent =
            Intent(requireContext(), MsTtsEditActivity::class.java)
        startForResult.launch(intent)
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return binding.root
    }

    @SuppressLint("RestrictedApi", "SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val brv = binding.recyclerView.linear().setup {
            addType<SysTts>(R.layout.item_systts_config)
            onCreate {
                getBinding<ItemSysttsConfigBinding>().apply {
                    checkBoxSwitch.setOnClickListener { view ->
                        onSwitchClick(view, models as List<SysTts>, modelPosition)
                    }
                    btnEdit.setOnClickListener { edit(getModel()) }
                    btnDelete.setOnClickListener { delete(getModel()) }
                }
                itemView.setOnClickListener { showNumEditDialog(getModel()) }
                itemView.setOnLongClickListener { showItemPopupMenu(itemView, getModel()) }
            }

            onBind {
                val model = getModel<SysTts>()
                val binding = getBinding<ItemSysttsConfigBinding>()
                binding.apply {
                    tvDescription.text = Html.fromHtml(model.tts?.getDescription())
                    tvRaTarget.visibility =
                        if (model.readAloudTarget == ReadAloudTarget.DEFAULT) View.INVISIBLE else View.VISIBLE
                }
            }

            itemDifferCallback = object : ItemDifferCallback {
                override fun areItemsTheSame(oldItem: Any, newItem: Any): Boolean {
                    return (oldItem as SysTts).id == (newItem as SysTts).id
                }

                override fun getChangePayload(oldItem: Any, newItem: Any): Any {
                    return true
                }
            }
        }

        // 监听数据
        viewModel.viewModelScope.runOnIO {
            appDb.sysTtsDao.flowAll().conflate().collect {
                if (brv.models == null) runOnUI { brv.models = it } else brv.setDifferModels(it)
                runOnUI { checkFormatAndShowDialog() }
            }
        }

        if (CompatSysTtsConfig.migrationConfig()) App.context.longToast("旧版配置迁移成功，原文件已删除")
    }

    /* 警告 格式不同 */
    private val formatWarnDialog by lazy {
        AlertDialog.Builder(requireContext()).setTitle(getString(R.string.warning))
            .setMessage(getString(R.string.msg_aside_and_dialogue_format_different))
            .setPositiveButton(android.R.string.ok) { _, _ -> }.create()
    }

    /* 警告 多语音选项未开启 */
    private val checkMultiVoiceDialog by lazy {
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.warning)
            .setCancelable(false)
            .setPositiveButton(android.R.string.ok) { _, _ -> }
            .setMessage(R.string.please_check_multi_voice_option).create()
    }

    /* 检查格式 如不同则显示对话框 */
    private fun checkFormatAndShowDialog() {
        SysTtsConfig.apply {
            if (isMultiVoiceEnabled && !isInAppPlayAudio && !viewModel.checkMultiVoiceFormat()) {
                runOnUI {
                    formatWarnDialog.show()
                }
            }
        }
    }

    private fun onSwitchClick(view: View?, list: List<SysTts>, position: Int) {
        val checkBox = view as CheckBox
        // 检测是否开启多语音
        if (viewModel.onCheckBoxChanged(list, position, checkBox.isChecked))
            requireContext().sendBroadcast(Intent(ACTION_ON_CONFIG_CHANGED))
        else {
            checkBox.isChecked = false
            checkMultiVoiceDialog.show()
        }
    }

    private fun showNumEditDialog(data: SysTts) {
        // 修改数据要clone，不然对比时数据相同导致UI不更新
        data.clone<SysTts>()?.let { clonedData ->
            clonedData.tts?.onDescriptionClick(requireContext(), view) {
                it?.let {
                    appDb.sysTtsDao.update(clonedData)
                    if (clonedData.isEnabled) requireContext().sendBroadcast(
                        Intent(
                            ACTION_ON_CONFIG_CHANGED
                        )
                    )
                }
            }
        }
    }

    private fun edit(data: SysTts) {
        val cls = when (data.tts) {
            is MsTTS -> MsTtsEditActivity::class.java
            else -> HttpTtsEditActivity::class.java
        }
        startForResult.launch(Intent(requireContext(), cls).apply { putExtra(KEY_DATA, data) })
    }

    private fun delete(data: SysTts) {
        AlertDialog.Builder(requireContext()).setTitle(R.string.is_confirm_delete)
            .setPositiveButton(R.string.delete) { _, _ ->
                appDb.sysTtsDao.delete(data)
                if (data.isEnabled) requireContext().sendBroadcast(
                    Intent(
                        ACTION_ON_CONFIG_CHANGED
                    )
                )
            }
            .show()
    }

    /* 列表item的长按菜单 */
    private fun showItemPopupMenu(view: View, data: SysTts): Boolean {
        val popupMenu = PopupMenu(requireContext(), view)
        popupMenu.menuInflater.inflate(R.menu.menu_systts_list_item, popupMenu.menu)
        popupMenu.setOnMenuItemClickListener { item ->
            val target = when (item.itemId) {
                R.id.menu_setAsDialogue -> ReadAloudTarget.DIALOGUE
                R.id.menu_setAsAside -> ReadAloudTarget.ASIDE
                else -> ReadAloudTarget.DEFAULT
            }

            if (data.isEnabled) {
                val isMultiVoice = SysTtsConfig.isMultiVoiceEnabled
                if (target == ReadAloudTarget.DEFAULT) {
                    if (isMultiVoice) { // 开多语音 但想启用单语音
                        longToast(getString(R.string.off_multi_voice_use_global))
                        return@setOnMenuItemClickListener false
                    }
                } else if (!isMultiVoice) { // 未开启多语音
                    checkMultiVoiceDialog.show()
                    return@setOnMenuItemClickListener false
                } else { // 已开启多语音并且target为旁白或对话
                    if (target == ReadAloudTarget.DEFAULT) data.isEnabled = false
                    appDb.sysTtsDao.update(data.copy(readAloudTarget = target))
                    requireContext().sendBroadcast(Intent(ACTION_ON_CONFIG_CHANGED))
                    return@setOnMenuItemClickListener false
                }
            }
            appDb.sysTtsDao.update(data.copy(readAloudTarget = target))

            false
        }
        popupMenu.show()

        return true
    }

    @SuppressLint("SetTextI18n")
    fun setAudioRequestTimeout() {
        val numPicker = NumberPicker(requireContext())
        numPicker.maxValue = 30
        numPicker.minValue = 2
        numPicker.value = 5
        val displayList = ArrayList<String>()
        for (i in 2..30) {
            displayList.add("${i}秒")
        }
        numPicker.displayedValues = displayList.toList().toTypedArray()

        numPicker.value = SysTtsConfig.requestTimeout / 1000 //转为秒
        AlertDialog.Builder(requireContext()).setTitle(R.string.set_audio_request_timeout)
            .setView(numPicker)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                SysTtsConfig.requestTimeout = numPicker.value * 1000 //转为毫秒
                requireContext().sendBroadcast(Intent(ACTION_ON_CONFIG_CHANGED))
            }
            .setNegativeButton(R.string.reset) { _, _ ->
                SysTtsConfig.requestTimeout = 5000
            }
            .show()
    }

    fun showImportConfig() {
        val et = EditText(requireContext())
        et.hint = getString(R.string.url_net)
        AlertDialog.Builder(requireContext()).setTitle(R.string.import_config).setView(et)
            .setPositiveButton(R.string.import_from_clip) { _, _ ->
                viewModel.viewModelScope.launch {
                    val err = viewModel.importConfig(ClipboardUtils.text.toString())
                    err?.let {
                        longToast("导入配置失败：$err")
                    }
                }
            }.setNegativeButton(getString(R.string.import_from_url)) { _, _ ->
                val err = viewModel.importConfigByUrl(et.text.toString())
                err?.let {
                    longToast("导入配置失败：$err")
                }
            }
            .show()
    }

    fun showExportConfig() {
        val jsonStr = viewModel.exportConfig()
        val tv = TextView(requireContext())
        tv.setTextIsSelectable(true)
        tv.setPadding(50, 50, 50, 0)
        tv.text = jsonStr
        AlertDialog.Builder(requireContext()).setTitle(R.string.export_config).setView(tv)
            .setPositiveButton(R.string.copy) { _, _ ->
                ClipboardUtils.copyText(jsonStr)
                toast(R.string.copied)
            }.setNegativeButton("上传到URL") { _, _ ->
                viewModel.viewModelScope.launch {
                    val result = viewModel.uploadConfigToUrl(jsonStr)
                    if (result.isSuccess) {
                        ClipboardUtils.copyText(result.getOrNull())
                        longToast("已复制URL：\n${result.getOrNull()}")
                    }
                }
            }.show()
    }

    fun showSetMinDialogueLength() {
        val numList = arrayListOf("不限制")
        for (i in 1..10)
            numList.add("对话字数 ≥ $i")

        val picker = NumberPicker(requireContext()).apply {
            maxValue = numList.size - 1
            displayedValues = numList.toTypedArray()
            value = SysTtsConfig.minDialogueLength
        }
        AlertDialog.Builder(requireContext()).setTitle("对话文本最小匹配汉字数").setMessage("(包括中文符号)")
            .setView(picker)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                SysTtsConfig.minDialogueLength = picker.value
                requireContext().sendBroadcast(Intent(ACTION_ON_CONFIG_CHANGED))
            }
            .setNegativeButton(R.string.reset) { _, _ ->
                SysTtsConfig.minDialogueLength = 0
                requireContext().sendBroadcast(Intent(ACTION_ON_CONFIG_CHANGED))
            }
            .show()
    }

    fun startHttpTtsEditActivity() {
        startForResult.launch(Intent(requireContext(), HttpTtsEditActivity::class.java))
    }
}
