package com.github.jing332.tts_server_android.ui.systts.base

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.github.jing332.tts_server_android.util.runOnUI
import tts_server_lib.ScriptCodeSyncServerCallback
import tts_server_lib.ScriptSyncServer

class BaseScriptEditorViewModel : ViewModel() {
    companion object {
        const val TAG = "EditorViewModel"

        const val SYNC_ACTION_DEBUG = "debug"
    }

    private val _pushCodeLiveData: MutableLiveData<String> = MutableLiveData()
    val pushCodeLiveData: LiveData<String> = _pushCodeLiveData

    private var server: ScriptSyncServer? = null

    // 代码同步服务器
    fun startSyncServer(
        port: Int,
        onPush: (code: String) -> Unit,
        onPull: () -> String,
        onDebug: () -> Unit,
        onAction: (name: String, body: ByteArray?) -> Unit
    ) {
        server = tts_server_lib.ScriptSyncServer()
        server?.init(object : ScriptCodeSyncServerCallback {
            override fun log(level: Int, msg: String?) {
                Log.i(TAG, "$level $msg")
            }

            override fun action(name: String, body: ByteArray?) {
                runOnUI {
                    if (name == SYNC_ACTION_DEBUG) {
                        onDebug.invoke()
                    } else
                        onAction.invoke(name, body)
                }
            }

            override fun pull(): String {
                return onPull.invoke()
            }

            override fun push(code: String) {
                runOnUI {
                    onPush.invoke(code)
                }
            }
        })
        server?.start(port.toLong())
    }

    override fun onCleared() {
        super.onCleared()

        server?.close()
        server = null
    }
}