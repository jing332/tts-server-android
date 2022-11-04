package com.github.jing332.tts_server_android.service.systts.help

import androidx.annotation.IntDef

data class TtsAudioFormat(
    val name: String,
    val value: String,
    val hz: Int,
    val bitRate: Int,
    @SupportedApi val supportedApi: Int,
    val needDecode: Boolean
) {
//    companion object {
//        const val DEFAULT = "audio-24khz-48kbitrate-mono-mp3"
//    }

    constructor(
        name: String,
        hz: Int,
        bitRate: Int,
        @SupportedApi supportedApi: Int,
        needDecode: Boolean
    ) : this(name, name, hz, bitRate, supportedApi, needDecode)

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