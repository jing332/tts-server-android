package com.github.jing332.tts_server_android.service.systts.help

import android.content.Context
import android.util.Log
import com.github.jing332.tts_server_android.R
import com.github.jing332.tts_server_android.constant.SpeechTarget
import com.github.jing332.tts_server_android.data.appDb
import com.github.jing332.tts_server_android.help.audio.AudioDecoder
import com.github.jing332.tts_server_android.help.audio.AudioPlayer
import com.github.jing332.tts_server_android.help.audio.Sonic
import com.github.jing332.tts_server_android.help.config.SysTtsConfig
import com.github.jing332.tts_server_android.model.SysTtsLib
import com.github.jing332.tts_server_android.model.speech.ITextToSpeechSynthesizer
import com.github.jing332.tts_server_android.model.speech.TtsTextPair
import com.github.jing332.tts_server_android.model.speech.tts.BaseAudioFormat
import com.github.jing332.tts_server_android.model.speech.tts.BgmTTS
import com.github.jing332.tts_server_android.model.speech.tts.ITextToSpeechEngine
import com.github.jing332.tts_server_android.model.speech.tts.MsTTS
import com.github.jing332.tts_server_android.service.systts.help.exception.ConfigLoadException
import com.github.jing332.tts_server_android.service.systts.help.exception.PlayException
import com.github.jing332.tts_server_android.service.systts.help.exception.RequestException
import com.github.jing332.tts_server_android.service.systts.help.exception.SpeechRuleException
import com.github.jing332.tts_server_android.service.systts.help.exception.SynthesisException
import com.github.jing332.tts_server_android.service.systts.help.exception.TtsManagerException
import com.github.jing332.tts_server_android.util.StringUtils
import com.github.jing332.tts_server_android.util.toast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import kotlin.coroutines.coroutineContext
import kotlin.system.measureTimeMillis

class TextToSpeechManager(val context: Context) : ITextToSpeechSynthesizer<ITextToSpeechEngine>() {
    companion object {
        const val TAG = "TextToSpeechManager"

        private val defaultTtsConfig by lazy { MsTTS() }
    }

    var listener: Listener? = null

    var isSynthesizing: Boolean = false
        private set

    var audioFormat: BaseAudioFormat = BaseAudioFormat()
        private set

    init {
        SysTtsLib.setTimeout(SysTtsConfig.requestTimeout)
    }

    private val mConfigMap: MutableMap<Int, List<ITextToSpeechEngine>> = mutableMapOf()
    private val mLoadedTtsMap = mutableSetOf<ITextToSpeechEngine>()
    private val mSpeechRuleHelper = SpeechRuleHelper()

    override suspend fun handleText(text: String): List<TtsTextPair> {
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
                listOf(TtsTextPair(mConfigMap[SpeechTarget.ALL]?.get(0) ?: defaultTtsConfig, text))
            }.run {
                if (SysTtsConfig.isSplitEnabled) {
                    val list = mutableListOf<TtsTextPair>()
                    forEach { ttsText ->
                        splitText(list, ttsText.text, ttsText.tts, SysTtsConfig.isMultiVoiceEnabled)
                    }
                    list
                } else
                    this
            }
        }.onFailure {
            listener?.onError(
                SpeechRuleException(text = text, tts = null, message = it.message, cause = it)
            )
        }

        return emptyList()
    }

    private fun splitText(
        list: MutableList<TtsTextPair>,
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
                texts.forEach { list.add(TtsTextPair(tts, it)) }
        } else {
            Log.d(TAG, "使用内置分割规则...")
            StringUtils.splitSentences(text).forEach {
                list.add(TtsTextPair(tts, it))
            }
        }
    }

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
        retry(times = 20,
            onCatch = { times, e ->
                retryTimes = times
                Log.w(TAG, "请求失败: times=$times, $text, $tts, ${e.stackTraceToString()}")
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

                // 第二次音频为空失败后退出 return false
                val canContinue =
                    !(e is RequestException && e.errorCode == RequestException.ERROR_CODE_AUDIO_NULL && times >= 2)
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
                            tts.onStop()
                        }.job
                        timeoutJob.start()

                        audioResult?.inputStream =
                            tts.getAudioWithSystemParams(text, sysRate, sysPitch)
                                ?: throw RequestException(
                                    errorCode = RequestException.ERROR_CODE_AUDIO_NULL,
                                    tts = tts, text = text
                                )
                        if (hasTimeout) throw RequestException(
                            tts = tts,
                            text = text,
                            errorCode = RequestException.ERROR_CODE_TIMEOUT
                        )
                        else timeoutJob.cancelAndJoin()
                    }
                }
                audioResult?.data = costTime to retryTimes
