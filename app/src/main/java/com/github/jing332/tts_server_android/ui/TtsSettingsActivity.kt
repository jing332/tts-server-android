package com.github.jing332.tts_server_android.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.github.jing332.tts_server_android.R
import com.github.jing332.tts_server_android.databinding.ActivityTtsSettingsBinding
import com.github.jing332.tts_server_android.ui.fragment.TtsConfigFragment
import com.github.jing332.tts_server_android.ui.fragment.TtsLogFragment

class TtsSettingsActivity : AppCompatActivity() {
    companion object {
        const val TAG = "TtsSettingsActivity"
    }

    private val binding: ActivityTtsSettingsBinding by lazy { ActivityTtsSettingsBinding.inflate(layoutInflater) }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val configFragment = TtsConfigFragment()
        val logFragment = TtsLogFragment()

        binding.bottomVNavigationView.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.navbar_config -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragmentContainerView, configFragment)
                        .commitNow()
                }
                R.id.navbar_log -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragmentContainerView, logFragment).commitNow()
                }
            }
            true
        }
    }
}