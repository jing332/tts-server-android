package com.github.jing332.tts_server_android.service.tts.help

import android.content.Context
import com.github.jing332.tts_server_android.constant.TtsApiType

class TtsConfig(
    var api: Int,
    var locale: String,
    var voiceName: String,
    var voiceId: String,
    var format: String,
    var volume: Int
) {
    constructor() : this(
        TtsApiType.CREATION,
        "zh-CN",
        "zh-CN-XiaoxiaoNeural",
        "5f55541d-c844-4e04-a7f8-1723ffbea4a9",
        "audio-24khz-48kbitrate-mono-mp3",
        50
    )

    /* 转为百分比字符串 */
    fun volumeToPctString(): String {
        return "${volume - 50}%"
    }

    fun loadConfig(ctx: Context): TtsConfig {
        api = getConfigInt(ctx, "api", TtsApiType.CREATION)
        locale = getConfigString(ctx, "locale", "zh-CN")
        voiceName = getConfigString(ctx, "voiceName", "zh-CN-XiaoxiaoNeural")
        voiceId = getConfigString(ctx, "voiceId", "5f55541d-c844-4e04-a7f8-1723ffbea4a9")
        format = getConfigString(ctx, "format", "audio-24khz-48kbitrate-mono-mp3")
        volume = getConfigInt(ctx, "volume", 50)
        return this
    }

    fun writeConfig(ctx: Context) {
        setConfigInt(ctx,"api", api)
        setConfigString(ctx, "locale", locale)
        setConfigString(ctx, "voiceName", voiceName)
        setConfigString(ctx, "voiceId", voiceId)
        setConfigString(ctx, "format", format)
        setConfigInt(ctx, "volume", volume)
    }

    private fun getConfigString(ctx: Context, key: String, default: String): String {
        val pref = ctx.getSharedPreferences("tts_service", Context.MODE_PRIVATE)
        return pref.getString(key, default).toString()
    }

    private fun setConfigString(ctx: Context, key: String, value: String) {
        ctx.getSharedPreferences("tts_service", Context.MODE_PRIVATE).edit()
            .putString(key, value)
            .apply()
    }

    private fun getConfigInt(ctx: Context, key: String, default: Int): Int {
        val pref = ctx.getSharedPreferences("tts_service", Context.MODE_PRIVATE)
        return pref.getInt(key, default)
    }

    private fun setConfigInt(ctx: Context, key: String, value: Int) {
        ctx.getSharedPreferences("tts_service", Context.MODE_PRIVATE).edit()
            .putInt(key, value)
            .apply()
    }

    override fun toString(): String {
        return "locale: $locale, voiceName: $voiceName, voiceId: $voiceId, format: $format, volume: $volume"
    }
}