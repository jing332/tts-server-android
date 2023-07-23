package com.github.jing332.tts_server_android.model.rhino.core

import com.script.javascript.RhinoScriptEngine
import org.mozilla.javascript.NativeObject

open class BaseScriptEngine(
    open val rhino: RhinoScriptEngine = RhinoScriptEngine(),
    open val ttsrvObject: BaseScriptEngineContext,
    open var code: String = "",
    open val logger: Logger = Logger.global,
) {
    companion object {
        const val OBJ_TTSRV = "ttsrv"
        const val OBJ_LOGGER = "logger"
    }

    open fun findObject(name: String): NativeObject {
        return rhino.get(name).run {
            if (this == null) throw Exception("Not found object: $name")
            else this as NativeObject
        }
    }

    fun putDefaultObjects() {
        rhino.put(OBJ_TTSRV, ttsrvObject)
        rhino.put(OBJ_LOGGER, logger)
    }

    @Synchronized
    open fun eval(
        prefixCode: String = ""
    ): Any? {
        putDefaultObjects()

        return rhino.eval("${prefixCode.removePrefix(";").removeSuffix(";")};$code")
    }

}