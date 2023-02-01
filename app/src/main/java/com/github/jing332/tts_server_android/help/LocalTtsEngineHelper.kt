package com.github.jing332.tts_server_android.help

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.Voice
import kotlinx.coroutines.delay
import java.util.*

class LocalTtsEngineHelper(val context: Context) {
    companion object {
        private const val INIT_STATUS_WAITING = -2
    }

    private var tts: TextToSpeech? = null

    private var engineName: String = ""

    /**
     * return 是否 初始化成功
     */
    suspend fun setEngine(name: String): Boolean {
        if (engineName != name) {
            engineName = name
            shutdown()

            var status = INIT_STATUS_WAITING
            tts = TextToSpeech(context, { status = it }, name)

            for (i in 1..50) { // 5s
                if (status == TextToSpeech.SUCCESS) break
                else if (i == 50) return false
                delay(100)
            }

        }
        return true
    }

    fun shutdown() {
        tts?.shutdown()
    }


    val voices: List<Voice>
        get() = try {
            tts!!.voices?.toList()!!
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