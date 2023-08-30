package com.github.jing332.tts_server_android.service.systts.help

import android.content.Context
import android.media.AudioFormat
import android.media.AudioTrack
import android.util.Log
import com.github.jing332.tts_server_android.R
import com.github.jing332.tts_server_android.constant.AppPattern
import com.github.jing332.tts_server_android.constant.ReplaceExecution
import com.github.jing332.tts_server_android.constant.SpeechTarget
import com.github.jing332.tts_server_android.data.appDb
import com.github.jing332.tts_server_android.help.audio.AudioDecoder
import com.github.jing332.tts_server_android.help.audio.AudioDecoder.Companion.readPcmChunk
import com.github.jing332.tts_server_android.help.audio.ExoAudioPlayer
import com.github.jing332.tts_server_android.help.audio.Sonic
import com.github.jing332.tts_server_android.help.audio.exo.ExoAudioDecoder
import com.github.jing332.tts_server_android.conf.SysTtsConfig
import com.github.jing332.tts_server_android.model.SysTtsLib
import com.github.jing332.tts_server_android.model.speech.ITextToSpeechSynthesizer
import com.github.jing332.tts_server_android.model.speech.TtsTextSegment
import com.github.jing332.tts_server_android.model.speech.tts.BaseAudioFormat
import com.github.jing332.tts_server_android.model.speech.tts.BgmTTS
import com.github.jing332.tts_server_android.model.speech.tts.ITextToSpeechEngine
import com.github.jing332.tts_server_android.model.speech.tts.MsTTS
import com.github.jing332.tts_server_android.model.speech.tts.PlayerParams
import com.github.jing332.tts_server_android.service.systts.help.exception.ConfigLoadException
import com.github.jing332.tts_server_android.service.systts.help.exception.PlayException
import com.github.jing332.tts_server_android.service.systts.help.exception.RequestException
import com.github.jing332.tts_server_android.service.systts.help.exception.SpeechRuleException
import com.github.jing332.tts_server_android.service.systts.help.exception.TtsManagerException
import com.github.jing332.tts_server_android.utils.StringUtils
import com.github.jing332.tts_server_android.utils.longToast
import com.github.jing332.tts_server_android.utils.toast
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.IOException
import kotlin.coroutines.coroutineContext
import kotlin.random.Random
import kotlin.system.measureTimeMillis

class TextToSpeechManager(val context: Context) : ITextToSpeechSynthesizer<ITextToSpeechEngine>() {
    companion object {
        const val TAG = "TextToSpeechManager"

        private val defaultTtsConfig by lazy { MsTTS() }
    }

    var listener: Listener? = null

    var isSynthesizing: Boolean = false
        private set

    private var audioFormat: BaseAudioFormat = BaseAudioFormat()

    init {
        SysTtsLib.setTimeout(SysTtsConfig.requestTimeout)
    }

    private val mConfigMap: MutableMap<Int, List<ITextToSpeechEngine>> = mutableMapOf()
    private val mLoadedTtsMap = mutableSetOf<ITextToSpeechEngine>()
    private val mSpeechRuleHelper = SpeechRuleHelper()
    private val mRandom = Random(System.currentTimeMillis())

    override suspend fun handleText(text: String): List<TtsTextSegment> {
        kotlin.runCatching {
            return if (SysTtsConfig.isMultiVoiceEnabled) {
                val tagTtsMap = mutableMapOf<String, MutableList<ITextToSpeechEngine>>()
                mConfigMap[SpeechTarget.CUSTOM_TAG]?.forEach { tts ->
                    if (tagTtsMap[tts.speechRule.tag] == null)
                        tagTtsMap[tts.speechRule.tag] = mutableListOf()

                    tagTtsMap[tts.speechRule.tag]?.add(tts)
                }

                mSpeechRuleHelper.handleText(text, tagTtsMap, defaultTtsConfig)
            } else {
                val list = mConfigMap[SpeechTarget.ALL] ?: listOf(defaultTtsConfig)
                listOf(TtsTextSegment(list[mRandom.nextInt(list.size)], text))
            }.run {
                (if (SysTtsConfig.isSplitEnabled) {
                    val list = mutableListOf<TtsTextSegment>()
                    forEach { ttsText ->
                        splitText(list, ttsText.text, ttsText.tts, SysTtsConfig.isMultiVoiceEnabled)
                    }
                    list
                } else this).run {
                    val list = if (SysTtsConfig.isReplaceEnabled) {
                        val l = mutableListOf<TtsTextSegment>()
                        this.forEach {
                            mTextReplacer.replace(it.text, ReplaceExecution.AFTER) { e ->
                                listener?.onError(e)
                            }.also { text ->
                                l.add(TtsTextSegment(it.tts, text))
                            }
                        }

                        l
                    } else this

                    if (SysTtsConfig.isSkipSilentText) list.filterNot {
                        AppPattern.notReadAloudRegex.matches(it.text)
                    } else list
                }

            }
        }.onFailure {
            listener?.onError(
                SpeechRuleException(text = text, tts = null, message = it.message, cause = it)
            )
        }

        return emptyList()
    }

