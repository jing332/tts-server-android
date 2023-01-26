package com.github.jing332.tts_server_android.service.systts.help

import android.content.Context
import android.os.SystemClock
import android.speech.tts.SynthesisCallback
import android.speech.tts.SynthesisRequest
import android.util.Log
import com.github.jing332.tts_server_android.R
import com.github.jing332.tts_server_android.constant.ReadAloudTarget
import com.github.jing332.tts_server_android.data.appDb
import com.github.jing332.tts_server_android.data.entities.systts.SystemTts
import com.github.jing332.tts_server_android.help.AudioPlayer
import com.github.jing332.tts_server_android.help.SysTtsConfig
import com.github.jing332.tts_server_android.model.tts.BaseAudioFormat
import com.github.jing332.tts_server_android.model.tts.BaseTTS
import com.github.jing332.tts_server_android.model.tts.MsTTS
import com.github.jing332.tts_server_android.util.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.*

class TtsManager(val context: Context) {
    companion object {
        private const val TAG = "TtsManager"
        private const val CLOSE_1006_PREFIX = "websocket: close 1006"
        const val BAD_HANDSHAKE_PREFIX = "websocket: bad handshake"

        const val ERROR_DECODE_FAILED = 0
        const val ERROR_GET_FAILED = 1
        const val ERROR_AUDIO_NULL = 2

        // 音频请求间隔
        private const val requestInterval = 100L

        private val defaultConfig by lazy {
            listOf(SystemTtsWithState(SystemTts(tts = MsTTS())))
        }
    }

    interface EventListener {
        // 开始请求
        fun onStartRequest(text: String, tts: BaseTTS)

        // 请求成功
        fun onRequestSuccess(text: String, size: Int, costTime: Int = 0, retryNum: Int = 1)

        // 错误
        fun onError(errCode: Int, speakText: String? = null, reason: String? = null)

        // 开始重试
        fun onStartRetry(retryNum: Int, message: Throwable)

        // 播放完毕
        fun onPlayDone(text: String? = null)

        // 取消播放
        fun onPlayCanceled(text: String? = null)
    }

    // 事件监听
    var event: EventListener? = null

    // 协程作用域
    private val mScope = CoroutineScope(Job() + Dispatchers.IO)

    // 是否合成中
    var isSynthesizing = false

    // 音频解码器
    private val mAudioDecoder by lazy { AudioDecoder() }

    // 音频播放器
    private var mAudioPlayer: AudioPlayer? = null

    // 归一化，将某个范围值缩小到另一范围 (500-0 -> 200-0)
    private val mNorm by lazy { NormUtil(500F, 0F, 200F, 0F) }

    // 替换
    private val mReplacer: ReplaceHelper by lazy { ReplaceHelper() }

    // 音频格式 合成开始前和解码时使用
    private lateinit var mAudioFormat: BaseAudioFormat

    // Room数据库
    private val mSysTts by lazy { appDb.systemTtsDao }

    // 偏好设置
    private val mCfg = SysTtsConfig

    // TTS
    private val configMap: MutableMap<Int, List<SystemTtsWithState>> = mutableMapOf()

    // 备用TTS
    private val sbyConfigMap: MutableMap<Int, List<SystemTtsWithState>> = mutableMapOf()

    data class SystemTtsWithState(
        val data: SystemTts,
        var isRateFollowSystem: Boolean = false,
        var isPitchFollowSystem: Boolean = false
    )

    /**
     * 停止合成及播放
     */
    fun stop() {
        isSynthesizing = false
        mAudioDecoder.stop()
        mAudioPlayer?.stop()

        configMap.forEach { (_, v) ->
            v.forEach { it.data.tts.onStop() }
        }
    }

    /*
    * 销毁时调用
    * */
    fun destroy() {
        mScope.cancel()
        if (isSynthesizing) stop()

        configMap.forEach { (_, v) ->
            v.forEach { it.data.tts.onDestroy() }
        }
    }

