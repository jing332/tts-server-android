package com.github.jing332.tts_server_android.service.systts.help

import android.content.Context
import com.github.jing332.tts_server_android.data.entities.SpeechRule
import com.github.jing332.tts_server_android.model.rhino.speech_rule.SpeechRuleEngine
import com.github.jing332.tts_server_android.model.speech.TtsText
import com.github.jing332.tts_server_android.model.tts.ITextToSpeechEngine

class SpeechRuleHelper {
    lateinit var engine: SpeechRuleEngine

    fun init(context: Context, rule: SpeechRule) {
        engine = SpeechRuleEngine(context, rule)
        engine.eval()
    }

    fun handleText(
        text: String,
        config: Map<String, ITextToSpeechEngine>,
        defaultConfig: ITextToSpeechEngine,
    ): List<TtsText<ITextToSpeechEngine>> {
        if (!this::engine.isInitialized) return listOf(TtsText(defaultConfig, text))

        val resultList = mutableListOf<TtsText<ITextToSpeechEngine>>()
        engine.handleText(text).forEach {
            val tts = config[it.tag] ?: defaultConfig
            resultList.add(TtsText(text = it.text, tts = tts))
        }

        return resultList
    }

}