package com.github.jing332.tts_server_android.constant

import androidx.annotation.IntDef

@IntDef(TtsApiType.EDGE, TtsApiType.AZURE, TtsApiType.CREATION)
@Retention(AnnotationRetention.SOURCE)
annotation class TtsApiType {
    companion object {
        const val EDGE: Int = 0
        const val AZURE = 1
        const val CREATION = 2

        fun toString(@TtsApiType apiType: Int): String {
            return when (apiType) {
                EDGE -> "Edge"
                AZURE -> "Azure"
                CREATION -> "Creation"
                else -> ""
            }
        }
    }
}