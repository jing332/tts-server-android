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
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.delay
import tts_server_lib.AzureApi
import tts_server_lib.CreationApi
import tts_server_lib.EdgeApi
import kotlin.system.measureTimeMillis

class TtsManager(val context: Context) {
    companion object {
        const val TAG = "TtsManager"
    }

    var ttsConfig: TtsConfig = TtsConfig().loadConfig(context)
    var isSynthesizing = false
    private val audioDecode: AudioDecode by lazy { AudioDecode() }
    private val norm: NormUtil by lazy { NormUtil(500F, 0F, 200F, 0F) }


    fun stop() {
        isSynthesizing = false
        audioDecode.stop()
    }

    lateinit var producer: ReceiveChannel<ChannelData>

    @OptIn(ExperimentalCoroutinesApi::class, DelicateCoroutinesApi::class)
    private fun producer(
        text: String,
        rate: String,
        pitch: String
    ): ReceiveChannel<ChannelData> = GlobalScope.produce(capacity = 3) {
        val regex = Regex("[。？?！!;；]")
        val sentences = text.split(regex).filter { it.replace("”", "").isNotBlank() }
        sentences.forEach { str ->
            var audioData: ByteArray? = null
            val timeCost = measureTimeMillis {
                for (i in 1..1000) {
                    if (!isSynthesizing)
                        return@produce
                    try {
                        audioData = getAudio(
                            ttsConfig.api, str, rate, pitch
                        )
                        return@measureTimeMillis
                    } catch (e: Exception) {
                        e.printStackTrace()
                        sendLog(LogLevel.ERROR, "获取音频失败: ${e.message}")
                    }
                    sendLog(LogLevel.WARN, "开始第${i}次重试...")
                    delay(2000)
                }
            }
            audioData?.let {
                sendLog(
                    LogLevel.INFO,
                    "获取音频成功, 大小: ${it.size / 1024}KB, 耗时: ${timeCost}ms"
                )
            }
            send(ChannelData(str, audioData))
            delay(500)
        }
    }

    suspend fun synthesizeText(request: SynthesisRequest?, callback: SynthesisCallback?) {
        isSynthesizing = true
        val text = request?.charSequenceText.toString().trim()
        val rate =
            if (ttsConfig.rate == 0) "${(norm.normalize(request?.speechRate?.toFloat()!!) - 100).toInt()}%"
            else ttsConfig.rateToPcmString()

        val pitch = "${request?.pitch?.minus(100)}%"
        val format = TtsFormatManger.getFormat(ttsConfig.format)
        if (format == null) {
            sendLog(LogLevel.ERROR, "不支持解码此格式: ${ttsConfig.format}")
            callback!!.start(
                16000,
                AudioFormat.ENCODING_PCM_16BIT, 1
            )
            callback.error(TextToSpeech.ERROR_INVALID_REQUEST)
            return
        }

        callback?.start(format.hz, format.bitRate, 1)
        if (ttsConfig.isSplitSentences) { /* 分句 */
            /* 异步获取音频、缓存 */
            producer = producer(text, rate, pitch)
            /* 阻塞接收 */
            producer.consumeEach { data ->
                if (!isSynthesizing) return@consumeEach
                if (data.audio == null) {
                    sendLog(LogLevel.WARN, "音频为空！")
                } else {
                    val hz = TtsFormatManger.getFormat(ttsConfig.format)?.hz ?: 16000
                    audioDecode.doDecode(
                        data.audio!!,
                        hz,
                        onRead = { writeToCallBack(callback!!, it) },
                        error = {
                            sendLog(LogLevel.ERROR, "解码失败: $it")
                        })
                    val str = if (data.text.length > 20)
                        data.text.substring(0, 19) + "..."
                    else data.text
                    sendLog(LogLevel.WARN, "播放完毕：$str")
                }
            }
        } else { /* 不使用分段*/
            getAudioAndDecodePlay(text, rate, pitch, callback)
        }

        stop()
    }

    /* 获取音频并解码播放*/
    private fun getAudioAndDecodePlay(
        text: String,
        rate: String,
        pitch: String,
        callback: SynthesisCallback?
    ) {
        var audio: ByteArray? = null
        val timeCost = measureTimeMillis {
            for (i in 1..1000) {
                try {
                    audio = getAudio(ttsConfig.api, text, rate, pitch)
                    return@measureTimeMillis
                } catch (e: Exception) {
                    e.printStackTrace()
                    sendLog(LogLevel.ERROR, "获取音频失败: ${e.message}")
                }
                sendLog(LogLevel.WARN, "开始第${i}次重试...")
                Thread.sleep(2000) // 2s
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
            sendLog(LogLevel.WARN, "音频内容为空！")
            callback?.done()
        }
    }

    private val mEdgeApi: EdgeApi by lazy { EdgeApi() }
    private val mAzureApi: AzureApi by lazy { AzureApi() }
    private val mCreationApi: CreationApi by lazy { CreationApi() }

    private fun getAudio(api: Int, text: String, rate: String, pitch: String): ByteArray? {
        val voice = ttsConfig.voiceName
        val style = ttsConfig.voiceStyle.ifEmpty { "general" }
        val role = ttsConfig.voiceRole.ifEmpty { "default" }
        val volume = ttsConfig.volumeToPctString()
        val format = ttsConfig.format
        when (api) {
            TtsApiType.EDGE -> {
                sendLog(
                    Log.INFO,
                    "\n请求音频(Edge): voiceName=${voice}, text=$text, rate=$rate, " +
                            "pitch=$pitch, volume=${volume}, format=${format}"
                )
                return mEdgeApi.getEdgeAudio(
                    voice,
                    text,
                    rate,
                    pitch,
                    volume,
                    format
                )
            }
            TtsApiType.AZURE -> {
                sendLog(
                    LogLevel.INFO,
                    "\n请求音频(Azure): voiceName=${voice}, text=$text, style=${style}" +
                            ", role=${role}, rate=$rate, " +
                            "pitch=$pitch, volume=${volume}, format=$format"
                )
                return mAzureApi.getAudio(
                    voice,
                    text,
                    style,
                    "1.0",
                    role,
                    rate,
                    pitch,
                    volume,
                    format
                )
            }
            TtsApiType.CREATION -> {
                val arg = tts_server_lib.CreationArg()
                arg.text = text
                arg.voiceName = ttsConfig.voiceName
                arg.voiceId = ttsConfig.voiceId
                arg.style = ttsConfig.voiceStyle
                arg.styleDegree = "1.0"
                arg.role = role
                arg.rate = rate
                arg.volume = volume
                arg.format = format
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
        Log.e(TAG, "$level, $msg")
        val intent = Intent(SystemTtsService.ACTION_ON_LOG)
        intent.putExtra("data", MyLog(level, msg))
        context.sendBroadcast(intent)
    }

    class ChannelData(var text: String, var audio: ByteArray?)
}