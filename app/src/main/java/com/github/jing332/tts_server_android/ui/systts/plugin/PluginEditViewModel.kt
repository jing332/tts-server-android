package com.github.jing332.tts_server_android.ui.systts.plugin

import androidx.lifecycle.ViewModel
import com.github.jing332.tts_server_android.help.plugin.EditUiJsEngine
import com.github.jing332.tts_server_android.model.tts.PluginTTS

class PluginEditViewModel : ViewModel() {
    lateinit var pluginEngine: EditUiJsEngine
    fun setData(data: PluginTTS) {
        pluginEngine = EditUiJsEngine(pluginTts = data)
    }


}