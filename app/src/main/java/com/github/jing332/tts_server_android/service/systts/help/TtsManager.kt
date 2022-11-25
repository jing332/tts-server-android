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
import com.github.jing332.tts_server_android.data.appDb
import com.github.jing332.tts_server_android.data.entities.SysTts
import com.github.jing332.tts_server_android.help.SysTtsConfig
import com.github.jing332.tts_server_android.model.SysTtsLib
import com.github.jing332.tts_server_android.model.tts.BaseAudioFormat
import com.github.jing332.tts_server_android.model.tts.BaseTTS
import com.github.jing332.tts_server_android.model.tts.HttpTTS
import com.github.jing332.tts_server_android.model.tts.MsTTS
import com.github.jing332.tts_server_android.service.systts.SystemTtsService
import com.github.jing332.tts_server_android.util.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.channels.produce

class TtsManager(val context: Context) {
    companion object {
        const val TAG = "TtsManager"
        private const val CLOSE_1006_PREFIX = "websocket: close 1006"

        // 音频请求间隔
        const val requestInterval = 100L
    }

    interface Callback {
        fun onError(title: String, content: String)
        fun onRetrySuccess()
    }

    var callback: Callback? = null
    var isSynthesizing = false
    private val mAudioDecoder by lazy { AudioDecoder() }
    private val mNorm by lazy { NormUtil(500F, 0F, 200F, 0F) }

    private val mReplacer: ReplaceHelper by lazy { ReplaceHelper() }
    private lateinit var mAudioFormat: BaseAudioFormat

    private val mSysTts by lazy { appDb.sysTtsDao }

    private var mDefaultConfig: SysTts? = null
    private var mAsideConfig: SysTts? = null
    private var mDialogueConfig: SysTts? = null

    private var mIsSplitEnabled = false
    private var mIsReplaceEnabled = false
    private var mIsMultiVoiceEnabled = false
    private var mMinDialogueLen = 0

    fun stop() {
        isSynthesizing = false
        mAudioDecoder.stop()
    }

    /* 加载配置 */
    fun loadConfig() {
        SysTtsConfig.apply {
            mIsSplitEnabled = isSplitEnabled
            mIsMultiVoiceEnabled = isMultiVoiceEnabled
            mIsReplaceEnabled = isReplaceEnabled
            mMinDialogueLen = minDialogueLength
        }

        mSysTts.apply {
            // MsTTS 设置
            SysTtsLib.setUseDnsLookup(SharedPrefsUtils.getUseDnsEdge(context))
            SysTtsLib.setTimeout(SysTtsConfig.requestTimeout)

            if (mIsReplaceEnabled) mReplacer.load()

            if (SysTtsConfig.isMultiVoiceEnabled) {
                mAsideConfig = getByReadAloudTarget(ReadAloudTarget.ASIDE)
                if (mAsideConfig == null) {
                    context.toastOnUi("警告：缺少{旁白}，使用默认配置！")
                    mAsideConfig = SysTts(
                        readAloudTarget = ReadAloudTarget.ASIDE,
                        tts = MsTTS()
                    )
                }
                mDialogueConfig = getByReadAloudTarget(ReadAloudTarget.DIALOGUE)
                if (mDialogueConfig == null) {
                    context.toastOnUi("警告：缺少{对话}，使用默认配置！")
                    mDialogueConfig = SysTts(
                        readAloudTarget = ReadAloudTarget.ASIDE,
                        tts = MsTTS()
                    )
                }

                mAudioFormat = mAsideConfig?.tts?.audioFormat!!
            } else {
                mDefaultConfig = mSysTts.getByReadAloudTarget()
                if (mDefaultConfig == null) {
                    context.toastOnUi("警告：缺少{全部}，使用默认！")
                    mDefaultConfig = SysTts(isEnabled = true, tts = MsTTS())
                }
                mAudioFormat = mDefaultConfig?.tts?.audioFormat!!
            }
        }
    }

    private var mProducer: ReceiveChannel<ChannelData>? = null

