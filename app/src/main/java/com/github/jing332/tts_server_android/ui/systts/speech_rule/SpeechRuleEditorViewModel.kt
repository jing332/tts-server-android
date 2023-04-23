package com.github.jing332.tts_server_android.ui.systts.speech_rule

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.github.jing332.tts_server_android.constant.SpeechTarget
import com.github.jing332.tts_server_android.data.appDb
import com.github.jing332.tts_server_android.data.entities.SpeechRule
import com.github.jing332.tts_server_android.data.entities.systts.SpeechRuleInfo
import com.github.jing332.tts_server_android.model.rhino.ExceptionExt.lineMessage
import com.github.jing332.tts_server_android.model.rhino.core.Logger
import com.github.jing332.tts_server_android.model.rhino.speech_rule.SpeechRuleEngine
import com.script.ScriptException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SpeechRuleEditorViewModel(application: Application) : AndroidViewModel(application) {
    private val _errorLiveData: MutableLiveData<Throwable> = MutableLiveData()
    val errorLiveData: LiveData<Throwable>
        get() = _errorLiveData

    private val _displayLoggerLiveData: MutableLiveData<Boolean> = MutableLiveData()
    val displayLoggerLiveData: LiveData<Boolean>
        get() = _displayLoggerLiveData

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
        mRuleEngine = SpeechRuleEngine(getApplication(), mSpeechRule, mSpeechRule.code, Logger())

        _codeLiveData.value = mSpeechRule.code
    }

    fun evalRuleInfo(): Boolean {
        kotlin.runCatching {
            mRuleEngine.evalInfo()
            _displayLoggerLiveData.postValue(true)
        }.onFailure {
            _errorLiveData.postValue(it)
            return false
        }

        return true
    }

    fun debug(text: String) {
        viewModelScope.launch(Dispatchers.IO) {
            if (evalRuleInfo()) {
                kotlin.runCatching {
                    logger.i("handleText()...")

                    val rules = appDb.systemTtsDao.getEnabledList(SpeechTarget.CUSTOM_TAG).map {
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

}
