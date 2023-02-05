package com.github.jing332.tts_server_android.help.plugin

import com.script.javascript.RhinoScriptEngine

class JsEngineConfig {
    companion object {
        val SCRIPT_ENGINE by lazy { RhinoScriptEngine() }

        const val ENGINE_VERSION = 1
    }
}