package com.github.jing332.tts_server_android.service.systts.help.exception

import com.github.jing332.tts_server_android.model.speech.tts.ITextToSpeechEngine

class RequestException(
    val errorCode: Int = 0,
    val times: Int = 1,
    override val tts: ITextToSpeechEngine?,
    override val text: String?,
    override val message: String? = null,
    override val cause: Throwable? = null
) :
    SynthesisException() {
    companion object {
        const val ERROR_CODE_REQUEST = 0
        const val ERROR_CODE_AUDIO_NULL = 1
        const val ERROR_CODE_TIMEOUT = 2
    }

    override fun getLocalizedMessage(): String? {
        return when (errorCode) {
            ERROR_CODE_REQUEST -> return "Request error"
            ERROR_CODE_AUDIO_NULL -> return "Audio is null"
            ERROR_CODE_TIMEOUT -> return "Request timeout"
            else -> super.getLocalizedMessage()
        }
    }
}