package com.github.jing332.tts_server_android.service.systts.help

import com.github.jing332.tts_server_android.constant.TtsApiType
import com.github.jing332.tts_server_android.data.MsTtsProperty
import tts_server_lib.*

/* 系统TTS Go库的包装 */
class SysTtsLib {
    companion object {
        const val TAG = "SysTtsLib"

        fun toLibProperty(pro: MsTtsProperty): ResultProperty {
            val libPro = VoiceProperty()
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

    /**
     * 设置超时
     */
    fun setTimeout(ms: Int) {
        mEdgeApi.timeout = ms
        mAzureApi.timeout = ms
        mCreationApi.timeout = ms
    }

    fun setUseDnsLookup(isEnabled:Boolean){
        mEdgeApi.useDnsLookup = isEnabled
    }

    /**
     * 获取音频流 仅支持Azure
     */
    fun getAudioStream(
        text: String,
        pro: MsTtsProperty,
        onRead: (ByteArray) -> Unit
    ): String? {
        val libPro = toLibProperty(pro)
        try {
            mAzureApi.getAudioStream(
                text,
                pro.format,
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

    /**
     * 获取音频，失败自动重试
     */
    fun getAudioForRetry(
        text: String,
        pro: MsTtsProperty,
        retryNum: Int,
        onError: (reason: String, num: Int) -> Boolean
    ): ByteArray? {
        for (i in 1..retryNum) {
            try {
                return getAudio(text, pro, pro.format)
            } catch (e: Exception) {
                e.printStackTrace()
                if (!onError.invoke(e.message ?: "", i)) //不再重试
                    return null
            }
        }

        return null
    }

    /**
     *  获取音频 在Go中生成SSML
     */
    fun getAudio(
        text: String,
        pro: MsTtsProperty,
        format: String,
    ): ByteArray? {
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
    var voiceProperty: VoiceProperty,
    var voiceProsody: VoiceProsody,
    var voiceExpressAs: VoiceExpressAs
)