package com.github.jing332.tts_server_android.model

import com.github.jing332.tts_server_android.constant.MsTtsApiType
import com.github.jing332.tts_server_android.model.speech.tts.MsTTS
import tts_server_lib.*

/* 系统TTS Go库的包装 */
object SysTtsLib {
    private fun toLibProperty(pro: MsTTS): ResultProperty {
        val libPro = VoiceProperty()
        libPro.api = pro.api.toLong()
        libPro.voiceName = pro.voiceName
        libPro.voiceId = pro.voiceId
        libPro.secondaryLocale = pro.secondaryLocale
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

    private val mEdgeApi: EdgeApi by lazy { EdgeApi() }

    /**
     * 设置超时
     */
    fun setTimeout(ms: Int) {
        mEdgeApi.timeout = ms
    }

    fun setUseDnsLookup(isEnabled: Boolean) {
        mEdgeApi.useDnsLookup = isEnabled
    }

    /**
     *  获取音频 在Go中生成SSML
     */
    fun getAudio(
        text: String,
        pro: MsTTS,
        format: String,
    ): ByteArray? {
        val libPro = toLibProperty(pro)
        when (pro.api) {
            MsTtsApiType.EDGE -> {
                return mEdgeApi.getEdgeAudio(
                    text,
                    format,
                    libPro.voiceProperty,
                    libPro.voiceProsody
                )
            }

            MsTtsApiType.AZURE -> {
                throw Exception("Azure 已失效！")
            }

            MsTtsApiType.CREATION -> {
                throw Exception("Creation 已失效！")
            }
        }
        return null
    }
}

data class ResultProperty(
    var voiceProperty: VoiceProperty,
    var voiceProsody: VoiceProsody,
    var voiceExpressAs: VoiceExpressAs
)