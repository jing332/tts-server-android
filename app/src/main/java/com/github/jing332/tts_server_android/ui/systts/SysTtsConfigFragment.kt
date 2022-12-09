package com.github.jing332.tts_server_android.ui.systts

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isGone
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
import com.github.jing332.tts_server_android.databinding.DialogInAppPlaySettingsBinding
import com.github.jing332.tts_server_android.databinding.FragmentSysttsConfigBinding
import com.github.jing332.tts_server_android.databinding.ItemSysttsConfigBinding
import com.github.jing332.tts_server_android.help.SysTtsConfig
import com.github.jing332.tts_server_android.model.tts.MsTTS
import com.github.jing332.tts_server_android.service.systts.SystemTtsService
import com.github.jing332.tts_server_android.ui.MainActivity
import com.github.jing332.tts_server_android.ui.custom.widget.ConvenientSeekbar
import com.github.jing332.tts_server_android.ui.systts.edit.HttpTtsEditActivity
import com.github.jing332.tts_server_android.ui.systts.edit.MsTtsEditActivity
import com.github.jing332.tts_server_android.ui.systts.replace.ReplaceManagerActivity
import com.github.jing332.tts_server_android.util.*
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.launch
import kotlin.system.measureTimeMillis


@Suppress("DEPRECATION", "UNCHECKED_CAST")
class SysTtsConfigFragment : Fragment() {
    companion object {
        const val TAG = "TtsConfigFragment"
    }

    private val vm: SysTtsConfigViewModel by activityViewModels()
    private val binding: FragmentSysttsConfigBinding by lazy {
        FragmentSysttsConfigBinding.inflate(layoutInflater)
    }

    private val mReceiver: MyReceiver by lazy { MyReceiver() }

    /* EditActivity的返回值 */
    private val startForResult =
        registerForActivityResult(StartActivityForResult()) { result: ActivityResult ->
            val data = result.data?.getParcelableExtra<SysTts>(KEY_DATA)
            data?.let {
                if (result.resultCode == RESULT_ADD) appDb.sysTtsDao.insert(data)
                else appDb.sysTtsDao.update(data)

                notifyTtsUpdate(data.isEnabled)
            }
        }

