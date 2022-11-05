package com.github.jing332.tts_server_android.service.systts.help

import com.github.jing332.tts_server_android.constant.TtsApiType
import com.github.jing332.tts_server_android.data.Prosody
import com.github.jing332.tts_server_android.data.VoiceProperty
import com.github.jing332.tts_server_android.service.systts.help.ssml.EdgeSSML
import kotlinx.coroutines.delay
import tts_server_lib.AzureApi
import tts_server_lib.CreationApi
import tts_server_lib.EdgeApi

/* 系统TTS Go库的包装 */
class SysTtsLib {
    companion object {
        const val TAG = "SysTtsLib"
    }

    private val mEdgeApi: EdgeApi by lazy { EdgeApi() }
    private val mAzureApi: AzureApi by lazy { AzureApi() }
    private val mCreationApi: CreationApi by lazy { CreationApi() }

    /**
     * 获取多Voice的音频
     * @param text 整段文本
     * @param asideProperty 旁白朗读属性
     * @param dialogueProperty 对话朗读属性
     * @param format 音频格式
     * @param onStart 开始请前的回调
     * @param onAudio 获取到音频
     * @param onError 请求音频错误
     */
    suspend fun getAudioForMultiVoice(
        text: String,
        asideProperty: VoiceProperty,
        dialogueProperty: VoiceProperty,
        format: String,
        onStart: (ssml: String) -> Unit,
        onAudio: (text: String, ByteArray?) -> Unit,
        onError: (msg: String, count: Int) -> Boolean
    ): ByteArray? {
        val map = EdgeSSML.genSsmlForMultiVoice(text, asideProperty, dialogueProperty)
        map.forEach {
            val pro = it.voiceProperty
            for (i in 1..100) // 结束循环需5分钟
                try {
                    val audio = when (pro.api) {
                        TtsApiType.EDGE -> {
                            onStart.invoke(it.ssml)
                            mEdgeApi.getEdgeAudioBySsml(it.ssml, format)
                        }
                        TtsApiType.AZURE -> {
                            onStart.invoke("${it.raText}, $pro")
                            mAzureApi.getAudio(
                                pro.voiceName,
                                it.raText,
                                pro.expressAs?.style,
                                "${pro.expressAs?.styleDegree}%",
                                pro.expressAs?.role,
                                "${pro.prosody.rate}%",
                                "${pro.prosody.pitch}",
                                "${pro.prosody.volume}%",
                                format
                            )
                        }
                        TtsApiType.CREATION -> {
                            onStart.invoke("${it.raText}, $pro")
                            val arg = tts_server_lib.CreationArg()
                            pro.apply {
                                arg.voiceName = voiceName
                                arg.voiceId = voiceId
                                arg.style = expressAs?.style
                                arg.styleDegree = "${expressAs?.styleDegree}%"
                                arg.role = expressAs?.role
                                arg.rate = "${prosody.rate}%"
                                arg.volume = "${prosody.volume}%"
                                arg.format = format
                            }
                            arg.text = it.raText
                            mCreationApi.getCreationAudio(arg)
                        }
                        else -> {
                            mEdgeApi.getEdgeAudioBySsml(it.ssml, format)
                        }
                    }
                    onAudio.invoke(it.raText, audio)
                    break
                } catch (e: Exception) {
                    e.printStackTrace()
                    if (onError.invoke(e.message.toString(), i + 1)) { // 是否重试
                        delay(1000 * 3)
                    } else {
                        break
                    }
                }
        }
        return null
    }

    /* 获取音频 在Go中生成SSML */
    fun getAudio(
        text: String,
        pro: VoiceProperty,
        format: String,
    ): ByteArray? {
//        val data = ttsConfig.selectedItem() ?: SysTtsConfigItem()
        val voice = pro.voiceName
        val rate = Prosody.toPcmString(pro.prosody.rate)
        val volume = Prosody.toPcmString(pro.prosody.volume)
        val pitch = Prosody.toPcmString(pro.prosody.pitch)
        val style = pro.expressAs?.style ?: "general"
        val styleDegree = "${(pro.expressAs?.styleDegree ?: 1).toFloat()}"
        val role = pro.expressAs?.role ?: "default"

        when (pro.api) {
            TtsApiType.EDGE -> {
//                onLog.invoke(
//                    "\n请求音频(Edge): voiceName=${voice}, text=$text, rate=$rate, " +
//                            "pitch=$pitch, volume=${volume}, format=${format}"
//                )
                return mEdgeApi.getEdgeAudio(
                    voice,
                    text,
                    rate,
                    pitch,
                    volume,
                    format
                )
            }
            TtsApiType.AZURE -> {
//                onLog.invoke(
//                    "\n请求音频(Azure): voiceName=${voice}, text=$text, style=${style}" +
//                            ", styleDegree=$styleDegree" +
//                            ", role=${role}, rate=$rate, " +
//                            "pitch=$pitch, volume=${volume}, format=$format"
//                )
                return mAzureApi.getAudio(
                    voice,
                    text,
                    style,
                    styleDegree,
                    role,
                    rate,
                    pitch,
                    volume,
                    format
                )
            }
            TtsApiType.CREATION -> {
                val arg = tts_server_lib.CreationArg()
                arg.text = text
                arg.voiceName = voice
                arg.voiceId = pro.voiceId
                arg.style = style
                arg.styleDegree = styleDegree
                arg.role = role
                arg.rate = rate
                arg.volume = volume
                arg.format = format
//                sendLog(LogLevel.INFO, "\n请求音频: $arg")
                return mCreationApi.getCreationAudio(arg)
            }
        }
        return null
    }

}