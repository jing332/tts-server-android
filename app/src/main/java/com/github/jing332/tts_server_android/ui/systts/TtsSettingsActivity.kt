package com.github.jing332.tts_server_android.ui.systts

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.text.Html
import android.view.Menu
import android.view.MenuItem
import android.widget.FrameLayout
import android.widget.NumberPicker
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.view.menu.MenuBuilder
import androidx.core.view.isGone
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.github.jing332.tts_server_android.R
import com.github.jing332.tts_server_android.databinding.ActivityTtsSettingsBinding
import com.github.jing332.tts_server_android.databinding.DialogInAppPlaySettingsBinding
import com.github.jing332.tts_server_android.help.SysTtsConfig
import com.github.jing332.tts_server_android.service.systts.SystemTtsService
import com.github.jing332.tts_server_android.ui.custom.BackActivity
import com.github.jing332.tts_server_android.ui.custom.widget.ConvenientSeekbar
import com.github.jing332.tts_server_android.ui.fragment.SysTtsConfigFragment
import com.github.jing332.tts_server_android.ui.fragment.SysTtsConfigViewModel
import com.github.jing332.tts_server_android.ui.fragment.SysTtsLogFragment
import com.github.jing332.tts_server_android.ui.systts.replace.ReplaceManagerActivity
import com.github.jing332.tts_server_android.util.MyTools
import com.github.jing332.tts_server_android.util.setFadeAnim


class TtsSettingsActivity : BackActivity() {
    companion object {
        const val TAG = "TtsSettingsActivity"
    }

    private val cfgViewModel: SysTtsConfigViewModel by viewModels()

    private val binding: ActivityTtsSettingsBinding by lazy {
        ActivityTtsSettingsBinding.inflate(
            layoutInflater
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.viewPager.adapter = FragmentAdapter(this)
        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                binding.bottomNavigationView.menu.getItem(position).isChecked = true
            }
        })

        binding.bottomNavigationView.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.navbar_config -> binding.viewPager.setCurrentItem(0, true)
                R.id.navbar_log -> binding.viewPager.setCurrentItem(1, true)
            }
            true
        }
    }

    @SuppressLint("RestrictedApi")
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        if (menu is MenuBuilder) {
            menu.setOptionalIconsVisible(true)
        }
        menuInflater.inflate(R.menu.menu_systts_settings, menu)
        return super.onCreateOptionsMenu(menu)
    }


    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        menu?.apply {
            findItem(R.id.menu_isMultiVoice)?.isChecked = SysTtsConfig.isMultiVoiceEnabled
            findItem(R.id.menu_doSplit)?.isChecked = SysTtsConfig.isSplitEnabled
            findItem(R.id.menu_replace_manager)?.isChecked = SysTtsConfig.isReplaceEnabled
            findItem(R.id.menu_isInAppPlayAudio)?.isChecked = SysTtsConfig.isInAppPlayAudio
        }

        return super.onPrepareOptionsMenu(menu)
    }

    @Suppress("DEPRECATION")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_desktopShortcut -> {
                MyTools.addShortcut(
                    this,
                    getString(R.string.tts_config),
                    "tts_config",
                    R.mipmap.ic_launcher_round,
                    Intent(this, TtsSettingsActivity::class.java)
                )
            }
            /* 添加配置 */
            R.id.menu_addMsTts -> configFragment.startMsTtsEditActivity()
            R.id.menu_addHttpTts -> configFragment.startHttpTtsEditActivity()

            R.id.menu_doSplit -> {
                item.isChecked = !item.isChecked
                SysTtsConfig.isSplitEnabled = item.isChecked
                SystemTtsService.notifyUpdateConfig()
            }
            R.id.menu_isMultiVoice -> {
                item.isChecked = !item.isChecked
                SysTtsConfig.isMultiVoiceEnabled = item.isChecked
                SystemTtsService.notifyUpdateConfig()
            }
            R.id.menu_isInAppPlayAudio -> showInAppSettingsDialog()
            R.id.menu_setAudioRequestTimeout -> showSetAudioRequestTimeoutDialog()
            R.id.menu_setMinDialogueLen -> showSetMinDialogueLengthDialog()

            R.id.menu_replace_manager -> startActivity(
                Intent(this, ReplaceManagerActivity::class.java)
            )

            /* 导入导出 */
            R.id.menu_importConfig -> configFragment.showImportConfig()
            R.id.menu_exportConfig -> configFragment.showExportConfig()
        }

        return super.onOptionsItemSelected(item)
    }

    @Suppress("DEPRECATION")
    private fun showInAppSettingsDialog() {
        val view = FrameLayout(this)
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

        AlertDialog.Builder(this)
            .setView(view).setOnDismissListener {
                SysTtsConfig.isInAppPlayAudio = inAppBinding.switchOnOff.isChecked
                SysTtsConfig.inAppPlaySpeed = (inAppBinding.seekbarSpeed.progress * 0.1).toFloat()
                SysTtsConfig.inAppPlayPitch = (inAppBinding.seekbarPitch.progress * 0.1).toFloat()
                SystemTtsService.notifyUpdateConfig()
            }.setFadeAnim().show()
    }

    @SuppressLint("SetTextI18n")
    private fun showSetAudioRequestTimeoutDialog() {
        val numPicker = NumberPicker(this)
        numPicker.maxValue = 30
        numPicker.minValue = 2
        numPicker.value = 5
        val displayList = ArrayList<String>()
        for (i in 2..30) {
            displayList.add("${i}秒")
        }
        numPicker.displayedValues = displayList.toList().toTypedArray()

        numPicker.value = SysTtsConfig.requestTimeout / 1000 //转为秒
        AlertDialog.Builder(this).setTitle(R.string.set_audio_request_timeout)
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

        val picker = NumberPicker(this).apply {
            maxValue = numList.size - 1
            displayedValues = numList.toTypedArray()
            value = SysTtsConfig.minDialogueLength
        }
        AlertDialog.Builder(this).setTitle("对话文本最小匹配汉字数")
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

    val configFragment = SysTtsConfigFragment()
    val logFragment = SysTtsLogFragment()

    inner class FragmentAdapter(fragmentActivity: FragmentActivity) :
        FragmentStateAdapter(fragmentActivity) {
        private val fragmentList = arrayListOf(configFragment, logFragment)
        override fun getItemCount(): Int {
            return fragmentList.size
        }

        override fun createFragment(position: Int): Fragment {
            return fragmentList[position]
        }
    }
}