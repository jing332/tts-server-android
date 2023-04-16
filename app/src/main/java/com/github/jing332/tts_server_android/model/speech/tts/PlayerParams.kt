package com.github.jing332.tts_server_android.model.speech.tts

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Serializable
@Parcelize
data class PlayerParams(
    var rate: Float = VALUE_FOLLOW_GLOBAL,
    var pitch: Float = VALUE_FOLLOW_GLOBAL,
    var volume: Float = VALUE_FOLLOW_GLOBAL,
) : Parcelable {
    companion object {
        const val VALUE_FOLLOW_GLOBAL = 0f
    }

    fun setParamsIfFollow(gRate: Float, gVolume: Float, gPitch: Float): PlayerParams {
        if (this.rate == VALUE_FOLLOW_GLOBAL) this.rate = gRate
        if (this.volume == VALUE_FOLLOW_GLOBAL) this.volume = gVolume
        if (this.pitch == VALUE_FOLLOW_GLOBAL) this.pitch = gPitch

        return this
    }
}
