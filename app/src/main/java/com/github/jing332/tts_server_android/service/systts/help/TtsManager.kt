package com.github.jing332.tts_server_android.service.systts.help

import android.content.Context
import android.content.Intent
import android.speech.tts.SynthesisCallback
import android.speech.tts.SynthesisRequest
import android.util.Log
import com.github.jing332.tts_server_android.LogLevel
import com.github.jing332.tts_server_android.MyLog
import com.github.jing332.tts_server_android.constant.ReadAloudTarget
import com.github.jing332.tts_server_android.constant.TtsApiType
import com.github.jing332.tts_server_android.data.SysTtsConfig
import com.github.jing332.tts_server_android.data.SysTtsConfigItem
import com.github.jing332.tts_server_android.data.VoiceProperty
import com.github.jing332.tts_server_android.service.systts.SystemTtsService
import com.github.jing332.tts_server_android.util.NormUtil
import com.github.jing332.tts_server_android.util.longToastOnUi
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.channels.produce
import kotlin.system.measureTimeMillis

class TtsManager(val context: Context) {
    companion object {
        const val TAG = "TtsManager"
    }

    var ttsConfig: SysTtsConfig = SysTtsConfig()
    lateinit var audioFormat: TtsAudioFormat

    var isSynthesizing = false
    private val audioDecode by lazy { AudioDecode() }
    private val norm by lazy { NormUtil(500F, 0F, 200F, 0F) }
    private val sysTtsLib by lazy { SysTtsLib() }

    fun stop() {
        isSynthesizing = false
        audioDecode.stop()
//        if (ttsConfig.list[0].api == TtsApiType.CREATION) {
//            mCreationApi.cancel()
//        }
    }

    /* 加载配置 */
    fun loadConfig() {
        ttsConfig = SysTtsConfig.read()
        Log.d(TAG, "loadConfig: $ttsConfig")

        ttsConfig.apply {
            if (isMultiVoice) {
                var cfgItem = ttsConfig.currentAsideItem()
                if (cfgItem == null) {
                    context.longToastOnUi("错误：缺少朗读对象，使用默认配置！")
                    cfgItem = SysTtsConfigItem()
                    ttsConfig.list.add(cfgItem)
                    ttsConfig.currentAside = ttsConfig.list.size - 1
                }
                audioFormat = TtsFormatManger.getFormat(cfgItem.format)
                    ?: TtsFormatManger.getDefault()
            } else {
                var cfgItem = ttsConfig.selectedItem()
                if (cfgItem == null) {
                    cfgItem = SysTtsConfigItem()
                    ttsConfig.list.add(cfgItem)
                    ttsConfig.currentSelected = ttsConfig.list.size - 1
                }
                audioFormat = TtsFormatManger.getFormat(cfgItem.format)
                    ?: TtsFormatManger.getDefault()
            }
        }
    }

    private var producer: ReceiveChannel<ChannelData>? = null

    @OptIn(ExperimentalCoroutinesApi::class, DelicateCoroutinesApi::class)
    private fun splitSentencesProducer(
        text: String,
        voiceProperty: VoiceProperty,
        format: String
    ): ReceiveChannel<ChannelData> = GlobalScope.produce(capacity = 3) {
        val regex = Regex("[。？?！!;；]")
        val sentences = text.split(regex).filter { it.replace("”", "").isNotBlank() }
        sentences.forEach { splitedText ->
            var audio: ByteArray?
            val timeCost =
                measureTimeMillis { audio = getAudioUseRetry(splitedText, voiceProperty, format) }
            audio?.let {
                sendLog(
                    LogLevel.INFO,
                    "获取音频成功, 大小: ${it.size / 1024}KB, 耗时: ${timeCost}ms"
                )
            }
            send(ChannelData(splitedText, audio))
            delay(500)
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class, DelicateCoroutinesApi::class)
    private fun multiVoiceProducer(
        text: String,
        pitch: String,
        format: String
    ): ReceiveChannel<ChannelData> = GlobalScope.produce(capacity = 3) {
        val aside = ttsConfig.currentAsideItem() ?: SysTtsConfigItem()
        val dialogue = ttsConfig.currentDialogueItem() ?: SysTtsConfigItem()
        var startTime = System.currentTimeMillis()
        sysTtsLib.getAudioForMultiVoice(
            text,
            aside.voiceProperty,
            dialogue.voiceProperty,
            format,
            { // onStart:
                sendLog(LogLevel.INFO, "\n请求音频：$it")
            },
            { raText: String, audio: ByteArray? -> // onAudio:
                val elapsedTime = System.currentTimeMillis() - startTime
                startTime = System.currentTimeMillis()
                audio?.let {
                    sendLog(
                        LogLevel.INFO,
                        "获取音频成功, 大小: ${audio.size / 1024}KB, 耗时: ${elapsedTime}ms"
                    )
                }
                runBlocking {
                    send(ChannelData(raText, audio))
                    delay(500)
                }
            }, // onError:
            { msg, count ->
                if (isSynthesizing) {
                    sendLog(LogLevel.ERROR, "获取音频失败: $msg")
                    sendLog(LogLevel.WARN, "开始第${count}次重试...")
                    true // 重试
                } else {
                    false
                }
            },
        )
    }

    /* 开始转语音 */
    suspend fun synthesizeText(request: SynthesisRequest?, callback: SynthesisCallback?) {
        isSynthesizing = true
        val text = request?.charSequenceText.toString().trim()
        val pitch = "${request?.pitch?.minus(100)}%"

        callback?.start(audioFormat.hz, audioFormat.bitRate, 1)
        val format = audioFormat.value
        producer = null
        if (ttsConfig.isMultiVoice) { // 多声音
            Log.d(TAG, "multiVoiceProducer...")
            producer = multiVoiceProducer(text, pitch, format)
        } else if (ttsConfig.currentSelected == ReadAloudTarget.DEFAULT && ttsConfig.isSplitSentences) { // 分句
            Log.d(TAG, "splitSentences...")
            val voicePro = ttsConfig.selectedItem()?.voiceProperty ?: VoiceProperty("")
            producer = splitSentencesProducer(text, voicePro, audioFormat.value)
        } else { // 不分句
            val voicePro = ttsConfig.selectedItem()?.voiceProperty ?: VoiceProperty("")
            getAudioAndDecodePlay(text, voicePro, audioFormat.value, callback)
        }

        producer?.consumeEach { data ->
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

        stop()
    }

    /* 获取音频并解码播放*/
    private suspend fun getAudioAndDecodePlay(
        text: String,
        voiceProperty: VoiceProperty,
        format: String,
        callback: SynthesisCallback?
    ) {
        val audio: ByteArray?
        val timeCost = measureTimeMillis { audio = getAudioUseRetry(text, voiceProperty, format) }
        if (audio != null) {
            sendLog(LogLevel.INFO, "获取音频成功, 大小: ${audio.size / 1024}KB, 耗时: ${timeCost}ms")
            audioDecode.doDecode(
                audio,
                audioFormat.hz,
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
        voiceProperty: VoiceProperty, format: String
    ): ByteArray? {
        var audio: ByteArray?
        for (i in 1..1000) {
            if (!isSynthesizing) return null
            sendLog(
                LogLevel.INFO, "\n请求音频(${TtsApiType.toString(voiceProperty.api)}): " +
                        "$voiceProperty"
            )
            try {
                audio =
                    sysTtsLib.getAudio(
                        text, voiceProperty,
                        format
                    )
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