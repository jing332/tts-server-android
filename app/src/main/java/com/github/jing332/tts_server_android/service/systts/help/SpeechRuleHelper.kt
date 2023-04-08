package com.github.jing332.tts_server_android.service.systts.help

import android.content.Context
import android.os.SystemClock
import com.github.jing332.tts_server_android.data.entities.SpeechRule
import com.github.jing332.tts_server_android.model.rhino.speech_rule.SpeechRuleEngine
import com.github.jing332.tts_server_android.model.speech.TtsText
import com.github.jing332.tts_server_android.model.tts.ITextToSpeechEngine
import java.util.Random

class SpeechRuleHelper {
    private val random: Random by lazy { Random(SystemClock.elapsedRealtime()) }
    lateinit var engine: SpeechRuleEngine

    fun init(context: Context, rule: SpeechRule) {
        engine = SpeechRuleEngine(context, rule)
        engine.eval()
        random.setSeed(SystemClock.elapsedRealtime())
    }

    fun splitText(text: String): List<String> {
        return engine.splitText(text).map { it.toString() }
    }

    fun handleText(
        text: String,
        config: Map<String, List<ITextToSpeechEngine>>,
        defaultConfig: ITextToSpeechEngine,
    ): List<TtsText<ITextToSpeechEngine>> {
        if (!this::engine.isInitialized) return listOf(TtsText(defaultConfig, text))
        val resultList = mutableListOf<TtsText<ITextToSpeechEngine>>()

        val list = config.entries.map { it.value }.flatten().map { it.speechRule }
        engine.handleText(text, list).forEach { txtWithTag ->
            if (txtWithTag.text.isNotBlank()) {
                val sameTagList = config[txtWithTag.tag] ?: listOf(defaultConfig)
                val ttsFromId = sameTagList.find { it.speechRule.configId == txtWithTag.id }

                val tts = ttsFromId ?: sameTagList[random.nextInt(sameTagList.size)]
                resultList.add(TtsText(text = txtWithTag.text, tts = tts))
            }
        }

        return resultList
    }

}