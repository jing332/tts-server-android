package com.github.jing332.tts_server_android.service.systts.help

import android.content.Context
import com.github.jing332.tts_server_android.data.entities.ReadRule
import com.github.jing332.tts_server_android.model.rhino.readrule.ReadRuleEngine
import com.github.jing332.tts_server_android.model.speech.TtsText
import com.github.jing332.tts_server_android.model.tts.ITextToSpeechEngine

class ReadRuleHelper {
    lateinit var engine: ReadRuleEngine

    fun init(context: Context, rule: ReadRule) {
        engine = ReadRuleEngine(context, rule)
        engine.eval()
    }

    fun handleText(
        text: String,
        config: Map<String, ITextToSpeechEngine>,
        defaultConfig: ITextToSpeechEngine,
    ): List<TtsText<ITextToSpeechEngine>> {
        val resultList = mutableListOf<TtsText<ITextToSpeechEngine>>()
        engine.handleText(text).forEach {
            val tts = config[it.tag] ?: defaultConfig
            resultList.add(TtsText(text = it.text, tts = tts))
        }

        return resultList
    }

}