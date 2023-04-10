package com.github.jing332.tts_server_android.model.speech.tts

import android.media.AudioFormat
import com.github.jing332.tts_server_android.constant.MsTtsApiType

object MsTtsFormatManger {
    private val formats = arrayListOf<MsTtsAudioFormat>(
      /*  MsTtsAudioFormat(
            "raw-8khz-16bit-mono-pcm",
            8000,
            AudioFormat.ENCODING_PCM_16BIT,
            MsTtsAudioFormat.SupportedApi.AZURE,
            false
        ),
        MsTtsAudioFormat(
            "raw-16khz-16bit-mono-pcm",
            16000,
            AudioFormat.ENCODING_PCM_16BIT,
            MsTtsAudioFormat.SupportedApi.AZURE,
            false
        ),
        MsTtsAudioFormat(
            "raw-24khz-16bit-mono-pcm",
            24000,
            AudioFormat.ENCODING_PCM_16BIT,
            MsTtsAudioFormat.SupportedApi.AZURE,
            false
        ),
        MsTtsAudioFormat(
            "raw-48khz-16bit-mono-pcm",
            48000,
            AudioFormat.ENCODING_PCM_16BIT,
            MsTtsAudioFormat.SupportedApi.AZURE,
            false
        ),*/
        MsTtsAudioFormat(
            "webm-16khz-16bit-mono-opus",
            24000 * 2,
            AudioFormat.ENCODING_PCM_16BIT,
            MsTtsAudioFormat.SupportedApi.AZURE,
            true
        ),
        MsTtsAudioFormat(
            "webm-24khz-16bit-mono-opus",
            24000 * 2,
            AudioFormat.ENCODING_PCM_16BIT,
            MsTtsAudioFormat.SupportedApi.EDGE or MsTtsAudioFormat.SupportedApi.AZURE,
            true
        ),
        MsTtsAudioFormat(
            "webm-24khz-16bit-24kbps-mono-opus",
            24000 * 2,
            AudioFormat.ENCODING_PCM_16BIT,
            MsTtsAudioFormat.SupportedApi.AZURE,
            true
        ),
        MsTtsAudioFormat(
            "audio-16khz-32kbitrate-mono-mp3",
            16000,
            AudioFormat.ENCODING_PCM_16BIT,
            MsTtsAudioFormat.SupportedApi.AZURE or MsTtsAudioFormat.SupportedApi.CREATION,
            true
        ),
        MsTtsAudioFormat(
            "audio-24khz-48kbitrate-mono-mp3",
            24000,
            AudioFormat.ENCODING_PCM_16BIT,
            MsTtsAudioFormat.SupportedApi.EDGE or MsTtsAudioFormat.SupportedApi.AZURE or MsTtsAudioFormat.SupportedApi.CREATION,
            true
        ),
        MsTtsAudioFormat(
            "audio-24khz-96kbitrate-mono-mp3",
            24000,
            AudioFormat.ENCODING_PCM_16BIT,
            MsTtsAudioFormat.SupportedApi.EDGE or MsTtsAudioFormat.SupportedApi.AZURE or MsTtsAudioFormat.SupportedApi.CREATION,
            true
        ),
        MsTtsAudioFormat(
            "audio-48khz-96kbitrate-mono-mp3",
            48000,
            AudioFormat.ENCODING_PCM_16BIT,
            MsTtsAudioFormat.SupportedApi.AZURE or MsTtsAudioFormat.SupportedApi.CREATION,
            true
        ),

        MsTtsAudioFormat(
            "ogg-16khz-16bit-mono-opus",
            16000 * 2,
            AudioFormat.ENCODING_PCM_16BIT,
            MsTtsAudioFormat.SupportedApi.AZURE,
            true
        ),
        MsTtsAudioFormat(
            "ogg-24khz-16bit-mono-opus",
            24000 * 2,
            AudioFormat.ENCODING_PCM_16BIT,
            MsTtsAudioFormat.SupportedApi.AZURE,
            true
        ),
        MsTtsAudioFormat(
            "ogg-48khz-16bit-mono-opus",
            48000,
            AudioFormat.ENCODING_PCM_16BIT,
            MsTtsAudioFormat.SupportedApi.AZURE,
            true
        )
    )

    fun getDefault(): MsTtsAudioFormat {
        return formats[4] //audio-24khz-48kbitrate-mono-mp3
    }

    /* 通过name查找格式Item */
    fun getFormat(name: String): MsTtsAudioFormat? {
        formats.forEach { v ->
            if (v.name == name) {
                return v
            }
        }
        return null
    }

    fun getFormatOrDefault(name: String?): MsTtsAudioFormat {
        if (name == null) return getDefault()
        var f = getFormat(name)
        if (f == null) f = getDefault()
        return f
    }

    fun getFormatsBySupportedApi(@MsTtsAudioFormat.SupportedApi api: Int): ArrayList<String> {
        val list = arrayListOf<String>()
        formats.forEach {
            if (api and it.supportedApi != 0)
                list.add(it.value)
        }

        return list
    }

    fun getFormatsByApiType(@MsTtsApiType api: Int): List<String> {
        return getFormatsBySupportedApi(MsTtsAudioFormat.SupportedApi.fromApiType(api))
    }
}
