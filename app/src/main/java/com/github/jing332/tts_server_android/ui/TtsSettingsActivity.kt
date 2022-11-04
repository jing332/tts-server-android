package com.github.jing332.tts_server_android.ui

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.github.jing332.tts_server_android.R
import com.github.jing332.tts_server_android.databinding.ActivityTtsSettingsBinding
import com.github.jing332.tts_server_android.ui.custom.BackActivity
import com.github.jing332.tts_server_android.ui.fragment.TtsConfigFragment
import com.github.jing332.tts_server_android.ui.fragment.TtsConfigFragmentViewModel
import com.github.jing332.tts_server_android.ui.fragment.TtsLogFragment
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

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_systts_settings, menu)
        return true
    }

    private var menuItemDoSplit: MenuItem? = null
    private var menuItemMultiVoice: MenuItem? = null

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        cfgViewModel.ttsCfg.value?.apply {
            menuItemMultiVoice = menu?.findItem(R.id.menu_isMultiVoice)
            menuItemMultiVoice?.isChecked = isMultiVoice
            menuItemDoSplit = menu?.findItem(R.id.menu_doSplit)
            menuItemDoSplit?.isChecked = isSplitSentences
        }

        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_doSplit -> { /* 二者只能选一 */
                item.isChecked = !item.isChecked
                if (item.isChecked) {
                    menuItemMultiVoice?.isChecked = false
                }
                cfgViewModel.ttsCfg.value?.apply {
                    isSplitSentences = menuItemDoSplit?.isChecked == true
                    isMultiVoice = menuItemMultiVoice?.isChecked ==true
                }?.save()
            }
            R.id.menu_isMultiVoice -> { /* 二者只能选一 */
                item.isChecked = !item.isChecked
                if (item.isChecked) {
                    menuItemDoSplit?.isChecked = false
                }
                cfgViewModel.ttsCfg.value?.apply {
                    isSplitSentences = menuItemDoSplit?.isChecked == true
                    isMultiVoice = menuItemMultiVoice?.isChecked ==true
                }?.save()
            }

            R.id.menu_desktopShortcut -> {
                MyTools.addShortcut(
                    this,
                    getString(R.string.tts_config),
                    "tts_config",
                    R.mipmap.ic_launcher_round,
                    Intent(this, TtsSettingsActivity::class.java)
                )
            }
        }

        return super.onOptionsItemSelected(item)
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