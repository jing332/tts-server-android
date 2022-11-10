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
import com.github.jing332.tts_server_android.data.SysTtsConfig
import com.github.jing332.tts_server_android.data.SysTtsConfigItem
import com.github.jing332.tts_server_android.data.VoiceProperty
import com.github.jing332.tts_server_android.service.systts.SystemTtsService
import com.github.jing332.tts_server_android.util.NormUtil
import com.github.jing332.tts_server_android.util.limitLength
import com.github.jing332.tts_server_android.util.toastOnUi
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
                    context.toastOnUi("警告：缺少{旁白}，使用默认配置！")
                    aside = SysTtsConfigItem(true, ReadAloudTarget.ASIDE)
                    ttsConfig.list.add(aside)
                }
                var dialogue = ttsConfig.currentDialogueItem()
                if (dialogue == null) {
                    context.toastOnUi("警告：缺少{对话}，使用默认配置！")
                    dialogue = SysTtsConfigItem(true, ReadAloudTarget.DIALOGUE)
                    ttsConfig.list.add(dialogue)
                }

                audioFormat = TtsFormatManger.getFormat(aside.voiceProperty.format)
                    ?: TtsFormatManger.getDefault()
            } else {
                var cfg = ttsConfig.selectedItem()
                if (cfg == null) {
                    context.toastOnUi("警告：缺少全局配置，使用默认！")
                    cfg = SysTtsConfigItem(true, ReadAloudTarget.DEFAULT)
                    ttsConfig.list.add(cfg)
                }
                audioFormat = TtsFormatManger.getFormat(cfg.voiceProperty.format)
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
            producer = multiVoiceProducer(text, aside, dialogue)
        } else { //单语音
            val pro = ttsConfig.selectedItem()?.voiceProperty?.clone() ?: VoiceProperty()
            pro.prosody.setRateIfFollowSystem(sysRate)
            pro.prosody.pitch = pitch
            Log.d(TAG, "单语音：${pro}")
            if (ttsConfig.isSplitSentences && ttsConfig.selectedItem()?.readAloudTarget == ReadAloudTarget.DEFAULT) {
                Log.d(TAG, "splitSentences...")
                producer = splitSentencesProducer(text, pro)
            } else {
                if (audioFormat.needDecode) {
                    getAudioAndDecodePlay(text, pro, callback)
                } else {
                    producer = audioStreamProducer(text, pro)
                }
            }
        }

        /* 阻塞，接收者 */
        producer?.consumeEach { data ->
            val shortText = data.text?.limitLength(20)
            if (!isSynthesizing) {
                shortText?.apply { sendLog(LogLevel.WARN, "系统已取消播放：${shortText}") }
                return@consumeEach
            }
            if (data.audio == null) {
                shortText?.apply { sendLog(LogLevel.WARN, "音频为空：${shortText}") }
            } else {
                if (audioFormat.needDecode) {
                    audioDecode.doDecode(
                        srcData = data.audio,
                        sampleRate = audioFormat.hz,
                        onRead = { writeToCallBack(callback!!, it) },
                        error = {
                            sendLog(LogLevel.ERROR, "解码失败: $shortText")
                        })
                    sendLog(LogLevel.WARN, "播放完毕：${shortText}")
                } else
                    writeToCallBack(callback!!, data.audio)
            }
        }

        stop()
    }

    /* 分割长句生产者 */
    @OptIn(ExperimentalCoroutinesApi::class, DelicateCoroutinesApi::class)
    private fun splitSentencesProducer(
        text: String,
        voiceProperty: VoiceProperty
    ): ReceiveChannel<ChannelData> = GlobalScope.produce(Dispatchers.IO, capacity = 100) {
        val regex = Regex("[。？?！!;；]")
        val sentences = text.split(regex).filter { it.replace("”", "").isNotBlank() }
        sentences.forEach { splitedText ->
            if (audioFormat.needDecode) {
                val audio = getAudioHelper(splitedText, voiceProperty)
                send(ChannelData(splitedText, audio))
            } else {
                getAudioStreamHelper(splitedText, voiceProperty) {
                    runBlocking { send(ChannelData(null, it)) }
                }
            }
            delay(500)
        }
    }

    /* 多语音生产者 */
    @OptIn(ExperimentalCoroutinesApi::class, DelicateCoroutinesApi::class)
    private fun multiVoiceProducer(
        text: String,
        aside: VoiceProperty,
        dialogue: VoiceProperty
    ): ReceiveChannel<ChannelData> = GlobalScope.produce(Dispatchers.IO, capacity = 100) {
        /* 分割为多语音  */
        val map = VoiceTools.splitMultiVoice(text, aside, dialogue)
        map.forEach {
            if (audioFormat.needDecode) {
                val audio = getAudioHelper(it.raText, it.voiceProperty)
                send(ChannelData(it.raText, audio))
            } else {
                getAudioStreamHelper(it.raText, it.voiceProperty) {
                    runBlocking {
                        send(ChannelData(null, it))
                    }
                }
            }
            delay(500)
        }
    }

    /* 音频流边下边播生产者 */
    @OptIn(ExperimentalCoroutinesApi::class, DelicateCoroutinesApi::class)
    private fun audioStreamProducer(
        text: String,
        voiceProperty: VoiceProperty
    ): ReceiveChannel<ChannelData> = GlobalScope.produce(Dispatchers.IO, capacity = 100) {
        getAudioStreamHelper(
            text,
            voiceProperty
        ) { launch(Dispatchers.IO) { send(ChannelData(null, it)) } }
    }

    /* 完整下载 */
    private fun getAudioHelper(
        text: String,
        voiceProperty: VoiceProperty
    ): ByteArray? {
        sendLog(LogLevel.INFO, "<br>请求音频：<b>${text}</b> <br><small><i>${voiceProperty}</small></i>")
        var audio: ByteArray? = null
        val timeCost = measureTimeMillis {
            audio = sysTtsLib.getAudioForRetry(
                text,
                voiceProperty,
                100
            ) { reason, num ->
                if (isSynthesizing) {
                    sendLog(LogLevel.ERROR, "获取音频失败: ${text.limitLength(20)}\n$reason")
                    if (num > 3) SystemClock.sleep(3000) else SystemClock.sleep(500)
                    sendLog(LogLevel.WARN, "开始第${num}次重试...")
                    return@getAudioForRetry true // 重试
                }
                return@getAudioForRetry false //继续重试
            }
        }

        audio?.let {
            sendLog(
                LogLevel.INFO,
                "获取音频成功, 大小: <b>${(audio?.size?.div(1024))}KB</b>, 耗时: <b>${timeCost}ms</b>"
            )
        }
        return audio
    }

    /* 音频流 */
    private fun getAudioStreamHelper(
        text: String,
        voiceProperty: VoiceProperty,
        onRead: (ByteArray) -> Unit
    ) {
        var lastFailLength = -1
        var audioSize = 0
        for (i in 1..3) {
            if (!isSynthesizing) return
            sendLog(
                LogLevel.INFO,
                "<br>请求音频(Azure边下边播)：<b>${text}</b> <br><small><i>${voiceProperty}</small></i>"
            )
            var currentLength = 0
            val err = sysTtsLib.getAudioStream(text, voiceProperty) { data ->
                if (currentLength >= lastFailLength) {
                    onRead.invoke(data)
                    lastFailLength = -1
                }
                audioSize += data.size
                currentLength += data.size
            }
            if (err == null) {
                sendLog(LogLevel.WARN, "播放完毕(${audioSize / 1024}KB)：${text.limitLength(20)}")
                break
            } else {
                sendLog(LogLevel.ERROR, "请求失败：${text.limitLength(20)}\n$err")
                sendLog(LogLevel.WARN, "开始第${i}次重试...")
                lastFailLength = currentLength
            }
        }
    }

    /* 获取音频并解码播放 */
    private fun getAudioAndDecodePlay(
        text: String,
        voiceProperty: VoiceProperty,
        callback: SynthesisCallback?
    ) {
        val audio = getAudioHelper(text, voiceProperty)
        if (audio != null) {
            audioDecode.doDecode(
                audio,
                audioFormat.hz,
                onRead = { writeToCallBack(callback!!, it) },
                error = {
                    sendLog(LogLevel.ERROR, "解码失败: $it")
                })
            sendLog(LogLevel.WARN, "播放完毕：${text.limitLength(20)}")
        } else {
            sendLog(LogLevel.WARN, "音频内容为空或被终止请求")
            callback?.done()
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
    class ChannelData(val text: String?, val audio: ByteArray?)
}