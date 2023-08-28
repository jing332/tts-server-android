package com.github.jing332.tts_server_android.compose.codeeditor

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.drake.net.utils.withMain
import com.github.jing332.tts_server_android.utils.runOnUI
import kotlinx.coroutines.runBlocking
import tts_server_lib.ScriptCodeSyncServerCallback
import tts_server_lib.ScriptSyncServer

class CodeEditorViewModel : ViewModel() {
    companion object {
        const val TAG = "CodeEditorViewModel"

        const val SYNC_ACTION_DEBUG = "debug"
    }

    private var server: ScriptSyncServer? = null

    // 代码同步服务器
    fun startSyncServer(
        port: Int,
        onPush: (code: String) -> Unit,
        onPull: () -> String,
        onDebug: () -> Unit,
        onAction: (name: String, body: ByteArray?) -> Unit
    ) {
        if (server != null) return
        server = ScriptSyncServer()
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

            override fun pull(): String = runBlocking {
                return@runBlocking withMain {
                    return@withMain onPull.invoke()
                }
            }

            override fun push(code: String) {
                runOnUI {
                    onPush.invoke(code)
                }
            }
        })
        server?.start(port.toLong())
    }

    private fun closeSyncServer() {
        server?.close()
        server = null
    }

    override fun onCleared() {
        super.onCleared()
        closeSyncServer()
    }
}