    /* 开始转语音 */
    suspend fun synthesizeText(request: SynthesisRequest?, callback: SynthesisCallback?) {
        isSynthesizing = true
        callback?.start(mAudioFormat.sampleRate, mAudioFormat.bitRate, 1)

        var text = request?.charSequenceText.toString().trim()
        if (mIsReplaceEnabled) text = mReplacer.doReplace(text)

        val pitch = request?.pitch?.minus(100) ?: 100
        val sysRate = (mNorm.normalize(request?.speechRate?.toFloat()!!) - 100).toInt()

        mProducer = null
        if (mIsMultiVoiceEnabled) { //多语音
            Log.d(TAG, "multiVoiceProducer...")

            val aside = mAsideConfig?.copy()?.tts
            aside?.pitch = pitch
            if (aside?.rate == BaseTTS.VALUE_WITH_SYSTEM) aside.rate = sysRate

            val dialogue =
                mDialogueConfig?.copy()?.tts
            dialogue?.pitch = pitch
            if (dialogue?.rate == BaseTTS.VALUE_WITH_SYSTEM) dialogue.rate = sysRate

            Log.d(TAG, "旁白：${aside}, 对话：${dialogue}")
            mProducer = multiVoiceProducer(mIsSplitEnabled, text, aside!!, dialogue!!)
        } else { //单语音
            val pro = mDefaultConfig?.copy()?.tts
            pro?.pitch = pitch
            if (pro?.rate == BaseTTS.VALUE_WITH_SYSTEM) pro.rate = sysRate

            Log.d(TAG, "单语音：${pro}")
            if (mIsSplitEnabled) {
                Log.d(TAG, "splitSentences...")
                mProducer = splitSentencesProducer(text, pro!!)
            } else {
                if (mAudioFormat.isNeedDecode) {
                    getAudioAndDecodePlay(text, pro!!, callback)
                } else {
                    mProducer = audioStreamProducer(text, pro!!)
                }
            }
        }

        /* 阻塞，接收者 */
        mProducer?.consumeEach { data ->
            val shortText = data.text?.limitLength(20)
            if (!isSynthesizing) {
                shortText?.apply {
                    if (App.isSysTtsLogEnabled) sendLog(
                        LogLevel.WARN,
                        "系统已取消播放：${shortText}"
                    )
                }
                return@consumeEach
            }

            if (data.audio == null) {
                shortText?.apply {
                    if (App.isSysTtsLogEnabled) sendLog(
                        LogLevel.WARN,
                        "音频为空：${shortText}"
                    )
                }
            } else {
                if (data.isNeedDecode) {
                    mAudioDecoder.doDecode(
                        srcData = data.audio,
                        sampleRate = mAudioFormat.sampleRate,
                        onRead = { writeToCallBack(callback!!, it) },
                        error = {
                            if (App.isSysTtsLogEnabled) sendLog(LogLevel.ERROR, "解码失败: $shortText")
                        })
                    if (App.isSysTtsLogEnabled) sendLog(LogLevel.WARN, "播放完毕：${shortText}")
                } else {
                    writeToCallBack(callback!!, data.audio)
                }
            }
        }

        stop()
    }

    /* 分割长句生产者 */
    @OptIn(ExperimentalCoroutinesApi::class, DelicateCoroutinesApi::class)
    private fun splitSentencesProducer(
        text: String,
        msTtsProperty: BaseTTS
    ): ReceiveChannel<ChannelData> = GlobalScope.produce(Dispatchers.IO, capacity = 100) {
        StringUtils.splitSentences(text).forEach { splitedText ->
            if (!isSynthesizing) return@produce
            if (!StringUtils.isSilent(splitedText)) {
                getAudioAndSend(this, splitedText, msTtsProperty)
                delay(requestInterval)
            }
        }
    }

    /* 多语音生产者 */
    @OptIn(ExperimentalCoroutinesApi::class, DelicateCoroutinesApi::class)
    private fun multiVoiceProducer(
        isSplit: Boolean,
        text: String,
        aside: BaseTTS,
        dialogue: BaseTTS
    ): ReceiveChannel<ChannelData> = GlobalScope.produce(Dispatchers.IO, capacity = 100) {
        /* 分割为多语音  */
        val map =
            VoiceTools.splitMultiVoice(text, aside, dialogue, mMinDialogueLen)
        if (isSplit)
            map.forEach {
                StringUtils.splitSentences(it.speakText).forEach { splitedText ->
                    if (!isSynthesizing) return@produce
                    if (!StringUtils.isSilent(splitedText)) {
                        getAudioAndSend(this, splitedText, it.ttsProperty)
                        delay(requestInterval)
                    }
                }
            }
        else // 不分割
            map.forEach {
                if (!isSynthesizing) return@produce
                if (!StringUtils.isSilent(it.speakText)) {
                    getAudioAndSend(this, it.speakText, it.ttsProperty)
                    delay(requestInterval)
                }
            }
    }

    /* 音频流边下边播生产者 */
    @OptIn(ExperimentalCoroutinesApi::class, DelicateCoroutinesApi::class)
    private fun audioStreamProducer(
        text: String,
        tts: BaseTTS,
    ): ReceiveChannel<ChannelData> = GlobalScope.produce(Dispatchers.IO, capacity = 100) {
        getAudioStreamHelper(text, tts) {
            launch { send(ChannelData(audio = it)) }
        }
    }

