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
import com.github.jing332.tts_server_android.R
import com.github.jing332.tts_server_android.constant.KeyConst.KEY_DATA
import com.github.jing332.tts_server_android.constant.ReadAloudTarget
import com.github.jing332.tts_server_android.constant.TtsApiType
import com.github.jing332.tts_server_android.data.appDb
import com.github.jing332.tts_server_android.data.entities.SysTts
import com.github.jing332.tts_server_android.databinding.FragmentTtsConfigBinding
import com.github.jing332.tts_server_android.databinding.ItemSysttsConfigBinding
import com.github.jing332.tts_server_android.help.SysTtsConfig
import com.github.jing332.tts_server_android.ui.custom.SysTtsNumericalEditView
import com.github.jing332.tts_server_android.ui.systts.TtsConfigEditActivity
import com.github.jing332.tts_server_android.ui.systts.TtsConfigEditActivity.Companion.RESULT_ADD
import com.github.jing332.tts_server_android.util.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch


@Suppress("DEPRECATION", "UNCHECKED_CAST")
class TtsConfigFragment : Fragment() {
    companion object {
        const val TAG = "TtsConfigFragment"
        const val ACTION_ON_CONFIG_CHANGED = "on_config_changed"
    }

    private val viewModel: TtsConfigFragmentViewModel by activityViewModels()
    private val binding: FragmentTtsConfigBinding by lazy {
        FragmentTtsConfigBinding.inflate(
            layoutInflater
        )
    }

    /* EditActivity的返回值 */
    private val startForResult =
        registerForActivityResult(StartActivityForResult()) { result: ActivityResult ->
            val extraData = result.data?.getSerializableExtra(KEY_DATA)
            extraData?.let {
                val data = extraData as SysTts
                if (result.resultCode == RESULT_ADD) appDb.sysTtsDao.insert(data)
                else appDb.sysTtsDao.update(data)

                if (data.isEnabled) requireContext().sendBroadcast(Intent(ACTION_ON_CONFIG_CHANGED))
            }
        }

    fun startEditActivity() {
        val intent =
            Intent(requireContext(), TtsConfigEditActivity::class.java)
        startForResult.launch(intent)
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return binding.root
    }

    @SuppressLint("RestrictedApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val brv = binding.recyclerView.linear().setup {
            addType<SysTts>(R.layout.item_systts_config)
            onCreate {
                getBinding<ItemSysttsConfigBinding>().apply {
                    checkBoxSwitch.setOnClickListener { view ->
                        onSwitchClick(view, models as List<SysTts>, modelPosition)
                    }
                    tvContent.setOnClickListener { onContentClick(getModel()) }
                    tvContent.setOnLongClickListener { onItemLongClick(itemView, getModel()) }
                    btnEdit.setOnClickListener { onEdit(getModel()) }
                    btnDelete.setOnClickListener { onDelete(getModel()) }
                }
                itemView.setOnLongClickListener { onItemLongClick(itemView, getModel()) }
            }

            onBind {
                val model = getModel<SysTts>()
                val binding = getBinding<ItemSysttsConfigBinding>()
                binding.apply {
                    model.msTts?.let {
                        tvRaTarget.visibility =
                            if (model.readAloudTarget == ReadAloudTarget.DEFAULT) View.INVISIBLE else View.VISIBLE
                        tvApiType.text =
                            TtsApiType.toString(it.api)
                        tvContent.text = Html.fromHtml(model.description)
                    }
                }
            }

            itemDifferCallback = object : ItemDifferCallback {
                override fun areItemsTheSame(oldItem: Any, newItem: Any): Boolean {
                    return (oldItem as SysTts).id == (newItem as SysTts).id
                }

                override fun getChangePayload(oldItem: Any, newItem: Any): Any? {
                    return if (oldItem is SysTts && newItem is SysTts && oldItem.isEnabled != newItem.isEnabled) {
                        return true // 不刷新Item 确保CheckBox点击动画完整
                    } else null
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

        // 兼容旧的Json数据
        if (viewModel.compatOldConfig()) longToast("旧版配置兼容成功，文件已删除")
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


    private fun checkFormatAndShowDialog() {
        val isMultiVoice = SysTtsConfig.isMultiVoiceEnabled
        if (isMultiVoice && !viewModel.checkMultiVoiceFormat()) {
            runOnUI {
                formatWarnDialog.show()
            }
        }
    }

    private fun onSwitchClick(view: View?, list: List<SysTts>, position: Int) {
        val checkBox = view as CheckBox
        // 检测是否开启多语音
        if (viewModel.onCheckBoxChanged(list, position, checkBox.isChecked)) {
            requireContext().sendBroadcast(Intent(ACTION_ON_CONFIG_CHANGED))
        } else {
//            checkMultiVoiceDialog.show()
            checkBox.isChecked = false
        }
    }

    private fun onContentClick(data: SysTts) {
        val editView = SysTtsNumericalEditView(requireContext())
        editView.setPadding(25, 25, 25, 50)
        data.msTts?.let { pro ->
            editView.setRate(pro.prosody.rate)
            editView.setVolume(pro.prosody.volume)
            editView.setStyleDegree(pro.expressAs?.styleDegree ?: 1F)
            editView.isStyleDegreeVisible = pro.api != TtsApiType.EDGE

            val dlg = AlertDialog.Builder(requireContext())
                .setTitle("数值调节").setView(editView)
                .setOnDismissListener {

                    val data2 = data.copy(msTts = data.msTts?.clone())
                    data2.msTts?.let {
                        it.prosody.rate = editView.rateValue
                        it.prosody.volume = editView.volumeValue
                        it.expressAs?.styleDegree = editView.styleDegreeValue
                    }
                    appDb.sysTtsDao.update(data2)
                    if (data2.isEnabled) requireContext().sendBroadcast(
                        Intent(
                            ACTION_ON_CONFIG_CHANGED
                        )
                    )
                }.create()
            dlg.window?.setDimAmount(0.5F)
            dlg.show()
        }
    }

    private fun onEdit(data: SysTts) {
        val intent =
            Intent(requireContext(), TtsConfigEditActivity::class.java)
        intent.putExtra(KEY_DATA, data)
        startForResult.launch(intent)
    }

    private fun onDelete(data: SysTts) {
        AlertDialog.Builder(requireContext()).setTitle(R.string.is_confirm_delete)
            .setPositiveButton(R.string.delete) { _, _ ->
                appDb.sysTtsDao.delete(data)
                if (data.isEnabled) requireContext().sendBroadcast(Intent(ACTION_ON_CONFIG_CHANGED))
            }
            .show()
    }

    private fun onItemLongClick(view: View, data: SysTts): Boolean {
        /* 列表item的长按菜单 */
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
                    if (isMultiVoice) {
                        longToast("请关闭多语音选项以使用单语音模式")
                        return@setOnMenuItemClickListener false
                    }
                } else if (!isMultiVoice) { // 未开启多语音
                    checkMultiVoiceDialog.show()
                    return@setOnMenuItemClickListener false
                } else { // 已开启多语音并且target为旁白或对话
                    if (data.readAloudTarget == ReadAloudTarget.DEFAULT) data.isEnabled = false
                    appDb.sysTtsDao.update(data.copy(readAloudTarget = target))
                    requireContext().sendBroadcast(Intent(ACTION_ON_CONFIG_CHANGED))
                    return@setOnMenuItemClickListener false
                }
            }
            data.readAloudTarget = target
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
                toastOnUi(R.string.copied)
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
}
