package com.github.jing332.tts_server_android.model.script.core

import android.content.Context
import com.github.jing332.tts_server_android.model.script.core.ext.JsExtensions
import com.script.javascript.RhinoScriptEngine
import org.mozilla.javascript.NativeObject

open class BaseScriptEngine(
    open val rhino: RhinoScriptEngine,
    override val context: Context,
    open var code: String,
    open val id: String,
    open val logger: Logger = Logger.global,
) : JsExtensions(context, id) {
    companion object {
        const val OBJ_TTSRV = "ttsrv"
        const val OBJ_LOGGER = "logger"
    }


    fun findObject(name: String): NativeObject {
        return rhino.get(name).run {
            if (this == null) throw Exception("Not found object: $name")
            else this as NativeObject
        }
    }

    open fun eval(prefixCode: String = ""): Any? {
        rhino.put(OBJ_TTSRV, this)
        rhino.put(OBJ_LOGGER, logger)

        return rhino.eval("${prefixCode.removePrefix(";").removeSuffix(";")};$code")
    }

}