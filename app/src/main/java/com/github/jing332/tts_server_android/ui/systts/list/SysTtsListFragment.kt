package com.github.jing332.tts_server_android.ui.systts.list

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
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.drake.net.utils.withMain
import com.github.jing332.tts_server_android.App
import com.github.jing332.tts_server_android.R
import com.github.jing332.tts_server_android.constant.KeyConst.KEY_DATA
import com.github.jing332.tts_server_android.constant.KeyConst.RESULT_ADD
import com.github.jing332.tts_server_android.constant.ReadAloudTarget
import com.github.jing332.tts_server_android.data.CompatSysTtsConfig
import com.github.jing332.tts_server_android.data.appDb
import com.github.jing332.tts_server_android.data.entities.SysTts
import com.github.jing332.tts_server_android.databinding.DialogInAppPlaySettingsBinding
import com.github.jing332.tts_server_android.databinding.SysttsListFragmentBinding
import com.github.jing332.tts_server_android.help.SysTtsConfig
import com.github.jing332.tts_server_android.service.systts.SystemTtsService
import com.github.jing332.tts_server_android.ui.MainActivity
import com.github.jing332.tts_server_android.ui.custom.widget.ConvenientSeekbar
import com.github.jing332.tts_server_android.ui.systts.edit.HttpTtsEditActivity
import com.github.jing332.tts_server_android.ui.systts.edit.MsTtsEditActivity
import com.github.jing332.tts_server_android.ui.systts.replace.ReplaceManagerActivity
import com.github.jing332.tts_server_android.util.*
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.launch


@Suppress("DEPRECATION", "UNCHECKED_CAST")
class SysTtsListFragment : Fragment() {
    companion object {
        const val TAG = "TtsConfigFragment"
    }

    private val vm: SysTtsListViewModel by activityViewModels()
    private val binding: SysttsListFragmentBinding by lazy {
        SysttsListFragmentBinding.inflate(layoutInflater)
    }

    private lateinit var vpAdapter: GroupPageAdapter

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

    private fun startMsTtsEditActivity() {
        val intent = Intent(requireContext(), MsTtsEditActivity::class.java)
        startForResult.launch(intent)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val fragmentList = listOf(
            SysTtsListGroupFragment.newInstance(),
            SysTtsListGroupFragment.newInstance(ReadAloudTarget.ASIDE),
            SysTtsListGroupFragment.newInstance(ReadAloudTarget.DIALOGUE),
        )
        vpAdapter = GroupPageAdapter(this, fragmentList)
        binding.viewPager.isSaveEnabled = false
        binding.viewPager.adapter = vpAdapter

        val tabTitles = listOf("全部", "旁白", "对话")
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, pos ->
            tab.text = tabTitles[pos]
        }.attach()

        vm.viewModelScope.launch(Dispatchers.IO) {
            appDb.sysTtsDao.flowAll().conflate().collect {
                withMain { checkFormatAndShowDialog() }
            }
        }

    }

    @SuppressLint("RestrictedApi", "SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState != null) return

        App.localBroadcast.registerReceiver(mReceiver,
            IntentFilter().apply { addAction(MainActivity.ACTION_OPTION_ITEM_SELECTED_ID) })

        if (CompatSysTtsConfig.migrationConfig()) requireContext().longToast("旧版配置迁移成功，原文件已删除")
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

    /* 检查格式 如不同则显示对话框 */
    private fun checkFormatAndShowDialog() {
        SysTtsConfig.apply {
            if (isMultiVoiceEnabled && !isInAppPlayAudio && !vm.checkMultiVoiceFormat())
                formatWarnDialog.show()
        }
    }

    private fun notifyTtsUpdate(isUpdate: Boolean = true) {
        if (isUpdate) SystemTtsService.notifyUpdateConfig()
    }

    private fun showImportConfig() {
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

    private fun showExportConfig() {
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

    private fun startHttpTtsEditActivity() {
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
                        seekBar: ConvenientSeekbar, progress: Int, fromUser: Boolean
                    ) {
                        if (progress <= 0) seekBar.progress = 1
                        tvRateValue.text = "${(seekBar.progress * 0.1).toFloat()}"
                    }
                }
            seekbarSpeed.progress = (SysTtsConfig.inAppPlaySpeed * 10).toInt()

            seekbarPitch.onSeekBarChangeListener =
                object : ConvenientSeekbar.OnSeekBarChangeListener {
                    override fun onProgressChanged(
                        seekBar: ConvenientSeekbar, progress: Int, fromUser: Boolean
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

        AlertDialog.Builder(requireContext()).setView(view).setOnDismissListener {
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
            .setView(numPicker).setPositiveButton(android.R.string.ok) { _, _ ->
                SysTtsConfig.requestTimeout = numPicker.value * 1000 //转为毫秒
                SystemTtsService.notifyUpdateConfig()
            }.setNegativeButton(R.string.reset) { _, _ ->
                SysTtsConfig.requestTimeout = 5000
            }.setFadeAnim().show()
    }

    private fun showSetMinDialogueLengthDialog() {
        val numList = arrayListOf("不限制")
        for (i in 1..10) numList.add("对话字数 ≥ $i")

        val picker = NumberPicker(requireContext()).apply {
            maxValue = numList.size - 1
            displayedValues = numList.toTypedArray()
            value = SysTtsConfig.minDialogueLength
        }
        AlertDialog.Builder(requireContext()).setTitle("对话文本最小匹配汉字数")
            .setMessage(R.string.set_dialogue_min_match_count_msg).setView(picker)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                SysTtsConfig.minDialogueLength = picker.value
                SystemTtsService.notifyUpdateConfig()
            }.setNegativeButton(R.string.reset) { _, _ ->
                SysTtsConfig.minDialogueLength = 0
                SystemTtsService.notifyUpdateConfig()
            }.setFadeAnim().show()
    }

    private fun onOptionsItemSelected(itemId: Int) {
        when (itemId) {
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


    class GroupPageAdapter(parent: Fragment, val list: List<Fragment>) :
        FragmentStateAdapter(parent) {
        override fun getItemCount() = list.size

        override fun createFragment(position: Int): Fragment {
            return list[position]
        }
    }

}