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
import com.github.jing332.tts_server_android.constant.KeyConst.KEY_DATA
import com.github.jing332.tts_server_android.constant.ReadAloudTarget
import com.github.jing332.tts_server_android.data.CompatSysTtsConfig
import com.github.jing332.tts_server_android.data.appDb
import com.github.jing332.tts_server_android.data.entities.systts.SystemTts
import com.github.jing332.tts_server_android.data.entities.systts.SystemTtsGroup
import com.github.jing332.tts_server_android.databinding.DialogInAppPlaySettingsBinding
import com.github.jing332.tts_server_android.databinding.SysttsListFragmentBinding
import com.github.jing332.tts_server_android.help.SysTtsConfig
import com.github.jing332.tts_server_android.model.tts.HttpTTS
import com.github.jing332.tts_server_android.model.tts.MsTTS
import com.github.jing332.tts_server_android.service.systts.SystemTtsService
import com.github.jing332.tts_server_android.ui.MainActivity
import com.github.jing332.tts_server_android.ui.custom.AppDialogs
import com.github.jing332.tts_server_android.ui.custom.MaterialTextInput
import com.github.jing332.tts_server_android.ui.custom.widget.Seekbar
import com.github.jing332.tts_server_android.ui.systts.edit.HttpTtsEditActivity
import com.github.jing332.tts_server_android.ui.systts.edit.MsTtsEditActivity
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

    /* EditActivity???????????? */
    private val startForResult =
        registerForActivityResult(StartActivityForResult()) { result: ActivityResult ->
            result.data?.getParcelableExtra<SystemTts>(KEY_DATA)?.let {
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

        // ??????????????????
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

        if (CompatSysTtsConfig.migrationConfig()) requireContext().longToast("?????????????????????????????????????????????")
    }

    override fun onDestroy() {
        super.onDestroy()
        App.localBroadcast.unregisterReceiver(mReceiver)
    }

    /* ?????? ???????????? */
    private val formatWarnDialog by lazy {
        MaterialAlertDialogBuilder(requireContext()).setTitle(getString(R.string.warning))
            .setMessage(getString(R.string.systts_sample_rate_different_in_enabled_list))
            .setPositiveButton(android.R.string.ok) { _, _ -> }.create()
    }

    /* ???????????? ??????????????????????????? */
    private fun checkFormatAndShowDialog(list: List<SystemTts>) {
        SysTtsConfig.apply {
            if (isMultiVoiceEnabled && !isInAppPlayAudio && !vm.checkMultiVoiceFormat(list)) runOnUI { formatWarnDialog.show() }
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
                    return (progress * 0.1).toFloat()
                }

                override fun valueToProgress(value: Any): Int {
                    return ((value as Float) * 10).toInt()
                }
            }

            seekbarSpeed.progressConverter = converter
            seekbarPitch.progressConverter = converter

            seekbarSpeed.value = SysTtsConfig.inAppPlaySpeed
            seekbarPitch.value = SysTtsConfig.inAppPlayPitch

            SysTtsConfig.isInAppPlayAudio.let {
                switchOnOff.isChecked = it
                tvTip.isGone = it
                layoutNumEdit.isGone = !it
            }
        }

        MaterialAlertDialogBuilder(requireContext()).setView(view).setOnDismissListener {
            SysTtsConfig.isInAppPlayAudio = inAppBinding.switchOnOff.isChecked
            SysTtsConfig.inAppPlaySpeed = (inAppBinding.seekbarSpeed.progress * 0.1).toFloat()
            SysTtsConfig.inAppPlayPitch = (inAppBinding.seekbarPitch.progress * 0.1).toFloat()
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

        numPicker.value = SysTtsConfig.requestTimeout / 1000 //?????????
        MaterialAlertDialogBuilder(requireContext()).setTitle(R.string.systts_set_request_timeout)
            .setMessage(R.string.systts_set_request_timeout_msg).setView(numPicker)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                SysTtsConfig.requestTimeout = numPicker.value * 1000 //????????????
                SystemTtsService.notifyUpdateConfig()
            }.setNegativeButton(R.string.reset) { _, _ ->
                SysTtsConfig.requestTimeout = 5000
                SystemTtsService.notifyUpdateConfig()
            }.show()
    }

    private fun showSetMinDialogueLengthDialog() {
        val numList = arrayListOf(getString(R.string.unlimited))
        for (i in 1..10) numList.add(" ??? $i")

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

    private fun addHttpTTS(
        @ReadAloudTarget raTarget: Int = ReadAloudTarget.ALL,
        groupId: Long = SystemTtsGroup.DEFAULT_GROUP_ID
    ) {
        startForResult.launch(Intent(
            requireContext(), HttpTtsEditActivity::class.java
        ).apply {
            putExtra(
                KEY_DATA,
                SystemTts(readAloudTarget = raTarget, tts = HttpTTS(), groupId = groupId)
            )
        })
    }

    private fun addMsTTS(
        @ReadAloudTarget raTarget: Int = ReadAloudTarget.ALL,
        groupId: Long = SystemTtsGroup.DEFAULT_GROUP_ID
    ) {
        val intent = Intent(requireContext(), MsTtsEditActivity::class.java).apply {
            putExtra(
                KEY_DATA, SystemTts(readAloudTarget = raTarget, tts = MsTTS(), groupId = groupId)
            )
        }
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
            /* ???????????? */
            R.id.menu_addMsTts -> addMsTTS(binding.viewPager.currentItem)
            R.id.menu_addHttpTts -> addHttpTTS(binding.viewPager.currentItem)
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

            /* ???????????? */
            R.id.menu_importConfig -> importConfig()
            R.id.menu_export_config -> exportConfig()
        }
    }


    inner class MyReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                // MainActivity ???????????????????????????ID
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
