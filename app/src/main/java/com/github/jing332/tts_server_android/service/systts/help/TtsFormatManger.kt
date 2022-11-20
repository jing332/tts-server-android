package com.github.jing332.tts_server_android.service.systts.help

import android.media.AudioFormat

object TtsFormatManger {
    private val formats = arrayListOf<TtsAudioFormat>(
        TtsAudioFormat(
            "raw-8khz-16bit-mono-pcm",
            8000,
            AudioFormat.ENCODING_PCM_16BIT,
            TtsAudioFormat.SupportedApi.AZURE,
            false
        ),
        TtsAudioFormat(
            "raw-16khz-16bit-mono-pcm",
            16000,
            AudioFormat.ENCODING_PCM_16BIT,
            TtsAudioFormat.SupportedApi.AZURE,
            false
        ),
        TtsAudioFormat(
            "raw-24khz-16bit-mono-pcm",
            24000,
            AudioFormat.ENCODING_PCM_16BIT,
            TtsAudioFormat.SupportedApi.AZURE,
            false
        ),
        TtsAudioFormat(
            "raw-48khz-16bit-mono-pcm",
            48000,
            AudioFormat.ENCODING_PCM_16BIT,
            TtsAudioFormat.SupportedApi.AZURE,
            false
        ),
        TtsAudioFormat(
            "webm-16khz-16bit-mono-opus",
            24000 * 2,
            AudioFormat.ENCODING_PCM_16BIT,
            TtsAudioFormat.SupportedApi.AZURE,
            true
        ),
        TtsAudioFormat(
            "webm-24khz-16bit-mono-opus",
            24000 * 2,
            AudioFormat.ENCODING_PCM_16BIT,
            TtsAudioFormat.SupportedApi.EDGE or TtsAudioFormat.SupportedApi.AZURE,
            true
        ),
        TtsAudioFormat(
            "webm-24khz-16bit-24kbps-mono-opus",
            24000 * 2,
            AudioFormat.ENCODING_PCM_16BIT,
            TtsAudioFormat.SupportedApi.AZURE,
            true
        ),
        TtsAudioFormat(
            "audio-16khz-32kbitrate-mono-mp3",
            16000,
            AudioFormat.ENCODING_PCM_16BIT,
            TtsAudioFormat.SupportedApi.AZURE or TtsAudioFormat.SupportedApi.CREATION,
            true
        ),
        TtsAudioFormat(
            "audio-24khz-48kbitrate-mono-mp3",
            24000,
            AudioFormat.ENCODING_PCM_16BIT,
            TtsAudioFormat.SupportedApi.EDGE or TtsAudioFormat.SupportedApi.AZURE or TtsAudioFormat.SupportedApi.CREATION,
            true
        ),
        TtsAudioFormat(
            "audio-24khz-96kbitrate-mono-mp3",
            24000,
            AudioFormat.ENCODING_PCM_16BIT,
            TtsAudioFormat.SupportedApi.EDGE or TtsAudioFormat.SupportedApi.AZURE or TtsAudioFormat.SupportedApi.CREATION,
            true
        ),
        TtsAudioFormat(
            "audio-48khz-96kbitrate-mono-mp3",
            48000,
            AudioFormat.ENCODING_PCM_16BIT,
            TtsAudioFormat.SupportedApi.AZURE or TtsAudioFormat.SupportedApi.CREATION,
            true
        ),

        TtsAudioFormat(
            "ogg-16khz-16bit-mono-opus",
            16000 * 2,
            AudioFormat.ENCODING_PCM_16BIT,
            TtsAudioFormat.SupportedApi.AZURE,
            true
        ),
        TtsAudioFormat(
            "ogg-24khz-16bit-mono-opus",
            24000 * 2,
            AudioFormat.ENCODING_PCM_16BIT,
            TtsAudioFormat.SupportedApi.AZURE,
            true
        ),
        TtsAudioFormat(
            "ogg-48khz-16bit-mono-opus",
            48000,
            AudioFormat.ENCODING_PCM_16BIT,
            TtsAudioFormat.SupportedApi.AZURE,
            true
        )
    )

    fun getDefault(): TtsAudioFormat {
        return formats[4] //audio-24khz-48kbitrate-mono-mp3
    }

    /* 通过name查找格式Item */
    fun getFormat(name: String): TtsAudioFormat? {
        formats.forEach { v ->
            if (v.name == name) {
                return v
            }
        }
        return null
    }

    /*fun getAllFormatName(): ArrayList<String> {
        val list = arrayListOf<String>()
        formats.forEach { v ->
            list.add(v.name)
        }
        return list
    }*/

    fun getFormatsBySupportedApi(@TtsAudioFormat.SupportedApi api: Int): ArrayList<String> {
        val list = arrayListOf<String>()
        formats.forEach {
            if (api and it.supportedApi != 0)
                list.add(it.value)
        }

        return list
    }

    /**
     * 判断格式以及采样率是否相同
     */
    fun isFormatSampleEqual(format1: String, format2: String): Boolean {
        return try {
            getFormat(format1)!!.hz == getFormat(format2)!!.hz
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
