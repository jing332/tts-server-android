package com.github.jing332.tts_server_android.ui.systts.list

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Html
import android.view.*
import android.widget.*
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.core.view.isGone
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.github.jing332.tts_server_android.R
import com.github.jing332.tts_server_android.data.CompatSysTtsConfig
import com.github.jing332.tts_server_android.data.appDb
import com.github.jing332.tts_server_android.data.entities.AbstractListGroup.Companion.DEFAULT_GROUP_ID
import com.github.jing332.tts_server_android.data.entities.systts.SystemTts
import com.github.jing332.tts_server_android.data.entities.systts.SystemTtsGroup
import com.github.jing332.tts_server_android.databinding.SysttsBgmSettingsBinding
import com.github.jing332.tts_server_android.databinding.SysttsBuiltinPlayerSettingsBinding
import com.github.jing332.tts_server_android.databinding.SysttsListFragmentBinding
import com.github.jing332.tts_server_android.help.config.SysTtsConfig
import com.github.jing332.tts_server_android.model.speech.tts.PluginTTS
import com.github.jing332.tts_server_android.model.speech.tts.*
import com.github.jing332.tts_server_android.service.systts.SystemTtsService
import com.github.jing332.tts_server_android.ui.systts.ConfigExportBottomSheetFragment
import com.github.jing332.tts_server_android.ui.systts.direct_upload.DirectUploadSettingsActivity
import com.github.jing332.tts_server_android.ui.systts.edit.BaseTtsEditActivity
import com.github.jing332.tts_server_android.ui.systts.edit.bgm.BgmTtsEditActivity
import com.github.jing332.tts_server_android.ui.systts.edit.http.HttpTtsEditActivity
import com.github.jing332.tts_server_android.ui.systts.edit.local.LocalTtsEditActivity
import com.github.jing332.tts_server_android.ui.systts.edit.microsoft.MsTtsEditActivity
import com.github.jing332.tts_server_android.ui.systts.edit.plugin.PluginTtsEditActivity
import com.github.jing332.tts_server_android.ui.systts.plugin.PluginManagerActivity
import com.github.jing332.tts_server_android.ui.systts.speech_rule.SpeechRuleManagerActivity
import com.github.jing332.tts_server_android.ui.systts.replace.ReplaceManagerActivity
import com.github.jing332.tts_server_android.ui.view.AppDialogs
import com.github.jing332.tts_server_android.util.*
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.coroutines.flow.conflate
import kotlin.math.min


