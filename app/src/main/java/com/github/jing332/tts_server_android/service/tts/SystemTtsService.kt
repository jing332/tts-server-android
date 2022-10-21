package com.github.jing332.tts_server_android.service.tts

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioFormat
import android.os.PowerManager
import android.speech.tts.SynthesisCallback
import android.speech.tts.SynthesisRequest
import android.speech.tts.TextToSpeech
import android.speech.tts.TextToSpeechService
import android.util.Log
import com.github.jing332.tts_server_android.service.tts.help.TtsManager
import com.github.jing332.tts_server_android.utils.GcManager
import kotlinx.coroutines.runBlocking
import java.util.*


class SystemTtsService : TextToSpeechService() {
    companion object {
        const val TAG = "TtsService"
        const val ACTION_ON_CONFIG_CHANGED = "action_on_config_changed"
        const val ACTION_ON_LOG = "action_on_log"
    }

    private val currentLanguage: MutableList<String> = mutableListOf("zho", "CHN", "")

    private val ttsManager: TtsManager by lazy { TtsManager(this) }

    private val mReceiver: MyReceiver by lazy { MyReceiver() }
    private val mWakeLock by lazy {
        val powerManager = getSystemService(POWER_SERVICE) as PowerManager
        return@lazy powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK or PowerManager.ON_AFTER_RELEASE,
            "tts-server:tts"
        )
    }


    override fun onCreate() {
        super.onCreate()
        val intentFilter = IntentFilter(ACTION_ON_CONFIG_CHANGED)
        registerReceiver(mReceiver, intentFilter)
        mWakeLock.acquire(60 * 20 * 100)
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(mReceiver)
        mWakeLock.release()
    }

    override fun onIsLanguageAvailable(lang: String?, country: String?, variant: String?): Int {
        return if (Locale.SIMPLIFIED_CHINESE.isO3Language == lang || Locale.US.isO3Language == lang) {
            if (Locale.SIMPLIFIED_CHINESE.isO3Country == country || Locale.US.isO3Country == country) TextToSpeech.LANG_COUNTRY_AVAILABLE else TextToSpeech.LANG_AVAILABLE
        } else TextToSpeech.LANG_NOT_SUPPORTED
    }

    override fun onGetLanguage(): Array<String> {
        return currentLanguage.toTypedArray()
    }

    override fun onLoadLanguage(lang: String?, country: String?, variant: String?): Int {
        val result = onIsLanguageAvailable(lang, country, variant)
        currentLanguage.clear()
        currentLanguage.addAll(
            mutableListOf(
                lang.toString(),
                country.toString(),
                variant.toString()
            )
        )

        return result
    }

    override fun onStop() {
        Log.d(TAG, "onStop")
        ttsManager.stop()
    }

    override fun onSynthesizeText(request: SynthesisRequest?, callback: SynthesisCallback?) {
        synchronized(this) {
            reNewWakeLock()
            val text = request?.charSequenceText.toString()
            Log.d(TAG, "开始合成: $text")

            if (text.isBlank()) {
                Log.d(TAG, "文本为空，跳过")
                callback!!.start(
                    16000,
                    AudioFormat.ENCODING_PCM_16BIT, 1
                )
                callback.done()
                return
            }
            runBlocking {
                ttsManager.synthesizeText(request, callback)
            }
        }
    }

    private fun reNewWakeLock() {
        if (!mWakeLock.isHeld) {
            mWakeLock.acquire(60 * 20 * 1000)
            GcManager.doGC()
            Log.i(TAG, "刷新WakeLock 20分钟")
        }
    }

    inner class MyReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == ACTION_ON_CONFIG_CHANGED) {
                ttsManager.ttsConfig.loadConfig(this@SystemTtsService)
            }
        }
    }
}