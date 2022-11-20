package com.github.jing332.tts_server_android.ui.systts

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.view.menu.MenuBuilder
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.github.jing332.tts_server_android.App
import com.github.jing332.tts_server_android.R
import com.github.jing332.tts_server_android.databinding.ActivityTtsSettingsBinding
import com.github.jing332.tts_server_android.ui.custom.BackActivity
import com.github.jing332.tts_server_android.ui.fragment.TtsConfigFragment
import com.github.jing332.tts_server_android.ui.fragment.TtsConfigFragmentViewModel
import com.github.jing332.tts_server_android.ui.fragment.TtsLogFragment
import com.github.jing332.tts_server_android.ui.systts.replace.ReplaceManagerActivity
import com.github.jing332.tts_server_android.ui.systts.replace.ReplaceManagerActivity.Companion.KEY_SWITCH
import com.github.jing332.tts_server_android.util.MyTools


class TtsSettingsActivity : BackActivity() {
    companion object {
        const val TAG = "TtsSettingsActivity"
    }

    private val cfgViewModel: TtsConfigFragmentViewModel by viewModels()

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
        cfgViewModel.ttsConfig?.apply {
            menu?.findItem(R.id.menu_isMultiVoice)?.isChecked = isMultiVoice
            menu?.findItem(R.id.menu_doSplit)?.isChecked = isSplitSentences
            menu?.findItem(R.id.menu_replace_manager)?.isChecked = isReplace
        }

        return super.onPrepareOptionsMenu(menu)
    }

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
                cfgViewModel.onMenuIsSplitChanged(item.isChecked)
                App.localBroadcast.sendBroadcast(Intent(TtsConfigFragment.ACTION_ON_CONFIG_CHANGED))
            }
            R.id.menu_isMultiVoice -> {
                item.isChecked = !item.isChecked
                cfgViewModel.onMenuMultiVoiceChanged(item.isChecked)
                App.localBroadcast.sendBroadcast(Intent(TtsConfigFragment.ACTION_ON_CONFIG_CHANGED))
            }
            R.id.menu_setAudioRequestTimeout -> {
                configFragment.setAudioRequestTimeout()
            }
            R.id.menu_setMinDialogueLen -> {
                configFragment.setMinDialogueLength()
            }

            R.id.menu_replace_manager -> {
                val intent = Intent(this, ReplaceManagerActivity::class.java)
                intent.putExtra(KEY_SWITCH, cfgViewModel.ttsConfig?.isReplace)
                startForResult.launch(intent)
            }

            /* 导入导出 */
            R.id.menu_importConfig -> {
                configFragment.importConfig()
            }
            R.id.menu_exportConfig -> {
                configFragment.exportConfig()
            }
        }

        return super.onOptionsItemSelected(item)
    }

    private val startForResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val isEnabled = result.data?.getBooleanExtra(KEY_SWITCH, false) ?: false
                cfgViewModel.onReplaceSwitchChanged(isEnabled)
            }
        }

    val configFragment = TtsConfigFragment()
    val logFragment = TtsLogFragment()

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