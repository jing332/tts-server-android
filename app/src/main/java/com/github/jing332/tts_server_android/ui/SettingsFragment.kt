package com.github.jing332.tts_server_android.ui

import android.graphics.Typeface
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import com.github.jing332.tts_server_android.BuildConfig
import com.github.jing332.tts_server_android.R
import com.github.jing332.tts_server_android.app
import com.github.jing332.tts_server_android.constant.CodeEditorTheme
import com.github.jing332.tts_server_android.help.config.AppConfig
import com.github.jing332.tts_server_android.help.config.ScriptEditorConfig
import com.github.jing332.tts_server_android.help.config.SysTtsConfig
import com.github.jing332.tts_server_android.util.dp
import com.github.jing332.tts_server_android.util.longToast
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.util.Locale


class SettingsFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        preferenceManager.sharedPreferencesName = AppConfig.kotprefName
        setPreferencesFromResource(R.xml.root_preferences, rootKey)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        findPreference<ListPreference>("maxRetryCount")!!.apply {
            entries = buildList<String> {
                add("不重试")
                addAll((1..50).map { "$it" })
            }.toTypedArray()

            entryValues = (0..50).map { "$it" }.toTypedArray()
            kotlin.runCatching {
                value = SysTtsConfig.maxRetryCount.toString()
            }.onFailure { value = "1" }
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
            kotlin.runCatching {
                value = SysTtsConfig.standbyTriggeredRetryIndex.toString()
            }.onFailure {
                value = "1"
            }
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

        val appLocales = BuildConfig.TRANSLATION_ARRAY.map { Locale.forLanguageTag(it) }
        val entries = appLocales.map { it.getDisplayName(it) }.toMutableList()
            .apply { add(0, getString(R.string.follow_system)) }

        langPre.entries = entries.toTypedArray()
        langPre.entryValues =
            mutableListOf("").apply { addAll(BuildConfig.TRANSLATION_ARRAY) }.toTypedArray()

        val currentLocale = AppCompatDelegate.getApplicationLocales().get(0)
        langPre.setValueIndexNoException(
            if (currentLocale == null) 0
            else {
                val languageTag = currentLocale.toLanguageTag()
                appLocales.indexOfFirst { it.toLanguageTag() == languageTag } + 1
            }
        )
        langPre.summary =
            if (currentLocale == null) getString(R.string.follow_system)
            else currentLocale.getDisplayName(currentLocale)
        langPre.setDialogMessage(R.string.app_language_to_follow_tip_msg)
        langPre.setOnPreferenceChangeListener { _, newValue ->
            val locale = if (newValue == null || newValue.toString().isEmpty()) { //随系统
                longToast(R.string.app_language_to_follow_tip_msg)
                app.updateLocale(Locale.getDefault())
                LocaleListCompat.getEmptyLocaleList()
            } else {
                LocaleListCompat.create(Locale.forLanguageTag(newValue.toString()))
            }
            AppCompatDelegate.setApplicationLocales(locale)

            true
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
            }

            val layout =
                LinearLayout(preference.context).apply {
                    orientation = LinearLayout.VERTICAL
                    tvMsg?.let { addView(it) }
                    addView(listView)
                }

            val dlg = MaterialAlertDialogBuilder(requireContext())
                .setTitle(preference.dialogTitle)
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
}