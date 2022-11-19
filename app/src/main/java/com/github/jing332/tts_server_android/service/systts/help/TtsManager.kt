package com.github.jing332.tts_server_android.service.systts.help

import android.content.Context
import android.content.Intent
import android.os.SystemClock
import android.speech.tts.SynthesisCallback
import android.speech.tts.SynthesisRequest
import android.util.Log
import com.github.jing332.tts_server_android.App
import com.github.jing332.tts_server_android.LogLevel
import com.github.jing332.tts_server_android.MyLog
import com.github.jing332.tts_server_android.constant.KeyConst.KEY_DATA
import com.github.jing332.tts_server_android.constant.ReadAloudTarget
import com.github.jing332.tts_server_android.data.SysTtsConfig
import com.github.jing332.tts_server_android.data.SysTtsConfigItem
import com.github.jing332.tts_server_android.data.VoiceProperty
import com.github.jing332.tts_server_android.service.systts.SystemTtsService
import com.github.jing332.tts_server_android.util.NormUtil
import com.github.jing332.tts_server_android.util.StringUtils
import com.github.jing332.tts_server_android.util.limitLength
import com.github.jing332.tts_server_android.util.toastOnUi
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.channels.produce
import kotlin.system.measureTimeMillis

class TtsManager(val context: Context) {
    companion object {
        const val TAG = "TtsManager"

        /* 音频请求间隔 */
        const val requestInterval = 100L
    }

    interface Callback {
        fun onError(title: String, content: String)
        fun onRetrySuccess()
    }

    var callback: Callback? = null

    private var mTtsCfg: SysTtsConfig = SysTtsConfig()
    private val replaceHelper: ReplaceHelper by lazy { ReplaceHelper() }
    private lateinit var mAudioFormat: TtsAudioFormat

    var isSynthesizing = false
    private val mAudioDecoder by lazy { AudioDecoder() }
    private val mNorm by lazy { NormUtil(500F, 0F, 200F, 0F) }
    private val mLib by lazy { SysTtsLib() }


    fun stop() {
        isSynthesizing = false
        mAudioDecoder.stop()
    }

    /* 加载配置 */
    fun loadConfig() {
        mTtsCfg = SysTtsConfig.read()
        Log.d(TAG, "loadConfig: $mTtsCfg")

        mTtsCfg.apply {
            mLib.setTimeout(timeout)
            if (isReplace) {
                replaceHelper.load()
            }

            if (isMultiVoice) {
                var aside = mTtsCfg.currentAsideItem()
                if (aside == null) {
                    context.toastOnUi("警告：缺少{旁白}，使用默认配置！")
                    aside = SysTtsConfigItem(true, ReadAloudTarget.ASIDE)
                    mTtsCfg.list.add(aside)
                }
                var dialogue = mTtsCfg.currentDialogueItem()
                if (dialogue == null) {
                    context.toastOnUi("警告：缺少{对话}，使用默认配置！")
                    dialogue = SysTtsConfigItem(true, ReadAloudTarget.DIALOGUE)
                    mTtsCfg.list.add(dialogue)
                }

                mAudioFormat = TtsFormatManger.getFormat(aside.voiceProperty.format)
                    ?: TtsFormatManger.getDefault()
            } else {
                var cfg = mTtsCfg.selectedItem()
                if (cfg == null) {
                    context.toastOnUi("警告：缺少全局配置，使用默认！")
                    cfg = SysTtsConfigItem(true, ReadAloudTarget.DEFAULT)
                    mTtsCfg.list.add(cfg)
                }
                mAudioFormat = TtsFormatManger.getFormat(cfg.voiceProperty.format)
                    ?: TtsFormatManger.getDefault()
            }
        }
    }


    private var mProducer: ReceiveChannel<ChannelData>? = null

