package com.github.jing332.tts_server_android.constant

import androidx.annotation.IntDef

@IntDef(MsTtsApiType.EDGE, MsTtsApiType.AZURE, MsTtsApiType.CREATION)
@Retention(AnnotationRetention.SOURCE)
annotation class MsTtsApiType {
    companion object {
        const val EDGE: Int = 0
        const val AZURE = 1
        const val CREATION = 2

        fun toString(@MsTtsApiType apiType: Int): String {
            return when (apiType) {
                EDGE -> "Edge"
                AZURE -> "Azure"
                CREATION -> "Creation"
                else -> ""
            }
        }
    }
}