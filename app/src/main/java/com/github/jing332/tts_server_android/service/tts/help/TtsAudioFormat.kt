package com.github.jing332.tts_server_android.service.tts.help

class TtsAudioFormat(
    val name: String,
    val value: String,
    val hz: Int,
    val bitRate: Int,
    val supportedApi: SupportedApi,
    val needDecode: Boolean
) {
    constructor(
        name: String,
        hz: Int,
        bitRate: Int,
        supportedApi: SupportedApi,
        needDecode: Boolean
    ) : this(name, name, hz, bitRate, supportedApi, needDecode)

    override fun toString(): String {
        return "TtsOutputFormat{" +
                "name='" + name + '\'' +
                ", value='" + value + '\'' +
                ", hz=" + hz +
                ", bitRate=" + bitRate +
                ", needDecode=" + needDecode +
                '}'
    }

    class SupportedApi(
        var isEdge: Boolean,
        var isAzure: Boolean,
        var isCreation: Boolean
    )

    companion object {
        const val API_EDGE = 0
        const val API_AZURE = 1
        const val API_CREATION = 2
    }
}