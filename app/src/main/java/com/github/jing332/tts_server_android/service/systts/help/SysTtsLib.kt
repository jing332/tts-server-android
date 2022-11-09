package com.github.jing332.tts_server_android.service.systts.help

import com.github.jing332.tts_server_android.constant.TtsApiType
import com.github.jing332.tts_server_android.data.VoiceProperty
import tts_server_lib.*

/* 系统TTS Go库的包装 */
class SysTtsLib {
    companion object {
        const val TAG = "SysTtsLib"

        fun toLibProperty(pro: VoiceProperty): ResultProperty {
            val libPro = tts_server_lib.VoiceProperty()
            libPro.api = pro.api.toLong()
            libPro.voiceName = pro.voiceName
            libPro.voiceId = pro.voiceId
            val libProsody = VoiceProsody()
            libProsody.rate = pro.prosody.rate.toByte()
            libProsody.volume = pro.prosody.volume.toByte()
            libProsody.pitch = pro.prosody.pitch.toByte()
            val libExp = VoiceExpressAs()
            libExp.style = pro.expressAs?.style
            libExp.styleDegree = pro.expressAs?.styleDegree ?: 1F
            libExp.role = pro.expressAs?.role

            return ResultProperty(libPro, libProsody, libExp)
        }

    }

    private val mEdgeApi: EdgeApi by lazy { EdgeApi() }
    private val mAzureApi: AzureApi by lazy { AzureApi() }
    private val mCreationApi: CreationApi by lazy { CreationApi() }

    fun getAudioStream(
        text: String,
        pro: VoiceProperty,
        format: String,
        onRead: (ByteArray) -> Unit
    ): String? {
        val libPro = toLibProperty(pro)
        try {
            mAzureApi.getAudioStream(
                text,
                format,
                libPro.voiceProperty,
                libPro.voiceProsody,
                libPro.voiceExpressAs
            ) { data ->
                if (data != null)
                    onRead(data)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return e.message
        }
        return null
    }

    fun getAudioForRetry(
        text: String,
        pro: VoiceProperty,
        format: String,
        retryNum: Int,
        onError: (reason: String, num: Int) -> Boolean
    ): ByteArray? {
        for (i in 1..retryNum) {
            try {
                return getAudio(text, pro, format)
            } catch (e: Exception) {
                e.printStackTrace()
                if (!onError.invoke(e.message ?: "", i)) //不再重试
                    return null
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
//        val voice = pro.voiceName
//        val rate = Prosody.toPcmString(pro.prosody.rate)
//        val volume = Prosody.toPcmString(pro.prosody.volume)
//        val pitch = Prosody.toPcmString(pro.prosody.pitch)
//        val style = pro.expressAs?.style ?: "general"
//        val styleDegree = "${(pro.expressAs?.styleDegree ?: 1).toFloat()}"
//        val role = pro.expressAs?.role ?: "default"

        val libPro = toLibProperty(pro)
        when (pro.api) {
            TtsApiType.EDGE -> {
                return mEdgeApi.getEdgeAudio(
                    text,
                    format,
                    libPro.voiceProperty,
                    libPro.voiceProsody
                )
            }
            TtsApiType.AZURE -> {
                return mAzureApi.getAudio(
                    text,
                    format,
                    libPro.voiceProperty,
                    libPro.voiceProsody,
                    libPro.voiceExpressAs
                )
            }
            TtsApiType.CREATION -> {
                return mCreationApi.getCreationAudio(
                    text,
                    format,
                    libPro.voiceProperty,
                    libPro.voiceProsody,
                    libPro.voiceExpressAs
                )
            }
        }
        return null
    }

}

data class ResultProperty(
    var voiceProperty: tts_server_lib.VoiceProperty,
    var voiceProsody: VoiceProsody,
    var voiceExpressAs: VoiceExpressAs
)