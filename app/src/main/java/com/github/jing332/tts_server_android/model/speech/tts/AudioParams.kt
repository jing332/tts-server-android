package com.github.jing332.tts_server_android.model.speech.tts

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
data class AudioParams(var speed: Float = 1f, var volume: Float = 1f, var pitch: Float = 1f) :
    Parcelable {
    val isDefaultValue: Boolean
        get() = speed == 1f && volume == 1f && pitch == 1f

}