package com.github.jing332.tts_server_android.compose.systts.plugin

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.github.jing332.tts_server_android.app
import com.github.jing332.tts_server_android.conf.PluginConfig
import com.github.jing332.tts_server_android.data.entities.plugin.Plugin
import com.github.jing332.tts_server_android.model.rhino.tts.TtsPluginUiEngine
import com.github.jing332.tts_server_android.model.speech.tts.PluginTTS
import com.github.jing332.tts_server_android.utils.readableString
import com.github.jing332.tts_server_android.utils.rootCause
import com.script.ScriptException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

class PluginEditorViewModel(application: Application) : AndroidViewModel(application) {
    companion object {
        private const val TAG = "PluginEditViewModel"
    }

    lateinit var pluginEngine: TtsPluginUiEngine
    internal lateinit var plugin: Plugin private set
    internal lateinit var pluginTTS: PluginTTS private set

    private val _displayLoggerLiveData: MutableLiveData<Boolean> = MutableLiveData()
    val displayLoggerLiveData: LiveData<Boolean>
        get() = _displayLoggerLiveData

    private val _updateCodeLiveData = MutableLiveData<String>()

    val codeLiveData: LiveData<String>
        get() = _updateCodeLiveData

    fun init(plugin: Plugin, defaultCode: String) {
        this.plugin = plugin.apply { if (code.isEmpty()) code = defaultCode }
        pluginTTS = PluginTTS(plugin = this.plugin)
        updateTTS(pluginTTS)

        _updateCodeLiveData.postValue(this.plugin.code)
    }

    fun updateTTS(tts: PluginTTS) {
        pluginEngine = TtsPluginUiEngine(tts, getApplication())
        pluginTTS = tts.also {
            it.pluginEngine = pluginEngine
            it.plugin = plugin
        }
    }

    fun updateCode(code: String) {
        plugin.code = code
        pluginEngine.code = code
//        if (isSave) appDb.pluginDao.update()
    }

    fun clearPluginCache() {
        val file = File("${app.externalCacheDir!!.absolutePath}/${plugin.pluginId}")
        file.deleteRecursively()
    }

    fun evalInfo(): Boolean {
        _displayLoggerLiveData.value = true
        val plugin = try {
            pluginEngine.evalPluginInfo()
        } catch (e: Exception) {
            writeErrorLog(e)
            return false
        }
        pluginEngine.logger.d(plugin.toString().replace(", ", "\n"))
        return true
    }

    fun debug() {
        if (!evalInfo()) return
        viewModelScope.launch(Dispatchers.IO) {
            kotlin.runCatching {
                val sampleRate = pluginEngine.getSampleRate(pluginTTS.locale, pluginTTS.voice)
                pluginEngine.logger.d("采样率: $sampleRate")
            }.onFailure {
                writeErrorLog(it)
            }

            runCatching {
                val isNeedDecode = pluginEngine.isNeedDecode(pluginTTS.locale, pluginTTS.voice)
                pluginEngine.logger.d("需要解码: $isNeedDecode")
            }.onFailure {
                writeErrorLog(it)
            }

            kotlin.runCatching {
                pluginTTS.onLoad()
                val audio = pluginTTS.getAudioWithSystemParams(PluginConfig.textParam.value)
                if (audio == null)
                    pluginEngine.logger.w("音频为空！")
                else{
                    val bytes = audio.readBytes()
                    pluginEngine.logger.i("音频大小: ${bytesToReadable(bytes.size.toLong())}")
                }
            }.onFailure {
                writeErrorLog(it)
            }
        }
    }

    private fun writeErrorLog(t: Throwable) {
        val errStr = if (t is ScriptException) {
            "第 ${t.lineNumber} 行错误：${t.rootCause?.message ?: t}"
        } else {
            t.message + "(${t.readableString})"
        }
        pluginEngine.logger.e(errStr)
    }

    private fun bytesToReadable(bytes: Long): String {
        val kb = 1024
        val mb = kb * 1024
        val gb = mb * 1024

        return when {
            bytes < mb -> "${"%.2f".format(bytes.toDouble() / kb)} KB"
            bytes < gb -> "${"%.2f".format(bytes.toDouble() / mb)} MB"
            else -> "$bytes B"
        }
    }
}