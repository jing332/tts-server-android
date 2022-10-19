package com.github.jing332.tts_server_android.service.tts.help

import androidx.annotation.IntDef

class TtsAudioFormat(
    val name: String,
    val value: String,
    val hz: Int,
    val bitRate: Int,
    @SupportedApi val supportedApi: Int,
    val needDecode: Boolean
) {

    constructor(
        name: String,
        hz: Int,
        bitRate: Int,
        @SupportedApi supportedApi: Int,
        needDecode: Boolean
    ) : this(name, name, hz, bitRate, supportedApi, needDecode)

    override fun toString(): String {
        return "TtsOutputFormat{" +
                "name='" + name + '\'' +
                ", value='" + value + '\'' +
                ", hz=" + hz +
                ", bitRate=" + bitRate +
                ", needDecode=" + needDecode +
                '}'
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

   /* class SupportedApi(
        var isEdge: Boolean,
        var isAzure: Boolean,
        var isCreation: Boolean
    )

    companion object {
        const val API_EDGE = 0
        const val API_AZURE = 1
        const val API_CREATION = 2
    }*/
}