package com.github.jing332.tts_server_android.service.tts.help

import android.content.Context
import com.github.jing332.tts_server_android.constant.TtsApiType

class TtsConfig(
    var api: Int,
    var locale: String,
    var voiceName: String,
    var voiceId: String,
    var format: String,
    var volume: Int,
    var isSplitSentences: Boolean
) {
    constructor() : this(
        TtsApiType.CREATION,
        "zh-CN",
        "zh-CN-XiaoxiaoNeural",
        "5f55541d-c844-4e04-a7f8-1723ffbea4a9",
        "audio-24khz-48kbitrate-mono-mp3",
        50, false
    )

    /* 转为百分比字符串 */
    fun volumeToPctString(): String {
        return "${volume - 50}%"
    }

    fun loadConfig(ctx: Context): TtsConfig {
        api = getConfig(ctx, "api", TtsApiType.CREATION)
        locale = getConfig(ctx, "locale", "zh-CN")
        voiceName = getConfig(ctx, "voiceName", "zh-CN-XiaoxiaoNeural")
        voiceId = getConfig(ctx, "voiceId", "5f55541d-c844-4e04-a7f8-1723ffbea4a9")
        format = getConfig(ctx, "format", "audio-24khz-48kbitrate-mono-mp3")
        volume = getConfig(ctx, "volume", 50)
        isSplitSentences = getConfig(ctx, "isSplitSentences", false)
        return this
    }

    fun writeConfig(ctx: Context) {
        setConfig(ctx, "api", api)
        setConfig(ctx, "locale", locale)
        setConfig(ctx, "voiceName", voiceName)
        setConfig(ctx, "voiceId", voiceId)
        setConfig(ctx, "format", format)
        setConfig(ctx, "volume", volume)
        setConfig(ctx, "isSplitSentences", isSplitSentences)
    }

    private fun getConfig(ctx: Context, key: String, default: String): String {
        val pref = ctx.getSharedPreferences("tts_service", Context.MODE_PRIVATE)
        return pref.getString(key, default).toString()
    }

    private fun setConfig(ctx: Context, key: String, value: String) {
        ctx.getSharedPreferences("tts_service", Context.MODE_PRIVATE).edit()
            .putString(key, value)
            .apply()
    }

    private fun getConfig(ctx: Context, key: String, default: Int): Int {
        val pref = ctx.getSharedPreferences("tts_service", Context.MODE_PRIVATE)
        return pref.getInt(key, default)
    }

    private fun setConfig(ctx: Context, key: String, value: Int) {
        ctx.getSharedPreferences("tts_service", Context.MODE_PRIVATE).edit()
            .putInt(key, value)
            .apply()
    }

    private fun getConfig(ctx: Context, key: String, default: Boolean): Boolean {
        val pref = ctx.getSharedPreferences("tts_service", Context.MODE_PRIVATE)
        return pref.getBoolean(key, default)
    }

    private fun setConfig(ctx:Context, key:String, value :Boolean){
        ctx.getSharedPreferences("tts_service", Context.MODE_PRIVATE).edit()
            .putBoolean(key, value)
            .apply()
    }

    override fun toString(): String {
        return "locale: $locale, voiceName: $voiceName, voiceId: $voiceId, format: $format, volume: $volume"
    }
}