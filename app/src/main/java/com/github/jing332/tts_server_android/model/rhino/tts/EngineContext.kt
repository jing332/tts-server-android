package com.github.jing332.tts_server_android.model.rhino.tts

import android.content.Context
import androidx.annotation.Keep
import com.github.jing332.tts_server_android.model.rhino.core.BaseScriptEngineContext
import com.github.jing332.tts_server_android.model.speech.tts.PluginTTS

/**
 * @param tts 在JS中用 `ttsrv.tts` 访问
 */
@Keep
data class EngineContext(
    var tts: PluginTTS,
    val userVars: Map<String, String> = mutableMapOf(),
    override val context: Context,
    override val engineId: String
) :
    BaseScriptEngineContext(context, engineId) {
}