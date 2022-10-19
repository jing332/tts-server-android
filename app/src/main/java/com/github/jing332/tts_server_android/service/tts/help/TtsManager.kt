package com.github.jing332.tts_server_android.service.tts.help

import android.content.Context
import android.content.Intent
import android.media.AudioFormat
import android.speech.tts.SynthesisCallback
import android.speech.tts.SynthesisRequest
import android.speech.tts.TextToSpeech
import android.util.Log
import com.github.jing332.tts_server_android.LogLevel
import com.github.jing332.tts_server_android.MyLog
import com.github.jing332.tts_server_android.constant.TtsApiType
import com.github.jing332.tts_server_android.service.tts.SystemTtsService
import com.github.jing332.tts_server_android.utils.NormUtil
import tts_server_lib.CreationApi
import tts_server_lib.EdgeApi
import kotlin.system.measureTimeMillis

class TtsManager(val context: Context) {
    companion object {
        const val TAG = "TtsManager"
    }

    var ttsConfig: TtsConfig = TtsConfig().loadConfig(context)
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


        var audio: ByteArray? = null
        val timeCost = measureTimeMillis {
            try {
                audio = getAudio(ttsConfig.api, text, rate, pitch)
            } catch (e: Exception) {
                e.printStackTrace()
                sendLog(LogLevel.ERROR, "获取音频失败: ${e.message}")
            }
        }

        if (audio != null) {
            sendLog(LogLevel.INFO, "获取音频成功, 大小: ${audio!!.size / 1024}KB, 耗时: ${timeCost}ms")
            val hz = TtsFormatManger.getFormat(ttsConfig.format)?.hz ?: 16000
            audioDecode.doDecode(
                audio!!,
                hz,
                onRead = { writeToCallBack(callback!!, it) },
                error = {
                    sendLog(LogLevel.ERROR, "解码失败: $it")
                })
            sendLog(LogLevel.INFO, "播放完毕")
        } else {
            sendLog(LogLevel.ERROR, "音频内容为空！")
//            callback?.error()
            callback?.done()
        }
        stop()
    }

    private val mEdgeApi: EdgeApi by lazy { EdgeApi() }
    private val mCreationApi: CreationApi by lazy { CreationApi() }

    private fun getAudio(api: Int, text: String, rate: String, pitch: String): ByteArray? {
        when (api) {
            TtsApiType.EDGE -> {
                sendLog(
                    Log.INFO,
                    "\n请求音频(Edge): voiceName=${ttsConfig.voiceName}, text=$text, rate=$rate, pitch=$pitch, format=${ttsConfig.format}"
                )
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
                sendLog(LogLevel.INFO, "\n请求音频: $arg")
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

    private fun sendLog(level: Int, msg: String) {
        val intent = Intent(SystemTtsService.ACTION_ON_LOG)
        intent.putExtra("data", MyLog(level, msg))
        context.sendBroadcast(intent)
    }
}