    private fun splitText(
        list: MutableList<TtsTextSegment>,
        text: String,
        tts: ITextToSpeechEngine,
        isMultiVoice: Boolean
    ) {
        if (isMultiVoice) {
            val texts = try {
                mSpeechRuleHelper.splitText(text)
            } catch (e: NoSuchMethodException) {
                return splitText(list, text, tts, false)
            }
            if (texts.isEmpty())
                listener?.onError(
                    SpeechRuleException(text = text, tts = tts, message = "splittedTexts is empty.")
                )
            else
                texts.forEach { list.add(TtsTextSegment(tts, it)) }
        } else {
            Log.d(TAG, "使用内置分割规则...")
            StringUtils.splitSentences(text).forEach {
                list.add(TtsTextSegment(tts, it))
            }
        }
    }

    private fun getBufferSize(sampleRate: Int): Int =
        AudioTrack.getMinBufferSize(
            sampleRate,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        )

    override suspend fun getAudio(
        tts: ITextToSpeechEngine,
        text: String,
        sysRate: Int,
        sysPitch: Int
    ): AudioResult? {
        Log.i(TAG, "请求音频：$tts, $text")
        if (!coroutineContext.isActive) return null

        var audioResult: AudioResult? = AudioResult(null, null)
        var retryTimes = 0
        retry(times = 50,
            onCatch = { times, e ->
                retryTimes = times
                Log.w(TAG, "请求失败: times=$times, $text, $tts, $e")
                listener?.onError(
                    if (e is RequestException) e else RequestException(
                        text = text, tts = tts, cause = e, times = times
                    )
                )

                if (times > SysTtsConfig.maxRetryCount) {
                    return@retry false // 超过最大重试次数，跳过
                }

                // 备用TTS
                if (SysTtsConfig.standbyTriggeredRetryIndex == times)
                    tts.speechRule.standbyTts?.let { sbyTts ->
                        Log.i(TAG, "使用备用TTS：$sbyTts")
                        audioResult = if (sbyTts.isDirectPlay()) {
                            null
                        } else
                            getAudio(sbyTts, text, sysRate, sysPitch)
                        return@retry false // 取消重试
                    }

                // 第 maxEmptyAudioRetryCount 音频为空失败后退出 return false
                val canContinue = if (SysTtsConfig.maxEmptyAudioRetryCount == 0) false else
                    !(e is RequestException && e.errorCode == RequestException.ERROR_CODE_AUDIO_NULL
                            && times >= SysTtsConfig.maxEmptyAudioRetryCount)

                if (canContinue) listener?.onStartRetry(times)
                return@retry canContinue
            },
            block = {
                if (!coroutineContext.isActive) return@retry
                listener?.onRequestStarted(text, tts)

                val costTime = measureTimeMillis {
                    if (!mLoadedTtsMap.contains(tts)) {
                        Log.i(TAG, "加载TTS：$tts")
                        tts.onLoad()
                        mLoadedTtsMap.add(tts)
                    }
                    // 直接播放 不用获取音频, 在接收者处播放
                    if (tts.isDirectPlay()) {
                        audioResult = AudioResult()
                        return@retry
                    }
                    coroutineScope {
                        var hasTimeout = false
                        val timeoutJob = launch {
                            delay(SysTtsConfig.requestTimeout.toLong())
                            hasTimeout = true
                        }

                        audioResult?.inputStream =
                            try {
                                tts.getAudioWithSystemParams(text, sysRate, sysPitch)
                                    ?: throw RequestException(
                                        errorCode = RequestException.ERROR_CODE_AUDIO_NULL,
                                        tts = tts, text = text
                                    )
                            } catch (e: IOException) {
                                if (e.message != "Canceled") throw e
                                null
                            }

                        if (!SysTtsConfig.isStreamPlayModeEnabled) {
                            audioResult?.bytes = audioResult?.inputStream?.readBytes()
                            audioResult?.inputStream?.close()
                            if (audioResult?.bytes == null || audioResult?.bytes?.size!! < 1024)
                                throw RequestException(
                                    errorCode = RequestException.ERROR_CODE_AUDIO_NULL,
                                    tts = tts, text = text
                                )
                        }

                        if (hasTimeout) {
                            tts.onStop()
                            throw RequestException(
                                tts = tts,
                                text = text,
                                errorCode = RequestException.ERROR_CODE_TIMEOUT
                            )
                        } else timeoutJob.cancelAndJoin()
                    }
                }
                audioResult?.data = costTime to retryTimes
            })

        return audioResult
    }