    /* 开始转语音 */
    suspend fun synthesizeText(request: SynthesisRequest?, callback: SynthesisCallback?) {
        isSynthesizing = true
        callback?.start(mAudioFormat.hz, mAudioFormat.bitRate, 1)

        var text = request?.charSequenceText.toString().trim()
        if (mTtsCfg.isReplace) {
            text = replaceHelper.doReplace(text)
        }

        val pitch = request?.pitch?.minus(100) ?: 100
        val sysRate = (mNorm.normalize(request?.speechRate?.toFloat()!!) - 100).toInt()

        mProducer = null
        if (mTtsCfg.isMultiVoice) { //多语音
            Log.d(TAG, "multiVoiceProducer...")
            val aside = mTtsCfg.currentAsideItem()?.voiceProperty?.clone() ?: VoiceProperty()
            aside.prosody.pitch = pitch
            aside.prosody.setRateIfFollowSystem(sysRate)
            val dialogue =
                mTtsCfg.currentDialogueItem()?.voiceProperty?.clone() ?: VoiceProperty()
            dialogue.prosody.pitch = pitch
            dialogue.prosody.setRateIfFollowSystem(sysRate)

            Log.d(TAG, "旁白：${aside}, 对话：${dialogue}")
            mProducer = multiVoiceProducer(mTtsCfg.isSplitSentences, text, aside, dialogue)
        } else { //单语音
            val pro = mTtsCfg.selectedItem()?.voiceProperty?.clone() ?: VoiceProperty()
            pro.prosody.setRateIfFollowSystem(sysRate)
            pro.prosody.pitch = pitch
            Log.d(TAG, "单语音：${pro}")
            if (mTtsCfg.isSplitSentences) {
                Log.d(TAG, "splitSentences...")
                mProducer = splitSentencesProducer(text, pro)
            } else {
                if (mAudioFormat.needDecode) {
                    getAudioAndDecodePlay(text, pro, callback)
                } else {
                    mProducer = audioStreamProducer(text, pro)
                }
            }
        }

        /* 阻塞，接收者 */
        mProducer?.consumeEach { data ->
            val shortText = data.text?.limitLength(20)
            if (!isSynthesizing) {
                shortText?.apply { sendLog(LogLevel.WARN, "系统已取消播放：${shortText}") }
                return@consumeEach
            }
            if (data.audio == null) {
                shortText?.apply { sendLog(LogLevel.WARN, "音频为空：${shortText}") }
            } else {
                if (mAudioFormat.needDecode) {
                    mAudioDecoder.doDecode(
                        srcData = data.audio,
                        sampleRate = mAudioFormat.hz,
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
        StringUtils.splitSentences(text).forEach { splitedText ->
            if (!isSynthesizing) return@produce
            if (!StringUtils.isSilent(splitedText)) {
                getAudioAndSend(this, splitedText, voiceProperty)
                delay(requestInterval)
            }
        }
    }

    /* 多语音生产者 */
    @OptIn(ExperimentalCoroutinesApi::class, DelicateCoroutinesApi::class)
    private fun multiVoiceProducer(
        isSplit: Boolean,
        text: String,
        aside: VoiceProperty,
        dialogue: VoiceProperty
    ): ReceiveChannel<ChannelData> = GlobalScope.produce(Dispatchers.IO, capacity = 100) {
        /* 分割为多语音  */
        val map = VoiceTools.splitMultiVoice(text, aside, dialogue)
        if (isSplit)
            map.forEach {
                StringUtils.splitSentences(it.raText).forEach { splitedText ->
                    if (!isSynthesizing) return@produce
                    if (!StringUtils.isSilent(splitedText)) {
                        getAudioAndSend(this, splitedText, it.voiceProperty)
                        delay(requestInterval)
                    }
                }
            }
        else
            map.forEach {
                if (!isSynthesizing) return@produce
                if (!StringUtils.isSilent(it.raText)) {
                    getAudioAndSend(this, it.raText, it.voiceProperty)
                    delay(requestInterval)
                }
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

    /* 获取音频并发送到Channel */
    private suspend fun getAudioAndSend(
        channel: SendChannel<ChannelData>,
        text: String,
        voiceProperty: VoiceProperty
    ) {
        if (!isSynthesizing) return
        if (mAudioFormat.needDecode) {
            val audio = getAudioHelper(text, voiceProperty)
            channel.send(ChannelData(text, audio))
        } else {
            getAudioStreamHelper(text, voiceProperty) {
                runBlocking { channel.send(ChannelData(null, it)) }
            }
        }
    }

    /* 完整下载 */
    private fun getAudioHelper(
        text: String,
        voiceProperty: VoiceProperty
    ): ByteArray? {
        sendLog(LogLevel.INFO, "<br>请求音频：<b>${text}</b> <br><small><i>${voiceProperty}</small></i>")
        var audio: ByteArray? = null
        val timeCost = measureTimeMillis {
            audio = mLib.getAudioForRetry(
                text,
                voiceProperty,
                100
            ) { reason, num ->
                if (isSynthesizing) {
                    val shortText = text.limitLength(20)
                    sendLog(LogLevel.ERROR, "获取音频失败: <b>${shortText}</b> <br>$reason")
                    if (!reason.startsWith("websocket: close 1006"))
                        if (num > 3) SystemClock.sleep(3000) else SystemClock.sleep(500)
                    "开始第${num}次重试...".let {
                        sendLog(LogLevel.WARN, it)
                        callback?.onError("请求音频失败：$it", "${shortText}\n${reason}")
                    }
                    return@getAudioForRetry true // 重试
                }
                return@getAudioForRetry false //不再重试
            }
        }

        audio?.let {
            sendLog(
                LogLevel.INFO,
                "获取音频成功, 大小: <b>${(audio?.size?.div(1024))}KB</b>, 耗时: <b>${timeCost}ms</b>"
            )
            callback?.onRetrySuccess()
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
            val err = mLib.getAudioStream(text, voiceProperty) { data ->
                if (currentLength >= lastFailLength) {
                    onRead.invoke(data)
                    lastFailLength = -1
                }
                audioSize += data.size
                currentLength += data.size
            }
            if (err == null) {
                sendLog(LogLevel.WARN, "下载完成，大小：${audioSize / 1024}KB")
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
            mAudioDecoder.doDecode(
                audio,
                mAudioFormat.hz,
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
        intent.putExtra(KEY_DATA, MyLog(level, msg))
        App.localBroadcast.sendBroadcast(intent)
    }

    /* 分句缓存Data */
    class ChannelData(val text: String?, val audio: ByteArray?)
}