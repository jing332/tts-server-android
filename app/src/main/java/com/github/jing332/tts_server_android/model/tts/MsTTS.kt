package com.github.jing332.tts_server_android.model.tts

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Parcelable
import android.view.View
import com.github.jing332.tts_server_android.App
import com.github.jing332.tts_server_android.R
import com.github.jing332.tts_server_android.app
import com.github.jing332.tts_server_android.constant.CnLocalMap
import com.github.jing332.tts_server_android.constant.MsTtsApiType
import com.github.jing332.tts_server_android.data.entities.systts.SpeechRuleInfo
import com.github.jing332.tts_server_android.data.entities.systts.SystemTts
import com.github.jing332.tts_server_android.databinding.SysttsMsEditBottomSheetBinding
import com.github.jing332.tts_server_android.help.config.AppConfig
import com.github.jing332.tts_server_android.help.config.SysTtsConfig
import com.github.jing332.tts_server_android.model.SysTtsLib
import com.github.jing332.tts_server_android.ui.systts.edit.microsoft.MsTtsEditActivity
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Parcelize
@Serializable
@SerialName("internal")
data class MsTTS(
    @MsTtsApiType var api: Int = MsTtsApiType.EDGE,
    var format: String = MsTtsAudioFormat.DEFAULT,
    var locale: String = DEFAULT_LOCALE,
    // 二级语言（语言技能）仅限en-US-JennyMultilingualNeural
    var secondaryLocale: String? = null,
    var voiceName: String = DEFAULT_VOICE,
    var voiceId: String? = null,
    var prosody: Prosody = Prosody(),
    var expressAs: ExpressAs? = null,

    override var audioPlayer: PlayerParams = PlayerParams(),
    @Transient
    override var audioFormat: BaseAudioFormat = MsTtsFormatManger.getFormatOrDefault(format),
    @Transient
    override var speechRule: SpeechRuleInfo = SpeechRuleInfo(),
) : Parcelable, ITextToSpeechEngine() {
    companion object {
        const val RATE_FOLLOW_SYSTEM = -100
        const val PITCH_FOLLOW_SYSTEM = -50

        const val DEFAULT_LOCALE = "zh-CN"
        const val DEFAULT_VOICE = "zh-CN-XiaoxiaoNeural"
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
        get() = prosody.rate
        set(value) {
            prosody.rate = value
        }

    override fun isRateFollowSystem(): Boolean {
        return RATE_FOLLOW_SYSTEM == rate
    }

    override fun isPitchFollowSystem(): Boolean {
        return PITCH_FOLLOW_SYSTEM == pitch
    }

    override fun getEditActivity() = MsTtsEditActivity::class.java

    override fun getDescription(): String {
        val strFollow by lazy { app.getString(R.string.follow) }
        val strNone by lazy { app.getString(R.string.none) }

        val rateStr = if (isRateFollowSystem()) strFollow else rate
        val pitchStr = if (isPitchFollowSystem()) strFollow else pitch

        var style = strNone
        val styleDegree = expressAs?.styleDegree ?: 1F
        var role = strNone
        expressAs?.also { exp ->
            exp.style?.let { style = if (App.isCnLocale) CnLocalMap.getStyleAndRole(it) else it }
            exp.role?.let { role = if (App.isCnLocale) CnLocalMap.getStyleAndRole(it) else it }
        }

        val expressAs =
            if (api == MsTtsApiType.EDGE) ""
            else App.context.getString(
                R.string.systts_ms_express_as_description,
                "<b>${style}</b>", "<b>${role}</b>", "<b>${styleDegree}</b>"
            ) + "<br>"

        return expressAs + App.context.getString(
            R.string.systts_play_params_description,
            "<b>${rateStr}</b>",
            "<b>${volume}</b>",
            "<b>${pitchStr}</b>"
        )
    }

    @Suppress("DEPRECATION")
    @SuppressLint("RestrictedApi")
    override fun onDescriptionClick(
        activity: Activity,
        view: View?,
        data: SystemTts,
        done: (modifiedData: SystemTts?) -> Unit
    ) {
        val binding =
            SysttsMsEditBottomSheetBinding.inflate(activity.layoutInflater, null, false)
        binding.apply {
            basicEdit.setData(data)
            editView.setData(this@MsTTS)
            root.minimumHeight = activity.windowManager.defaultDisplay.height
        }

        BottomSheetDialog(activity).apply {
            setContentView(binding.root)
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

    override suspend fun getAudio(speakText: String, rate: Int, pitch: Int): ByteArray? {
        return SysTtsLib.getAudio(
            speakText,
            this.copy(prosody = prosody.copy(rate = rate, pitch = pitch)),
            format
        )
    }

//    override suspend fun getAudioStream(
//        speakText: String,
//        chunkSize: Int,
//        onData: (ByteArray?) -> Unit
//    ) {
//        SysTtsLib.getAudioStream(speakText, this@MsTTS) {
//            onData(it)
//        }
//    }
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