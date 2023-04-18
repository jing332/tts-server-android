package com.github.jing332.tts_server_android.ui.systts.list

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
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
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.github.jing332.tts_server_android.R
import com.github.jing332.tts_server_android.constant.AppConst
import com.github.jing332.tts_server_android.constant.KeyConst
import com.github.jing332.tts_server_android.data.CompatSysTtsConfig
import com.github.jing332.tts_server_android.data.appDb
import com.github.jing332.tts_server_android.data.entities.AbstractListGroup.Companion.DEFAULT_GROUP_ID
import com.github.jing332.tts_server_android.data.entities.systts.SystemTts
import com.github.jing332.tts_server_android.data.entities.systts.SystemTtsGroup
import com.github.jing332.tts_server_android.databinding.SysttsBgmSettingsBinding
import com.github.jing332.tts_server_android.databinding.SysttsBuiltinPlayerSettingsBinding
import com.github.jing332.tts_server_android.databinding.SysttsListFragmentBinding
import com.github.jing332.tts_server_android.help.config.SysTtsConfig
import com.github.jing332.tts_server_android.model.speech.tts.*
import com.github.jing332.tts_server_android.service.systts.SystemTtsService
import com.github.jing332.tts_server_android.ui.systts.AudioParamsSettingsView
import com.github.jing332.tts_server_android.ui.systts.ConfigExportBottomSheetFragment
import com.github.jing332.tts_server_android.ui.systts.direct_upload.DirectUploadSettingsActivity
import com.github.jing332.tts_server_android.ui.systts.edit.BaseTtsEditActivity
import com.github.jing332.tts_server_android.ui.systts.edit.bgm.BgmTtsEditActivity
import com.github.jing332.tts_server_android.ui.systts.edit.http.HttpTtsEditActivity
import com.github.jing332.tts_server_android.ui.systts.edit.local.LocalTtsEditActivity
import com.github.jing332.tts_server_android.ui.systts.edit.microsoft.MsTtsEditActivity
import com.github.jing332.tts_server_android.ui.systts.edit.plugin.PluginTtsEditActivity
import com.github.jing332.tts_server_android.ui.systts.plugin.PluginManagerActivity
import com.github.jing332.tts_server_android.ui.systts.replace.ReplaceManagerActivity
import com.github.jing332.tts_server_android.ui.systts.speech_rule.SpeechRuleManagerActivity
import com.github.jing332.tts_server_android.ui.view.AppDialogs
import com.github.jing332.tts_server_android.utils.*
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import kotlin.math.min


@Suppress("DEPRECATION")
class SysTtsListFragment : Fragment() {
    companion object {
        const val TAG = "TtsConfigFragment"
        const val ACTION_ADD_TTS = "com.github.jing332.tts_server_android.ui.systts.list.ADD_TTS"
        const val KEY_MENU_ID = "menu_id"
        const val KEY_SYSTEM_TTS_DATA = "system_tts_data"
    }

    private val mReceiver by lazy { MyReceiver() }
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

        AppConst.localBroadcast.registerReceiver(mReceiver, IntentFilter(ACTION_ADD_TTS))

