package com.github.jing332.tts_server_android.service.tts.help

import android.media.AudioFormat

object TtsFormatManger {
    val formats = arrayListOf<TtsAudioFormat>()

    init {
        formats.add(
            TtsAudioFormat(
                "webm-24khz-16bit-mono-opus",
                24000,
                AudioFormat.ENCODING_PCM_16BIT,
                TtsAudioFormat.SupportedApi(isEdge = true, isAzure = true, isCreation = false),
                true
            )
        )
        formats.add(
            TtsAudioFormat(
                "audio-16khz-32kbitrate-mono-mp3",
                16000,
                AudioFormat.ENCODING_PCM_16BIT,
                TtsAudioFormat.SupportedApi(isEdge = true, isAzure = true, isCreation = true),
                true
            )
        )
        formats.add(
            TtsAudioFormat(
                "audio-24khz-48kbitrate-mono-mp3",
                24000,
                AudioFormat.ENCODING_PCM_16BIT,
                TtsAudioFormat.SupportedApi(isEdge = true, isAzure = true, isCreation = true), true
            )
        )
        formats.add(
            TtsAudioFormat(
                "audio-24khz-96kbitrate-mono-mp3",
                24000,
                AudioFormat.ENCODING_PCM_16BIT,
                TtsAudioFormat.SupportedApi(isEdge = false, isAzure = true, isCreation = true), true
            )
        )

        formats.add(
            TtsAudioFormat(
                "audio-48khz-96kbitrate-mono-mp3",
                48000,
                AudioFormat.ENCODING_PCM_16BIT,
                TtsAudioFormat.SupportedApi(isEdge = false, isAzure = true, isCreation = true), true
            )
        )
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

    fun getAllFormatName(): ArrayList<String> {
        val list = arrayListOf<String>()
        formats.forEach { v ->
            list.add(v.name)
        }
        return list
    }

    fun getFormatsBySupportedApi(api: Int): ArrayList<String> {
        val list = arrayListOf<String>()
        when (api) {
            TtsAudioFormat.API_EDGE -> {
                formats.forEach { if (it.supportedApi.isEdge) list.add(it.value) }
            }
            TtsAudioFormat.API_AZURE -> {
                formats.forEach { if (it.supportedApi.isAzure) list.add(it.value) }
            }
            TtsAudioFormat.API_CREATION -> {
                formats.forEach { if (it.supportedApi.isCreation) list.add(it.value) }
            }
        }

        return list
    }
}