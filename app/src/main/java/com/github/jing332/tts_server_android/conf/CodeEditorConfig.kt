package com.github.jing332.tts_server_android.conf

import com.funny.data_saver.core.DataSaverConverter.registerTypeConverters
import com.funny.data_saver.core.DataSaverPreferences
import com.funny.data_saver.core.mutableDataSaverStateOf
import com.github.jing332.tts_server_android.app
import com.github.jing332.tts_server_android.constant.CodeEditorTheme

object CodeEditorConfig {
    private val pref = DataSaverPreferences(app.getSharedPreferences("code_editor", 0))

    init {
        registerTypeConverters(
            save = { it.id },
            restore = { value ->
                CodeEditorTheme.values().find { it.id == value } ?: CodeEditorTheme.AUTO
            }
        )
    }

    val theme = mutableDataSaverStateOf(pref, "codeEditorTheme", CodeEditorTheme.AUTO)

    val isWordWrapEnabled = mutableDataSaverStateOf(pref, "isWordWrapEnabled", false)
    val isRemoteSyncEnabled = mutableDataSaverStateOf(pref, "isRemoteSyncEnabled", false)
    val remoteSyncPort = mutableDataSaverStateOf(pref, "remoteSyncPort", 4566)
}