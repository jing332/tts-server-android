package com.github.jing332.tts_server_android.ui

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.github.jing332.tts_server_android.R
import com.github.jing332.tts_server_android.databinding.ActivityTtsSettingsBinding
import com.github.jing332.tts_server_android.ui.fragment.TtsConfigFragment
import com.github.jing332.tts_server_android.ui.fragment.TtsLogFragment
import com.github.jing332.tts_server_android.utils.MyTools

class TtsSettingsActivity : BackActivity() {
    companion object {
        const val TAG = "TtsSettingsActivity"
    }

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
        }

        return super.onOptionsItemSelected(item)
    }

    class FragmentAdapter(fragmentActivity: FragmentActivity) :
        FragmentStateAdapter(fragmentActivity) {
        private val configFragment = TtsConfigFragment()
        private val logFragment = TtsLogFragment()
        private val fragmentList = arrayListOf(configFragment, logFragment)

        override fun getItemCount(): Int {
            return fragmentList.size
        }

        override fun createFragment(position: Int): Fragment {
            return fragmentList[position]
        }
    }
}