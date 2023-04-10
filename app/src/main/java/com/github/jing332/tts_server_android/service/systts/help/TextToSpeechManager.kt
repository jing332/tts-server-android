package com.github.jing332.tts_server_android.service.systts.help

import android.content.Context
import android.provider.MediaStore.Audio
import android.util.Log
import com.drake.net.utils.withIO
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
import com.github.jing332.tts_server_android.service.systts.help.exception.ConfigLoadException
import com.github.jing332.tts_server_android.service.systts.help.exception.RequestException
import com.github.jing332.tts_server_android.service.systts.help.exception.TextHandleException
import com.github.jing332.tts_server_android.service.systts.help.exception.TtsManagerException
import com.github.jing332.tts_server_android.util.StringUtils
import com.github.jing332.tts_server_android.util.toast
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import java.io.ByteArrayInputStream
import java.io.InputStream
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

    var audioFormat: BaseAudioFormat = BaseAudioFormat()
        private set

    init {
        SysTtsLib.setTimeout(SysTtsConfig.requestTimeout)
    }

    private val mConfigMap: MutableMap<Int, List<ITextToSpeechEngine>> = mutableMapOf()
    private val mLoadedTtsMap = mutableSetOf<ITextToSpeechEngine>()
    private val mSpeechRuleHelper = SpeechRuleHelper()

    override suspend fun handleText(text: String): List<TtsText<ITextToSpeechEngine>> {
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
        }.onFailure {
            listener?.onError(
                TextHandleException(text = text, tts = null, message = it.message, cause = it)
            )
        }

        return emptyList()
    }

    private fun splitText(
        list: MutableList<TtsText<ITextToSpeechEngine>>,
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
                    TextHandleException(text = text, tts = tts, message = "splittedTexts is empty.")
                )
            else
                texts.forEach { list.add(TtsText(tts, it)) }
        } else {
            Log.d(TAG, "使用内置分割规则...")
            StringUtils.splitSentences(text).forEach {
                list.add(TtsText(tts, it))
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
                Log.w(TAG, "请求失败: times=$times, $text, $tts")
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
                        audioResult = getAudio(sbyTts, text, sysRate, sysPitch)
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
                        audioResult = null
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

                        val bytes = tts.getAudioWithSystemParams(text, sysRate, sysPitch)
                            ?: throw RequestException(
                                errorCode = RequestException.ERROR_CODE_AUDIO_NULL,
                                tts = tts, text = text
                            )

                        audioResult?.inputStream = ByteArrayInputStream(bytes)
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

    @Suppress("UNCHECKED_CAST")
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
                if (!coroutineContext.isActive) return@synthesizeText
                val txtTts = data.txtTts
                val audio = data.audio?.inputStream?.readBytes()
                data.audio?.inputStream?.close()
                data.done()

                if (data.audio?.data is Pair<*, *>) {
                    val costTime = (data.audio.data as Pair<Long, Int>).first as Long
                    val retryTimes = (data.audio.data as Pair<Long, Int>).second as Int
                    listener?.onRequestSuccess(
                        text, txtTts.tts, audio?.size ?: 0, costTime, retryTimes
                    )
                }

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
                            mAudioDecoder.doDecode(audio, audioFormat.sampleRate) { pcmData ->
                                if (!isSynthesizing) return@doDecode
                                onPcmAudio.invoke(pcmData)
                            }
                    else
                        onPcmAudio.invoke(audio)

                    listener?.onPlayFinished(txtTts.text, txtTts.tts)
                }
            }.onFailure {
                listener?.onError(TtsManagerException(cause = it, message = it.message))
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