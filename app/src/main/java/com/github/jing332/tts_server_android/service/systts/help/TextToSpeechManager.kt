package com.github.jing332.tts_server_android.service.systts.help

import android.content.Context
import android.util.Log
import com.github.jing332.tts_server_android.R
import com.github.jing332.tts_server_android.constant.ReadAloudTarget
import com.github.jing332.tts_server_android.data.appDb
import com.github.jing332.tts_server_android.help.audio.AudioDecoder
import com.github.jing332.tts_server_android.help.audio.AudioPlayer
import com.github.jing332.tts_server_android.help.config.SysTtsConfig
import com.github.jing332.tts_server_android.model.SysTtsLib
import com.github.jing332.tts_server_android.model.speech.ITextToSpeechSynthesizer
import com.github.jing332.tts_server_android.model.tts.BaseAudioFormat
import com.github.jing332.tts_server_android.model.tts.BgmTTS
import com.github.jing332.tts_server_android.model.tts.ITextToSpeechEngine
import com.github.jing332.tts_server_android.model.tts.MsTTS
import com.github.jing332.tts_server_android.service.systts.help.exception.RequestException
import com.github.jing332.tts_server_android.service.systts.help.exception.TtsManagerException
import com.github.jing332.tts_server_android.util.toast
import kotlinx.coroutines.TimeoutCancellationException
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
    }

    var listener: Listener? = null

    var isSynthesizing: Boolean = false
        private set

    lateinit var audioFormat: BaseAudioFormat
        private set


    init {
        SysTtsLib.setTimeout(5000)
    }

    val configMap: MutableMap<Int, List<ITextToSpeechEngine>> = mutableMapOf()
    private val mLoadedTtsMap = mutableSetOf<ITextToSpeechEngine>()

    private val isMultiVoice: Boolean
        get() = SysTtsConfig.isMultiVoiceEnabled

    private fun initConfig(target: Int): Boolean {
        var isMissing = false
        configMap[target] =
            appDb.systemTtsDao.getEnabledList(target, false).map {
                it.tts.apply {
                    info.target = it.readAloudTarget
                }
            }
        if (configMap[target]?.isEmpty() == true) {
            isMissing = true
            Log.w(TAG, "缺少朗读目标$target, 使用内置MsTTS！")
            configMap[target] = listOf(MsTTS())
        }

        val standby =
            appDb.systemTtsDao.getEnabledList(target, true).map { it.tts }.getOrNull(0)
        configMap[target]?.forEach {
            it.info.standbyTts = standby
        }

        return isMissing
    }


    override suspend fun handleText(text: String): List<TtsText<ITextToSpeechEngine>> {
        return if (isMultiVoice) {
            VoiceTools.splitMultiVoice(
                text,
                configMap[ReadAloudTarget.ASIDE]!!,
                configMap[ReadAloudTarget.DIALOGUE]!!,
                0
            ).map {
                TtsText(it.tts, it.speakText)
            }
        } else {
            listOf(TtsText(configMap[ReadAloudTarget.ALL]!![0], text))
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
                Log.i(TAG, "请求失败: times=$times, $text, $tts")
                listener?.onError(
                    RequestException(text = text, tts = tts, cause = e, times = times)
                )
                // 备用TTS
                tts.info.standbyTts?.let { sbyTts ->
                    Log.i(TAG, "使用备用TTS：$sbyTts")
                    audioResult = getAudio(sbyTts, text, sysRate, sysPitch)
                    return@retry false // 取消重试
                }
                // 空音频三次后跳过
                return@retry !(e is RequestException && e.errorCode == RequestException.ERROR_CODE_AUDIO_NULL && times > 3)
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
                        try {
                            var hasTimeout = false
                            val timeoutJob = launch {
                                delay(SysTtsConfig.requestTimeout.toLong())
                                tts.onStop()
                                hasTimeout = true
                            }.job
                            timeoutJob.start()

                            audioResult =
                                tts.getAudio(text, sysRate, sysPitch) ?: throw RequestException(
                                    errorCode = RequestException.ERROR_CODE_AUDIO_NULL,
                                    tts = tts, text = text
                                )
                            if (hasTimeout) throw Exception(
                                context.getString(
                                    R.string.failed_timed_out,
                                    SysTtsConfig.requestTimeout
                                )
                            )

                            else timeoutJob.cancelAndJoin()
                        } catch (e: TimeoutCancellationException) {
                            TODO("Not yet implemented")
                        }
                    }
                }
                listener?.onRequestSuccess(text, tts, audioResult?.size ?: 0, costTime, retryTimes)
            })

        return audioResult
    }

    private val mAudioDecoder = AudioDecoder()
    private var mAudioPlayer: AudioPlayer? = null
    private var mBgmPlayer: BgmPlayer? = null

    private fun initBgm() {
        val list = mutableSetOf<Pair<Float, String>>()
        appDb.systemTtsDao.getEnabledList(ReadAloudTarget.BGM).forEach {
            val tts = (it.tts as BgmTTS)
            val volume = if (tts.volume == 0) SysTtsConfig.bgmVolume else it.tts.volume / 100f
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
        audioFormat = if (isMultiVoice) {
            if (initConfig(ReadAloudTarget.ASIDE))
                context.toast(R.string.systts_warn_no_ra_aside)
            if (initConfig(ReadAloudTarget.DIALOGUE))
                context.toast(R.string.systts_warn_no_ra_dialogue)

            configMap[ReadAloudTarget.ASIDE]!![0].audioFormat
        } else {
            if (initConfig(ReadAloudTarget.ALL))
                context.toast(R.string.systts_warn_no_ra_all)
            configMap[ReadAloudTarget.ALL]!![0].audioFormat
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

        synthesizeText(text, sysRate, sysPitch) { audio, txtTts -> // 音频获取完毕
            if (!coroutineContext.isActive) return@synthesizeText

            if (txtTts.tts.isDirectPlay())
                txtTts.tts.startPlay(txtTts.text, sysRate, sysPitch)
            else if (audio == null) {
                Log.w(TAG, "音频为空！ $txtTts")
                txtTts.tts.info.standbyTts?.startPlay(txtTts.text, sysRate, sysPitch)
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
            }

        }
        isSynthesizing = false
    }


    interface Listener {
        fun onError(e: TtsManagerException)
        fun onRequestStarted(text: String, tts: ITextToSpeechEngine)

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