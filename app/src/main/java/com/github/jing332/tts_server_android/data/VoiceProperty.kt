package com.github.jing332.tts_server_android.data

import com.github.jing332.tts_server_android.constant.TtsApiType
import java.io.Serializable

@kotlinx.serialization.Serializable
data class VoiceProperty(
    @TtsApiType var api: Int,
    var locale: String,
    var voiceName: String,
    var voiceId: String,
    var prosody: Prosody,
    var expressAs: ExpressAs?
) : Serializable, Cloneable {
    constructor() : this(DEFAULT_VOICE)
    constructor(voiceName: String) : this(voiceName, Prosody())
    constructor(voiceName: String, voiceId: String) : this(
        TtsApiType.CREATION,
        DEFAULT_LOCALE,
        voiceName,
        voiceId,
        Prosody(),
        null
    )

    constructor(voiceName: String, prosody: Prosody) : this(
        TtsApiType.EDGE,
        DEFAULT_LOCALE,
        voiceName,
        "",
        prosody,
        null
    )

    companion object {
        const val DEFAULT_LOCALE = "zh-CN"
        const val DEFAULT_VOICE = "zh-CN-XiaoxiaoNeural"
        const val DEFAULT_VOICE_ID = "5f55541d-c844-4e04-a7f8-1723ffbea4a9"
    }

    public override fun clone(): VoiceProperty {
        val obj = super.clone() as VoiceProperty
        obj.api = api
        obj.voiceName = voiceName
        obj.voiceId = voiceId
        obj.expressAs = expressAs
        obj.prosody = prosody.clone()

        return obj
    }
}

@kotlinx.serialization.Serializable
data class ExpressAs(var style: String, var styleDegree: Float, var role: String) : Serializable {
    constructor() : this("", 1F, "")
}

/* Prosody 基本数值参数 单位: %百分比 */
@kotlinx.serialization.Serializable
data class Prosody(var rate: Int, var volume: Int, var pitch: Int) : Serializable, Cloneable {
    constructor() : this(RATE_FOLLOW_SYSTEM_VALUE, 0, 0)

    companion object {
        const val RATE_FOLLOW_SYSTEM_VALUE = -100
    }

    public override fun clone(): Prosody {
        return super.clone() as Prosody
    }

    fun setRateIfFollowSystem(sysRate: Int): Prosody {
        if (rate <= RATE_FOLLOW_SYSTEM_VALUE) rate = sysRate
        return this
    }

}