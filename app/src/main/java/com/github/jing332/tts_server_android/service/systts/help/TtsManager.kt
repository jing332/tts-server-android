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
import com.github.jing332.tts_server_android.service.systts.SystemTtsService
import com.github.jing332.tts_server_android.service.systts.help.ssml.EdgeSSML
import com.github.jing332.tts_server_android.util.NormUtil
import com.github.jing332.tts_server_android.util.longToastOnUi
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

    var ttsConfig: SysTtsConfig = SysTtsConfig()
    lateinit var audioFormat: TtsAudioFormat

    var isSynthesizing = false
    private val audioDecode: AudioDecode by lazy { AudioDecode() }
    private val norm: NormUtil by lazy { NormUtil(500F, 0F, 200F, 0F) }

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

//    lateinit var multiVoiceProducer: ReceiveChannel<ChannelData>

    @OptIn(ExperimentalCoroutinesApi::class, DelicateCoroutinesApi::class)
    private fun multiVoiceProducer(
        text: String,
        pitch: String
    ): ReceiveChannel<ChannelData> = GlobalScope.produce(capacity = 3) {
        val aside = ttsConfig.currentAsideItem() ?: SysTtsConfigItem()
        val dialogue = ttsConfig.currentDialogueItem() ?: SysTtsConfigItem()
        val asidePro = aside.toVoiceProperty(pitch)
        val dialoguePro = dialogue.toVoiceProperty(pitch)

        val ssmlList = EdgeSSML.genSsmlForMultiVoice(
            text, asidePro, dialoguePro
        )
        ssmlList.forEach {
            sendLog(LogLevel.INFO, "\n发送SSML: ${it.value}")
            var audio: ByteArray? = null
            val timeCost =
                measureTimeMillis {
                    for (i in 1..1000) {
                        if (!isSynthesizing) return@measureTimeMillis
                        try {
                            audio =
                                mEdgeApi.getEdgeAudioBySsml(
                                    it.value,
                                    ttsConfig.selectedItem()?.format
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
            audio?.let { data ->
                sendLog(
                    LogLevel.INFO,
                    "获取音频成功, 大小: ${data.size / 1024}KB, 耗时: ${timeCost}ms"
                )
            }
            send(ChannelData(it.key, audio))
            delay(500)
        }
    }

    /* 开始转语音 */
    suspend fun synthesizeText(request: SynthesisRequest?, callback: SynthesisCallback?) {
        isSynthesizing = true
        val text = request?.charSequenceText.toString().trim()
        val pitch = "${request?.pitch?.minus(100)}%"
        val rate =
            if (ttsConfig.selectedItem()?.rate == 0) "${(norm.normalize(request?.speechRate?.toFloat()!!) - 100).toInt()}%"
            else ttsConfig.selectedItem()?.rateToPcmString()
                ?: "0%"

        callback?.start(audioFormat.hz, audioFormat.bitRate, 1)

        producer = null
        if (ttsConfig.isMultiVoice) { // 多声音
            Log.d(TAG, "multiVoiceProducer...")
            producer = multiVoiceProducer(text, pitch)
        } else if (ttsConfig.currentSelected == ReadAloudTarget.DEFAULT && ttsConfig.isSplitSentences) { // 分句
            Log.d(TAG, "splitSentences...")
            producer = splitSentencesProducer(text, rate, pitch)
        } else { // 不分句
            getAudioAndDecodePlay(text, rate, pitch, callback)
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
        rate: String,
        pitch: String,
        callback: SynthesisCallback?
    ) {
        val audio: ByteArray?
        val timeCost = measureTimeMillis { audio = getAudioUseRetry(text, rate, pitch) }
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
        rate: String,
        pitch: String,
    ): ByteArray? {
        var audio: ByteArray?
        for (i in 1..1000) {
            if (!isSynthesizing) return null
            try {
                audio =
                    getAudio(ttsConfig.selectedItem()?.api ?: TtsApiType.AZURE, text, rate, pitch)
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

    /* 获取音频 */
    private fun getAudio(api: Int, text: String, rate: String, pitch: String): ByteArray? {
        val data = ttsConfig.selectedItem() ?: SysTtsConfigItem()
        val voice = data.voiceName
        val style = data.voiceStyle.ifEmpty { "general" }
        val styleDegree = "${(data.voiceStyleDegree * 0.01).toFloat()}"
        val role = data.voiceRole.ifEmpty { "default" }
        val volume = data.volumeToPctString()
        val format = data.format
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
                arg.voiceName = data.voiceName
                arg.voiceId = data.voiceId
                arg.style = data.voiceStyle
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