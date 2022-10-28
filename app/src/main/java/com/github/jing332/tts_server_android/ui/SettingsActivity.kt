package com.github.jing332.tts_server_android.ui

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import com.github.jing332.tts_server_android.R
import com.github.jing332.tts_server_android.ui.custom.BackActivity

class SettingsActivity : BackActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)
        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.settings, SettingsFragment())
                .commit()
        }
    }

    class SettingsFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)
        }
    }
}