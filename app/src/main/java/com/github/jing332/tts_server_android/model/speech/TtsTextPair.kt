package com.github.jing332.tts_server_android.model.speech

import com.github.jing332.tts_server_android.model.speech.tts.ITextToSpeechEngine

data class TtsTextPair(val tts: ITextToSpeechEngine, val text: String)