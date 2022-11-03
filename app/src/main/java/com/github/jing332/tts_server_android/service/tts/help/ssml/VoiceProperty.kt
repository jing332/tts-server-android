package com.github.jing332.tts_server_android.service.tts.help.ssml

data class VoiceProperty(val voiceName: String, val prosody: Prosody) {
    fun toString(text: String): String {
        return "<voice name=\"$voiceName\">${prosody.toString(text)}</voice>"
    }
}

data class Prosody(var rate: String, var volume: String, var pitch: String) {
    constructor() : this("0%", "0%", "0%")

    fun toString(text: String): String {
        return "<prosody rate=\"$rate\" volume=\"$volume\" pitch=\"$pitch\">$text</prosody>"
    }
}