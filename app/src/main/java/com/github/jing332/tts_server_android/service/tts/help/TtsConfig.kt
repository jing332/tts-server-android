package com.github.jing332.tts_server_android.service.tts.help

import android.content.Context
import com.github.jing332.tts_server_android.constant.TtsApiType

class TtsConfig(
    var api: Int,
    var locale: String,
    var voiceName: String,
    var voiceStyle: String,
    var voiceStyleDegree: Int,
    var voiceRole: String,
    var voiceId: String,
    var format: String,
    var volume: Int,
    var rate: Int,
    var isSplitSentences: Boolean
) {
    companion object {
        const val KEY_API = "api"
        const val KEY_LOCALE = "locale"
        const val KEY_VOICE_NAME = "voiceName"
        const val KEY_VOICE_STYLE = "voiceStyle"
        const val KEY_VOICE_STYLE_DEGREE = "voiceStyleDegree"
        const val KEY_VOICE_ROLE = "voiceRole"
        const val KEY_VOICE_ID = "voiceId"
        const val KEY_FORMAT = "format"
        const val KEY_VOLUME = "volume"
        const val KEY_RATE = "rate"
        const val KEY_IS_SPLIT_SENTENCES = "isSplitSentences"
    }

    constructor() : this(
        TtsApiType.CREATION,
        "zh-CN",
        "zh-CN-XiaoxiaoNeural",
        "", 100, "",
        "5f55541d-c844-4e04-a7f8-1723ffbea4a9",
        "audio-24khz-48kbitrate-mono-mp3",
        50, 0, false
    )

    /* 转为百分比字符串 */
    fun volumeToPctString(): String {
        return "${volume - 50}%"
    }

    fun rateToPcmString(): String {
        return "${(rate - 50) * 2}%"
    }

    fun loadConfig(ctx: Context): TtsConfig {
        val pref = ctx.getSharedPreferences("tts_service", Context.MODE_PRIVATE)
        api = pref.getInt(KEY_API, TtsApiType.CREATION)
        locale = pref.getString(KEY_LOCALE, locale) ?: locale
        voiceName = pref.getString(KEY_VOICE_NAME, voiceName) ?: voiceName
        voiceStyle = pref.getString(KEY_VOICE_STYLE, voiceStyle) ?: voiceStyle
        voiceId = pref.getString(KEY_VOICE_ID, voiceId) ?: voiceId
        format = pref.getString(KEY_FORMAT, format) ?: format
        volume = pref.getInt(KEY_VOLUME, volume)
        rate = pref.getInt(KEY_RATE, rate)
        isSplitSentences = pref.getBoolean(KEY_IS_SPLIT_SENTENCES, false)

        return this
    }

    fun writeConfig(ctx: Context) {
        ctx.getSharedPreferences("tts_service", Context.MODE_PRIVATE).edit().apply {
            putInt(KEY_API, api)
            putString(KEY_LOCALE, locale)
            putString(KEY_VOICE_NAME, voiceName)
            putString(KEY_VOICE_STYLE, voiceStyle)
            putInt(KEY_VOICE_STYLE_DEGREE, voiceStyleDegree)
            putString(KEY_VOICE_ROLE, voiceRole)
            putString(KEY_VOICE_ID, voiceId)
            putString(KEY_FORMAT, format)
            putInt(KEY_VOLUME, volume)
            putInt(KEY_RATE, rate)
            putBoolean(KEY_IS_SPLIT_SENTENCES, isSplitSentences)
            apply()
        }
    }

    override fun toString(): String {
        return "locale: $locale, voiceName: $voiceName, voiceId: $voiceId, format: $format, volume: $volume"
    }
}