    /* 获取音频并发送到Channel */
    private suspend fun getAudioAndSend(
        channel: SendChannel<ChannelData>,
        text: String,
        msTtsProperty: BaseTTS
    ) {
        if (!isSynthesizing) return
        if (msTtsProperty.audioFormat.isNeedDecode) {
            val audio = getAudioHelper(text, msTtsProperty)
            channel.send(ChannelData(text, audio, msTtsProperty.audioFormat.isNeedDecode))
        } else {
            getAudioStreamHelper(text, msTtsProperty) {
                runBlocking {
                    channel.send(
                        ChannelData(
                            null,
                            it,
                            msTtsProperty.audioFormat.isNeedDecode
                        )
                    )
                }
            }
        }
    }

    /* 完整下载 */
    private fun getAudioHelper(
        text: String,
        tts: BaseTTS
    ): ByteArray? {
        if (App.isSysTtsLogEnabled) sendLog(
            LogLevel.INFO,
            "<br>请求音频：<b>${text}</b> <br><small><i>${tts}</small></i>"
        )

        val startTime = System.currentTimeMillis()
        for (retryIndex in 1..100) {
            kotlin.runCatching { tts.getAudio(text) }.onSuccess {
                it?.let {
                    if (App.isSysTtsLogEnabled) sendLog(
                        LogLevel.INFO,
                        "获取音频成功, 大小: <b>${(it.size / 1024)}KB</b>, " +
                                "耗时: <b>${System.currentTimeMillis() - startTime}ms</b>"
                    )
                    callback?.onRetrySuccess()
                }
                return it
            }.onFailure {
                if (!isSynthesizing) return null

                val shortText = text.limitLength(20)
                if (App.isSysTtsLogEnabled) sendLog(
                    LogLevel.ERROR,
                    "获取音频失败: <b>${shortText}</b> <br>${it.message}"
                )
                // 为close 1006则直接跳过等待
                if (it.message?.startsWith(CLOSE_1006_PREFIX) == false)
                    if (retryIndex > 3) SystemClock.sleep(3000) else SystemClock.sleep(500)


                "开始第${retryIndex}次重试...".let { s ->
                    if (App.isSysTtsLogEnabled) sendLog(LogLevel.WARN, s)
                    callback?.onError("请求音频失败：$s", "${shortText}\n${it.message}")
                }

            }
        }

        return null
    }

    /* 音频流 */
    private fun getAudioStreamHelper(
        text: String,
        tts: BaseTTS,
        onRead: (ByteArray?) -> Unit
    ) {
        var lastFailLength = -1
        var audioSize = 0
        for (retryIndex in 1..100) {
            if (!isSynthesizing) return
            if (App.isSysTtsLogEnabled) {
                val s = if (tts is HttpTTS) "" else "(边下边播)"
                sendLog(
                    LogLevel.INFO,
                    "<br>请求音频${s}：<b>${text}</b> <br><small><i>${tts}</small></i>"
                )
            }

            var breakPoint = false
            var currentLength = 0
            kotlin.runCatching {
                breakPoint = tts.getAudioStream(text, 4096) { data ->
                    if (currentLength >= lastFailLength) {
                        onRead.invoke(data)
                        lastFailLength = -1
                    }

                    data?.let {
                        audioSize += it.size
                        currentLength += it.size
                    }
                }
            }.onSuccess {
                if (App.isSysTtsLogEnabled) sendLog(
                    LogLevel.WARN,
                    "下载完成，大小：${audioSize / 1024}KB"
                )
                return
            }.onFailure {
                if (App.isSysTtsLogEnabled) sendLog(
                    LogLevel.ERROR,
                    "请求失败：${text.limitLength(20)}\n${it.message}"
                )
                if (App.isSysTtsLogEnabled) sendLog(LogLevel.WARN, "开始第${retryIndex}次重试...")

                lastFailLength = if (breakPoint) currentLength else -1

                // 1006则跳过等待
                if (it.message?.startsWith(CLOSE_1006_PREFIX) == false)
                    if (retryIndex > 3) SystemClock.sleep(3000) else SystemClock.sleep(500)
            }
        }
    }

    /* 获取音频并解码播放 */
    private fun getAudioAndDecodePlay(
        text: String,
        msTtsProperty: BaseTTS,
        callback: SynthesisCallback?
    ) {
        val audio = getAudioHelper(text, msTtsProperty)
        if (audio != null) {
            mAudioDecoder.doDecode(
                audio,
                mAudioFormat.sampleRate,
                onRead = { writeToCallBack(callback!!, it) },
                error = {
                    if (App.isSysTtsLogEnabled) sendLog(LogLevel.ERROR, "解码失败: $it")
                })
            if (App.isSysTtsLogEnabled) sendLog(LogLevel.WARN, "播放完毕：${text.limitLength(20)}")
        } else {
            if (App.isSysTtsLogEnabled) sendLog(LogLevel.WARN, "音频内容为空或被终止请求")
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
    class ChannelData(
        val text: String? = null,
        val audio: ByteArray?,
        val isNeedDecode: Boolean = true
    )
}