package com.github.jing332.tts_server_android.service.systts.help

import android.content.Context
import android.content.Intent
import android.os.SystemClock
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
import com.github.jing332.tts_server_android.util.limitLength
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
                var aside = ttsConfig.currentAsideItem()
                if (aside == null) {
                    context.longToastOnUi("警告：缺少{旁白}，使用默认配置！")
                    aside = SysTtsConfigItem(true, ReadAloudTarget.ASIDE)
                    ttsConfig.list.add(aside)
                }
                var dialogue = ttsConfig.currentDialogueItem()
                if (dialogue == null) {
                    context.longToastOnUi("警告：缺少{对话}，使用默认配置！")
                    dialogue = SysTtsConfigItem(true, ReadAloudTarget.DIALOGUE)
                    ttsConfig.list.add(dialogue)
                }
                /* 格式校验 */
                if (!TtsFormatManger.isFormatSampleEqual(aside.format, dialogue.format)) {
                    context.longToastOnUi("警告：旁白与对话 格式采样率不相等，\n使用默认格式！")
                    aside.format = TtsAudioFormat.DEFAULT
                    dialogue.format = TtsAudioFormat.DEFAULT
                }

                audioFormat = TtsFormatManger.getFormat(aside.format)
                    ?: TtsFormatManger.getDefault()
            } else {
                var cfg = ttsConfig.selectedItem()
                if (cfg == null) {
                    context.longToastOnUi("警告：缺少全局配置，使用默认！")
                    cfg = SysTtsConfigItem(true, ReadAloudTarget.DEFAULT)
                    ttsConfig.list.add(cfg)
                }
                audioFormat = TtsFormatManger.getFormat(cfg.format)
                    ?: TtsFormatManger.getDefault()
            }
        }
    }


    private var producer: ReceiveChannel<ChannelData>? = null

    /* 开始转语音 */
    suspend fun synthesizeText(request: SynthesisRequest?, callback: SynthesisCallback?) {
        isSynthesizing = true
        callback?.start(audioFormat.hz, audioFormat.bitRate, 1)

        val text = request?.charSequenceText.toString().trim()
        val pitch = request?.pitch?.minus(100) ?: 100
        val sysRate = (norm.normalize(request?.speechRate?.toFloat()!!) - 100).toInt()

        val format = audioFormat.value

        producer = null
        if (ttsConfig.isMultiVoice) { //多语音
            Log.d(TAG, "multiVoiceProducer...")
            val aside = ttsConfig.currentAsideItem()?.voiceProperty?.clone() ?: VoiceProperty()
            aside.prosody.pitch = pitch
            aside.prosody.setRateIfFollowSystem(sysRate)
            val dialogue =
                ttsConfig.currentDialogueItem()?.voiceProperty?.clone() ?: VoiceProperty()
            dialogue.prosody.pitch = pitch
            dialogue.prosody.setRateIfFollowSystem(sysRate)

            Log.d(TAG, "旁白：${aside}, 对话：${dialogue}")
            producer = multiVoiceProducer(text, format, aside, dialogue)
        } else {
            val pro = ttsConfig.selectedItem()?.voiceProperty?.clone() ?: VoiceProperty()
            pro.prosody.setRateIfFollowSystem(sysRate)
            pro.prosody.pitch = pitch
            Log.d(TAG, "单语音：${pro}")
            if (ttsConfig.isSplitSentences && ttsConfig.selectedItem()?.readAloudTarget == ReadAloudTarget.DEFAULT) {
                Log.d(TAG, "splitSentences...")
                producer = splitSentencesProducer(text, format, pro)
            } else {
                if (audioFormat.needDecode) {
                    getAudioAndDecodePlay(text, pro, format, callback)
                } else {
                    getAudioStreamAndPlay(text, pro, format, callback)
                }
            }
        }

        /* 阻塞，接收者 */
        producer?.consumeEach { data ->
            val shortText = data.text.limitLength(20)
            if (!isSynthesizing) {
                sendLog(LogLevel.WARN, "系统已取消播放：${shortText}")
                return@consumeEach
            }
            if (data.audio == null) {
                sendLog(LogLevel.WARN, "音频为空：${shortText}")
            } else {
                audioDecode.doDecode(
                    srcData = data.audio,
                    sampleRate = audioFormat.hz,
                    onRead = { writeToCallBack(callback!!, it) },
                    error = {
                        sendLog(LogLevel.ERROR, "解码失败: $shortText")
                    })
                sendLog(LogLevel.WARN, "播放完毕：${shortText}")
            }
        }

        stop()
    }

    /* 分割长句生产者 */
    @OptIn(ExperimentalCoroutinesApi::class, DelicateCoroutinesApi::class)
    private fun splitSentencesProducer(
        text: String,
        format: String,
        voiceProperty: VoiceProperty
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

    /* 多语音生产者 */
    @OptIn(ExperimentalCoroutinesApi::class, DelicateCoroutinesApi::class)
    private fun multiVoiceProducer(
        text: String,
        format: String,
        aside: VoiceProperty,
        dialogue: VoiceProperty
    ): ReceiveChannel<ChannelData> = GlobalScope.produce(capacity = 3) {
        /* 分割为多语音  */
        val map = VoiceTools.splitMultiVoice(text, aside, dialogue)
        map.forEach {
            sendLog(LogLevel.INFO, "\n请求音频：${it.raText}\n${it.voiceProperty}")
            var audio: ByteArray? = null
            val timeCost = measureTimeMillis {
                audio = sysTtsLib.getAudioForRetry(
                    it.raText,
                    it.voiceProperty,
                    format,
                    100
                ) { reason, num ->
                    if (isSynthesizing) {
                        sendLog(LogLevel.ERROR, "获取音频失败: ${it.raText.limitLength(20)}\n$reason")
                        SystemClock.sleep(3000)
                        sendLog(LogLevel.WARN, "开始第${num}次重试...")
                        return@getAudioForRetry true // 重试
                    }
                    return@getAudioForRetry false //继续重试
                }
            }

            audio?.let {
                sendLog(
                    LogLevel.INFO,
                    "获取音频成功, 大小: ${(audio?.size?.div(1024))}KB, 耗时: ${timeCost}ms"
                )
            }
            runBlocking {
                send(ChannelData(it.raText, audio))
                delay(500)
            }
        }
    }

    /* 获取音频并解码播放*/
    private fun getAudioAndDecodePlay(
        text: String,
        voiceProperty: VoiceProperty,
        format: String,
        callback: SynthesisCallback?
    ) {
        val audio: ByteArray?
        val timeCost =
            measureTimeMillis { audio = getAudioUseRetry(text, voiceProperty, format) }
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

    private fun getAudioStreamAndPlay(
        text: String,
        voiceProperty: VoiceProperty,
        format: String,
        callback: SynthesisCallback?
    ) {
        for (i in 1..3) {
            sendLog(Log.INFO, "\n请求音频(Azure边下边播)：$text\n$voiceProperty")
            val err = sysTtsLib.getAudioStream(text, voiceProperty, format) { data ->
                writeToCallBack(callback!!, data)
            }
            if (err == null) {
                sendLog(LogLevel.WARN, "播放完毕：${text.limitLength(20)}")
                break
            } else {
                sendLog(LogLevel.ERROR, "请求失败：${text.limitLength(20)}\n$err")
                sendLog(LogLevel.WARN, "开始第${i}次重试...")
            }
        }
        callback?.done()
    }

    /* 获取音频，失败则重试 */
    private fun getAudioUseRetry(
        text: String,
        voiceProperty: VoiceProperty, format: String
    ): ByteArray? {
        sendLog(
            LogLevel.INFO, "\n请求音频(${TtsApiType.toString(voiceProperty.api)}): " +
                    "$voiceProperty"
        )
        return sysTtsLib.getAudioForRetry(
            text, voiceProperty,
            format, 100
        ) { reason, num ->
            if (!isSynthesizing || reason.endsWith("context canceled")) {
                return@getAudioForRetry false/* 为主动取消请求 */
            } else {
                sendLog(LogLevel.ERROR, "获取音频失败: $reason")
                SystemClock.sleep(3000)
                sendLog(LogLevel.WARN, "开始第${num}次重试...")
            }
            return@getAudioForRetry true
        }
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