@Suppress("DEPRECATION")
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
            ListGroupPageFragment(), ListPageFragment.newInstance()
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
                checkSampleRate(it)
            }
        }

        // 插入默认分组
        if (appDb.systemTtsDao.getGroup() == null) {
            appDb.systemTtsDao.insertGroup(
                SystemTtsGroup(id = DEFAULT_GROUP_ID, name = getString(R.string.default_group))
            )
        }

        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(MyMenuProvider(), viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    inner class MyMenuProvider : MenuProvider {
        override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
            menuInflater.inflate(R.menu.systts, menu)
        }

        override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
            return when (menuItem.itemId) {
                /* 添加配置 */
                R.id.menu_add_ms_tts -> {
                    addTtsConfig(MsTtsEditActivity::class.java)
                    true
                }

                R.id.menu_add_local_tts -> {
                    addTtsConfig(LocalTtsEditActivity::class.java)
                    true
                }

                R.id.menu_add_http_tts -> {
                    addTtsConfig(HttpTtsEditActivity::class.java)
                    true
                }

                R.id.menu_add_plugin_tts -> {
                    addPluginTts()
                    true
                }

                R.id.menu_add_bgm_tts -> {
                    addTtsConfig(BgmTtsEditActivity::class.java)
                    true
                }

                R.id.menu_add_group -> {
                    addGroup()
                    true
                }

                R.id.menu_builtin_player_settings -> {
                    displayBuiltinPlayerSettings()
                    true
                }

                R.id.menu_bgm_settings -> {
                    displayBgmSettings()
                    true
                }

                R.id.menu_do_split -> {
                    SysTtsConfig.isSplitEnabled = !SysTtsConfig.isSplitEnabled
                    if (SysTtsConfig.isSplitEnabled)
                        MaterialAlertDialogBuilder(requireContext())
                            .setTitle(R.string.tip)
                            .setMessage(R.string.systts_split_text_tip_msg)
                            .setPositiveButton(android.R.string.ok, null)
                            .show()

                    SystemTtsService.notifyUpdateConfig()
                    true
                }

                R.id.menu_switch_multi_voice -> {
                    SysTtsConfig.isMultiVoiceEnabled = !SysTtsConfig.isMultiVoiceEnabled
                    SystemTtsService.notifyUpdateConfig()
                    true
                }

                R.id.menu_read_rule_manager -> {
                    startActivity(Intent(requireContext(), SpeechRuleManagerActivity::class.java))
                    true
                }

                R.id.menu_plugin_manager -> {
                    startActivity(Intent(requireContext(), PluginManagerActivity::class.java))
                    true
                }

                R.id.menu_replace_manager -> {
                    startActivity(Intent(requireContext(), ReplaceManagerActivity::class.java))
                    true
                }

                R.id.menu_direct_upload_settings -> {
                    startActivity(
                        Intent(requireContext(), DirectUploadSettingsActivity::class.java)
                    )
                    true
                }

                /* 导入导出 */
                R.id.menu_importConfig -> {
                    importConfig()
                    true
                }

                R.id.menu_export_config -> {
                    exportConfig()
                    true
                }

                else -> false
            }
        }


        override fun onPrepareMenu(menu: Menu) {
            super.onPrepareMenu(menu)
            SysTtsConfig.apply {
                menu.apply {
                    findItem(R.id.menu_switch_multi_voice)?.isChecked = isMultiVoiceEnabled
                    findItem(R.id.menu_do_split)?.isChecked = isSplitEnabled
                    findItem(R.id.menu_replace_manager)?.isChecked = isReplaceEnabled
                    findItem(R.id.menu_builtin_player_settings)?.isChecked = isInAppPlayAudio
                }
            }
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

        CompatSysTtsConfig.migrationConfig()
    }

    /* 警告 格式不同 */
    private val formatWarnDialog by lazy {
        MaterialAlertDialogBuilder(requireContext()).setTitle(getString(R.string.warning))
            .setMessage(getString(R.string.systts_sample_rate_different_in_enabled_list))
            .setPositiveButton(android.R.string.ok) { _, _ -> }.create()
    }

    /* 检查格式 如不同则显示对话框 */
    private fun checkSampleRate(list: List<SystemTts>) {
        SysTtsConfig.apply {
            if (isMultiVoiceEnabled && !isInAppPlayAudio && !vm.checkMultiVoiceFormat(list))
                runOnUI { formatWarnDialog.show() }
        }
    }

    private fun notifyTtsUpdate(isUpdate: Boolean = true) {
        if (isUpdate) SystemTtsService.notifyUpdateConfig()
    }

    private fun importConfig() {
        val fragment = ImportConfigBottomSheetFragment()
        fragment.show(requireActivity().supportFragmentManager, ImportConfigBottomSheetFragment.TAG)
    }


    private var savedData: ByteArray? = null

    private lateinit var getFileUriToSave: ActivityResultLauncher<String>
    override fun onAttach(context: Context) {
        super.onAttach(context)
        getFileUriToSave = FileUtils.registerResultCreateDocument(
            this@SysTtsListFragment,
            "application/json"
        ) { savedData }
    }

    private fun exportConfig() {
        val fragment = ConfigExportBottomSheetFragment(
            onGetConfig = { vm.exportConfig() },
            onGetName = { "ttsrv-list.json" }
        )
        fragment.show(requireActivity().supportFragmentManager, ConfigExportBottomSheetFragment.TAG)
    }

    private fun displayBgmSettings() {
        val view = FrameLayout(requireContext())
        val viewBinding = SysttsBgmSettingsBinding.inflate(layoutInflater, view, true)
        viewBinding.apply {
            seekVolume.value = min(1000, (SysTtsConfig.bgmVolume * 1000f).toInt())
            switchShuffle.isChecked = SysTtsConfig.isBgmShuffleEnabled
        }
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.bgm_settings)
            .setView(view)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                SysTtsConfig.bgmVolume = (viewBinding.seekVolume.value as Int) / 1000f
                if (SysTtsConfig.bgmVolume < 0) SysTtsConfig.bgmVolume = 1f

                SysTtsConfig.isBgmShuffleEnabled = viewBinding.switchShuffle.isChecked
                notifyTtsUpdate()
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }


    @Suppress("DEPRECATION")
    private fun displayBuiltinPlayerSettings() {
        val view = FrameLayout(requireContext())
        val inAppBinding = SysttsBuiltinPlayerSettingsBinding.inflate(layoutInflater, view, true)
        inAppBinding.apply {
            tvTip.text = Html.fromHtml(getString(R.string.systts_in_app_play_info_html))
            switchOnOff.setOnCheckedChangeListener { _, isChecked ->
                layoutNumEdit.isGone = !isChecked
                tvTip.isGone = isChecked
            }

            seekRate.setFloatType(2)
            seekPitch.setFloatType(2)

            seekRate.value = SysTtsConfig.inAppPlaySpeed
            seekPitch.value = SysTtsConfig.inAppPlayPitch

            btnReset.visibility = View.VISIBLE
            btnReset.clickWithThrottle {
                seekRate.value = 1F
                seekPitch.value = 1F
            }

            SysTtsConfig.isInAppPlayAudio.let {
                switchOnOff.isChecked = it
                tvTip.isGone = it
                layoutNumEdit.isGone = !it
            }
        }

        MaterialAlertDialogBuilder(requireContext())
            .setView(view).setOnDismissListener {
                SysTtsConfig.isInAppPlayAudio = inAppBinding.switchOnOff.isChecked
                SysTtsConfig.inAppPlaySpeed = inAppBinding.seekRate.value as Float
                SysTtsConfig.inAppPlayPitch = inAppBinding.seekPitch.value as Float
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

    private fun addTtsConfig(cls: Class<*>) {
        val intent = Intent(requireContext(), cls)
        startForResult.launch(intent)
    }

    private fun addPluginTts() {
        val plugins = appDb.pluginDao.allEnabled
        val pluginItems = plugins.map { "${it.name} (${it.pluginId})" }.toTypedArray()
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.select_plugin)
            .setItems(pluginItems) { _, which ->
                val selected = plugins[which]
                startForResult.launch(
                    Intent(
                        requireContext(),
                        PluginTtsEditActivity::class.java
                    ).apply {
                        putExtra(
                            BaseTtsEditActivity.KEY_DATA,
                            SystemTts(tts = PluginTTS(pluginId = selected.pluginId))
                        )
                    })
            }.apply {
                if (plugins.isEmpty()) {
                    setMessage(R.string.no_plugins)
                    setPositiveButton(R.string.plugin_manager) { _, _ ->
                        startActivity(Intent(requireContext(), PluginManagerActivity::class.java))
                    }
                }
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun addGroup() {
        AppDialogs.displayInputDialog(
            requireContext(),
            getString(R.string.add_group),
            getString(R.string.name)
        ) {
            appDb.systemTtsDao.insertGroup(
                SystemTtsGroup(name = it.ifEmpty { getString(R.string.unnamed) })
            )
        }
    }

    private fun displayStandbySettings() {
        val ranges = 1..10
        val picker = NumberPicker(requireContext()).apply {
            minValue = 1
            maxValue = ranges.last
            displayedValues = ranges.map { it.toString() }.toTypedArray()
            value = SysTtsConfig.standbyTriggeredRetryIndex
        }

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.systts_sby_conditions_for_use)
            .setMessage(R.string.systts_sby_settings_msg)
            .setView(picker)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                SysTtsConfig.standbyTriggeredRetryIndex = picker.value
            }
            .setNegativeButton(R.string.reset) { _, _ ->
                SysTtsConfig.standbyTriggeredRetryIndex = 1
                toast(R.string.ok_reset)
            }
            .show()
    }

    class GroupPageAdapter(parent: Fragment, val list: List<Fragment>) :
        FragmentStateAdapter(parent) {
        override fun getItemCount() = list.size

        override fun createFragment(position: Int): Fragment {
            return list[position]
        }
    }

}
