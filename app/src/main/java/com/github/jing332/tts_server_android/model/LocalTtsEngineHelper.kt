package com.github.jing332.tts_server_android.model

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.Voice
import com.drake.net.utils.withMain
import com.github.jing332.tts_server_android.App
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import java.util.Locale

class LocalTtsEngine(val context: Context) {
    companion object {
        private const val INIT_STATUS_WAITING = -2

        fun getEngines(): List<TextToSpeech.EngineInfo> {
            val tts = TextToSpeech(App.context, null)
            val engines = tts.engines
            tts.shutdown()
            return engines
        }
    }

    private var tts: TextToSpeech? = null


    /**
     * @return 是否成功
     */
    suspend fun setEngine(name: String): Boolean = coroutineScope {
        shutdown()

        var status = INIT_STATUS_WAITING
        withMain { tts = TextToSpeech(context, { status = it }, name) }

        while (isActive) {
            if (status == TextToSpeech.SUCCESS) break
            else if (status != INIT_STATUS_WAITING) {
                tts = null
                return@coroutineScope false // 初始化失败
            }

            try {
                delay(100)
            } catch (_: CancellationException) {
            }
        }
        if (!isActive) { // 取消了
            tts = null
            return@coroutineScope false
        }

        return@coroutineScope true
    }

    fun shutdown() {
        tts?.shutdown()
        tts = null
    }

    val voices: List<Voice>
        get() = try {
            tts?.voices?.toList()!!
        } catch (e: NullPointerException) {
            emptyList()
        }

    val locales: List<Locale>
        get() = try {
            tts!!.availableLanguages.toList().sortedBy { it.toString() }
        } catch (e: NullPointerException) {
            emptyList()
        }

}