    @Suppress("ReplaceIsEmptyWithIfEmpty")
    private fun initConfigByTarget(@ReadAloudTarget target: Int): Boolean {
        val list = mSysTts.getAllEnabledByTarget(target).map { SystemTtsWithState(it) }

        var isMissing = false
        configMap[target] = if (list.isEmpty()) {
            isMissing = true
            defaultConfig
        } else list

        configMap[target]?.forEach { it.data.tts.onLoad() }

//        val sbyList = mSysTts.getAllEnabledStandbyTts(target).map { SystemTtsWithState(it) }
//        sbyConfigMap[target] = sbyList
//        sbyList.forEach { it.data.tts.onLoad() }

        return isMissing
    }

    /**
     * 加载配置
     */
    @Suppress("ReplaceIsEmptyWithIfEmpty")
    fun loadConfig() {
        mAudioPlayer = mAudioPlayer ?: AudioPlayer(context, mScope)
        mSysTts.apply {
            if (mCfg.isReplaceEnabled) mReplacer.load()

            if (SysTtsConfig.isMultiVoiceEnabled) {
                configMap.remove(ReadAloudTarget.ALL)

                if (initConfigByTarget(ReadAloudTarget.ASIDE))
                    context.toast(R.string.systts_warn_no_ra_aside)

                if (initConfigByTarget(ReadAloudTarget.DIALOGUE))
                    context.toast(R.string.systts_warn_no_ra_dialogue)

                mAudioFormat = configMap[ReadAloudTarget.ASIDE]!![0].data.tts.audioFormat
            } else {
                configMap.remove(ReadAloudTarget.ASIDE)
                configMap.remove(ReadAloudTarget.DIALOGUE)

                if (initConfigByTarget(ReadAloudTarget.DIALOGUE))
                    context.toast(R.string.systts_warn_no_ra_all)

                mAudioFormat = configMap[ReadAloudTarget.ALL]!![0].data.tts.audioFormat
            }
        }
    }

    // 音频生产者
    private var mProducer: ReceiveChannel<ChannelData>? = null

    private fun handleTtsAndConvert(
        list: List<SystemTtsWithState>?,
        rate: Int,
        pitch: Int
    ): List<BaseTTS> {
        list?.forEach {
            val sysTts = it.data

            if (sysTts.tts.isRateFollowSystem() || it.isRateFollowSystem) {
                it.isRateFollowSystem = true
                sysTts.tts.rate = rate
            }

            if (sysTts.tts.isPitchFollowSystem() || it.isPitchFollowSystem) {
                it.isPitchFollowSystem = true
                sysTts.tts.pitch = pitch
            }
        }
        return list?.map { it.data.tts } ?: emptyList()
    }

    /* 开始转语音 */
    suspend fun synthesizeText(
        aText: String, request: SynthesisRequest, callback: SynthesisCallback
    ) {
        isSynthesizing = true
        callback.start(mAudioFormat.sampleRate, mAudioFormat.bitRate, 1)

        val text = if (mCfg.isReplaceEnabled) mReplacer.doReplace(aText) else aText
        val sysPitch = request.pitch - 100
        val sysRate = (mNorm.normalize(request.speechRate.toFloat()) - 100).toInt()

        mProducer = null
        if (mCfg.isMultiVoiceEnabled) { //多语音
            Log.d(TAG, "multiVoiceProducer...")

            val aside = handleTtsAndConvert(configMap[ReadAloudTarget.ASIDE], sysRate, sysPitch)
            val dialogue =
                handleTtsAndConvert(configMap[ReadAloudTarget.DIALOGUE]!!, sysRate, sysPitch)

            Log.d(TAG, "旁白：${aside}, 对话：${dialogue}")
            mProducer = multiVoiceProducer(mCfg.isSplitEnabled, text, aside, dialogue)
        } else { //单语音
            val tts = handleTtsAndConvert(configMap[ReadAloudTarget.ALL]!!, sysRate, sysPitch)[0]

            Log.d(TAG, "单语音：${tts}")
            if (mCfg.isSplitEnabled) {
                Log.d(TAG, "splitSentences...")
                mProducer = splitSentencesProducer(text, tts)
            } else {
                if (tts.isDirectPlay() || mAudioFormat.isNeedDecode)
                    getAudioAndDecodeWrite(text, tts, callback)
                else
                    mProducer = audioStreamProducer(text, tts)
            }
        }

        try {
            /* 阻塞 消费者 */
            mProducer?.consumeEach { data ->
                val shortText = data.text?.limitLength()
                if (!isSynthesizing) {
                    shortText?.let { event?.onPlayCanceled(it) }
                    return@consumeEach
                }

                mScope.launch {
                    if (data.tts.isDirectPlay()) {
                        event?.onStartRequest(data.text!!, data.tts)
                        if (!data.tts.directPlay(data.text!!)) {
                            event?.onError(ERROR_GET_FAILED, shortText)
                            return@launch
                        }
                    } else if (data.audio == null) {
                        event?.onError(ERROR_AUDIO_NULL, shortText)
                        return@launch
                    } else playAudioHelper(data.audio, data.tts.audioFormat, callback)

                    shortText?.let { event?.onPlayDone(it) }
                }.join()
            } // producer

            stop()
        } catch (e: CancellationException) {
            Log.w(TAG, "consume job cancel: ${e.message}")
        }
    }

