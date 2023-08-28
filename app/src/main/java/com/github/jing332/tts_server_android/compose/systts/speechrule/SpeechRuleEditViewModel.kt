package com.github.jing332.tts_server_android.compose.systts.speechrule

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.jing332.tts_server_android.app
import com.github.jing332.tts_server_android.constant.SpeechTarget
import com.github.jing332.tts_server_android.data.appDb
import com.github.jing332.tts_server_android.data.entities.SpeechRule
import com.github.jing332.tts_server_android.model.rhino.ExceptionExt.lineMessage
import com.github.jing332.tts_server_android.model.rhino.core.Logger
import com.github.jing332.tts_server_android.model.rhino.speech_rule.SpeechRuleEngine
import com.script.ScriptException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SpeechRuleEditViewModel : ViewModel() {
    private val _codeLiveData: MutableLiveData<String> = MutableLiveData()
    val codeLiveData: LiveData<String>
        get() = _codeLiveData

    private lateinit var mSpeechRule: SpeechRule
    private lateinit var mRuleEngine: SpeechRuleEngine

    val logger: Logger
        get() = mRuleEngine.logger

    val speechRule: SpeechRule
        get() = mSpeechRule

    var code: String
        get() = mRuleEngine.code
        set(value) {
            mRuleEngine.code = value
            mSpeechRule.code = value
        }

    fun init(speechRule: SpeechRule, defaultCode: String) {
        mSpeechRule = speechRule

        if (mSpeechRule.code.isBlank()) mSpeechRule.code = defaultCode
        mRuleEngine = SpeechRuleEngine(app, mSpeechRule, mSpeechRule.code, Logger())

        _codeLiveData.value = mSpeechRule.code
    }

    fun evalRuleInfo() {
        mRuleEngine.evalInfo()
    }

    fun debug(text: String) {
        evalRuleInfo()
        viewModelScope.launch(Dispatchers.IO) {
            kotlin.runCatching {
                logger.i("handleText()...")

                val rules =
                    appDb.systemTtsDao.getEnabledListForSort(SpeechTarget.CUSTOM_TAG).map {
                        it.speechRule.apply { configId = it.id }
                    }
                val list = mRuleEngine.handleText(text, rules)
                try {
                    list.forEach {
                        val texts = mRuleEngine.splitText(it.text)
                        logger.i(
                            "\ntag=${it.tag}, id=${it.id}, text=${it.text.trim()}, splittedTexts=${
                                texts.joinToString(" | ").trim()
                            }"
                        )
                    }
                } catch (_: NoSuchMethodException) {
                }
            }.onFailure {
                if (it is ScriptException) {
                    logger.e(it.lineMessage())
                } else {
                    logger.e(it.stackTraceToString())
                }

            }
        }
    }

}