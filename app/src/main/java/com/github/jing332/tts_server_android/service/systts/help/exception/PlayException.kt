package com.github.jing332.tts_server_android.service.systts.help.exception

import com.github.jing332.tts_server_android.model.tts.ITextToSpeechEngine

class PlayException(override val tts: ITextToSpeechEngine?, override val text: String?) :
    TtsManagerException() {
}