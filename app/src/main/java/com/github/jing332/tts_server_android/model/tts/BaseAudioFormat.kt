package com.github.jing332.tts_server_android.model.tts

import android.media.AudioFormat
import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
open class BaseAudioFormat(
    var sampleRate: Int = 16000,
    var bitRate: Int = AudioFormat.ENCODING_PCM_16BIT,
    var isNeedDecode: Boolean = false,
) : Parcelable {
    override fun toString(): String {
        return if (isNeedDecode) "${sampleRate}hz (解码)" else "${sampleRate}hz"
    }
}