    private val mTextReplacer = TextReplacer()
    private val mAudioDecoder = AudioDecoder()
    private val mExoDecoder by lazy { ExoAudioDecoder(context) }
    private var mAudioPlayer: ExoAudioPlayer? = null
    private var mBgmPlayer: BgmPlayer? = null

    private fun getEnabledList(target: Int, isStandby: Boolean): List<ITextToSpeechEngine> {
        return appDb.systemTtsDao.getEnabledListForSort(target, isStandby).map {
            appDb.systemTtsDao.getGroup(it.groupId)?.let { group ->
                val groupAudioParams = group.audioParams.copyIfFollow(
                    SysTtsConfig.audioParamsSpeed,
                    SysTtsConfig.audioParamsVolume, SysTtsConfig.audioParamsPitch
                )
                it.tts.audioParams = it.tts.audioParams.copyIfFollow(
                    followSpeed = groupAudioParams.speed,
                    followVolume = groupAudioParams.volume,
                    followPitch = groupAudioParams.pitch,
                )
            }
            it.tts.speechRule = it.speechRule
            it.tts.speechRule.configId = it.id
            return@map it.tts
        }
    }

    private fun initConfig(target: Int): Boolean {
        Log.i(TAG, "initConfig: $target")
        var isOk = true
        mConfigMap[target] = getEnabledList(target, isStandby = false)
        if (mConfigMap[target]?.isEmpty() == true) {
            isOk = false
            Log.w(TAG, "缺少朗读目标$target, 使用内置MsTTS！")
            mConfigMap[target] = listOf(MsTTS())
        }

        val sbyList = getEnabledList(target, isStandby = true)

        mConfigMap[target]?.forEach { tts ->
            sbyList.find { it.speechRule.isTagSame(tts.speechRule) }
                ?.let { sbyTts ->
                    Log.i(TAG, "找到备用TTS：$sbyTts")
                    tts.speechRule.standbyTts = sbyTts
                }
        }

        return isOk
    }

    private fun initBgm() {
        val list = mutableSetOf<Pair<Float, String>>()
        appDb.systemTtsDao.getEnabledListForSort(SpeechTarget.BGM).forEach {
            val tts = (it.tts as BgmTTS)
            val volume = if (tts.volume == 0) SysTtsConfig.bgmVolume else it.tts.volume / 1000f
            list.addAll(tts.musicList.map { path ->
                Pair(volume, path)
            })
        }
        if (list.isEmpty()) {
            mBgmPlayer?.release()
            mBgmPlayer = null
        } else {
            mBgmPlayer = mBgmPlayer ?: BgmPlayer(context)
            mBgmPlayer?.setPlayList(SysTtsConfig.isBgmShuffleEnabled, list)
        }
    }

    private fun initAudioFormat(@SpeechTarget target: Int) {
        mConfigMap[target]?.maxBy { it.audioFormat.sampleRate }?.audioFormat?.also {
            audioFormat = it
        }
    }

    fun loadReplacer() {
        if (SysTtsConfig.isReplaceEnabled)
            mTextReplacer.load()
    }

