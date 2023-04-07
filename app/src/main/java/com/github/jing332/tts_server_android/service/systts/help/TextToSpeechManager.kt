package com.github.jing332.tts_server_android.service.systts.help

import android.content.Context
import android.util.Log
import com.github.jing332.tts_server_android.R
import com.github.jing332.tts_server_android.constant.SpeechTarget
import com.github.jing332.tts_server_android.data.appDb
import com.github.jing332.tts_server_android.help.audio.AudioDecoder
import com.github.jing332.tts_server_android.help.audio.AudioPlayer
import com.github.jing332.tts_server_android.help.config.SysTtsConfig
import com.github.jing332.tts_server_android.model.SysTtsLib
import com.github.jing332.tts_server_android.model.speech.ITextToSpeechSynthesizer
import com.github.jing332.tts_server_android.model.speech.TtsText
import com.github.jing332.tts_server_android.model.tts.BaseAudioFormat
import com.github.jing332.tts_server_android.model.tts.BgmTTS
import com.github.jing332.tts_server_android.model.tts.ITextToSpeechEngine
import com.github.jing332.tts_server_android.model.tts.MsTTS
import com.github.jing332.tts_server_android.service.systts.help.exception.RequestException
import com.github.jing332.tts_server_android.service.systts.help.exception.TtsManagerException
import com.github.jing332.tts_server_android.util.StringUtils
import com.github.jing332.tts_server_android.util.toast
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import kotlin.coroutines.coroutineContext
import kotlin.system.measureTimeMillis

class TextToSpeechManager(val context: Context) : ITextToSpeechSynthesizer<ITextToSpeechEngine>() {
    companion object {
        const val TAG = "TtsSynthesizer"

        private val defaultTtsConfig by lazy { MsTTS() }
    }

    var listener: Listener? = null

    var isSynthesizing: Boolean = false
        private set

    lateinit var audioFormat: BaseAudioFormat
        private set


    init {
        SysTtsLib.setTimeout(SysTtsConfig.requestTimeout)
    }

    private val mConfigMap: MutableMap<Int, List<ITextToSpeechEngine>> = mutableMapOf()
    private val mLoadedTtsMap = mutableSetOf<ITextToSpeechEngine>()
    private val mSpeechRuleHelper = SpeechRuleHelper()

    override suspend fun handleText(text: String): List<TtsText<ITextToSpeechEngine>> {
        return if (SysTtsConfig.isMultiVoiceEnabled) {
            val tagTtsMap = mutableMapOf<String, MutableList<ITextToSpeechEngine>>()
            mConfigMap[SpeechTarget.CUSTOM_TAG]?.forEach { tts ->
                if (tagTtsMap[tts.speechRule.tag] == null)
                    tagTtsMap[tts.speechRule.tag] = mutableListOf()

                tagTtsMap[tts.speechRule.tag]?.add(tts)
            }

            mSpeechRuleHelper.handleText(text, tagTtsMap, defaultTtsConfig)
        } else {
            listOf(TtsText(mConfigMap[SpeechTarget.ALL]?.get(0) ?: defaultTtsConfig, text))
        }.run {
            if (SysTtsConfig.isSplitEnabled) {
                val list = mutableListOf<TtsText<ITextToSpeechEngine>>()
                forEach { ttsText ->
                    splitText(list, ttsText.text, ttsText.tts, SysTtsConfig.isMultiVoiceEnabled)
                }
                list
            } else
                this
        }
    }

    private fun splitText(
        list: MutableList<TtsText<ITextToSpeechEngine>>,
        text: String,
        tts: ITextToSpeechEngine,
        isMultiVoice: Boolean
    ) {
        if (isMultiVoice) {
            val texts = mSpeechRuleHelper.splitText(text)
            if (texts.isEmpty()) return splitText(list, text, tts, false)

            texts.forEach {
                list.add(TtsText(tts, it))
            }
        } else
            StringUtils.splitSentences(text).forEach {
                list.add(TtsText(tts, it))
            }
    }

