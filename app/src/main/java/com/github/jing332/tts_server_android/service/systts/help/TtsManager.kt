package com.github.jing332.tts_server_android.service.systts.help

import android.annotation.SuppressLint
import android.content.Context
import android.os.SystemClock
import android.speech.tts.SynthesisCallback
import android.speech.tts.SynthesisRequest
import android.util.Log
import com.drake.net.utils.runMain
import com.github.jing332.tts_server_android.R
import com.github.jing332.tts_server_android.constant.ReadAloudTarget
import com.github.jing332.tts_server_android.data.appDb
import com.github.jing332.tts_server_android.data.entities.systts.SystemTts
import com.github.jing332.tts_server_android.help.ExoByteArrayMediaSource
import com.github.jing332.tts_server_android.help.SysTtsConfig
import com.github.jing332.tts_server_android.model.tts.BaseAudioFormat
import com.github.jing332.tts_server_android.model.tts.BaseTTS
import com.github.jing332.tts_server_android.model.tts.MsTTS
import com.github.jing332.tts_server_android.util.*
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.PlaybackParameters
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.source.DefaultMediaSourceFactory
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.upstream.DataSource
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.*

class TtsManager(val context: Context) {
    companion object {
        private const val TAG = "TtsManager"
        private const val CLOSE_1006_PREFIX = "websocket: close 1006"

        const val ERROR_DECODE_FAILED = 0
        const val ERROR_GET_FAILED = 1
        const val ERROR_AUDIO_NULL = 2

        // 音频请求间隔
        private const val requestInterval = 100L
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

    // 归一化，将某个范围值缩小到另一范围 (500-0 -> 200-0)
    private val mNorm by lazy { NormUtil(500F, 0F, 200F, 0F) }

    // 替换
    private val mReplacer: ReplaceHelper by lazy { ReplaceHelper() }

    // 音频格式 合成开始前和解码时使用
    private lateinit var mAudioFormat: BaseAudioFormat

    // Room数据库
    private val mSysTts by lazy { appDb.systemTtsDao }

    // 全局默认 TTS配置
    private lateinit var mDefaultConfig: List<SystemTts>

    // 旁白 TTS配置
    private lateinit var mAsideConfig: List<SystemTts>

    // 对话 TTS配置
    private lateinit var mDialogueConfig: List<SystemTts>

    // 一些开关偏好
    private var mInAppPlayAudio = false
    private var mInAppPlaySpeed = 1F
    private var mInAppPlayPitch = 1F
    private var mSplitEnabled = false
    private var mReplaceEnabled = false
    private var mMultiVoiceEnabled = false
    private var mVoiceMultipleEnabled = false
    private var mMinDialogueLen = 0

    /**
     * 停止合成及播放
     */
    fun stop() {
        isSynthesizing = false
        mAudioDecoder.stop()

        mInAppPlayJob?.cancel()
    }

    /*
    * 销毁时调用
    * */
    fun destroy() {
        mScope.cancel()
        if (isSynthesizing) stop()
    }

    /**
     * 加载配置
     */
    fun loadConfig() {
        SysTtsConfig.apply {
            mInAppPlayAudio = isInAppPlayAudio
            mInAppPlaySpeed = inAppPlaySpeed
            mInAppPlayPitch = inAppPlayPitch

            mVoiceMultipleEnabled = isVoiceMultipleEnabled
            mSplitEnabled = isSplitEnabled
            mMultiVoiceEnabled = isMultiVoiceEnabled
            mReplaceEnabled = isReplaceEnabled
            mMinDialogueLen = minDialogueLength
        }

        mSysTts.apply {
            if (mReplaceEnabled) mReplacer.load()

            if (SysTtsConfig.isMultiVoiceEnabled) {
                // 旁白
                getAllByReadAloudTarget(ReadAloudTarget.ASIDE).let { list ->
                    mAsideConfig = if (list == null || list.isEmpty()) {
                        context.toast(context.getString(R.string.systts_warn_no_ra_aside))
                        listOf(
                            SystemTts(
                                readAloudTarget = ReadAloudTarget.ASIDE,
                                tts = MsTTS()
                            )
                        )
                    } else list
                }
                mAsideConfig.forEach { it.tts.onLoad() }

                // 对话
                getAllByReadAloudTarget(ReadAloudTarget.DIALOGUE).let { list ->
                    mDialogueConfig = if (list == null || list.isEmpty()) {
                        context.toast(R.string.systts_warn_no_ra_dialogue)
                        listOf(
                            SystemTts(
                                readAloudTarget = ReadAloudTarget.DIALOGUE,
                                tts = MsTTS()
                            )
                        )
                    } else list
                }

                mDialogueConfig.forEach { it.tts.onLoad() }
                mAudioFormat = mDialogueConfig[0].tts.audioFormat
            } else {
                mSysTts.getAllByReadAloudTarget().let { list ->
                    mDefaultConfig = if (list == null || list.isEmpty()) {
                        context.toast(context.getString(R.string.systts_warn_no_ra_all))
                        listOf(SystemTts(isEnabled = true, tts = MsTTS()))
                    } else list

                    mDefaultConfig.forEach { it.tts.onLoad() }
                    mAudioFormat = mDefaultConfig[0].tts.audioFormat
                }
            }
        }
    }

    // 音频生产者
    private var mProducer: ReceiveChannel<ChannelData>? = null

    // APP内播放音频Job 用于 job.cancel() 取消播放
    private var mInAppPlayJob: Job? = null

    private fun handleTTS(list: List<SystemTts>, rate: Int, pitch: Int): List<BaseTTS> {
        val handledList = list.map { it.tts.clone<BaseTTS>()!! }
        handledList.forEach { it.setPlayBackParameters(rate, pitch) }
        return handledList
    }

    /* 开始转语音 */
    suspend fun synthesizeText(
        aText: String, request: SynthesisRequest?, callback: SynthesisCallback?
    ) {
        isSynthesizing = true
        callback!!.start(mAudioFormat.sampleRate, mAudioFormat.bitRate, 1)

        val text = if (mReplaceEnabled) mReplacer.doReplace(aText) else aText

        val sysPitch = request!!.pitch - 100
        val sysRate = (mNorm.normalize(request.speechRate.toFloat()) - 100).toInt()

        mProducer = null
        if (mMultiVoiceEnabled) { //多语音
            Log.d(TAG, "multiVoiceProducer...")

            val aside = handleTTS(mAsideConfig, sysRate, sysPitch)
            val dialogue = handleTTS(mDialogueConfig, sysRate, sysPitch)

            Log.d(TAG, "旁白：${aside}, 对话：${dialogue}")
            mProducer = multiVoiceProducer(mSplitEnabled, text, aside, dialogue)
        } else { //单语音
            val pro = handleTTS(mDefaultConfig, sysRate, sysPitch)[0]

            Log.d(TAG, "单语音：${pro}")
            if (mSplitEnabled) {
                Log.d(TAG, "splitSentences...")
                mProducer = splitSentencesProducer(text, pro)
            } else {
                if (mAudioFormat.isNeedDecode) getAudioAndDecodeWrite(text, pro, callback)
                else mProducer = audioStreamProducer(text, pro)
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
                    if (data.audio == null) {
                        event?.onError(ERROR_AUDIO_NULL, shortText)
                    } else {
                        playAudioHelper(data.audio, data.tts.audioFormat, callback)
                        shortText?.let { event?.onPlayDone(it) }
                    }
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
        isSplit: Boolean, text: String, aside: List<BaseTTS>, dialogue: List<BaseTTS>
    ): ReceiveChannel<ChannelData> = mScope.produce(capacity = 1000) {
        try {
            /* 分割为多语音  */
            val map = VoiceTools.splitMultiVoice(text, aside, dialogue, mMinDialogueLen)
            if (isSplit) map.forEach {
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
        if (tts.audioFormat.isNeedDecode) {
            val audio = getAudioHelper(text, tts)
            channel.send(ChannelData(text, audio, tts))
        } else {
            getAudioStreamHelper(text, tts) {
                runBlocking { channel.send(ChannelData(null, it, tts)) }
            }
        }
    }

    // 完整下载
    private suspend fun getAudioHelper(
        text: String, tts: BaseTTS
    ): ByteArray? {
        event?.onStartRequest(text, tts)

        val startTime = System.currentTimeMillis()
        for (retryIndex in 1..100) {
            kotlin.runCatching { tts.getAudio(text) }
                .onSuccess {
                    it?.let {
                        event?.onRequestSuccess(
                            text, it.size,
                            (System.currentTimeMillis() - startTime).toInt(), retryIndex
                        )
                    }
                    return it
                }.onFailure {
                    if (!isSynthesizing) return null

                    val shortText = text.limitLength(20)
                    event?.onError(ERROR_GET_FAILED, shortText, it.message)

                    // 为close 1006则直接跳过等待
                    if (!it.message.toString().startsWith(CLOSE_1006_PREFIX))
                        if (retryIndex > 3) delay(3000)
                        else delay(requestInterval)

                    event?.onStartRetry(retryIndex, it)
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

    // 创建音频媒体源
    private fun createMediaSourceFromByteArray(data: ByteArray): MediaSource {
        val factory = DataSource.Factory { ExoByteArrayMediaSource(data) }
        return DefaultMediaSourceFactory(context).setDataSourceFactory(factory)
            .createMediaSource(MediaItem.fromUri(""))
    }

    // APP内音频播放器 必须在主线程调用
    private val exoPlayer by lazy {
        ExoPlayer.Builder(context).build().apply {
            playWhenReady = true
            addListener(object : Player.Listener {
                @SuppressLint("SwitchIntDef")
                override fun onPlaybackStateChanged(playbackState: Int) {
                    when (playbackState) {
                        ExoPlayer.STATE_ENDED -> {
                            mInAppPlayJob?.cancel()
                        }
                    }

                    super.onPlaybackStateChanged(playbackState)
                }
            })
        }
    }

    // 在APP内直接播放
    private suspend fun playAudioInApp(audio: ByteArray) {
        mInAppPlayJob = mScope.launch {
            try {
                mScope.launch(Dispatchers.Main) {
                    exoPlayer.setMediaSource(createMediaSourceFromByteArray(audio))
                    exoPlayer.playbackParameters =
                        PlaybackParameters(mInAppPlaySpeed, mInAppPlayPitch)
                    exoPlayer.prepare()
                }.join()
                // 一直等待 直到 job.cancel
                awaitCancellation()
            } catch (e: CancellationException) {
                Log.w(TAG, "in-app play job cancel: ${e.message}")
                runMain { exoPlayer.stop() }
            }
        }
        mInAppPlayJob?.join()
        mInAppPlayJob = null
    }

    /* 获取音频并直接解码播放 */
    private suspend fun getAudioAndDecodeWrite(
        text: String, tts: BaseTTS, callback: SynthesisCallback
    ) {
        runBlocking {
            val audio = getAudioHelper(text, tts)
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

        if (mInAppPlayAudio) {
            playAudioInApp(audio)
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
        val text: String? = null, val audio: ByteArray?, val tts: BaseTTS
    )
}