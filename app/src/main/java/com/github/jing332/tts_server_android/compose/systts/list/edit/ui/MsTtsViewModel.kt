package com.github.jing332.tts_server_android.compose.systts.list.edit.ui

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import com.github.jing332.tts_server_android.constant.MsTtsApiType
import com.github.jing332.tts_server_android.ui.systts.edit.microsoft.GeneralVoiceData
import com.github.jing332.tts_server_android.ui.systts.edit.microsoft.MsTtsEditRepository

class MsTtsViewModel : ViewModel() {
    companion object {
        private val repo: MsTtsEditRepository by lazy { MsTtsEditRepository() }
    }

    private var mAllList: List<GeneralVoiceData> = emptyList()

    val locales = mutableStateListOf<Pair<String, String>>()
    val voices = mutableStateListOf<GeneralVoiceData>()

    suspend fun load() {
        mAllList = repo.voicesByApi(MsTtsApiType.EDGE)
    }

    fun updateLocales() {
        locales.clear()
        locales.addAll(
            mAllList.map { it.locale to it.localeName }.distinctBy { it.first }
                .sortedBy { it.first }
        )
    }

    fun updateVoices(locale: String) {
        voices.clear()
        voices.addAll(mAllList.filter { it.locale == locale }.sortedBy { it.voiceName })
    }
}