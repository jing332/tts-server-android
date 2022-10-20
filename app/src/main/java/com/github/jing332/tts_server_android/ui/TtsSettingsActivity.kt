package com.github.jing332.tts_server_android.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.github.jing332.tts_server_android.R
import com.github.jing332.tts_server_android.databinding.ActivityTtsSettingsBinding
import com.github.jing332.tts_server_android.ui.fragment.TtsConfigFragment
import com.github.jing332.tts_server_android.ui.fragment.TtsLogFragment

class TtsSettingsActivity : AppCompatActivity() {
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