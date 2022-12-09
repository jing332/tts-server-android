package com.github.jing332.tts_server_android.ui

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import com.github.jing332.tts_server_android.R
import com.github.jing332.tts_server_android.help.AppConfig

class SettingsFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        preferenceManager.sharedPreferencesName = AppConfig.kotprefName

        setPreferencesFromResource(R.xml.root_preferences, rootKey)
    }
}