    /* 分割长句生产者 */
    @OptIn(ExperimentalCoroutinesApi::class)
    private fun splitSentencesProducer(
        text: String, tts: BaseTTS
    ): ReceiveChannel<ChannelData> = mScope.produce(capacity = 1000) {
        try {
            StringUtils.splitSentences(text).forEach { splitedText ->
                if (!isSynthesizing) return@produce
                if (!StringUtils.isSilent(splitedText)) {
                    getAudioAndSend(this, splitedText, tts)
                    delay(requestInterval)
                }
            }
        } catch (e: CancellationException) {
            Log.w(TAG, "splitSentences producer job cancel: ${e.message}")
        }
    }

    /* 多语音生产者 */
    @OptIn(ExperimentalCoroutinesApi::class)
    private fun multiVoiceProducer(
        isSplit: Boolean,
        text: String,
        aside: List<BaseTTS>,
        dialogue: List<BaseTTS>,
    ): ReceiveChannel<ChannelData> = mScope.produce(capacity = 1000) {
        try {
            /* 分割为多语音  */
            val map = VoiceTools.splitMultiVoice(text, aside, dialogue, mCfg.minDialogueLength)
            if (isSplit) map.forEach {
                StringUtils.splitSentences(it.speakText).forEach { splitedText ->
                    if (!isSynthesizing) return@produce
                    if (!StringUtils.isSilent(splitedText)) {
                        getAudioAndSend(this, splitedText, it.tts)
                        delay(requestInterval)
                    }
                }
            }
            else // 不分割长句
                map.forEach {
                    if (!isSynthesizing) return@produce
                    if (!StringUtils.isSilent(it.speakText)) {
                        getAudioAndSend(this, it.speakText, it.tts)
                        delay(requestInterval)
                    }
                }
        } catch (e: CancellationException) {
            Log.w(TAG, "multiVoice producer job cancel: ${e.message}")
        }
    }

    /* 音频流边下边播生产者 */
    @OptIn(ExperimentalCoroutinesApi::class)
    private fun audioStreamProducer(
        text: String,
        tts: BaseTTS,
    ): ReceiveChannel<ChannelData> = mScope.produce(capacity = 1000) {
        try {
            getAudioStreamHelper(text, tts) {
                launch { send(ChannelData(audio = it, tts = tts)) }
            }
        } catch (e: CancellationException) {
            Log.w(TAG, "audioStreamProducer job cancel: ${e.message}")
        }
    }