    override fun load() {
        try {
            loadReplacer()
            initBgm()
            if (SysTtsConfig.isMultiVoiceEnabled) {
                val ok = initConfig(SpeechTarget.CUSTOM_TAG)
                initAudioFormat(SpeechTarget.CUSTOM_TAG)
                if (ok) {
                    mConfigMap[SpeechTarget.CUSTOM_TAG]?.getOrNull(0)?.also {
                        appDb.speechRuleDao.getByRuleId(it.speechRule.tagRuleId)?.let { rule ->
                            return@also mSpeechRuleHelper.init(context, rule)
                        }
                        throw ConfigLoadException()
                    }
                } else
                    context.toast(R.string.systts_no_custom_tag_config_warn)

            } else {
                if (!initConfig(SpeechTarget.ALL))
                    context.toast(R.string.systts_no_speech_target_all)
                initAudioFormat(SpeechTarget.ALL)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            listener?.onError(ConfigLoadException(cause = e))
        }
    }

    override fun stop() {
        isSynthesizing = false
        for (engine in mLoadedTtsMap) engine.onStop()
        mBgmPlayer?.pause()
    }

    override fun destroy() {
        for (engine in mLoadedTtsMap) engine.onDestroy()
        mBgmPlayer?.release()
        mAudioPlayer?.release()
    }

    suspend fun textToAudio(
        ttsId: Long = -1L,
        text: String,
        sysRate: Int,
        sysPitch: Int,
        onStart: (sampleRate: Int, bitRate: Int) -> Unit,
        onPcmAudio: (pcmAudio: ByteArray) -> Unit,
    ) {
        isSynthesizing = true

        val replaced = if (SysTtsConfig.isReplaceEnabled)
            mTextReplacer.replace(text, ReplaceExecution.BEFORE) { listener?.onError(it) } else text

        if (ttsId == -1L) {
            mBgmPlayer?.play()
            onStart(audioFormat.sampleRate, audioFormat.bitRate)
            synthesizeText(replaced, sysRate, sysPitch) { data -> // 音频获取完毕
                data.receiver(sysRate, sysPitch, audioFormat, onPcmAudio)
            }
        } else {
            val specifiedTts = appDb.systemTtsDao.getTts(ttsId)
            if (specifiedTts == null) {
                context.longToast(context.getString(R.string.tts_config_not_exist))
                delay(3000)
                return
            }

            val group = appDb.systemTtsDao.getGroup(specifiedTts.groupId)
            if (group != null) {
                val groupAudioParams = group.audioParams.copyIfFollow(
                    SysTtsConfig.audioParamsSpeed,
                    SysTtsConfig.audioParamsVolume,
                    SysTtsConfig.audioParamsPitch
                )
                specifiedTts.tts.audioParams = specifiedTts.tts.audioParams.copyIfFollow(
                    followSpeed = groupAudioParams.speed,
                    followVolume = groupAudioParams.volume,
                    followPitch = groupAudioParams.pitch,
                )
            }
            onStart(specifiedTts.tts.audioFormat.sampleRate, specifiedTts.tts.audioFormat.bitRate)
            synthesizeText(specifiedTts.tts, text, sysRate, sysPitch) {
                it.receiver(sysRate, sysPitch, it.txtTts.tts.audioFormat, onPcmAudio)
            }
        }


        isSynthesizing = false
    }

    private suspend fun AudioData<ITextToSpeechEngine>.receiver(
        sysRate: Int,
        sysPitch: Int,
        audioFormat: BaseAudioFormat,
        onPcmAudio: (pcmAudio: ByteArray) -> Unit
    ) {
        val data = this
        kotlin.runCatching {
            val txtTts = data.txtTts
            val audioParams = txtTts.tts.audioParams

            val srcSampleRate = txtTts.tts.audioFormat.sampleRate
            val targetSampleRate = audioFormat.sampleRate

            val sonic =
                if (audioParams.isDefaultValue && srcSampleRate == targetSampleRate) null
                else Sonic(txtTts.tts.audioFormat.sampleRate, 1)
            txtTts.playAudio(
                sysRate, sysPitch, data.audio,
                onDone = { data.done.invoke() })
            { pcmAudio ->
                if (sonic == null) onPcmAudio.invoke(pcmAudio)
                else {
                    sonic.volume = audioParams.volume
                    sonic.speed = audioParams.speed
                    sonic.pitch = audioParams.pitch
                    sonic.rate = srcSampleRate.toFloat() / targetSampleRate.toFloat()

                    sonic.writeBytesToStream(pcmAudio, pcmAudio.size)
                    onPcmAudio.invoke(sonic.readBytesFromStream(sonic.samplesAvailable()))
                }
            }
            listener?.onPlayFinished(txtTts.text, txtTts.tts)
        }.onFailure {
            listener?.onError(TtsManagerException(cause = it, message = it.message))
        }
    }

    @Suppress("UNCHECKED_CAST")
    private suspend fun TtsTextSegment.playAudio(
        sysRate: Int,
        sysPitch: Int,
        audioResult: AudioResult?,
        onDone: suspend () -> Unit,
        onPcmAudio: (pcmAudio: ByteArray) -> Unit,
    ) {
        var costTime = 0L
        var retryTimes = 0
        if (audioResult?.data is Pair<*, *>) {
            costTime = (audioResult.data as Pair<Long, Int>).first
            retryTimes = (audioResult.data as Pair<Long, Int>).second
        }

        // audioResult == null 说明请求失败
        if (audioResult == null && tts.speechRule.standbyTts?.isDirectPlay() == true) { // 直接播放备用TTS
            tts.speechRule.standbyTts?.startPlayWithSystemParams(text, sysRate, sysPitch)
            onDone.invoke()
            return
        } else if (tts.isDirectPlay()) { // 直接播放
            tts.startPlayWithSystemParams(text, sysRate, sysPitch)
            onDone.invoke()
            return
        } else if (audioResult?.inputStream == null) { // 无音频
            Log.w(TAG, "audio == null, $this")
            onDone.invoke()
            return
        }

        if (SysTtsConfig.isStreamPlayModeEnabled) {
            listener?.onRequestSuccess(text, tts, -1, costTime, retryTimes)
            if (tts.audioFormat.isNeedDecode) {
                if (SysTtsConfig.isInAppPlayAudio) {
                    onDone.invoke()
                    audioResult.builtinPlayAudio(tts.audioPlayer)
                    audioResult.inputStream?.close()
                } else {
                    try {
                        audioResult.decodeAudio { onPcmAudio.invoke(it) }
                    } catch (e: Exception) {
                        throw PlayException(
                            tts = tts,
                            cause = e,
                            message = "流播放下的音频解码失败"
                        )
                    } finally {
                        onDone.invoke()
                    }
                }
            } else {
                val bufferSize = getBufferSize(tts.audioFormat.sampleRate)
                Log.d(TAG, "raw buffer: $bufferSize")
//                val buffer = ByteArray(min(1024, bufferSize * 2))
                audioResult.inputStream?.use { ins ->
                    ins.buffered().use {
                        it.readPcmChunk(
                            bufferSize = bufferSize * 2,
                            chunkSize = bufferSize
                        ) { pcm ->
                            println("pcm  ${pcm.size}")
                            onPcmAudio(pcm)
                        }
                    }
                }
                onDone.invoke()
            }
        } else { // 全部加载到内存
            val audio = audioResult.bytes
            onDone.invoke()
            if (audio == null || audio.size < 48) return

            listener?.onRequestSuccess(text, tts, audio.size, costTime, retryTimes)

            if (tts.audioFormat.isNeedDecode) {
                if (SysTtsConfig.isInAppPlayAudio)
                    audioResult.builtinPlayAudio(tts.audioPlayer)
                else
                    try {
                        audioResult.decodeAudio { onPcmAudio.invoke(it) }
                    } catch (e: Exception) {
                        throw PlayException(tts = tts, cause = e, message = "音频解码失败")
                    }
            } else
                onPcmAudio.invoke(audio)
        }
    }

    // 内置播放器
    private suspend fun AudioResult.builtinPlayAudio(audioParams: PlayerParams) {
        val params = audioParams.copy().setParamsIfFollow(
            SysTtsConfig.inAppPlaySpeed,
            SysTtsConfig.inAppPlayVolume,
            SysTtsConfig.inAppPlayPitch
        )

        mAudioPlayer = mAudioPlayer ?: ExoAudioPlayer(context)
        if (SysTtsConfig.isStreamPlayModeEnabled)
            mAudioPlayer?.play(inputStream!!, params.rate, params.volume, params.pitch)
        else
            mAudioPlayer?.play(bytes!!, params.rate, params.volume, params.pitch)
    }

    // 解码音频
    private suspend fun AudioResult.decodeAudio(
        isStream: Boolean = SysTtsConfig.isStreamPlayModeEnabled,
        useExoDecoder: Boolean = SysTtsConfig.isExoDecoderEnabled,
        onPcmAudio: (pcmAudio: ByteArray) -> Unit,
    ) {
        if (useExoDecoder) {
            mExoDecoder.callback = ExoAudioDecoder.Callback { byteBuffer ->
                val buffer = ByteArray(byteBuffer.remaining())
                byteBuffer.get(buffer)
                onPcmAudio.invoke(buffer)
            }
            if (isStream) mExoDecoder.doDecode(inputStream!!)
            else mExoDecoder.doDecode((bytes!!))
        } else {
            if (isStream) mAudioDecoder.doDecode(
                inputStream!!,
                audioFormat.sampleRate
            ) { pcmData ->
                onPcmAudio.invoke(pcmData)
            }
            else
                mAudioDecoder.doDecode(bytes!!, audioFormat.sampleRate) { pcmData ->
                    onPcmAudio.invoke(pcmData)
                }
        }

    }

    interface Listener {
        fun onError(e: TtsManagerException)
        fun onStartRetry(times: Int)
        fun onRequestStarted(text: String, tts: ITextToSpeechEngine)
        fun onPlayFinished(text: String, tts: ITextToSpeechEngine)
        fun onRequestSuccess(
            text: String, tts: ITextToSpeechEngine, size: Int, costTime: Long, retryTimes: Int
        )
    }
}