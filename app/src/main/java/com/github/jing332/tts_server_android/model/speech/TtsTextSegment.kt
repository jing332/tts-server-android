package com.github.jing332.tts_server_android.model.speech

import com.github.jing332.tts_server_android.model.speech.tts.ITextToSpeechEngine

data class TtsTextSegment(val tts: ITextToSpeechEngine, val text: String)