    override suspend fun getAudio(
        tts: ITextToSpeechEngine,
        text: String,
        sysRate: Int,
        sysPitch: Int
    ): ByteArray? {
        Log.i(TAG, "请求音频：$tts, $text")
        if (!coroutineContext.isActive) return null

        var audioResult: ByteArray? = null
        var retryTimes = 0
        retry(times = 20,
            onCatch = { times, e ->
                retryTimes = times
                Log.w(TAG, "请求失败: times=$times, $text, $tts")
                listener?.onError(
                    RequestException(text = text, tts = tts, cause = e, times = times)
                )

                if (times > SysTtsConfig.maxRetryCount) {
                    Log.d(TAG, "超过最大重试次数 ${SysTtsConfig.maxRetryCount} >= times，跳过")
                    return@retry false
                }

                // 备用TTS
                if (SysTtsConfig.standbyTriggeredRetryIndex == times)
                    tts.speechRule.standbyTts?.let { sbyTts ->
                        Log.i(TAG, "使用备用TTS：$sbyTts")
                        audioResult = getAudio(sbyTts, text, sysRate, sysPitch)
                        return@retry false // 取消重试
                    }
                val canContinue =
                    !(e is RequestException && e.errorCode == RequestException.ERROR_CODE_AUDIO_NULL && times > 3)
                if (canContinue) listener?.onStartRetry(times)
                // 空音频三次后跳过(return false)
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
                        audioResult = null
                        return@retry
                    }
                    runCatching {
                        coroutineScope {
                            var hasTimeout = false
                            val timeoutJob = launch {
                                delay(SysTtsConfig.requestTimeout.toLong())
                                tts.onStop()
                                hasTimeout = true
                            }.job
                            timeoutJob.start()

                            audioResult =
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
                    }.onFailure {
                        throw it
                    }

                }
                listener?.onRequestSuccess(text, tts, audioResult?.size ?: 0, costTime, retryTimes)
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
        if (SysTtsConfig.isReplaceEnabled)
            mTextReplacer.load()

        audioFormat = if (SysTtsConfig.isMultiVoiceEnabled) {
            if (initConfig(SpeechTarget.CUSTOM_TAG)) {
                mConfigMap[SpeechTarget.CUSTOM_TAG]?.getOrNull(0)?.let {
                    appDb.speechRule.getByReadRuleId(it.speechRule.tagRuleId)?.let { rule ->
                        mSpeechRuleHelper.init(context, rule)
                    }
                }
            } else
                context.toast(R.string.systts_no_custom_tag_confg_warn)

            mConfigMap[SpeechTarget.CUSTOM_TAG]!![0].audioFormat
        } else {
            if (!initConfig(SpeechTarget.ALL))
                context.toast(R.string.systts_warn_no_ra_all)
            mConfigMap[SpeechTarget.ALL]!![0].audioFormat
        }

        initBgm()
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

        synthesizeText(replaced, sysRate, sysPitch) { audio, txtTts -> // 音频获取完毕
            if (!coroutineContext.isActive) return@synthesizeText

            if (txtTts.tts.isDirectPlay())
                txtTts.tts.startPlayWithSystemParams(txtTts.text, sysRate, sysPitch)
            else if (audio == null) {
                Log.w(TAG, "音频为空！ $txtTts")
                txtTts.tts.speechRule.standbyTts?.startPlayWithSystemParams(
                    txtTts.text,
                    sysRate,
                    sysPitch
                )
            } else {
                if (txtTts.tts.audioFormat.isNeedDecode)
                    if (SysTtsConfig.isInAppPlayAudio) {
                        mAudioPlayer = mAudioPlayer ?: AudioPlayer(context)
                        mAudioPlayer?.play(
                            audio, SysTtsConfig.inAppPlaySpeed, SysTtsConfig.inAppPlayPitch
                        )
                    } else
                        mAudioDecoder.doDecode(audio, audioFormat.sampleRate, { pcmData ->
                            if (!isSynthesizing) return@doDecode
                            onPcmAudio.invoke(pcmData)
                        }, { reason -> Log.e(TAG, "解码失败！$reason, $txtTts") })
                else
                    onPcmAudio.invoke(audio)

                listener?.onPlayFinished(txtTts.text, txtTts.tts)
            }

        }
        isSynthesizing = false
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