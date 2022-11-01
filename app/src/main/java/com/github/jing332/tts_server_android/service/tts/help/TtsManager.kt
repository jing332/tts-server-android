package com.github.jing332.tts_server_android.service.tts.help

import android.content.Context
import android.content.Intent
import android.speech.tts.SynthesisCallback
import android.speech.tts.SynthesisRequest
import android.util.Log
import com.github.jing332.tts_server_android.LogLevel
import com.github.jing332.tts_server_android.MyLog
import com.github.jing332.tts_server_android.constant.TtsApiType
import com.github.jing332.tts_server_android.service.tts.SystemTtsService
import com.github.jing332.tts_server_android.util.NormUtil
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
    lateinit var audioFormat: TtsAudioFormat

    var isSynthesizing = false
    private val audioDecode: AudioDecode by lazy { AudioDecode() }
    private val norm: NormUtil by lazy { NormUtil(500F, 0F, 200F, 0F) }

    fun stop() {
        isSynthesizing = false
        audioDecode.stop()
        if (ttsConfig.api == TtsApiType.CREATION) {
            mCreationApi.cancel()
        }
    }
    /* 加载配置 */
    fun loadConfig() {
        ttsConfig.loadConfig(context)
        audioFormat = TtsFormatManger.getFormat(ttsConfig.format) ?: TtsFormatManger.getDefault()
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
            var audio: ByteArray?
            val timeCost = measureTimeMillis { audio = getAudioUseRetry(str, rate, pitch) }
            audio?.let {
                sendLog(
                    LogLevel.INFO,
                    "获取音频成功, 大小: ${it.size / 1024}KB, 耗时: ${timeCost}ms"
                )
            }
            send(ChannelData(str, audio))
            delay(500)
        }
    }

    /* 开始转语音 */
    suspend fun synthesizeText(request: SynthesisRequest?, callback: SynthesisCallback?) {
        isSynthesizing = true
        val text = request?.charSequenceText.toString().trim()
        val pitch = "${request?.pitch?.minus(100)}%"
        val rate =
            if (ttsConfig.rate == 0) "${(norm.normalize(request?.speechRate?.toFloat()!!) - 100).toInt()}%"
            else ttsConfig.rateToPcmString()

        callback?.start(audioFormat.hz, audioFormat.bitRate, 1)
        if (ttsConfig.isSplitSentences) { /* 分句 */
            /* 异步获取音频、缓存 */
            producer = producer(text, rate, pitch)
            /* 阻塞接收 */
            producer.consumeEach { data ->
                if (!isSynthesizing) return@consumeEach
                if (data.audio == null) {
                    sendLog(LogLevel.WARN, "音频为空！")
                } else {
                    audioDecode.doDecode(
                        data.audio,
                        audioFormat.hz,
                        onRead = { writeToCallBack(callback!!, it) },
                        error = {
                            sendLog(LogLevel.ERROR, "解码失败: $it")
                        })
                    val str = if (data.text.length > 20)
                        data.text.substring(0, 19) + "···"
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
    private suspend fun getAudioAndDecodePlay(
        text: String,
        rate: String,
        pitch: String,
        callback: SynthesisCallback?
    ) {
        val audio: ByteArray?
        val timeCost = measureTimeMillis { audio = getAudioUseRetry(text, rate, pitch) }
        if (audio != null) {
            sendLog(LogLevel.INFO, "获取音频成功, 大小: ${audio.size / 1024}KB, 耗时: ${timeCost}ms")
            val hz = TtsFormatManger.getFormat(ttsConfig.format)?.hz ?: 16000
            audioDecode.doDecode(
                audio,
                hz,
                onRead = { writeToCallBack(callback!!, it) },
                error = {
                    sendLog(LogLevel.ERROR, "解码失败: $it")
                })
            sendLog(LogLevel.INFO, "播放完毕")
        } else {
            sendLog(LogLevel.WARN, "音频内容为空或被终止请求")
            callback?.done()
        }
    }

    /* 获取音频，失败则重试 */
    private suspend fun getAudioUseRetry(
        text: String,
        rate: String,
        pitch: String,
    ): ByteArray? {
        var audio: ByteArray?
        for (i in 1..1000) {
            if (!isSynthesizing) return null
            try {
                audio = getAudio(ttsConfig.api, text, rate, pitch)
                return audio
            } catch (e: Exception) {
                if (e.message?.endsWith("context canceled") == true) { /* 为主动取消请求 */
                    return null
                } else {
                    e.printStackTrace()
                    sendLog(LogLevel.ERROR, "获取音频失败: ${e.message}")
                }
            }
            sendLog(LogLevel.WARN, "开始第${i}次重试...")
            delay(2000)
        }
        return null
    }

    private val mEdgeApi: EdgeApi by lazy { EdgeApi() }
    private val mAzureApi: AzureApi by lazy { AzureApi() }
    private val mCreationApi: CreationApi by lazy { CreationApi() }

    private fun getAudio(api: Int, text: String, rate: String, pitch: String): ByteArray? {
        val voice = ttsConfig.voiceName
        val style = ttsConfig.voiceStyle.ifEmpty { "general" }
        val styleDegree = "${(ttsConfig.voiceStyleDegree * 0.01).toFloat()}"
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
                            ", styleDegree=$styleDegree" +
                            ", role=${role}, rate=$rate, " +
                            "pitch=$pitch, volume=${volume}, format=$format"
                )
                return mAzureApi.getAudio(
                    voice,
                    text,
                    style,
                    styleDegree,
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
                arg.styleDegree = styleDegree
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

    /* 写入PCM音频到系统组件 */
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

    /* 分句缓存Data */
    class ChannelData(val text: String, val audio: ByteArray?)
}