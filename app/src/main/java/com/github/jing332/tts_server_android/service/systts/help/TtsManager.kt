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
import com.github.jing332.tts_server_android.help.audio.AudioDecoder
import com.github.jing332.tts_server_android.help.audio.AudioPlayer
import com.github.jing332.tts_server_android.help.config.SysTtsConfig
import com.github.jing332.tts_server_android.model.tts.BaseAudioFormat
import com.github.jing332.tts_server_android.model.tts.BaseTTS
import com.github.jing332.tts_server_android.model.tts.BgmTTS
import com.github.jing332.tts_server_android.model.tts.MsTTS
import com.github.jing332.tts_server_android.util.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.*
import kotlin.system.measureTimeMillis

class TtsManager(val context: Context) {
    companion object {
        private const val TAG = "TtsManager"
        private const val CLOSE_1006_PREFIX = "websocket: close 1006"

        private const val AUDIO_NULL_MESSAGE = "AUDIO_NULL_MESSAGE"

        const val BAD_HANDSHAKE_PREFIX = "websocket: bad handshake"

        const val ERROR_DECODE = 0
        const val ERROR_GET = 1
        const val ERROR_AUDIO = 2
        const val ERROR_REPLACE = 3
        const val ERROR_LOAD_CONFIG = 4

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
        fun onRequestSuccess(
            text: String? = null,
            size: Int = 0,
            costTime: Int = 0,
            retryNum: Int = 2
        )

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

    // 是否合成中
    var isSynthesizing = false

    // 协程作用域
    private val mScope = CoroutineScope(Job() + Dispatchers.IO)

    // 音频解码器
    private val mAudioDecoder by lazy { AudioDecoder() }

    // 音频播放器
    private var mAudioPlayer: AudioPlayer? = null

    private val mBgmPlayer by lazy { BgmPlayer(context) }

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

    // BGM TTS
    private val bgmMap: MutableList<SystemTts> = mutableListOf()

    data class SystemTtsWithState(
        val data: SystemTts,
        var isRateFollowSystem: Boolean = false,
        var isPitchFollowSystem: Boolean = false
    )

    /**
     * 停止合成及播放
     */
    fun stop(fromUser: Boolean = false) {
        isSynthesizing = false
        mAudioDecoder.stop()
        mAudioPlayer?.stop()

        if (fromUser) {
            mBgmPlayer.stop()

            configMap.forEach { (_, v) ->
                v.forEach { it.data.tts.onStop() }
            }

            sbyConfigMap.forEach { (_, v) ->
                v.forEach { it.data.tts.onStop() }
            }
        }
    }

    /*
    * 销毁时调用
    * */
    fun destroy() {
        mBgmPlayer.release()
        mScope.cancel()
        if (isSynthesizing) stop()

        configMap.forEach { (_, v) ->
            v.forEach { it.data.tts.onDestroy() }
        }
        sbyConfigMap.forEach { (_, v) ->
            v.forEach { it.data.tts.onDestroy() }
        }
    }

    @Suppress("ReplaceIsEmptyWithIfEmpty")
    private fun initConfig(@ReadAloudTarget target: Int, isInitSby: Boolean = true): Boolean {
        val list = mSysTts.getEnabledList(target).map { SystemTtsWithState(it) }

        var isMissing = false
        configMap[target] = if (list.isEmpty()) {
            isMissing = true
            defaultConfig
        } else list

        configMap[target]?.forEach { it.data.tts.onLoad() }
        if (isInitSby) initSbyConfig(target)

        return isMissing
    }

    private fun initSbyConfig(target: Int) {
        val sbyList = mSysTts.getEnabledStandbyList(target).map { SystemTtsWithState(it) }
        sbyConfigMap[target] = sbyList
        sbyList.forEach { it.data.tts.onLoad() }
    }

    private fun initBgm() {
        val list = mutableSetOf<Pair<Float, String>>()
        mSysTts.getEnabledList(ReadAloudTarget.BGM).forEach {
            val tts = (it.tts as BgmTTS)
            val volume = if (tts.volume == 0) SysTtsConfig.bgmVolume else it.tts.volume / 100f
            list.addAll(tts.musicList.map { path ->
                Pair(volume, path)
            })
        }
        mBgmPlayer.setPlayList(SysTtsConfig.isBgmShuffleEnabled, list)
    }

    /**
     * 加载配置
     */
    fun loadConfig() {
        kotlin.runCatching {
            mAudioPlayer = mAudioPlayer ?: AudioPlayer(context, mScope)
            mSysTts.apply {
                if (mCfg.isReplaceEnabled) mReplacer.load()

                initBgm()
                initSbyConfig(ReadAloudTarget.ALL)
                if (SysTtsConfig.isMultiVoiceEnabled) {
                    configMap.remove(ReadAloudTarget.ALL)

                    if (initConfig(ReadAloudTarget.ASIDE))
                        context.toast(R.string.systts_warn_no_ra_aside)

                    if (initConfig(ReadAloudTarget.DIALOGUE))
                        context.toast(R.string.systts_warn_no_ra_dialogue)

                    mAudioFormat = configMap[ReadAloudTarget.ASIDE]!![0].data.tts.audioFormat
                } else {
                    configMap.remove(ReadAloudTarget.ASIDE)
                    configMap.remove(ReadAloudTarget.DIALOGUE)

                    if (initConfig(ReadAloudTarget.ALL, isInitSby = false))
                        context.toast(R.string.systts_warn_no_ra_all)

                    mAudioFormat = configMap[ReadAloudTarget.ALL]!![0].data.tts.audioFormat
                }
            }
        }.onFailure {
            event?.onError(
                ERROR_LOAD_CONFIG,
                "",
                it.localizedMessage ?: it.rootCause?.localizedMessage ?: it.stackTraceToString()
            )
        }
    }

    // 音频生产者
    private var mProducer: ReceiveChannel<ChannelData>? = null

    // 设置语速音高
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

    suspend fun synthesizeText(
        aText: String, request: SynthesisRequest, callback: SynthesisCallback
    ) {
        isSynthesizing = true
        callback.start(mAudioFormat.sampleRate, mAudioFormat.bitRate, 1)
        mBgmPlayer.play()

        val text = if (mCfg.isReplaceEnabled) {
            try {
                mReplacer.doReplace(aText)
            } catch (e: Exception) {
                event?.onError(ERROR_REPLACE, aText, e.message ?: e.cause?.message)
                aText
            }
        } else {
            aText
        }

        val sysPitch = request.pitch - 100
        val sysRate = (mNorm.normalize(request.speechRate.toFloat()) - 100).toInt()

        val sby =
            handleTtsAndConvert(sbyConfigMap[ReadAloudTarget.ALL], sysRate, sysPitch).getOrNull(0)
        mProducer = null
        if (mCfg.isMultiVoiceEnabled) { //多语音
            Log.d(TAG, "multiVoiceProducer...")

            val aside = handleTtsAndConvert(configMap[ReadAloudTarget.ASIDE], sysRate, sysPitch)
            val sbyAside =
                handleTtsAndConvert(sbyConfigMap[ReadAloudTarget.ASIDE], sysRate, sysPitch)
                    .ifEmpty { listOf(sby) }

            val dialogue =
                handleTtsAndConvert(configMap[ReadAloudTarget.DIALOGUE]!!, sysRate, sysPitch)
            val sbyDialogue =
                handleTtsAndConvert(
                    sbyConfigMap[ReadAloudTarget.DIALOGUE],
                    sysRate,
                    sysPitch
                ).ifEmpty { listOf(sby) }


            Log.d(TAG, "旁白：${aside}, 对话：${dialogue}")
            mProducer = multiVoiceProducer(
                mCfg.isSplitEnabled, text, aside, dialogue,
                sbyAside.getOrNull(0), sbyDialogue.getOrNull(0)
            )
        } else { //单语音
            val tts = handleTtsAndConvert(configMap[ReadAloudTarget.ALL]!!, sysRate, sysPitch)[0]

            Log.d(TAG, "单语音：${tts}")
            mProducer = singleVoiceProducer(text, tts, sby)
        }

        try {
            /* 阻塞 消费者 */
            mProducer?.consumeEach { data ->
                if (!isSynthesizing) {
                    event?.onPlayCanceled(data.text)
                    return@consumeEach
                }

                mScope.launch {
                    if (data.tts.isDirectPlay()) {
                        event?.onStartRequest(data.text.toString(), data.tts)
                        event?.onRequestSuccess()
                        if (!data.tts.directPlay(data.text.toString())) {
                            event?.onError(ERROR_GET, data.text)
                            return@launch
                        }
                    } else if (data.audio == null) {
                        delay(3000)
                        return@launch
                    } else playAudio(data.tts, data.audio, data.tts.audioFormat, callback)

                    data.text?.let { event?.onPlayDone(it) }
                }.join()
            } // producer

            stop()
        } catch (e: CancellationException) {
            Log.w(TAG, "consume job cancel: ${e.message}")
        }
    }

    /* 单语音生产者 */
    @OptIn(ExperimentalCoroutinesApi::class)
    private fun singleVoiceProducer(
        text: String, tts: BaseTTS, sbyTts: BaseTTS?
    ): ReceiveChannel<ChannelData> = mScope.produce(capacity = 1000) {
        try {
            if (!isSynthesizing) return@produce
            if (mCfg.isSplitEnabled)
                StringUtils.splitSentences(text).forEach { splitedText ->
                    if (!isSynthesizing) return@produce
                    if (!StringUtils.isSilent(splitedText)) {
                        getAudioAndSend(this, splitedText, tts, sbyTts)
                        delay(requestInterval)
                    }
                }
            else {
                if (!StringUtils.isSilent(text))
                    getAudioAndSend(this, text, tts, sbyTts)
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
        sbyAside: BaseTTS? = null,
        sbyDialogue: BaseTTS? = null
    ): ReceiveChannel<ChannelData> = mScope.produce(capacity = 1000) {
        try {
            /* 分割为多语音  */
            val map = VoiceTools.splitMultiVoice(text, aside, dialogue, mCfg.minDialogueLength)
            if (isSplit) map.forEach {
                StringUtils.splitSentences(it.speakText).forEach { splitedText ->
                    if (!isSynthesizing) return@produce
                    if (!StringUtils.isSilent(splitedText)) {
                        val sby = if (it.isDialogue) sbyDialogue else sbyAside
                        getAudioAndSend(this, splitedText, it.tts, sby)
                        delay(requestInterval)
                    }
                }
            }
            else // 不分割长句
                map.forEach {
                    if (!isSynthesizing) return@produce
                    if (!StringUtils.isSilent(it.speakText)) {
                        val sby = if (it.isDialogue) sbyDialogue else sbyAside
                        getAudioAndSend(this, it.speakText, it.tts, sby)
                        delay(requestInterval)
                    }
                }
        } catch (e: CancellationException) {
            Log.w(TAG, "multiVoice producer job cancel: ${e.message}")
        }
    }

    // 获取音频并发送到Channel
    private suspend fun getAudioAndSend(
        channel: SendChannel<ChannelData>, text: String, tts: BaseTTS, sbyTts: BaseTTS? = null
    ) {
        if (!isSynthesizing) return

        if (tts.isDirectPlay()) { // 转到消费者去播放
            channel.send(ChannelData(text, null, tts))
        } else if (tts.audioFormat.isNeedDecode) {
            var isSbyUsed = false
            val audio = getAudioForRetry(text, tts) { index ->
                if (index >= mCfg.standbyTriggeredRetryIndex)
                    sbyTts?.let { //备用TTS
                        val audio = it.getAudioWithEvent(text)
                        channel.send(ChannelData(text, audio, it))
                        isSbyUsed = true
                        return@getAudioForRetry true
                    }

                false
            }
            if (isSbyUsed) return
            channel.send(ChannelData(text, audio, tts))
        } else {
            getAudioStream(text, tts, { index ->
                if (index >= mCfg.standbyTriggeredRetryIndex)
                    sbyTts?.let { //备用TTS
                        val audio = it.getAudioWithEvent(text)
                        channel.send(ChannelData(text, audio, it))
                        return@getAudioStream true
                    }
                false
            }, {
                runBlocking { channel.send(ChannelData(null, it, tts)) }
            })
        }
    }

    // 获取音频并输出事件
    private fun BaseTTS.getAudioWithEvent(text: String, retryNum: Int = 0): ByteArray? {
        return if (isDirectPlay()) null
        else {
            event?.onStartRequest(text, this)
            var audio: ByteArray? = null
            var toastTime = 0L
            kotlin.runCatching { toastTime = measureTimeMillis { audio = getAudio(text) } }
                .onSuccess {
                    audio?.let {
                        event?.onRequestSuccess(text, it.size, toastTime.toInt(), retryNum)
                    }
                }
                .onFailure {
                    event?.onError(ERROR_GET, text, it.message ?: it.cause?.message)
                }
            audio
        }
    }

    // 获取音频
    private suspend fun getAudioForRetry(
        text: String, tts: BaseTTS, onFailure: suspend (Int) -> Boolean,
    ): ByteArray? {
        event?.onStartRequest(text, tts)

        val startTime = SystemClock.elapsedRealtime()
        for (retryIndex in 1..100) {
            kotlin.runCatching {
                var timedOut = false
                val timeoutJob = mScope.launch {
                    delay(SysTtsConfig.requestTimeout.toLong())
                    tts.onStop()
                    timedOut = true
                }.job
                timeoutJob.start()

                val audio = tts.getAudio(text)
                if (timedOut) throw Exception(
                    context.getString(R.string.failed_timed_out, SysTtsConfig.requestTimeout)
                )
                else timeoutJob.cancelAndJoin()

                audio ?: throw Exception(AUDIO_NULL_MESSAGE) // 抛出空音频异常 以重试
            }.onSuccess {
                event?.onRequestSuccess(
                    text, it.size,
                    (SystemClock.elapsedRealtime() - startTime).toInt(), retryIndex
                )
                return it
            }.onFailure { e ->
                if (!isSynthesizing) return null

                val shortText = text.limitLength(20)
                event?.onError(ERROR_GET, shortText, e.message)

                // 音频为空时至多重试两次
                if (e.message == AUDIO_NULL_MESSAGE && retryIndex > 2) return null

                if (onFailure.invoke(retryIndex)) return null

                if (retryIndex > 3) delay(3000)
                else delay(requestInterval)

                event?.onStartRetry(retryIndex, e)
            }
        }

        return null
    }

    // 音频流
    private suspend fun getAudioStream(
        text: String,
        tts: BaseTTS,
        onFailure: suspend (Int) -> Boolean,
        onRead: (ByteArray?) -> Unit
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
            }.onFailure { e ->
                event?.onError(ERROR_GET, text, e.message)
                // 是否断点
                lastFailLength = if (breakPoint) currentLength else -1
                // 1006则跳过等待
                // 为close 1006则直接跳过等待
                if (e.message.toString().startsWith(CLOSE_1006_PREFIX)) {
                    event?.onStartRetry(retryIndex, e)
                    return@onFailure
                }

                if (onFailure.invoke(retryIndex)) return
                if (retryIndex > 3) delay(3000) else delay(500)

                event?.onStartRetry(retryIndex, e)
            }
        }
    }

    // 播放音频
    private suspend fun playAudio(
        tts: BaseTTS,
        audio: ByteArray,
        format: BaseAudioFormat,
        callback: SynthesisCallback
    ) {
        if (!format.isNeedDecode) {
            writeToCallBack(callback, audio)
            return
        }

        if (mCfg.isInAppPlayAudio) {
            val params = tts.audioPlayer.copy().apply {
                setParamsIfFollow(mCfg.inAppPlaySpeed, mCfg.inAppPlayPitch)
            }
            mAudioPlayer!!.play(audio, params.rate, params.pitch)
        } else {
            mAudioDecoder.doDecode(audio,
                mAudioFormat.sampleRate,
                onRead = { writeToCallBack(callback, it) },
                error = { event?.onError(ERROR_DECODE, it) })
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