package com.github.jing332.tts_server_android.model.tts

import android.content.Context
import android.os.Parcelable
import android.view.View
import androidx.appcompat.app.AlertDialog
import com.github.jing332.tts_server_android.constant.CnLocalMap
import com.github.jing332.tts_server_android.constant.TtsApiType
import com.github.jing332.tts_server_android.model.MsTtsFormatManger
import com.github.jing332.tts_server_android.model.SysTtsLib
import com.github.jing332.tts_server_android.model.tts.BaseTTS.Companion.VALUE_FOLLOW_SYSTEM
import com.github.jing332.tts_server_android.ui.custom.MsTtsNumEditView
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
@SerialName("internal")
data class MsTTS(
    @TtsApiType var api: Int = TtsApiType.EDGE,
    var format: String = MsTtsAudioFormat.DEFAULT,
    var locale: String = DEFAULT_LOCALE,
    var voiceName: String,
    var voiceId: String? = null,
    var prosody: Prosody,
    var expressAs: ExpressAs? = null,

    @kotlinx.serialization.Transient
    override var audioFormat: BaseAudioFormat = MsTtsFormatManger.getFormatOrDefault(format),
) : Parcelable, BaseTTS() {


    constructor() : this(DEFAULT_VOICE)
    constructor(voiceName: String) : this(voiceName, Prosody())
    constructor(voiceName: String, prosody: Prosody) : this(
        TtsApiType.EDGE, MsTtsAudioFormat.DEFAULT,
        DEFAULT_LOCALE,
        voiceName,
        null,
        prosody,
        null
    )

    companion object {
        const val RATE_FOLLOW_SYSTEM = -100

        const val DEFAULT_LOCALE = "zh-CN"
        const val DEFAULT_VOICE = "zh-CN-XiaomiNeural"
        const val DEFAULT_VOICE_ID = "5f55541d-c844-4e04-a7f8-1723ffbea4a9"
    }

    @IgnoredOnParcel
    override var pitch: Int
        get() {
            return prosody.pitch
        }
        set(value) {
            prosody.pitch = value
        }

    @IgnoredOnParcel
    override var volume: Int
        get() {
            return prosody.volume
        }
        set(value) {
            prosody.volume = value
        }

    @IgnoredOnParcel
    override var rate: Int
        get() {
            return prosody.rate
        }
        set(value) {
            prosody.rate = value
        }

    override fun getDescription(): String {
        val rateStr = if (prosody.isRateFollowSystem) "跟随" else prosody.rate
        val volume = prosody.volume
        val style = if (expressAs?.style?.isEmpty() == false) {
            CnLocalMap.getStyleAndRole(expressAs?.style ?: "")
        } else "无"
        val styleDegree = expressAs?.styleDegree ?: 1F
        val role = if (expressAs?.role?.isEmpty() == false) {
            CnLocalMap.getStyleAndRole(expressAs?.role ?: "")
        } else "无"
        val expressAs =
            if (api == TtsApiType.EDGE) ""
            else "$style-$role | 强度: <b>${styleDegree}</b> | "
        return "${expressAs}语速:<b>$rateStr</b> | 音量:<b>$volume</b>"
    }

    override fun onDescriptionClick(
        context: Context,
        view: View?,
        done: (modifiedData: BaseTTS?) -> Unit
    ) {
        val editView = MsTtsNumEditView(context)
        editView.setPadding(25, 25, 25, 50)
        editView.setRate(prosody.rate)
        editView.setVolume(prosody.volume)
        editView.setStyleDegree(expressAs?.styleDegree ?: 1F)
        editView.isStyleDegreeVisible = api != TtsApiType.EDGE

        val dlg = AlertDialog.Builder(context)
            .setTitle("数值调节").setView(editView)
            .setOnDismissListener {
                prosody.rate = editView.rateValue
                prosody.volume = editView.volumeValue
                expressAs?.styleDegree = editView.styleDegreeValue
                done(this)
            }.create()
        dlg.window?.setDimAmount(0.5F)
        dlg.show()
    }


    override fun getType(): String {
        return TtsApiType.toString(api)
    }

    override fun getBottomContent(): String {
        return audioFormat.toString()
    }


    override fun toString(): String {
        return "api=${TtsApiType.toString(api)}, format=${format}, voiceName=${voiceName}, prosody=${prosody}, expressAs=${expressAs}"
    }

    override fun getAudio(speakText: String): ByteArray? {
        return SysTtsLib.getAudio(speakText, this, format)
    }

    override fun getAudioStream(
        speakText: String,
        chunkSize: Int,
        onData: (ByteArray?) -> Unit
    ): Boolean {
        SysTtsLib.getAudioStream(speakText, this) {
            onData(it)
        }
        return true
    }
}

@Serializable
@Parcelize
data class ExpressAs(
    var style: String? = null,
    var styleDegree: Float = 1F,
    var role: String? = null
) : Parcelable {
    constructor() : this("", 1F, "")
}

/* Prosody 基本数值参数 单位: %百分比 */
@Serializable
@Parcelize
data class Prosody(
    var rate: Int = VALUE_FOLLOW_SYSTEM,
    var volume: Int = 0,
    var pitch: Int = 0
) : Parcelable {
    val isRateFollowSystem: Boolean
        get() {
            return rate == -100
        }
}