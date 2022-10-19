package com.github.jing332.tts_server_android.service.tts.help

import android.media.AudioFormat
import android.speech.tts.SynthesisCallback
import android.speech.tts.SynthesisRequest
import android.speech.tts.TextToSpeech
import android.util.Log
import com.github.jing332.tts_server_android.constant.TtsApiType
import com.github.jing332.tts_server_android.service.tts.TtsService
import com.github.jing332.tts_server_android.utils.GcManager
import com.github.jing332.tts_server_android.utils.NormUtil
import tts_server_lib.CreationApi
import tts_server_lib.EdgeApi

class TtsManager(var ttsConfig: TtsConfig) {
    companion object {
        const val TAG = "TtsManager"
    }

    private var isSynthesizing = false
    private val audioDecode: AudioDecode by lazy { AudioDecode() }
    private val norm: NormUtil by lazy { NormUtil(500F, 0F, 200F, 0F) }

    fun stop() {
        isSynthesizing = false
        audioDecode.stop()
    }

    fun synthesizeText(request: SynthesisRequest?, callback: SynthesisCallback?) {
        isSynthesizing = true
//        val startTime = SystemClock.elapsedRealtime()
//        GlobalScope.launch {
//            Log.e(TAG, "启动一个超时循环")
//            while (isSynthesizing) {
//                val time = SystemClock.elapsedRealtime() - startTime
//                //超时15秒后跳过,保证长句不会被跳过
//                if (time > 15000) {
//                    callback?.done()
//                    stop()
//                }
//            }
//            Log.e(TAG, "已经过超时循环")
//        }

        val text = request?.charSequenceText.toString()
        val rate = "${norm.normalize(request?.speechRate?.toFloat()!!) - 100}%"
        val pitch = "${request.pitch - 100}%"
        val format = TtsFormatManger.getFormat(ttsConfig.format)
        if (format == null) {
            Log.e(TAG, "不支持解码此格式: ${ttsConfig.format}")
            callback!!.start(
                16000,
                AudioFormat.ENCODING_PCM_16BIT, 1
            )
            callback.error(TextToSpeech.ERROR_INVALID_REQUEST)
            return
        }

        callback?.start(format.hz, format.bitRate, 1)
        try {
            val audio = getAudio(ttsConfig.api, text, rate, pitch)
            if (audio != null) {
                Log.i(TAG, "获取音频成功, 大小: ${audio.size / 1024}KB")
                val hz = TtsFormatManger.getFormat(ttsConfig.format)?.hz ?: 16000
                audioDecode.doDecode(
                    audio,
                    hz,
                    onRead = { writeToCallBack(callback!!, it) },
                    error = {
                        Log.e(TAG, "解码失败: $it")
                    })
                Log.i(TAG, "播放完毕")
            } else {
                callback?.error()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            callback?.error()
            GcManager.doGC()
        }
        stop()
    }

    private val mEdgeApi: EdgeApi by lazy { EdgeApi() }
    private val mCreationApi: CreationApi by lazy { CreationApi() }

    private fun getAudio(api: Int, text: String, rate: String, pitch: String): ByteArray? {
        Log.e(TtsService.TAG, "api:$api")
        when (api) {
            TtsApiType.EDGE -> {
                return mEdgeApi.getEdgeAudio(
                    ttsConfig.voiceName,
                    text,
                    rate,
                    pitch,
                    ttsConfig.format
                )
            }
            TtsApiType.CREATION -> {
                val arg = tts_server_lib.CreationArg()
                arg.text = text
                arg.voiceName = ttsConfig.voiceName
                arg.voiceId = ttsConfig.voiceId
                arg.style = "general"
                arg.styleDegree = "1.0"
                arg.role = "default"
                arg.rate = rate
                arg.volume = ttsConfig.volumeToPctString()
                arg.format = ttsConfig.format
                Log.d(TtsService.TAG, arg.toString())
                return mCreationApi.getCreationAudio(arg)
            }
        }
        return null
    }

    private fun writeToCallBack(callback: SynthesisCallback, pcmData: ByteArray) {
        try {
            val maxBufferSize: Int = callback.maxBufferSize
            var offset = 0
            while (offset < pcmData.size && isSynthesizing) {
                val bytesToWrite = maxBufferSize.coerceAtMost(pcmData.size - offset)
                callback.audioAvailable(pcmData, offset, bytesToWrite)
                offset += bytesToWrite
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}