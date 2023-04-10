package com.github.jing332.tts_server_android.service.systts.help.exception

import com.github.jing332.tts_server_android.model.speech.tts.ITextToSpeechEngine

class SpeechRuleException(override val text: String?, override val tts: ITextToSpeechEngine?,
                          override val message: String? = null, override val cause: Throwable? = null) :
    SynthesisException() {
}