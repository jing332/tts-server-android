package com.github.jing332.tts_server_android.model.speech.tts

import android.os.Parcelable
import com.github.jing332.tts_server_android.help.config.SysTtsConfig
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
data class AudioParams(
    var speed: Float = FOLLOW_GLOBAL_VALUE,
    var volume: Float = FOLLOW_GLOBAL_VALUE,
    var pitch: Float = FOLLOW_GLOBAL_VALUE
) :
    Parcelable {
    companion object {
        const val FOLLOW_GLOBAL_VALUE = 0f
    }

    val isDefaultValue: Boolean
        get() = speed == 1f && volume == 1f && pitch == 1f

    fun newIfFollow(): AudioParams {
        return AudioParams(
            if (speed == FOLLOW_GLOBAL_VALUE) SysTtsConfig.audioParamsSpeed else speed,
            if (volume == FOLLOW_GLOBAL_VALUE) SysTtsConfig.audioParamsVolume else volume,
            if (pitch == FOLLOW_GLOBAL_VALUE) SysTtsConfig.audioParamsPitch else pitch
        )
    }

    fun reset(v: Float) {
        speed = v
        volume = v
        pitch = v
    }

}