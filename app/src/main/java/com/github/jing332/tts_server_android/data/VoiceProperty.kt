package com.github.jing332.tts_server_android.data

import com.github.jing332.tts_server_android.constant.TtsApiType
import java.io.Serializable

data class VoiceProperty(
    @TtsApiType var api: Int,
    var voiceName: String,
    var voiceId: String,
    var prosody: Prosody,
    var expressAs: ExpressAs?
) : Serializable, Cloneable {
    constructor() : this(DEFAULT_VOICE)
    constructor(voiceName: String) : this(voiceName, Prosody())
    constructor(voiceName: String, voiceId: String) : this(
        TtsApiType.CREATION,
        voiceName,
        voiceId,
        Prosody(),
        null
    )

    constructor(voiceName: String, prosody: Prosody) : this(
        TtsApiType.EDGE,
        voiceName,
        "",
        prosody,
        null
    )

    companion object {
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

    fun toString(text: String): String {
        val element = if (api != TtsApiType.EDGE && expressAs != null)
            expressAs?.toString(text, prosody)
        else
            prosody.toString(text)


        return "<voice name=\"$voiceName\">${element}</voice>"
    }
}

data class ExpressAs(var style: String, var styleDegree: Float, var role: String) :
    Serializable {
    constructor() : this("", 1F, "")

    fun toString(text: String, prosody: Prosody): String {
        return "<mstts:express-as style=\"${style}\" styledegree=\"${styleDegree}\" role=\"${role}\">" +
                "${prosody.toString(text)}</mstts:express-as>"
    }
}

/* Prosody 基本数值参数 单位: %百分比 */
data class Prosody(var rate: Int, var volume: Int, var pitch: Int) : Serializable, Cloneable {
    constructor() : this(0, 0, 0)

    public override fun clone(): Prosody {
        return super.clone() as Prosody
    }

    /**
     * @return 语速是否跟随系统
     */
    fun isRateFollowSystem(): Boolean {
        return rate <= -100
    }

    fun setRateIfFollowSystem(sysRate: Int): Prosody {
        if (isRateFollowSystem()) rate = sysRate
        return this
    }

    fun toString(text: String): String {
        return "<prosody rate=\"$rate%\" volume=\"$volume%\" pitch=\"$pitch%\">$text</prosody>"
    }
}