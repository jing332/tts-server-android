package com.github.jing332.tts_server_android.model.tts

import androidx.annotation.IntDef

data class MsTtsAudioFormat(
    val name: String,
    val value: String,
    @SupportedApi val supportedApi: Int = 0,
) : BaseAudioFormat() {
    companion object {
        const val DEFAULT = "audio-24khz-48kbitrate-mono-mp3"
    }

    constructor(
        value: String,
        sampleRate: Int,
        bitRate: Int,
        @SupportedApi supportedApi: Int,
        isNeedDecode: Boolean = true
    ) : this(value, value, supportedApi) {
        this.bitRate = bitRate
        this.sampleRate = sampleRate
        this.isNeedDecode = isNeedDecode
    }

    override fun toString(): String {
        return value
    }

    @IntDef(flag = true, value = [SupportedApi.AZURE, SupportedApi.EDGE, SupportedApi.CREATION])
    @Retention(AnnotationRetention.SOURCE)
    annotation class SupportedApi {
        companion object {
            const val EDGE: Int = 1
            const val AZURE = 1 shl 1
            const val CREATION = 1 shl 2
        }
    }
}