package com.github.jing332.tts_server_android.ui.systts.list

import androidx.lifecycle.ViewModel
import com.github.jing332.tts_server_android.App
import com.github.jing332.tts_server_android.constant.SpeechTarget
import com.github.jing332.tts_server_android.data.appDb
import com.github.jing332.tts_server_android.data.entities.systts.SystemTts
import kotlinx.serialization.encodeToString

class SysTtsListViewModel : ViewModel() {
    companion object {
        const val TAG = "TtsConfigFragmentViewModel"
    }

    /* 导出配置 */
    fun exportConfig(): String {
        return try {
            App.jsonBuilder.encodeToString(appDb.systemTtsDao.getSysTtsWithGroups())
        } catch (e: Exception) {
            "导出失败：${e.message}"
        }
    }
}