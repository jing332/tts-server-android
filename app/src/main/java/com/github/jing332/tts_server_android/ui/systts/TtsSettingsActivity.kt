package com.github.jing332.tts_server_android.ui.systts

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.text.Html
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.view.menu.MenuBuilder
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.github.jing332.tts_server_android.App
import com.github.jing332.tts_server_android.R
import com.github.jing332.tts_server_android.databinding.ActivityTtsSettingsBinding
import com.github.jing332.tts_server_android.help.SysTtsConfig
import com.github.jing332.tts_server_android.ui.custom.BackActivity
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
            R.id.menu_addConfig -> {
                configFragment.startEditActivity()
            }
            R.id.menu_addHttpTts -> {
                configFragment.startHttpTtsEditActivity()
            }

            /* 排序 */
            R.id.menu_sortByApi -> {
                cfgViewModel.sortListByApi()
            }
            R.id.menu_sortByDisplayName -> {
                cfgViewModel.sortListByDisplayName()
            }
            R.id.menu_sortByRaTarget -> {
                cfgViewModel.sortListByRaTarget()
            }

            /* 设置 */
            R.id.menu_doSplit -> {
                item.isChecked = !item.isChecked
                SysTtsConfig.isSplitEnabled = item.isChecked
                App.localBroadcast.sendBroadcast(Intent(SysTtsConfigFragment.ACTION_ON_CONFIG_CHANGED))
            }
            R.id.menu_isMultiVoice -> {
                item.isChecked = !item.isChecked
                SysTtsConfig.isMultiVoiceEnabled = item.isChecked
                App.localBroadcast.sendBroadcast(Intent(SysTtsConfigFragment.ACTION_ON_CONFIG_CHANGED))
            }
            R.id.menu_isInAppPlayAudio -> {
                if (!item.isChecked) {
                    val tv = TextView(this)
                    tv.setPadding(25, 25, 25, 25)
                    tv.text = Html.fromHtml(getString(R.string.msg_in_app_info_html))
                    AlertDialog.Builder(this).setTitle(R.string.in_app_play_audio)
                        .setView(tv)
                        .setPositiveButton(getString(R.string.turn_on_switch)) { _, _ ->
                            item.isChecked = !item.isChecked
                            SysTtsConfig.isInAppPlayAudio = item.isChecked
                            App.localBroadcast.sendBroadcast(Intent(SysTtsConfigFragment.ACTION_ON_CONFIG_CHANGED))
                        }.setFadeAnim().show()
                } else {
                    item.isChecked = !item.isChecked
                    SysTtsConfig.isInAppPlayAudio = item.isChecked
                    App.localBroadcast.sendBroadcast(Intent(SysTtsConfigFragment.ACTION_ON_CONFIG_CHANGED))
                }
            }

            R.id.menu_setAudioRequestTimeout -> {
                configFragment.setAudioRequestTimeout()
            }
            R.id.menu_setMinDialogueLen -> {
                configFragment.showSetMinDialogueLength()
            }

            R.id.menu_replace_manager -> {
                startActivity(Intent(this, ReplaceManagerActivity::class.java))
            }

            /* 导入导出 */
            R.id.menu_importConfig -> {
                configFragment.showImportConfig()
            }
            R.id.menu_exportConfig -> {
                configFragment.showExportConfig()
            }
        }

        return super.onOptionsItemSelected(item)
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