package com.github.jing332.tts_server_android.service.tts.help

import android.content.Context

class TtsConfig(
    var api: Int,
    var locale: String,
    var voiceName: String,
    var voiceId: String,
    var format: String,
    var volume: Int
) {
    constructor() : this(
        TtsAudioFormat.API_CREATION,
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

    private lateinit var ctx: Context

    fun loadConfig(ctx: Context): TtsConfig {
        this.ctx = ctx
        api = getConfigInt("api", TtsAudioFormat.API_CREATION)
        locale = getConfigString("locale", "zh-CN")
        voiceName = getConfigString("voiceName", "zh-CN-XiaoxiaoNeural")
        voiceId = getConfigString("voiceId", "5f55541d-c844-4e04-a7f8-1723ffbea4a9")
        format = getConfigString("format", "audio-24khz-48kbitrate-mono-mp3")
        volume = getConfigInt("volume", 50)
        return this
    }

    fun writeConfig(ctx: Context) {
        this.ctx = ctx
        setConfigInt("api", api)
        setConfigString("locale", locale)
        setConfigString("voiceName", voiceName)
        setConfigString("voiceId", voiceId)
        setConfigString("format", format)
        setConfigInt("volume", volume)
    }

    private fun getConfigString(key: String, default: String): String {
        val pref = ctx.getSharedPreferences("tts_service", Context.MODE_PRIVATE)
        return pref.getString(key, default).toString()
    }

    private fun setConfigString(key: String, value: String) {
        ctx.getSharedPreferences("tts_service", Context.MODE_PRIVATE).edit()
            .putString(key, value)
            .apply()
    }

    private fun getConfigInt(key: String, default: Int): Int {
        val pref = ctx.getSharedPreferences("tts_service", Context.MODE_PRIVATE)
        return pref.getInt(key, default)
    }

    private fun setConfigInt(key: String, value: Int) {
        ctx.getSharedPreferences("tts_service", Context.MODE_PRIVATE).edit()
            .putInt(key, value)
            .apply()
    }

    override fun toString(): String {
        return "locale: $locale, voiceName: $voiceName, voiceId: $voiceId, format: $format, volume: $volume"
    }
}