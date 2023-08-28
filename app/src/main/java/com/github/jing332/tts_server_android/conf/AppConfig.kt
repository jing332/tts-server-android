package com.github.jing332.tts_server_android.conf

import com.funny.data_saver.core.DataSaverConverter.registerTypeConverters
import com.funny.data_saver.core.DataSaverPreferences
import com.funny.data_saver.core.mutableDataSaverStateOf
import com.github.jing332.tts_server_android.R
import com.github.jing332.tts_server_android.app
import com.github.jing332.tts_server_android.compose.theme.AppTheme
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

object AppConfig {
    @OptIn(ExperimentalSerializationApi::class)
    private val json by lazy {
        Json {
            ignoreUnknownKeys = true
            explicitNulls = false
            allowStructuredMapKeys = true
        }
    }

    init {
        registerTypeConverters<List<Pair<String, String>>>(
            save = {
                json.encodeToString(it)
            },
            restore = {
                val list: List<Pair<String, String>> = try {
                    json.decodeFromString(it)
                } catch (_: Exception) {
                    emptyList()
                }
                list
            }
        )

        registerTypeConverters(
            save = { it.id },
            restore = { value ->
                AppTheme.values().find { it.id == value } ?: AppTheme.DEFAULT
            }
        )
    }

    private val dataSaverPref = DataSaverPreferences(app.getSharedPreferences("app", 0))

    val theme = mutableDataSaverStateOf(
        dataSaverInterface = dataSaverPref,
        key = "theme",
        initialValue = AppTheme.DEFAULT
    )

    val limitTagLength = mutableDataSaverStateOf(
        dataSaverInterface = dataSaverPref,
        key = "limitTagLength",
        initialValue = 0
    )

    val limitNameLength = mutableDataSaverStateOf(
        dataSaverInterface = dataSaverPref,
        key = "limitNameLength",
        initialValue = 0
    )

    val isSwapListenAndEditButton = mutableDataSaverStateOf(
        dataSaverInterface = dataSaverPref,
        key = "isSwapListenAndEditButton",
        initialValue = false
    )

    val isAutoCheckUpdateEnabled = mutableDataSaverStateOf(
        dataSaverInterface = dataSaverPref,
        key = "isAutoCheckUpdateEnabled",
        initialValue = true
    )

    val isEdgeDnsEnabled = mutableDataSaverStateOf(
        dataSaverInterface = dataSaverPref,
        key = "isEdgeDnsEnabled",
        initialValue = true
    )

    val testSampleText = mutableDataSaverStateOf(
        dataSaverInterface = dataSaverPref,
        key = "testSampleText",
        initialValue = app.getString(R.string.systts_sample_test_text)
    )

    val fragmentIndex = mutableDataSaverStateOf(
        dataSaverInterface = dataSaverPref,
        key = "fragmentIndex",
        initialValue = 0
    )

    val filePickerMode = mutableDataSaverStateOf(
        dataSaverInterface = dataSaverPref,
        key = "filePickerMode",
        initialValue = 0
    )

    val spinnerMaxDropDownCount = mutableDataSaverStateOf(
        dataSaverInterface = dataSaverPref,
        key = "spinnerMaxDropDownCount",
        initialValue = 20
    )

    val lastReadHelpDocumentVersion = mutableDataSaverStateOf(
        dataSaverInterface = dataSaverPref,
        key = "lastReadHelpDocumentVersion",
        initialValue = 0
    )

}