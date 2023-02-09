package com.github.jing332.tts_server_android.help.plugin.ext

import com.github.jing332.tts_server_android.help.audio.AudioDecoder

@Suppress("unused")
open class JsExtensions : JsNet(), JsCrypto {
    fun getAudioSampleRate(audio: ByteArray): Int {
        return AudioDecoder().getSampleRateAndMime(audio).first
    }

    /* Str转ByteArray */
    fun strToBytes(str: String): ByteArray {
        return str.toByteArray(charset("UTF-8"))
    }

    fun strToBytes(str: String, charset: String): ByteArray {
        return str.toByteArray(charset(charset))
    }

    /* ByteArray转Str */
    fun bytesToStr(bytes: ByteArray): String {
        return String(bytes, charset("UTF-8"))
    }

    fun bytesToStr(bytes: ByteArray, charset: String): String {
        return String(bytes, charset(charset))
    }
}