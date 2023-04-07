package com.github.jing332.tts_server_android.ui

import android.graphics.Typeface
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.TextView
import androidx.core.view.setPadding
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import com.github.jing332.tts_server_android.AppLocale
import com.github.jing332.tts_server_android.R
import com.github.jing332.tts_server_android.app
import com.github.jing332.tts_server_android.constant.CodeEditorTheme
import com.github.jing332.tts_server_android.help.config.AppConfig
import com.github.jing332.tts_server_android.help.config.ScriptEditorConfig
import com.github.jing332.tts_server_android.help.config.SysTtsConfig
import com.github.jing332.tts_server_android.util.dp
import com.github.jing332.tts_server_android.util.longToast
import com.google.android.material.dialog.MaterialAlertDialogBuilder


class SettingsFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        preferenceManager.sharedPreferencesName = AppConfig.kotprefName
        setPreferencesFromResource(R.xml.root_preferences, rootKey)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        findPreference<ListPreference>("filePickerMode")!!.apply {
            entryValues = (0..2).map { "$it" }.toTypedArray()
            entries = arrayOf(
                getString(R.string.file_picker_mode_prompt),
                getString(R.string.file_picker_mode_system),
                getString(R.string.file_picker_mode_builtin)
            )
            setValue(AppConfig.filePickerMode.toString(), "0")
            summary = entry
            setOnPreferenceChangeListener { _, newValue ->
                AppConfig.filePickerMode = newValue.toString().toInt()
                true
            }
        }

        findPreference<ListPreference>("maxRetryCount")!!.apply {
            entries = buildList<String> {
                add(getString(R.string.no_retries))
                addAll((1..50).map { "$it" })
            }.toTypedArray()

            entryValues = (0..50).map { "$it" }.toTypedArray()
            setValue(SysTtsConfig.maxRetryCount.toString(), "1")
            summary = entry
            setOnPreferenceChangeListener { _, newValue ->
                SysTtsConfig.maxRetryCount = (newValue as String).toInt()
                true
            }
        }

        // 备用配置使用条件
        findPreference<ListPreference>("standbyTriggeredRetryIndex")!!.apply {
            entries = (1..10).map { "$it" }.toTypedArray()
            entryValues = entries
            setValue(SysTtsConfig.standbyTriggeredRetryIndex.toString(), "1")
            summary = entry
            setOnPreferenceChangeListener { _, newValue ->
                SysTtsConfig.standbyTriggeredRetryIndex = (newValue as String).toInt()
                true
            }
        }

        // 请求超时
        findPreference<ListPreference>("requestTimeout")!!.apply {
            entries = (2..30).map { "${it}s" }.toTypedArray()
            entryValues = (2..30).map { "$it" }.toTypedArray()
            setValueIndexNoException((SysTtsConfig.requestTimeout / 1000) - 2)
            summary = entry
            setOnPreferenceChangeListener { _, newValue ->
                SysTtsConfig.requestTimeout = (newValue as String).toInt() * 1000
                true
            }
        }

        // 朗读目标多选开关
        val voiceMultipleSwitch: SwitchPreferenceCompat = findPreference("isVoiceMultiple")!!
        voiceMultipleSwitch.isChecked = SysTtsConfig.isVoiceMultipleEnabled
        voiceMultipleSwitch.setOnPreferenceChangeListener { _, newValue ->
            SysTtsConfig.isVoiceMultipleEnabled = newValue as Boolean
            true
        }

        // 分组多选开关
        val groupMultipleSwitch: SwitchPreferenceCompat = findPreference("groupMultiple")!!
        groupMultipleSwitch.isChecked = SysTtsConfig.isGroupMultipleEnabled
        groupMultipleSwitch.setOnPreferenceChangeListener { _, newValue ->
            SysTtsConfig.isGroupMultipleEnabled = newValue as Boolean
            true
        }

        // 唤醒或开关
        val wakeLockSwitch: SwitchPreferenceCompat = findPreference("isWakeLockEnabled")!!
        wakeLockSwitch.apply {
            wakeLockSwitch.isChecked = SysTtsConfig.isWakeLockEnabled
            setOnPreferenceChangeListener { _, newValue ->
                SysTtsConfig.isWakeLockEnabled = newValue.toString().toBoolean()
                true
            }
        }

        // 前台服务开关
        val foregroundServiceSwitch: SwitchPreferenceCompat =
            findPreference("isForegroundServiceEnabled")!!
        foregroundServiceSwitch.apply {
            isChecked = SysTtsConfig.isForegroundServiceEnabled
            setOnPreferenceChangeListener { _, newValue ->
                SysTtsConfig.isForegroundServiceEnabled = newValue.toString().toBoolean()
                true
            }
        }

        // 代码编辑器主题
        val editorTheme: ListPreference = findPreference("codeEditorTheme")!!
        val themes = linkedMapOf(
            CodeEditorTheme.AUTO to requireContext().getString(R.string.follow_system),
            CodeEditorTheme.QUIET_LIGHT to "Quiet-Light",
            CodeEditorTheme.SOLARIZED_DRAK to "Solarized-Dark",
            CodeEditorTheme.DARCULA to "Dracula",
            CodeEditorTheme.ABYSS to "Abyss"
        )
        editorTheme.entries = themes.values.toTypedArray()
        editorTheme.entryValues = themes.keys.map { it.toString() }.toTypedArray()
        editorTheme.setValueIndexNoException(ScriptEditorConfig.codeEditorTheme)
        editorTheme.summary = themes[ScriptEditorConfig.codeEditorTheme]
        editorTheme.setOnPreferenceChangeListener { _, newValue ->
            ScriptEditorConfig.codeEditorTheme = newValue.toString().toInt()
            editorTheme.setValueIndexNoException(ScriptEditorConfig.codeEditorTheme)
            true
        }

        // 语言
        val langPre: ListPreference = findPreference("language")!!
        langPre.apply {
            entries =
                AppLocale.localeMap.map { it.value.getDisplayName(it.value) }.toMutableList()
                    .apply { add(0, getString(R.string.follow_system)) }.toTypedArray()
            entryValues =
                mutableListOf("").apply { addAll(AppLocale.localeMap.keys) }.toTypedArray()
            setValue(AppLocale.getLocaleCodeFromFile(requireContext()), "")
            summary = entry

            setOnPreferenceChangeListener { _, newValue ->
                AppLocale.saveLocaleCodeToFile(requireContext(), newValue as String)
                AppLocale.updateApplicationLocale(app)
                longToast(R.string.app_languge_update_warn)
                true
            }

        }
    }

    override fun onDisplayPreferenceDialog(preference: Preference) {
        if (preference is ListPreference) {
            val tvMsg =
                if (preference.dialogMessage.isNullOrEmpty()) null
                else TextView(requireContext()).apply {
                    setTypeface(null, Typeface.BOLD)
                    text = preference.dialogMessage
                    textAlignment = TextView.TEXT_ALIGNMENT_CENTER
                    setPadding(/* left = */ 8.dp, /* top = */16.dp,
                        /* right = */8.dp, /* bottom = */8.dp
                    )
                }

            val listView = ListView(preference.context).apply {
                adapter = ArrayAdapter(
                    listView.context,
                    android.R.layout.simple_list_item_single_choice,
                    preference.entries
                )
                divider = null

                choiceMode = ListView.CHOICE_MODE_SINGLE
                val i = preference.findIndexOfValue(preference.value)
                setItemChecked(i, true)
                setSelection(i)
                setPadding(8.dp)
            }

            val layout =
                LinearLayout(preference.context).apply {
                    orientation = LinearLayout.VERTICAL
                    tvMsg?.let { addView(it) }
                    addView(listView)
                }

            val dlg = MaterialAlertDialogBuilder(requireContext())
                .setTitle(preference.dialogTitle)
                .apply { preference.icon?.let { setIcon(it) } }
                .setView(layout)
                .setPositiveButton(android.R.string.cancel) { _, _ -> }
                .show()

            listView.setOnItemClickListener { _, _, position, _ ->
                val value = preference.entryValues[position].toString()
                if (preference.callChangeListener(value)) {
                    preference.setValueIndexNoException(preference.findIndexOfValue(value))
                    preference.summary = preference.entries[position]
                }

                dlg.dismiss()
            }

        } else
            super.onDisplayPreferenceDialog(preference)
    }

    // 不抛异常
    private fun ListPreference.setValueIndexNoException(index: Int): Boolean {
        kotlin.runCatching {
            this.setValueIndex(index)
            return true
        }

        return false
    }

    private fun ListPreference.setValue(value: String, default: String) {
        kotlin.runCatching {
            this.value = value
        }.onFailure {
            this.value = default
        }
    }
}