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
import androidx.core.view.isGone
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.github.jing332.tts_server_android.App
import com.github.jing332.tts_server_android.R
import com.github.jing332.tts_server_android.data.CompatSysTtsConfig
import com.github.jing332.tts_server_android.data.appDb
import com.github.jing332.tts_server_android.data.entities.systts.SystemTts
import com.github.jing332.tts_server_android.data.entities.systts.SystemTtsGroup
import com.github.jing332.tts_server_android.databinding.DialogInAppPlaySettingsBinding
import com.github.jing332.tts_server_android.databinding.SysttsListFragmentBinding
import com.github.jing332.tts_server_android.help.SysTtsConfig
import com.github.jing332.tts_server_android.model.tts.*
import com.github.jing332.tts_server_android.service.systts.SystemTtsService
import com.github.jing332.tts_server_android.ui.MainActivity
import com.github.jing332.tts_server_android.ui.custom.AppDialogs
import com.github.jing332.tts_server_android.ui.custom.MaterialTextInput
import com.github.jing332.tts_server_android.ui.custom.widget.Seekbar
import com.github.jing332.tts_server_android.ui.systts.edit.BaseTtsEditActivity
import com.github.jing332.tts_server_android.ui.systts.edit.http.HttpTtsEditActivity
import com.github.jing332.tts_server_android.ui.systts.edit.local.LocalTtsEditActivity
import com.github.jing332.tts_server_android.ui.systts.edit.microsoft.MsTtsEditActivity
import com.github.jing332.tts_server_android.ui.systts.list.import1.ConfigImportActivity
import com.github.jing332.tts_server_android.ui.systts.replace.ReplaceManagerActivity
import com.github.jing332.tts_server_android.util.*
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.coroutines.flow.conflate


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
    private lateinit var tabLayout: TabLayout

    private val mReceiver: MyReceiver by lazy { MyReceiver() }

    /* EditActivity的返回值 */
    private val startForResult =
        registerForActivityResult(StartActivityForResult()) { result: ActivityResult ->
            result.data?.getParcelableExtra<SystemTts>(BaseTtsEditActivity.KEY_DATA)?.let {
                appDb.systemTtsDao.insertTts(it)
                notifyTtsUpdate(it.isEnabled)
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        tabLayout = requireActivity().findViewById(R.id.tabLayout)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (savedInstanceState != null) return

        val fragmentList = listOf(
            SysTtsListCustomGroupPageFragment(), SysTtsListSimpleGroupPageFragment.newInstance()
        )
        vpAdapter = GroupPageAdapter(this, fragmentList)
        binding.viewPager.isSaveEnabled = false
        binding.viewPager.adapter = vpAdapter
        binding.viewPager.reduceDragSensitivity(8)

        val tabTitles = listOf(
            getString(R.string.group),
            getString(R.string.all),
        )

        TabLayoutMediator(tabLayout, binding.viewPager) { tab, pos ->
            tab.text = tabTitles[pos]
        }.attach()

        lifecycleScope.runOnIO {
            appDb.systemTtsDao.flowAllTts.conflate().collect {
                checkFormatAndShowDialog(it)
            }
        }

        // 插入默认分组
        if (appDb.systemTtsDao.getGroupById(SystemTtsGroup.DEFAULT_GROUP_ID) == null) {
            appDb.systemTtsDao.insertGroup(
                SystemTtsGroup(
                    id = SystemTtsGroup.DEFAULT_GROUP_ID, name = getString(R.string.default_group)
                )
            )
        }
    }

    override fun onResume() {
        super.onResume()
        tabLayout.visibility = View.VISIBLE
    }

    override fun onPause() {
        super.onPause()
        tabLayout.visibility = View.INVISIBLE
    }

    @SuppressLint("RestrictedApi", "SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState != null) return

        App.localBroadcast.registerReceiver(mReceiver,
            IntentFilter(MainActivity.ACTION_OPTION_ITEM_SELECTED_ID).apply {
                addAction(SystemTtsService.ACTION_REQUEST_UPDATE_CONFIG)
            })

        if (CompatSysTtsConfig.migrationConfig()) requireContext().longToast("旧版配置迁移成功，原文件已删除")
    }

    override fun onDestroy() {
        super.onDestroy()
        App.localBroadcast.unregisterReceiver(mReceiver)
    }

    /* 警告 格式不同 */
    private val formatWarnDialog by lazy {
        MaterialAlertDialogBuilder(requireContext()).setTitle(getString(R.string.warning))
            .setMessage(getString(R.string.systts_sample_rate_different_in_enabled_list))
            .setPositiveButton(android.R.string.ok) { _, _ -> }.create()
    }

    /* 检查格式 如不同则显示对话框 */
    private fun checkFormatAndShowDialog(list: List<SystemTts>) {
        SysTtsConfig.apply {
            if (isMultiVoiceEnabled && !isInAppPlayAudio && !vm.checkMultiVoiceFormat(list))
                runOnUI { formatWarnDialog.show() }
        }
    }

    private fun notifyTtsUpdate(isUpdate: Boolean = true) {
        if (isUpdate) SystemTtsService.notifyUpdateConfig()
    }

    private fun importConfig() {
        startActivity(Intent(requireContext(), ConfigImportActivity::class.java))
    }

    private fun exportConfig() {
        AppDialogs.displayExportDialog(requireContext(), lifecycleScope, vm.exportConfig())
    }

    @Suppress("DEPRECATION")
    private fun showInAppSettingsDialog() {
        val view = FrameLayout(requireContext())
        val inAppBinding = DialogInAppPlaySettingsBinding.inflate(layoutInflater, view, true)
        inAppBinding.apply {
            tvTip.text = Html.fromHtml(getString(R.string.systts_in_app_play_info_html))
            switchOnOff.setOnCheckedChangeListener { _, isChecked ->
                layoutNumEdit.isGone = !isChecked
                tvTip.isGone = isChecked
            }

            val converter = object : Seekbar.ProgressConverter {
                override fun progressToValue(progress: Int): Any {
                    return (progress * 0.01).toFloat()
                }

                override fun valueToProgress(value: Any): Int {
                    return ((value as Float) * 100).toInt()
                }
            }

            seekRate.progressConverter = converter
            seekPitch.progressConverter = converter

            seekRate.value = SysTtsConfig.inAppPlaySpeed
            seekPitch.value = SysTtsConfig.inAppPlayPitch

            SysTtsConfig.isInAppPlayAudio.let {
                switchOnOff.isChecked = it
                tvTip.isGone = it
                layoutNumEdit.isGone = !it
            }
        }

        MaterialAlertDialogBuilder(requireContext()).setView(view).setOnDismissListener {
            SysTtsConfig.isInAppPlayAudio = inAppBinding.switchOnOff.isChecked
            SysTtsConfig.inAppPlaySpeed = (inAppBinding.seekRate.progress * 0.1).toFloat()
            SysTtsConfig.inAppPlayPitch = (inAppBinding.seekPitch.progress * 0.1).toFloat()
            SystemTtsService.notifyUpdateConfig()
        }.show()
    }

    @SuppressLint("SetTextI18n")
    private fun showSetAudioRequestTimeoutDialog() {
        val numPicker = NumberPicker(requireContext())
        numPicker.maxValue = 30
        numPicker.minValue = 2
        numPicker.value = 5
        val displayList = ArrayList<String>()
        for (i in 2..30) {
            displayList.add("${i}s")
        }
        numPicker.displayedValues = displayList.toList().toTypedArray()

        numPicker.value = SysTtsConfig.requestTimeout / 1000 //转为秒
        MaterialAlertDialogBuilder(requireContext()).setTitle(R.string.systts_set_request_timeout)
            .setMessage(R.string.systts_set_request_timeout_msg).setView(numPicker)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                SysTtsConfig.requestTimeout = numPicker.value * 1000 //转为毫秒
                SystemTtsService.notifyUpdateConfig()
            }.setNegativeButton(R.string.reset) { _, _ ->
                SysTtsConfig.requestTimeout = 5000
                SystemTtsService.notifyUpdateConfig()
            }.show()
    }

    private fun showSetMinDialogueLengthDialog() {
        val numList = arrayListOf(getString(R.string.unlimited))
        for (i in 1..10) numList.add(" ≥ $i")

        val picker = NumberPicker(requireContext()).apply {
            maxValue = numList.size - 1
            displayedValues = numList.toTypedArray()
            value = SysTtsConfig.minDialogueLength
        }
        MaterialAlertDialogBuilder(requireContext()).setTitle(R.string.systts_set_dialogue_min_match_word_count)
            .setMessage(R.string.systts_set_dialogue_min_info).setView(picker)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                SysTtsConfig.minDialogueLength = picker.value
                SystemTtsService.notifyUpdateConfig()
            }.setNegativeButton(R.string.reset) { _, _ ->
                SysTtsConfig.minDialogueLength = 0
                SystemTtsService.notifyUpdateConfig()
            }.show()
    }

    private fun addTtsConfig(cls: Class<*>) {
        val intent = Intent(requireContext(), cls)
        startForResult.launch(intent)
    }

    private fun addGroup() {
        val et = MaterialTextInput(requireContext())
        et.inputLayout.setHint(R.string.name)
        MaterialAlertDialogBuilder(requireContext()).setTitle(R.string.add_group).setView(et)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                appDb.systemTtsDao.insertGroup(
                    SystemTtsGroup(name = et.inputEdit.text.toString()
                        .ifEmpty { getString(R.string.unnamed) })
                )
            }.show()
    }

    private fun onOptionsItemSelected(itemId: Int) {
        when (itemId) {
            /* 添加配置 */
            R.id.menu_add_ms_tts -> addTtsConfig(MsTtsEditActivity::class.java)
            R.id.menu_add_local_tts -> addTtsConfig(LocalTtsEditActivity::class.java)
            R.id.menu_add_http_tts -> addTtsConfig(HttpTtsEditActivity::class.java)
            R.id.menu_addGroup -> addGroup()

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
            R.id.menu_voiceMultiple -> {
                SysTtsConfig.isVoiceMultipleEnabled = !SysTtsConfig.isVoiceMultipleEnabled
                SystemTtsService.notifyUpdateConfig()
                if (SysTtsConfig.isVoiceMultipleEnabled) longToast(R.string.systts_voice_multiple_hint)
            }
            R.id.menu_groupMultiple -> {
                SysTtsConfig.isGroupMultipleEnabled = !SysTtsConfig.isGroupMultipleEnabled
                if (SysTtsConfig.isGroupMultipleEnabled) longToast(getString(R.string.systts_groups_multiple_hint))
            }

            R.id.menu_replace_manager -> startActivity(
                Intent(requireContext(), ReplaceManagerActivity::class.java)
            )

            /* 导入导出 */
            R.id.menu_importConfig -> importConfig()
            R.id.menu_export_config -> exportConfig()
        }
    }

    inner class MyReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                // MainActivity 的点击右上角菜单的ID
                MainActivity.ACTION_OPTION_ITEM_SELECTED_ID -> {
                    val itemId = intent.getIntExtra(MainActivity.KEY_MENU_ITEM_ID, -1)
                    onOptionsItemSelected(itemId)
                }
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
