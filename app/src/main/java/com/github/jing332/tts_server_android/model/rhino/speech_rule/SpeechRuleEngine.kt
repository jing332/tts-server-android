package com.github.jing332.tts_server_android.model.rhino.speech_rule

import android.content.Context
import com.github.jing332.tts_server_android.R
import com.github.jing332.tts_server_android.data.entities.SpeechRule
import com.github.jing332.tts_server_android.model.rhino.core.BaseScriptEngine
import com.github.jing332.tts_server_android.model.rhino.core.BaseScriptEngineContext
import com.github.jing332.tts_server_android.model.rhino.core.Logger

class SpeechRuleEngine(
    val context: Context,
    private val rule: SpeechRule,
    override var code: String = rule.code,
    override val logger: Logger = Logger.global
) :
    BaseScriptEngine(ttsrvObject = BaseScriptEngineContext(context = context, "ReadRule")) {
    companion object {
        const val OBJ_JS = "SpeechRuleJS"

        const val FUNC_HANDLE_TEXT = "handleText"
        const val FUNC_SPLIT_TEXT = "splitText"
    }

    private val objJS
        get() = findObject(OBJ_JS)

    @Suppress("UNCHECKED_CAST")
    fun evalInfo() {
        eval()
        objJS.apply {
            rule.name = get("name").toString()
            rule.ruleId = get("id").toString()
            rule.author = get("author").toString()

            rule.tags = get("tags") as Map<String, String>

            runCatching {
                rule.version = (get("version") as Double).toInt()
            }.onFailure {
                throw NumberFormatException(context.getString(R.string.plugin_bad_format))
            }
        }
    }

    fun handleText(text: String): List<TextWithTag> {
        val resultList: MutableList<TextWithTag> = mutableListOf()
        rhino.invokeMethod(objJS, FUNC_HANDLE_TEXT, text)
            ?.run { this as List<*> }
            ?.let { list ->
                list.forEach {
                    if (it is Map<*, *>) {
                        resultList.add(TextWithTag(it["text"].toString(), it["tag"].toString()))
                    }
                }

            }
        return resultList
    }

    @Suppress("UNCHECKED_CAST")
    fun splitText(text: String): List<CharSequence> {
        return rhino.invokeMethod(
            objJS,
            FUNC_SPLIT_TEXT,
            text
        ) as List<CharSequence>
    }

    data class TextWithTag(val text: String, val tag: String)
}