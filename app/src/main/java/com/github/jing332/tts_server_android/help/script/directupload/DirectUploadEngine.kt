package com.github.jing332.tts_server_android.help.script.directupload

import android.content.Context
import com.github.jing332.tts_server_android.help.config.DirectUploadConfig
import com.github.jing332.tts_server_android.help.script.core.BaseScriptEngine
import com.github.jing332.tts_server_android.help.script.core.Logger
import com.script.javascript.RhinoScriptEngine
import org.mozilla.javascript.NativeObject

class DirectUploadEngine(
    override val rhino: RhinoScriptEngine = RhinoScriptEngine(),
    override val context: Context,
    override val logger: Logger = Logger(),
    override var code: String = DirectUploadConfig.code,
    override val id: String = "DirectUpload",
) : BaseScriptEngine(rhino, context, code, id, logger) {
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