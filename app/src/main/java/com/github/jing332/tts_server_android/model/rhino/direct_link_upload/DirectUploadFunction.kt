package com.github.jing332.tts_server_android.model.rhino.direct_link_upload

import com.script.javascript.RhinoScriptEngine
import org.mozilla.javascript.NativeObject

data class DirectUploadFunction(
    val scriptEngine: RhinoScriptEngine,
    val thisObj: NativeObject,
    val funcName: String,
) {
    fun invoke(config: String): String? {
        return scriptEngine.invokeMethod(thisObj, funcName, config)
            ?.run { this as String }
    }

}