package com.github.jing332.tts_server_android.model.speech.tts

import android.media.AudioFormat
import android.os.Parcelable
import com.github.jing332.tts_server_android.R
import com.github.jing332.tts_server_android.app
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
open class BaseAudioFormat(
    var sampleRate: Int = 16000,
    var bitRate: Int = AudioFormat.ENCODING_PCM_16BIT,
    var isNeedDecode: Boolean = true
) : Parcelable {
    override fun toString(): String {
        val str = if (isNeedDecode) " | " + app.getString(R.string.decode) else ""
        return "${sampleRate}hz" + str
    }
}