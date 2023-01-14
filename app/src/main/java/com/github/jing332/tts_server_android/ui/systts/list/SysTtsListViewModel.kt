package com.github.jing332.tts_server_android.ui.systts.list

import androidx.lifecycle.ViewModel
import com.github.jing332.tts_server_android.App
import com.github.jing332.tts_server_android.data.appDb
import com.github.jing332.tts_server_android.data.entities.systts.SystemTts
import kotlinx.serialization.encodeToString

class SysTtsListViewModel : ViewModel() {
    companion object {
        const val TAG = "TtsConfigFragmentViewModel"
    }

    /**
     * 检查多语音采样率是否相等
     */
    fun checkMultiVoiceFormat(list: List<SystemTts>): Boolean {
        list.filter { it.isEnabled && !it.tts.isDirectPlay() }.ifEmpty { return true }
            .let { enabledList ->
                val sampleRate = enabledList[0].tts.audioFormat.sampleRate
                val size = enabledList.filter { it.tts.audioFormat.sampleRate == sampleRate }.size
                return enabledList.size == size
            }
    }

    /* 导出配置 */
    fun exportConfig(): String {
        return try {
            App.jsonBuilder.encodeToString(appDb.systemTtsDao.sysTtsWithGroups)
        } catch (e: Exception) {
            "导出失败：${e.message}"
        }
    }
}