        val fragmentList = listOf(
            ListGroupPageFragment(), ListAllPageFragment.newInstance()
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

        // 插入默认分组
        if (appDb.systemTtsDao.getGroup() == null) {
            appDb.systemTtsDao.insertGroup(
                SystemTtsGroup(id = DEFAULT_GROUP_ID, name = getString(R.string.default_group))
            )
        }

        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(MyMenuProvider(), viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    override fun onDestroy() {
        super.onDestroy()
        AppConst.localBroadcast.unregisterReceiver(mReceiver)
    }

    inner class MyMenuProvider : MenuProvider {
        override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
            menuInflater.inflate(R.menu.systts, menu)
        }

        override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
            if (addTtsFromMenuId(menuItem.itemId)) return true

            return when (menuItem.itemId) {
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

                R.id.menu_audio_params -> {
                    displayAudioParamsSettings()
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

    private fun displayAudioParamsSettings() {
        val view = AudioParamsSettingsView(requireContext())
        val params = AudioParams(
            SysTtsConfig.audioParamsSpeed,
            SysTtsConfig.audioParamsVolume,
            SysTtsConfig.audioParamsPitch
        )
        view.setData(params, isGlobal = true)
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.audio_params_settings)
            .setView(view)
            .setOnDismissListener {
                SysTtsConfig.audioParamsSpeed = params.speed
                SysTtsConfig.audioParamsVolume = params.volume
                SysTtsConfig.audioParamsPitch = params.pitch
                SystemTtsService.notifyUpdateConfig()
            }
            .setPositiveButton(R.string.close, null)
            .setNegativeButton(R.string.reset) { _, _ ->
                params.reset(1f)
                requireContext().toast(R.string.ok_reset)
            }
            .show()
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

            seekVolume.setFloatType(2)
            seekRate.setFloatType(2)
            seekPitch.setFloatType(2)

            seekRate.value = SysTtsConfig.inAppPlaySpeed
            seekVolume.value = SysTtsConfig.inAppPlayVolume
            seekPitch.value = SysTtsConfig.inAppPlayPitch

            btnReset.visibility = View.VISIBLE
            btnReset.clickWithThrottle {
                seekRate.value = 1F
                seekVolume.value = 1F
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
                SysTtsConfig.inAppPlayVolume = inAppBinding.seekVolume.value as Float
                SysTtsConfig.inAppPlayPitch = inAppBinding.seekPitch.value as Float
                SystemTtsService.notifyUpdateConfig()
            }.show()
    }


    private fun addTtsConfig(cls: Class<*>) {
        val intent = Intent(requireContext(), cls)
        startForResult.launch(intent)
    }

    private fun addPluginTts(data: SystemTts? = null) {
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
                        if (data == null)
                            putExtra(
                                BaseTtsEditActivity.KEY_DATA,
                                SystemTts(tts = PluginTTS(pluginId = selected.pluginId))
                            )
                        else
                            putExtra(
                                BaseTtsEditActivity.KEY_DATA,
                                data.copy(
                                    tts = PluginTTS(pluginId = selected.pluginId),
                                    displayName = "",
                                    id = System.currentTimeMillis()
                                )
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

    fun addTtsFromMenuId(menuId: Int, data: SystemTts? = null): Boolean {
        return when (menuId) {
            R.id.menu_add_ms_tts -> {
                addTtsOrCopyAsOther(MsTtsEditActivity::class.java, data)
                true
            }

            R.id.menu_add_local_tts -> {
                addTtsOrCopyAsOther(LocalTtsEditActivity::class.java, data)
                true
            }

            R.id.menu_add_http_tts -> {
                addTtsOrCopyAsOther(HttpTtsEditActivity::class.java, data)
                true
            }

            R.id.menu_add_plugin_tts -> {
                addPluginTts(data)
                true
            }
            else -> false
        }
    }

    private fun addTtsOrCopyAsOther(cls: Class<*>, data: SystemTts? = null) {
        val intent = Intent(context, cls)
        if (data != null) {
            intent.putExtra(
                BaseTtsEditActivity.KEY_DATA,
                data.clone<SystemTts>()!!
                    .copy(id = System.currentTimeMillis(), displayName = "", isEnabled = false)
            )
        }
        startForResult.launch(intent)
    }

    class GroupPageAdapter(parent: Fragment, val list: List<Fragment>) :
        FragmentStateAdapter(parent) {
        override fun getItemCount() = list.size

        override fun createFragment(position: Int): Fragment {
            return list[position]
        }
    }

    inner class MyReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == ACTION_ADD_TTS) {
                val menuId = intent.getIntExtra(KEY_MENU_ID, 0)
                val systemTts = intent.getParcelableExtra<SystemTts>(KEY_SYSTEM_TTS_DATA)
                addTtsFromMenuId(menuId, systemTts)
            }
        }
    }

}
