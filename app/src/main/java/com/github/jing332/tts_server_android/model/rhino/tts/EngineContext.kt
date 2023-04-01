package com.github.jing332.tts_server_android.model.rhino.tts

import android.content.Context
import androidx.annotation.Keep
import com.github.jing332.tts_server_android.model.rhino.core.BaseScriptEngineContext
import com.github.jing332.tts_server_android.model.tts.PluginTTS

/**
 * @param tts 在JS中用 `ttsrv.tts` 访问
 */
@Keep
data class EngineContext(
    val tts: PluginTTS,
    override val context: Context,
    override val pluginId: String
) :
    BaseScriptEngineContext(context, pluginId) {
}