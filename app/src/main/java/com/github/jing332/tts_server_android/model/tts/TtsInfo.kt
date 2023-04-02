package com.github.jing332.tts_server_android.model.tts

import android.os.Parcelable
import com.github.jing332.tts_server_android.constant.ReadAloudTarget
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Serializable
@Parcelize
data class TtsInfo(
    @ReadAloudTarget var target: Int = ReadAloudTarget.ALL,
    var standbyTts: ITextToSpeechEngine? = null,

    var tag: String = ""
) :
    Parcelable {
}