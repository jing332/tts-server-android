package com.github.jing332.tts_server_android.help.audio

class AudioDecoderException(
    val errCode: Int = ERROR_CODE_DECODER, override val message: String? = null,
    override val cause: Throwable? = null
) : Exception() {
    companion object {
        const val ERROR_CODE_DECODER = 0
        const val ERROR_CODE_NO_AUDIO_TRACK = 1
        const val ERROR_CODE_NOT_SUPPORT_A5 = 2
    }

}