package com.github.jing332.tts_server_android.ui.preference

import android.content.Intent
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
import com.github.jing332.tts_server_android.ui.preference.backup_restore.BackupRestoreActivity
import com.github.jing332.tts_server_android.utils.dp
import com.github.jing332.tts_server_android.utils.longToast
import com.google.android.material.dialog.MaterialAlertDialogBuilder


class SettingsFragment : PreferenceFragmentCompat() {
    companion object {
        private const val TAG = "SettingsFragment"
        const val ACTION_RELOAD = "com.github.jing332.tts_server_android.action.RELOAD"
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        preferenceManager.sharedPreferencesName = AppConfig.kotprefName
        setPreferencesFromResource(R.xml.root_preferences, rootKey)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        findPreference<Preference>("backup_restore")!!.apply {
            setOnPreferenceClickListener {
                startActivity(Intent(requireContext(), BackupRestoreActivity::class.java))
                true
            }
        }

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

        initSwitchPreference("isSkipSilentTextEnabled", SysTtsConfig.isSkipSilentText) {
            SysTtsConfig.isSkipSilentText = it
        }

        initSwitchPreference("isExoDecoderEnabled", SysTtsConfig.isExoDecoderEnabled) {
            SysTtsConfig.isExoDecoderEnabled = it
        }

        initSwitchPreference("isStreamPlayModeEnabled", SysTtsConfig.isStreamPlayModeEnabled) {
            SysTtsConfig.isStreamPlayModeEnabled = it
        }

        findPreference<ListPreference>("maxEmptyAudioRetryCount")!!.apply {
            entries = buildList {
                add(getString(R.string.no_retries))
                addAll((1..10).map { "$it" })
            }.toTypedArray()

            entryValues = (0..10).map { "$it" }.toTypedArray()
            setValue(SysTtsConfig.maxEmptyAudioRetryCount.toString(), "1")
            summary = entry
            setOnPreferenceChangeListener { _, newValue ->
                SysTtsConfig.maxEmptyAudioRetryCount = (newValue as String).toInt()
                true
            }
        }

        findPreference<ListPreference>("maxRetryCount")!!.apply {
            entries = buildList {
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
            entries = (1..30).map { "${it}s" }.toTypedArray()
            entryValues = (1..30).map { "$it" }.toTypedArray()
            setValue((SysTtsConfig.requestTimeout / 1000).toString(), "5")
            summary = entry
            setOnPreferenceChangeListener { _, newValue ->
                SysTtsConfig.requestTimeout = (newValue as String).toInt() * 1000
                true
            }
        }

        initSwitchPreference("isVoiceMultiple", SysTtsConfig.isVoiceMultipleEnabled) {
            SysTtsConfig.isVoiceMultipleEnabled = it
        }

        initSwitchPreference("groupMultiple", SysTtsConfig.isGroupMultipleEnabled) {
            SysTtsConfig.isGroupMultipleEnabled = it
        }

        initSwitchPreference("isWakeLockEnabled", SysTtsConfig.isWakeLockEnabled) {
            SysTtsConfig.isWakeLockEnabled = it
        }

        initSwitchPreference(
            "isForegroundServiceEnabled", SysTtsConfig.isForegroundServiceEnabled
        ) {
            SysTtsConfig.isForegroundServiceEnabled = it
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

        val wordWrapSwitch = findPreference<SwitchPreferenceCompat>("isCodeEditorWordWrapEnabled")!!
        wordWrapSwitch.isChecked = ScriptEditorConfig.isCodeEditorWordWrapEnabled
        wordWrapSwitch.setOnPreferenceChangeListener { _, newValue ->
            ScriptEditorConfig.isCodeEditorWordWrapEnabled = newValue.toString().toBoolean()
            true
        }

        // 语言
        val langPre: ListPreference = findPreference("language")!!
        langPre.apply {
            entries =
                AppLocale.localeMap.map { "${it.value.displayName} - ${it.value.getDisplayName(it.value)}" }
                    .toMutableList()
                    .apply { add(0, getString(R.string.follow_system)) }.toTypedArray()
            entryValues =
                mutableListOf("").apply { addAll(AppLocale.localeMap.keys) }.toTypedArray()
            setValue(AppLocale.getLocaleCodeFromFile(requireContext()), "")
            summary = entry

            setOnPreferenceChangeListener { _, newValue ->
                AppLocale.saveLocaleCodeToFile(requireContext(), newValue as String)
                AppLocale.setLocale(app)
                longToast(R.string.app_language_update_warn)
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

    private fun initSwitchPreference(
        key: String,
        isChecked: Boolean = false,
        valueChange: (Boolean) -> Unit
    ) {
        findPreference<SwitchPreferenceCompat>(key)!!.init(isChecked) { valueChange.invoke(it) }
    }

    private fun SwitchPreferenceCompat.init(
        isChecked: Boolean = false,
        valueChange: (Boolean) -> Unit
    ) {
        this.isChecked = isChecked
        setOnPreferenceChangeListener { _, newValue ->
            valueChange.invoke(newValue.toString().toBoolean())
            true
        }
    }

}