    fun startMsTtsEditActivity() {
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
        if (savedInstanceState != null) return

        App.localBroadcast.registerReceiver(
            mReceiver,
            IntentFilter().apply { addAction(MainActivity.ACTION_OPTION_ITEM_SELECTED_ID) }
        )

        val brv = binding.recyclerView.linear().setup {
            addType<SysTts>(R.layout.item_systts_config)
            onCreate {
                getBinding<ItemSysttsConfigBinding>().apply {
                    checkBoxSwitch.setOnClickListener { view ->
                        onSwitchClick(view, models as List<SysTts>, modelPosition)
                    }
                    btnEdit.setOnClickListener { measureTimeMillis { edit(getModel()) } }
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
                        if (model.readAloudTarget == ReadAloudTarget.ALL) View.INVISIBLE else View.VISIBLE
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
        vm.viewModelScope.runOnIO {
            appDb.sysTtsDao.flowAll().conflate().collect {
                if (brv.models == null) runOnUI { brv.models = it } else brv.setDifferModels(it)
                runOnUI {
                    println("checkFormatAndShowDialog")
                    checkFormatAndShowDialog()
                }
            }
        }

        if (CompatSysTtsConfig.migrationConfig()) App.context.longToast("旧版配置迁移成功，原文件已删除")
    }

    override fun onDestroy() {
        super.onDestroy()
        App.localBroadcast.unregisterReceiver(mReceiver)
    }

    /* 警告 格式不同 */
    private val formatWarnDialog by lazy {
        AlertDialog.Builder(requireContext()).setTitle(getString(R.string.warning))
            .setMessage(getString(R.string.msg_aside_and_dialogue_format_different))
            .setPositiveButton(android.R.string.ok) { _, _ -> }.create()
            .apply { window?.setWindowAnimations(R.style.dialogFadeStyle) }
    }

    /* 警告 多语音选项未开启 */
    private val checkMultiVoiceDialog by lazy {
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.warning)
            .setCancelable(false)
            .setPositiveButton(android.R.string.ok) { _, _ -> }
            .setMessage(R.string.please_check_multi_voice_option).create()
            .apply { window?.setWindowAnimations(R.style.dialogFadeStyle) }
    }

    /* 检查格式 如不同则显示对话框 */
    private fun checkFormatAndShowDialog() {
        SysTtsConfig.apply {
            if (isMultiVoiceEnabled && !isInAppPlayAudio && !vm.checkMultiVoiceFormat()) {
                runOnUI {
                    formatWarnDialog.show()
                }
            }
        }
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

    private fun showNumEditDialog(data: SysTts) {
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
        startForResult.launch(Intent(requireContext(), cls).apply { putExtra(KEY_DATA, data) })
    }

    private fun delete(data: SysTts) {
        AlertDialog.Builder(requireContext()).setTitle(R.string.is_confirm_delete)
            .setPositiveButton(R.string.delete) { _, _ ->
                appDb.sysTtsDao.delete(data)
                notifyTtsUpdate(data.isEnabled)
            }
            .setFadeAnim().show()
    }

    /* 列表item的长按菜单 */
    private fun showItemPopupMenu(view: View, data: SysTts): Boolean {
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

    fun showImportConfig() {
        val et = EditText(requireContext())
        et.hint = getString(R.string.url_net)
        AlertDialog.Builder(requireContext()).setTitle(R.string.import_config).setView(et)
            .setPositiveButton(R.string.import_from_clip) { _, _ ->
                vm.viewModelScope.launch {
                    val err = vm.importConfig(ClipboardUtils.text.toString())
                    err?.let {
                        longToast("导入配置失败：$err")
                    }
                }
            }.setNegativeButton(getString(R.string.import_from_url)) { _, _ ->
                val err = vm.importConfigByUrl(et.text.toString())
                err?.let {
                    longToast("导入配置失败：$err")
                }
            }.setFadeAnim().show()
    }

    fun showExportConfig() {
        val jsonStr = vm.exportConfig()
        val tv = TextView(requireContext())
        tv.setTextIsSelectable(true)
        tv.setPadding(50, 50, 50, 0)
        tv.text = jsonStr
        AlertDialog.Builder(requireContext()).setTitle(R.string.export_config).setView(tv)
            .setPositiveButton(R.string.copy) { _, _ ->
                ClipboardUtils.copyText(jsonStr)
                toast(R.string.copied)
            }.setNegativeButton("上传到URL") { _, _ ->
                vm.viewModelScope.launch {
                    val result = vm.uploadConfigToUrl(jsonStr)
                    if (result.isSuccess) {
                        ClipboardUtils.copyText(result.getOrNull())
                        longToast("已复制URL：\n${result.getOrNull()}")
                    }
                }
            }.setFadeAnim().show()
    }

    fun startHttpTtsEditActivity() {
        startForResult.launch(Intent(requireContext(), HttpTtsEditActivity::class.java))
    }

    @Suppress("DEPRECATION")
    private fun showInAppSettingsDialog() {
        val view = FrameLayout(requireContext())
        val inAppBinding = DialogInAppPlaySettingsBinding.inflate(layoutInflater, view, true)
        inAppBinding.apply {
            tvTip.text = Html.fromHtml(getString(R.string.msg_in_app_info_html))
            switchOnOff.setOnCheckedChangeListener { _, isChecked ->
                layoutNumEdit.isGone = !isChecked
                tvTip.isGone = isChecked
            }
            seekbarSpeed.onSeekBarChangeListener =
                object : ConvenientSeekbar.OnSeekBarChangeListener {
                    override fun onProgressChanged(
                        seekBar: ConvenientSeekbar,
                        progress: Int,
                        fromUser: Boolean
                    ) {
                        if (progress <= 0) seekBar.progress = 1
                        tvRateValue.text = "${(seekBar.progress * 0.1).toFloat()}"
                    }
                }
            seekbarSpeed.progress = (SysTtsConfig.inAppPlaySpeed * 10).toInt()

            seekbarPitch.onSeekBarChangeListener =
                object : ConvenientSeekbar.OnSeekBarChangeListener {
                    override fun onProgressChanged(
                        seekBar: ConvenientSeekbar,
                        progress: Int,
                        fromUser: Boolean
                    ) {
                        if (progress <= 0) seekBar.progress = 1
                        tvPitchValue.text = "${(seekBar.progress * 0.1).toFloat()}"
                    }
                }
            seekbarPitch.progress = (SysTtsConfig.inAppPlayPitch * 10).toInt()


            SysTtsConfig.isInAppPlayAudio.let {
                switchOnOff.isChecked = it
                tvTip.isGone = it
                layoutNumEdit.isGone = !it
            }
        }

        AlertDialog.Builder(requireContext())
            .setView(view).setOnDismissListener {
                SysTtsConfig.isInAppPlayAudio = inAppBinding.switchOnOff.isChecked
                SysTtsConfig.inAppPlaySpeed = (inAppBinding.seekbarSpeed.progress * 0.1).toFloat()
                SysTtsConfig.inAppPlayPitch = (inAppBinding.seekbarPitch.progress * 0.1).toFloat()
                SystemTtsService.notifyUpdateConfig()
            }.setFadeAnim().show()
    }

    @SuppressLint("SetTextI18n")
    private fun showSetAudioRequestTimeoutDialog() {
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
                SystemTtsService.notifyUpdateConfig()
            }
            .setNegativeButton(R.string.reset) { _, _ ->
                SysTtsConfig.requestTimeout = 5000
            }
            .setFadeAnim()
            .show()
    }

    private fun showSetMinDialogueLengthDialog() {
        val numList = arrayListOf("不限制")
        for (i in 1..10)
            numList.add("对话字数 ≥ $i")

        val picker = NumberPicker(requireContext()).apply {
            maxValue = numList.size - 1
            displayedValues = numList.toTypedArray()
            value = SysTtsConfig.minDialogueLength
        }
        AlertDialog.Builder(requireContext()).setTitle("对话文本最小匹配汉字数")
            .setMessage(R.string.set_dialogue_min_match_count_msg)
            .setView(picker)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                SysTtsConfig.minDialogueLength = picker.value
                SystemTtsService.notifyUpdateConfig()
            }
            .setNegativeButton(R.string.reset) { _, _ ->
                SysTtsConfig.minDialogueLength = 0
                SystemTtsService.notifyUpdateConfig()
            }
            .setFadeAnim().show()
    }

    private fun onOptionsItemSelected(itemId: Int) {
        when (itemId) {
            /*R.id.menu_desktopShortcut -> {
                MyTools.addShortcut(
                    requireContext(),
                    getString(R.string.tts_config),
                    "tts_config",
                    R.mipmap.ic_launcher_round,
                    Intent(requireContext(), TtsSettingsActivity::class.java)
                )
            }*/
            /* 添加配置 */
            R.id.menu_addMsTts -> startMsTtsEditActivity()
            R.id.menu_addHttpTts -> startHttpTtsEditActivity()

            R.id.menu_isInAppPlayAudio -> showInAppSettingsDialog()
            R.id.menu_setAudioRequestTimeout -> showSetAudioRequestTimeoutDialog()
            R.id.menu_setMinDialogueLen -> showSetMinDialogueLengthDialog()

            R.id.menu_doSplit -> {
                SysTtsConfig.isSplitEnabled = !SysTtsConfig.isSplitEnabled
                SystemTtsService.notifyUpdateConfig()
            }
            R.id.menu_isMultiVoice -> {
                SysTtsConfig.isMultiVoiceEnabled = !SysTtsConfig.isMultiVoiceEnabled
                SystemTtsService.notifyUpdateConfig()
            }

            R.id.menu_replace_manager -> startActivity(
                Intent(requireContext(), ReplaceManagerActivity::class.java)
            )

            /* 导入导出 */
            R.id.menu_importConfig -> showImportConfig()
            R.id.menu_exportConfig -> showExportConfig()
        }
    }


    inner class MyReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            // MainActivity 的点击右上角菜单的ID
            if (intent?.action == MainActivity.ACTION_OPTION_ITEM_SELECTED_ID) {
                val itemId = intent.getIntExtra(MainActivity.KEY_MENU_ITEM_ID, -1)
                onOptionsItemSelected(itemId)
            }
        }
    }

}