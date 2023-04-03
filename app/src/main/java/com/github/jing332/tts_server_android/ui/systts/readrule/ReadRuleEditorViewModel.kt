package com.github.jing332.tts_server_android.ui.systts.readrule

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.github.jing332.tts_server_android.data.entities.ReadRule
import com.github.jing332.tts_server_android.model.rhino.ExceptionExt.lineMessage
import com.github.jing332.tts_server_android.model.rhino.core.Logger
import com.github.jing332.tts_server_android.model.rhino.readrule.ReadRuleEngine
import com.script.ScriptException

class ReadRuleEditorViewModel(application: Application) : AndroidViewModel(application) {
    private val _errorLiveData: MutableLiveData<Throwable> = MutableLiveData()
    val errorLiveData: LiveData<Throwable>
        get() = _errorLiveData

    private val _displayLoggerLiveData: MutableLiveData<Boolean> = MutableLiveData()
    val displayLoggerLiveData: LiveData<Boolean>
        get() = _displayLoggerLiveData

    private val _codeLiveData: MutableLiveData<String> = MutableLiveData()
    val codeLiveData: LiveData<String>
        get() = _codeLiveData

    private lateinit var mReadRule: ReadRule
    private lateinit var mRuleEngine: ReadRuleEngine

    val logger: Logger
        get() = mRuleEngine.logger

    val readRule: ReadRule
        get() = mReadRule

    var code: String
        get() = mRuleEngine.code
        set(value) {
            mRuleEngine.code = value
        }

    fun init(readRule: ReadRule, defaultCode: String) {
        mReadRule = readRule
        if (readRule.code.isBlank()) mReadRule.code = defaultCode
        mRuleEngine = ReadRuleEngine(getApplication(), mReadRule, "", Logger())

        _codeLiveData.value = mReadRule.code
    }

    fun evalRuleInfo(): Boolean {
        kotlin.runCatching {
            mRuleEngine.evalInfo()
            _displayLoggerLiveData.value = true
        }.onFailure {
            _errorLiveData.value = it
            return false
        }

        return true
    }

    fun debug(text: String) {
        if (evalRuleInfo()) {
            kotlin.runCatching {
                val list = mRuleEngine.handleText(text)
                logger.i(list)
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