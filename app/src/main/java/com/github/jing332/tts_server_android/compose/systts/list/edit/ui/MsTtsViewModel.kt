package com.github.jing332.tts_server_android.compose.systts.list.edit.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.drake.net.utils.withIO
import com.github.jing332.tts_server_android.constant.MsTtsApiType
import com.github.jing332.tts_server_android.model.GeneralVoiceData
import com.github.jing332.tts_server_android.model.MsTtsEditRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MsTtsViewModel : ViewModel() {
    companion object {
        private val repo: MsTtsEditRepository by lazy { MsTtsEditRepository() }
    }

    private var mAllList: List<GeneralVoiceData> = emptyList()

    var isLoading by mutableStateOf(true)

    val locales = mutableStateListOf<Pair<String, String>>()
    val voices = mutableStateListOf<GeneralVoiceData>()

   suspend fun load(){
       isLoading = true
       mAllList = withIO { repo.voicesByApi(MsTtsApiType.EDGE) }
       isLoading = false
   }

    fun updateLocales() {
        locales.clear()
        locales.addAll(
            mAllList.map { it.locale to it.localeName }.distinctBy { it.first }
                .sortedBy { it.first }
        )
    }

    fun onLocaleChanged(locale: String) {
        voices.clear()
//        if (locale.isEmpty()) return
        voices.addAll(mAllList.filter { it.locale == locale }.sortedBy { it.voiceName })
    }
}