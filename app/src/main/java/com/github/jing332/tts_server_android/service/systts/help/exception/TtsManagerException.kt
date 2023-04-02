package com.github.jing332.tts_server_android.service.systts.help.exception

import com.github.jing332.tts_server_android.model.tts.ITextToSpeechEngine

open class TtsManagerException(
    open val tts: ITextToSpeechEngine? = null,
    open  val text: String? = null,

    override val message: String? = null,
    override val cause: Throwable? = null
) :
    Exception() {
}