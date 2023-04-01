package com.github.jing332.tts_server_android.model.rhino.core

import android.content.Context
import com.github.jing332.tts_server_android.model.rhino.core.ext.JsExtensions

/**
 * ttsrv 对象类
 */
open class BaseScriptEngineContext(override val context: Context, override val pluginId: String) :
    JsExtensions(context, pluginId) {
}