package com.github.jing332.tts_server_android.model.tts

import android.content.Context
import android.os.Parcelable
import android.view.View
import android.widget.LinearLayout
import android.widget.LinearLayout.VERTICAL
import androidx.core.view.setPadding
import androidx.core.view.updatePadding
import com.github.jing332.tts_server_android.R
import com.github.jing332.tts_server_android.app
import com.github.jing332.tts_server_android.constant.CnLocalMap
import com.github.jing332.tts_server_android.constant.MsTtsApiType
import com.github.jing332.tts_server_android.data.entities.systts.SystemTts
import com.github.jing332.tts_server_android.help.AppConfig
import com.github.jing332.tts_server_android.help.SysTtsConfig
import com.github.jing332.tts_server_android.model.SysTtsLib
import com.github.jing332.tts_server_android.ui.systts.edit.BaseInfoEditView
import com.github.jing332.tts_server_android.ui.systts.edit.MsTtsQuickEditView
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
@SerialName("internal")
data class MsTTS(
    @MsTtsApiType var api: Int = MsTtsApiType.EDGE,
    var format: String = MsTtsAudioFormat.DEFAULT,
    var locale: String = DEFAULT_LOCALE,
    // 二级语言（语言技能）仅限en-US-JennyMultilingualNeural
    var secondaryLocale: String? = null,
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
        MsTtsApiType.EDGE, MsTtsAudioFormat.DEFAULT,
        DEFAULT_LOCALE,
        null,
        voiceName,
        null,
        prosody,
        null
    )

    companion object {
        const val RATE_FOLLOW_SYSTEM = -100
        const val PITCH_FOLLOW_SYSTEM = -50

        const val DEFAULT_LOCALE = "zh-CN"
        const val DEFAULT_VOICE = "zh-CN-XiaoxiaoNeural"

        //        const val DEFAULT_VOICE_ID = "5f55541d-c844-4e04-a7f8-1723ffbea4a9"
        val strFollow by lazy { app.getString(R.string.follow) }
        val strNone by lazy { app.getString(R.string.none) }
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

    override fun isRateFollowSystem(): Boolean {
        return RATE_FOLLOW_SYSTEM == rate
    }

    override fun isPitchFollowSystem(): Boolean {
        return PITCH_FOLLOW_SYSTEM == pitch
    }

    override fun getDescription(): String {
        val rateStr = if (isRateFollowSystem()) strFollow else rate
        val pitchStr = if (isPitchFollowSystem()) strFollow else pitch

        var style = strNone
        val styleDegree = expressAs?.styleDegree ?: 1F
        var role = strNone
        expressAs?.also { exp ->
            exp.style?.let { style = CnLocalMap.getStyleAndRole(it) }
            exp.role?.let { role = CnLocalMap.getStyleAndRole(it) }
        }

        val expressAs =
            if (api == MsTtsApiType.EDGE) ""
            else "$style-$role | 强度: <b>${styleDegree}</b><br>"
        return "${expressAs}语速:<b>$rateStr</b> | 音量:<b>$volume</b> | 音高:<b>$pitchStr</b>"
    }

    override fun onDescriptionClick(
        context: Context,
        view: View?,
        data: SystemTts,
        done: (modifiedData: SystemTts?) -> Unit
    ) {
        val baseEdit = BaseInfoEditView(context).apply { setData(data) }
        val editView = MsTtsQuickEditView(context).apply {
            setData(this@MsTTS)
            updatePadding(top = 8)
        }
        val layout = LinearLayout(context).apply {
            orientation = VERTICAL
            addView(baseEdit)
            addView(editView)
            setPadding(16)
        }

        BottomSheetDialog(context).apply {
            setContentView(layout)
            setOnDismissListener { done(data) }
            show()
        }
    }


    @IgnoredOnParcel
    private var lastLoadTime: Long = 0

    override fun onLoad() {
        // 500ms 内只可加载一次
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastLoadTime > 500) {
            SysTtsLib.setUseDnsLookup(AppConfig.isEdgeDnsEnabled)
            SysTtsLib.setTimeout(SysTtsConfig.requestTimeout)
            lastLoadTime = System.currentTimeMillis()
        }
    }

    override fun getType(): String {
        return MsTtsApiType.toString(api)
    }

    override fun getBottomContent(): String {
        return audioFormat.toString()
    }

    override fun toString(): String {
        var s =
            "api=${MsTtsApiType.toString(api)}, format=${format}, voiceName=${voiceName}, prosody=${prosody}, expressAs=${expressAs}"
        secondaryLocale?.let { s += ", secondaryLocale=$it" }
        return s
    }

    override fun getAudio(speakText: String): ByteArray? {
        return SysTtsLib.getAudio(speakText, this, format)
    }

    override fun getAudioStream(
        speakText: String,
        chunkSize: Int,
        onData: (ByteArray?) -> Unit
    ) {
        SysTtsLib.getAudioStream(speakText, this@MsTTS) {
            onData(it)
        }
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
    var rate: Int = MsTTS.RATE_FOLLOW_SYSTEM,
    var volume: Int = 0,
    var pitch: Int = MsTTS.PITCH_FOLLOW_SYSTEM
) : Parcelable