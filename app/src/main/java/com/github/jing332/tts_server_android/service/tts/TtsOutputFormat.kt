package com.github.jing332.tts_server_android.service.tts

class TtsOutputFormat {
    val name: String
    val value: String
    val HZ: Int
    val BitRate: Byte

    /**
     * 是否需要解码
     */
    private var needDecode = false

    constructor(name: String, hz: Int, bitRate: Int) {
        this.name = name
        value = name
        HZ = hz
        BitRate = bitRate.toByte()
    }

    constructor(name: String, hz: Int, bitRate: Int, needDecode: Boolean) {
        this.name = name
        if (name.contains(TAG)) {
            value = name.substring(TAG.length)
        } else {
            value = name
        }
        HZ = hz
        BitRate = bitRate.toByte()
        this.needDecode = needDecode
    }

    fun setNeedDecode(needDecode: Boolean) {
        this.needDecode = needDecode
    }

    override fun toString(): String {
        return "TtsOutputFormat{" +
                "name='" + name + '\'' +
                ", value='" + value + '\'' +
                ", HZ=" + HZ +
                ", BitRate=" + BitRate +
                ", needDecode=" + needDecode +
                '}'
    }

    companion object {
        const val TAG = "\uD83D\uDC96"
    }
}