package com.github.jing332.tts_server_android.service.tts

import android.media.AudioFormat

object TtsFormatManger {
    val formats = arrayListOf<TtsOutputFormat>()

    init {
        formats.add(
            TtsOutputFormat(
                "audio-24khz-48kbitrate-mono-mp3",
                24000,
                AudioFormat.ENCODING_PCM_16BIT
            )
        )

        formats.add(
            TtsOutputFormat(
                "audio-16khz-32kbitrate-mono-mp3",
                16000,
                AudioFormat.ENCODING_PCM_16BIT
            )
        )
    }

    fun getFormat(name: String): TtsOutputFormat? {
        formats.forEach { v ->
            if (v.name.equals(name)) {
                return v
            }
        }
        return null
    }
}