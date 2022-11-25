package com.github.jing332.tts_server_android.ui.fragment

import android.util.Log
import androidx.lifecycle.ViewModel
import com.github.jing332.tts_server_android.App
import com.github.jing332.tts_server_android.constant.ReadAloudTarget
import com.github.jing332.tts_server_android.data.CompatSysTtsConfig
import com.github.jing332.tts_server_android.data.appDb
import com.github.jing332.tts_server_android.data.entities.SysTts
import com.github.jing332.tts_server_android.help.SysTtsConfig
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import tts_server_lib.Tts_server_lib

class TtsConfigViewModel : ViewModel() {
    companion object {
        const val TAG = "TtsConfigFragmentViewModel"
    }

    /**
     *  兼容旧的配置文件
     *  return 旧配置文件是否删除成功（如果有）
     */
    fun compatOldConfig(): Boolean {
        val compatConfig = CompatSysTtsConfig.read()
        compatConfig?.apply {
            list.forEach {
                appDb.sysTtsDao.insert(
                    SysTts(
                        readAloudTarget = it.readAloudTarget,
                        tts = it.voiceProperty,
                        displayName = it.uiData.displayName,
                        isEnabled = it.isEnabled
                    )
                )
            }
            SysTtsConfig.isMultiVoiceEnabled = isMultiVoice
            SysTtsConfig.isSplitEnabled = isSplitSentences
            SysTtsConfig.requestTimeout = timeout

            return CompatSysTtsConfig.deleteConfigFile()
        }
        return false
    }

    fun onCheckBoxChanged(list: List<SysTts>, position: Int, checked: Boolean): Boolean {
        //检测多语音是否开启
        if (checked && !SysTtsConfig.isMultiVoiceEnabled) {
            val target =
                list.getOrNull(position)?.readAloudTarget ?: ReadAloudTarget.DEFAULT
            if (target == ReadAloudTarget.ASIDE || target == ReadAloudTarget.DIALOGUE) {
                return false
            }
        }

        list[position].let { data ->
            if (checked) { // 确保同类型只可单选
                list.forEach {
                    if (it.readAloudTarget == data.readAloudTarget) {
                        appDb.sysTtsDao.update(it.copy(isEnabled = false))
                        Log.e("TAG", "单选排斥")
                    }
                }
            }

            appDb.sysTtsDao.update(data.copy(isEnabled = checked))
        }
        return true
    }

    /**
     * 检查多语音音频格式是否一致
     * @return 是否相等
     */
    fun checkMultiVoiceFormat(): Boolean {
        val aside = appDb.sysTtsDao.getByReadAloudTarget(ReadAloudTarget.ASIDE)
        val dialogue = appDb.sysTtsDao.getByReadAloudTarget(ReadAloudTarget.DIALOGUE)
        if (aside == null || dialogue == null) {
            return true
        } else if (aside.isEnabled && dialogue.isEnabled) {
            return aside.tts?.audioFormat?.sampleRate == dialogue.tts?.audioFormat?.sampleRate
//            return MsTtsFormatManger.isFormatSampleEqual(
//                aside.format, dialogue.format
//            )
        }
        return true
    }


    /* 按API排序 */
    fun sortListByApi() {
    }

    /* 按朗读目标排序 */
    fun sortListByRaTarget() {
    }

    /* 按显示名称排序 */
    fun sortListByDisplayName() {
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
            App.jsonBuilder.encodeToString(appDb.sysTtsDao.all)
        } catch (e: Exception) {
            "导出失败：${e.message}"
        }
    }

    /* 从URL导入配置 */
    fun importConfigByUrl(url: String): String? {
        val jsonStr = try {
            Tts_server_lib.httpGet(url, "")
        } catch (e: Exception) {
            return e.message
        }
        return importConfig(jsonStr.decodeToString())
    }

    /* 从json导入配置 */
    fun importConfig(json: String): String? {
        try {
            val list = App.jsonBuilder.decodeFromString<List<SysTts>>(json)
            list.forEach {
                appDb.sysTtsDao.insert(it)
            }
        } catch (e: Exception) {
            return e.message
        }
        return null
    }


}