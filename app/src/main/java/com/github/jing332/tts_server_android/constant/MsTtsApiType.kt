package com.github.jing332.tts_server_android.constant

import androidx.annotation.IntDef

@IntDef(
    MsTtsApiType.EDGE, MsTtsApiType.AZURE, MsTtsApiType.CREATION, MsTtsApiType.EDGE_OKHTTP
)
@Retention(AnnotationRetention.SOURCE)
annotation class MsTtsApiType {
    companion object {
        const val EDGE: Int = 0
        const val AZURE = 1
        const val CREATION = 2
        const val EDGE_OKHTTP = 3

        fun toString(@MsTtsApiType apiType: Int): String {
            return when (apiType) {
                EDGE -> "Edge"
                EDGE_OKHTTP -> "Edge-OkHttp"
                AZURE -> "Azure"
                CREATION -> "Creation"
                else -> ""
            }
        }
    }
}