    // 获取音频并发送到Channel
    private suspend fun getAudioAndSend(
        channel: SendChannel<ChannelData>, text: String, tts: BaseTTS
    ) {
        if (!isSynthesizing) return

        if (tts.isDirectPlay()) {
            channel.send(ChannelData(text, null, tts))
        } else if (tts.audioFormat.isNeedDecode) {
            val audio = getAudioForRetry(text, tts)
            channel.send(ChannelData(text, audio, tts))
        } else {
            getAudioStreamHelper(text, tts) {
                runBlocking { channel.send(ChannelData(null, it, tts)) }
            }
        }
    }

    // 完整下载
    private suspend fun getAudioForRetry(
        text: String, tts: BaseTTS
    ): ByteArray? {
        event?.onStartRequest(text, tts)

        val startTime = SystemClock.elapsedRealtime()
        for (retryIndex in 1..100) {
            kotlin.runCatching {
                val audio = tts.getAudio(text) ?: throw Exception("audio null")
                audio
            }.onSuccess {
                event?.onRequestSuccess(
                    text, it.size,
                    (SystemClock.elapsedRealtime() - startTime).toInt(), retryIndex
                )
                return it
            }.onFailure { e ->
                if (!isSynthesizing) return null

                val shortText = text.limitLength(20)
                event?.onError(ERROR_GET_FAILED, shortText, e.message)

                // 音频为空时至多重试两次
                if (e.message == "audio null" && retryIndex > 2) return null

                // 为close 1006则直接跳过等待
                if (e.message.toString().startsWith(CLOSE_1006_PREFIX)) {
                    event?.onStartRetry(retryIndex, e)
                    return@onFailure
                }

                if (retryIndex > 3) delay(3000)
                else delay(requestInterval)

                event?.onStartRetry(retryIndex, e)
            }
        }

        return null
    }

    // 音频流
    private fun getAudioStreamHelper(
        text: String, tts: BaseTTS, onRead: (ByteArray?) -> Unit
    ) {
        var lastFailLength = -1
        var audioSize = 0
        for (retryIndex in 1..100) {
            if (!isSynthesizing) return

            event?.onStartRequest(text, tts)

            val breakPoint = tts is MsTTS // 是否续播
            var currentLength = 0
            kotlin.runCatching {
                tts.getAudioStream(text, 8192) { data ->
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
                event?.onRequestSuccess(text, audioSize, -1)
                return
            }.onFailure {
                event?.onError(ERROR_GET_FAILED, text, it.message)
                // 是否断点
                lastFailLength = if (breakPoint) currentLength else -1
                // 1006则跳过等待
                if (!it.message.toString()
                        .startsWith(CLOSE_1006_PREFIX)
                ) if (retryIndex > 3) SystemClock.sleep(
                    3000
                ) else SystemClock.sleep(500)

                event?.onStartRetry(retryIndex, it)
            }
        }
    }

    /* 获取音频并直接解码播放 */
    private suspend fun getAudioAndDecodeWrite(
        text: String, tts: BaseTTS, callback: SynthesisCallback
    ) {
        if (tts.isDirectPlay()) {
            event?.onStartRequest(text, tts)
            tts.directPlay(text)
            event?.onPlayDone(text)
        } else {
            val audio = getAudioForRetry(text, tts)
            if (audio != null) {
                playAudioHelper(audio, tts.audioFormat, callback)
                event?.onPlayDone(text)
            } else event?.onError(ERROR_AUDIO_NULL, text)
        }

    }

    // 播放音频
    private suspend fun playAudioHelper(
        audio: ByteArray,
        format: BaseAudioFormat,
        callback: SynthesisCallback
    ) {
        if (!format.isNeedDecode) {
            writeToCallBack(callback, audio)
            return
        }

        if (mCfg.isInAppPlayAudio) {
            mAudioPlayer!!.play(audio, mCfg.inAppPlaySpeed, mCfg.inAppPlayPitch)
        } else {
            mAudioDecoder.doDecode(audio,
                mAudioFormat.sampleRate,
                onRead = { writeToCallBack(callback, it) },
                error = { event?.onError(ERROR_DECODE_FAILED, it) })
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

    /* 生产的数据 */
    class ChannelData(
        val text: String? = null,
        val audio: ByteArray?,
        val tts: BaseTTS,
    )

}