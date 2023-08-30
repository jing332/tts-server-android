package com.github.jing332.tts_server_android.conf

import com.funny.data_saver.core.DataSaverConverter
import com.funny.data_saver.core.DataSaverMutableState
import com.funny.data_saver.core.DataSaverPreferences
import com.funny.data_saver.core.mutableDataSaverStateOf
import com.github.jing332.tts_server_android.app
import com.github.jing332.tts_server_android.constant.AppConst
import kotlinx.serialization.encodeToString

object ReplaceRuleConfig {
    private val pref = DataSaverPreferences(app.getSharedPreferences("replace_rule", 0))

    val defaultSymbols: LinkedHashMap<String, String> = listOf(
        "(",
        ")",
        "[",
        "]",
        "|",
        "\\",
        "/",
        "{",
        "}",
        "^",
        "$",
        ".",
        "*",
        "+",
        "?"
    ).associateWith { it } as LinkedHashMap<String, String>

    init {
        DataSaverConverter.registerTypeConverters(
            save = { AppConst.jsonBuilder.encodeToString(it) },
            restore = { value ->
                try {
                    AppConst.jsonBuilder.decodeFromString<Map<String, String>>(value)
                } catch (_: Exception) {
                    defaultSymbols
                }
            }
        )
    }

    val symbols: DataSaverMutableState<LinkedHashMap<String, String>> = mutableDataSaverStateOf(
        pref, key = "symbols",
        defaultSymbols
    )
}