//                listener?.onRequestSuccess(text, tts, 0, costTime, retryTimes)
            })

        return audioResult
    }

    private val mTextReplacer = TextReplacer()
    private val mAudioDecoder = AudioDecoder()
    private var mAudioPlayer: AudioPlayer? = null
    private var mBgmPlayer: BgmPlayer? = null

    private fun initConfig(target: Int): Boolean {
        Log.i(TAG, "initConfig: $target")
        var isOk = true
        mConfigMap[target] =
            appDb.systemTtsDao.getEnabledList(target, false).map {
                it.tts.speechRule = it.speechRule
                it.tts.speechRule.configId = it.id
                return@map it.tts
            }
        if (mConfigMap[target]?.isEmpty() == true) {
            isOk = false
            Log.w(TAG, "缺少朗读目标$target, 使用内置MsTTS！")
            mConfigMap[target] = listOf(MsTTS())
        }

        val sbyList =
            appDb.systemTtsDao.getEnabledList(target, true).map {
                it.tts.speechRule = it.speechRule
                it.tts.speechRule.configId = it.id
                return@map it.tts
            }

        mConfigMap[target]?.forEach { tts ->
            sbyList.find { it.speechRule.isTagSame(tts.speechRule).apply { println(this) } }
                ?.let { sbyTts ->
                    Log.i(TAG, "找到备用TTS：$sbyTts")
                    tts.speechRule.standbyTts = sbyTts
                }
        }

        return isOk
    }

    private fun initBgm() {
        val list = mutableSetOf<Pair<Float, String>>()
        appDb.systemTtsDao.getEnabledList(SpeechTarget.BGM).forEach {
            val tts = (it.tts as BgmTTS)
            val volume = if (tts.volume == 0) SysTtsConfig.bgmVolume else it.tts.volume / 1000f
            println(volume)
            list.addAll(tts.musicList.map { path ->
                Pair(volume, path)
            })
        }
        if (list.isNotEmpty()) {
            mBgmPlayer = mBgmPlayer ?: BgmPlayer(context)
            mBgmPlayer?.setPlayList(SysTtsConfig.isBgmShuffleEnabled, list)
        }
    }

    override fun load() {
        try {
            if (SysTtsConfig.isReplaceEnabled)
                mTextReplacer.load()

            initBgm()
            if (SysTtsConfig.isMultiVoiceEnabled) {
                val ok = initConfig(SpeechTarget.CUSTOM_TAG)
                audioFormat = mConfigMap[SpeechTarget.CUSTOM_TAG]!![0].audioFormat
                if (ok) {
                    mConfigMap[SpeechTarget.CUSTOM_TAG]?.getOrNull(0)?.also {
                        appDb.speechRule.getByReadRuleId(it.speechRule.tagRuleId)?.let { rule ->
                            return@also mSpeechRuleHelper.init(context, rule)
                        }
                        throw ConfigLoadException()
                    }
                } else
                    context.toast(R.string.systts_no_custom_tag_config_warn)

            } else {
                if (!initConfig(SpeechTarget.ALL))
                    context.toast(R.string.systts_warn_no_ra_all)
                audioFormat = mConfigMap[SpeechTarget.ALL]!![0].audioFormat
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
        text: String,
        sysRate: Int,
        sysPitch: Int,
        onPcmAudio: (pcmAudio: ByteArray) -> Unit,
    ) {
        isSynthesizing = true
        mBgmPlayer?.play()

        val replaced = if (SysTtsConfig.isReplaceEnabled)
            mTextReplacer.replace(text) { listener?.onError(it) } else text

        synthesizeText(replaced, sysRate, sysPitch) { data -> // 音频获取完毕
            kotlin.runCatching {
                if (!kotlin.coroutines.coroutineContext.isActive) return@synthesizeText
                val txtTts = data.txtTts

                val audioParams = txtTts.tts.audioParams
                val sonic = if (audioParams.isDefaultValue) null
                else Sonic(txtTts.tts.audioFormat.sampleRate, 1)
                txtTts.playAudio(
                    sysRate, sysPitch, data.audio,
                    onDone = { data.done.invoke() })
                { pcmAudio ->
                    if (sonic == null) onPcmAudio.invoke(pcmAudio)
                    else {
                        sonic.volume = txtTts.tts.audioParams.volume
                        sonic.speed = txtTts.tts.audioParams.speed
                        sonic.pitch = txtTts.tts.audioParams.pitch

                        val buffer = ByteArray(pcmAudio.size)
                        sonic.writeBytesToStream(pcmAudio, pcmAudio.size)
                        onPcmAudio.invoke(sonic.readBytesFromStream(pcmAudio.size))
                    }
                }
                listener?.onPlayFinished(txtTts.text, txtTts.tts)
            }.onFailure {
                listener?.onError(TtsManagerException(cause = it, message = it.message))
            }
        }

        isSynthesizing = false
    }

    @Suppress("UNCHECKED_CAST")
    private suspend fun TtsTextPair.playAudio(
        sysRate: Int,
        sysPitch: Int,
        audioResult: AudioResult?,
        onDone: suspend () -> Unit,
        onPcmAudio: suspend (pcmAudio: ByteArray) -> Unit,
    ) {
        var costTime = 0L
        var retryTimes = 0
        if (audioResult?.data is Pair<*, *>) {
            costTime = (audioResult.data as Pair<Long, Int>).first
            retryTimes = (audioResult.data as Pair<Long, Int>).second
            listener?.onRequestSuccess(
                text, tts, 0, costTime, retryTimes
            )
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
            listener?.onRequestSuccess(text, tts, 0, costTime, retryTimes)

            if (tts.audioFormat.isNeedDecode) {
                try {
                    mAudioDecoder.doDecode(audioResult.inputStream!!, audioFormat.sampleRate)
                    { pcmData -> onPcmAudio.invoke(pcmData) }
                } catch (e: Exception) {
                    throw PlayException(tts = tts, cause = e, message = "流播放下的音频解码失败")
                } finally {
                    onDone.invoke()
                }
            } else {
                onDone.invoke()
                throw TtsManagerException("暂不支持流播放下的raw音频")
            }
        } else { // 全部加载到内存
            val audio = audioResult.inputStream!!.readBytes()
            audioResult.inputStream!!.close()
            onDone.invoke()
            listener?.onRequestSuccess(text, tts, audio.size, costTime, retryTimes)

            if (tts.audioFormat.isNeedDecode) {
                if (SysTtsConfig.isInAppPlayAudio) {
                    mAudioPlayer = mAudioPlayer ?: AudioPlayer(context)
                    mAudioPlayer?.play(
                        audio, SysTtsConfig.inAppPlaySpeed, SysTtsConfig.inAppPlayPitch
                    )
                } else
                    mAudioDecoder.doDecode(audio, audioFormat.sampleRate) { pcmData ->
                        onPcmAudio.invoke(pcmData)
                    }
            } else
                onPcmAudio.invoke(audio)
        }
    }

    interface Listener {
        fun onError(e: TtsManagerException)
        fun onStartRetry(times: Int)
        fun onRequestStarted(text: String, tts: ITextToSpeechEngine)
        fun onPlayFinished(text: String, tts: ITextToSpeechEngine)

        /**
         * @param size 音频字节数
         * @param costTime 耗时 ms
         * @param retryTimes 已经重试次数
         */
        fun onRequestSuccess(
            text: String, tts: ITextToSpeechEngine, size: Int, costTime: Long, retryTimes: Int
        )
    }
}