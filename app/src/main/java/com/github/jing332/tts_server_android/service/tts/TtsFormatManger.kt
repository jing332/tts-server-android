package com.github.jing332.tts_server_android.service.tts

import android.media.AudioFormat

object TtsFormatManger {
    val formats = arrayListOf<TtsOutputFormat>()

    init {
        formats.add(
            TtsOutputFormat(
                "webm-24khz-16bit-mono-opus",
                24000,
                AudioFormat.ENCODING_PCM_16BIT,
                TtsOutputFormat.SupportedApi(isEdge = true, isAzure = true, isCreation = false),
                true
            )
        )
        formats.add(
            TtsOutputFormat(
                "audio-16khz-32kbitrate-mono-mp3",
                16000,
                AudioFormat.ENCODING_PCM_16BIT,
                TtsOutputFormat.SupportedApi(isEdge = true, isAzure = true, isCreation = true),
                true
            )
        )
        formats.add(
            TtsOutputFormat(
                "audio-24khz-48kbitrate-mono-mp3",
                24000,
                AudioFormat.ENCODING_PCM_16BIT,
                TtsOutputFormat.SupportedApi(isEdge = true, isAzure = true, isCreation = true), true
            )
        )
        formats.add(
            TtsOutputFormat(
                "audio-24khz-96kbitrate-mono-mp3",
                24000,
                AudioFormat.ENCODING_PCM_16BIT,
                TtsOutputFormat.SupportedApi(isEdge = true, isAzure = true, isCreation = true), true
            )
        )

        formats.add(
            TtsOutputFormat(
                "audio-48khz-96kbitrate-mono-mp3",
                48000,
                AudioFormat.ENCODING_PCM_16BIT,
                TtsOutputFormat.SupportedApi(isEdge = true, isAzure = true, isCreation = true), true
            )
        )
    }

    /* 通过name查找格式Item */
    fun getFormat(name: String): TtsOutputFormat? {
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
        formats.forEach { v ->
            when (api) {
                TtsOutputFormat.API_EDGE -> {
                    if (v.supportedApi.isEdge){
                        list.add(v.value)
                    }
                }
                TtsOutputFormat.API_AZURE -> {
                    if (v.supportedApi.isAzure){
                        list.add(v.value)
                    }
                }
                TtsOutputFormat.API_CREATION -> {
                    if (v.supportedApi.isCreation){
                        list.add(v.value)
                    }
                }
            }
        }
        return list
    }
}