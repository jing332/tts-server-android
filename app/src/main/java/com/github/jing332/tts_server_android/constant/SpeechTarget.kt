package com.github.jing332.tts_server_android.constant

import androidx.annotation.IntDef
import com.github.jing332.tts_server_android.App
import com.github.jing332.tts_server_android.R
import com.github.jing332.tts_server_android.app

@IntDef(
    SpeechTarget.ALL,
//    SpeechTarget.ASIDE,
//    SpeechTarget.DIALOGUE,
    SpeechTarget.BGM,
    SpeechTarget.CUSTOM_TAG
)
@Retention(AnnotationRetention.SOURCE)
annotation class SpeechTarget {
    companion object {
        const val ALL = 0

        @Deprecated("已有自定TAG")
        const val ASIDE = 1 //旁白

        @Deprecated("已有自定TAG")
        const val DIALOGUE = 2 //对话

        const val BGM = 3 //背景音乐
        const val CUSTOM_TAG = 4 // 自定义Tag

        fun toText(@SpeechTarget target: Int): String {
            return when (target) {
                BGM -> {
                    app.getString(R.string.bgm)
                }

                else -> ""
            }
        }
    }
}