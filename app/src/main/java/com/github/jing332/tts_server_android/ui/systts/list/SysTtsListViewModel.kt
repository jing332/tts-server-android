package com.github.jing332.tts_server_android.ui.systts.list

import androidx.lifecycle.ViewModel
import com.drake.net.Net
import com.github.jing332.tts_server_android.App
import com.github.jing332.tts_server_android.data.appDb
import com.github.jing332.tts_server_android.data.entities.systts.CompatSystemTts
import com.github.jing332.tts_server_android.data.entities.systts.GroupWithTtsItem
import com.github.jing332.tts_server_android.data.entities.systts.SystemTts
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import tts_server_lib.Tts_server_lib

class SysTtsListViewModel : ViewModel() {
    companion object {
        const val TAG = "TtsConfigFragmentViewModel"
    }

    /**
     * 检查多语音采样率是否相等
     */
    fun checkMultiVoiceFormat(list: List<SystemTts>): Boolean {
        list.filter { it.isEnabled }.ifEmpty { return true }.let { enabledList ->
            val sampleRate = enabledList[0].tts.audioFormat.sampleRate
            val size = enabledList.filter { it.tts.audioFormat.sampleRate == sampleRate }.size
            return enabledList.size == size
        }
    }

    /* 上传配置到URL */
    fun uploadConfigToUrl(json: String): Result<String> {
        return try {
            val url = Tts_server_lib.uploadConfig(json)

            Result.success(url)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /* 导出配置 */
    fun exportConfig(): String {
        return try {
            App.jsonBuilder.encodeToString(appDb.systemTtsDao.getSysTtsWithGroups())
        } catch (e: Exception) {
            "导出失败：${e.message}"
        }
    }

    /* 从URL导入配置 */
    fun importConfigByUrl(url: String): String? {
        val jsonStr = try {
            Net.get(url).execute<String>()
        } catch (e: Exception) {
            return e.message
        }
        return importConfig(jsonStr)
    }

    /* 从json导入配置 */
    fun importConfig(json: String): String? {
        try {
            if (json.contains("\"group\"")) { // 新数据结构
                App.jsonBuilder.decodeFromString<List<GroupWithTtsItem>>(json).let { list ->
                    appDb.systemTtsDao.insertGroup(*list.map { it.group }.toTypedArray())
                    list.forEach { appDb.systemTtsDao.insertTts(*it.list.toTypedArray()) }
                }
            } else {
                App.jsonBuilder.decodeFromString<List<CompatSystemTts>>(json).let { list ->
                    appDb.systemTtsDao.insertTts(*list.map {
                        SystemTts(displayName = it.displayName, tts = it.tts)
                    }.toTypedArray())
                }
            }
        } catch (e: Exception) {
            return e.message
        }
        return null
    }
}