package com.github.jing332.tts_server_android.model.rhino.direct_link_upload

import android.content.Context
import com.github.jing332.tts_server_android.conf.DirectUploadConfig
import com.github.jing332.tts_server_android.model.rhino.core.BaseScriptEngine
import com.github.jing332.tts_server_android.model.rhino.core.BaseScriptEngineContext
import com.github.jing332.tts_server_android.model.rhino.core.Logger
import com.script.javascript.RhinoScriptEngine
import org.mozilla.javascript.NativeObject

class DirectUploadEngine(
    override val rhino: RhinoScriptEngine = RhinoScriptEngine(),
    private val context: Context,
    override val logger: Logger = Logger(),
    override var code: String = DirectUploadConfig.code.value,
) : BaseScriptEngine(rhino, BaseScriptEngineContext(context, "DirectUpload"), code, logger) {
    companion object {
        private const val TAG = "DirectUploadEngine"
        const val OBJ_DIRECT_UPLOAD = "DirectUploadJS"
    }

    private val jsObject: NativeObject
        get() = findObject(OBJ_DIRECT_UPLOAD)

    /**
     * 获取所有方法
     */
    fun obtainFunctionList(): List<DirectUploadFunction> {
        eval()
        return jsObject.map {
            DirectUploadFunction(rhino, jsObject, it.key as String)
        }
    }

}