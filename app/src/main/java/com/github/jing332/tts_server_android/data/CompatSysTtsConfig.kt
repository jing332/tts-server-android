package com.github.jing332.tts_server_android.data

import com.github.jing332.tts_server_android.App
import com.github.jing332.tts_server_android.constant.AppConst
import com.github.jing332.tts_server_android.constant.SpeechTarget
import com.github.jing332.tts_server_android.data.entities.systts.SpeechRuleInfo
import com.github.jing332.tts_server_android.data.entities.systts.SystemTts
import com.github.jing332.tts_server_android.help.config.SysTtsConfig
import com.github.jing332.tts_server_android.model.speech.tts.MsTTS
import com.github.jing332.tts_server_android.utils.FileUtils
import kotlinx.serialization.decodeFromString
import java.io.File
import java.io.Serializable

/* 旧配置 已弃用*/
@kotlinx.serialization.Serializable
data class CompatSysTtsConfig(
    var list: ArrayList<CompatSysTtsConfigItem>,
    var isSplitSentences: Boolean = true,
    var isMultiVoice: Boolean = false,
    var isReplace: Boolean = false,
    var timeout: Int = 5000,
    var minDialogueLength: Int = 0
) {
    companion object {
        private val filepath by lazy { "${App.context.filesDir.absolutePath}/system_tts_config.json" }
        fun read(): CompatSysTtsConfig? {
            return try {
                val file = File(filepath)
                if (!FileUtils.exists(file)) return null

                val str = File(filepath).readText()
                AppConst.jsonBuilder.decodeFromString<CompatSysTtsConfig>(str)
            } catch (e: Exception) {
                e.printStackTrace()
                return null
            }
        }

        /**
         * 迁移旧的配置结构
         */
        fun migrationConfig(): Boolean {
            val compatConfig = read()
            compatConfig?.apply {
                list.forEach {
                    appDb.systemTtsDao.insertTts(
                        SystemTts(
                            speechRule = SpeechRuleInfo(target = it.speechTarget),
                            tts = it.voiceProperty,
                            displayName = it.uiData.displayName,
                            isEnabled = it.isEnabled
                        )
                    )
                }
                SysTtsConfig.isMultiVoiceEnabled = isMultiVoice
                SysTtsConfig.isSplitEnabled = isSplitSentences
                SysTtsConfig.requestTimeout = timeout

                return deleteConfigFile()
            }
            return false
        }

        /**
         * return 是否成功
         */
        private fun deleteConfigFile(): Boolean {
            return try {
                File(filepath).delete()
            } catch (e: Exception) {
                return false
            }
        }
    }
}

/* 旧配置 已弃用*/
@kotlinx.serialization.Serializable
data class CompatSysTtsConfigItem(
    var uiData: UiData, /* UI显示数据 */
    var isEnabled: Boolean = false,  /* 是否启用 */
    @SpeechTarget var speechTarget: Int = SpeechTarget.ALL,
    var voiceProperty: MsTTS, /* 朗读属性 */
) : Serializable {
    @kotlinx.serialization.Serializable
    data class UiData(
        var displayName: String,
    ) : Serializable
}