package com.github.jing332.tts_server_android.ui.systts.direct_upload

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Parcelable
import android.view.Menu
import android.view.View
import androidx.appcompat.view.menu.MenuBuilder
import androidx.appcompat.widget.PopupMenu
import androidx.lifecycle.lifecycleScope
import com.github.jing332.tts_server_android.R
import com.github.jing332.tts_server_android.help.config.DirectUploadConfig
import com.github.jing332.tts_server_android.model.rhino.core.Logger
import com.github.jing332.tts_server_android.model.rhino.directupload.DirectUploadEngine
import com.github.jing332.tts_server_android.ui.systts.base.LoggerBottomSheetFragment
import com.github.jing332.tts_server_android.ui.systts.base.BaseScriptEditorActivity
import com.github.jing332.tts_server_android.ui.view.AppDialogs.displayErrorDialog
import com.github.jing332.tts_server_android.util.readableString
import com.github.jing332.tts_server_android.util.runOnIO

class DirectUploadSettingsActivity : BaseScriptEditorActivity() {
    private val directUploadEngine by lazy { DirectUploadEngine(context = this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        editor.setText(DirectUploadConfig.code)
    }

    override fun onScriptSyncAction(name: String, body: ByteArray?) {

    }

    override fun onScriptSyncPush() {

    }

    override fun updateCode(code: String) {
        directUploadEngine.code = code
    }

    override fun clearCacheFile(): Boolean {
        return false
    }

    override fun onSaveAsFile(): String = "ttsrv-directLinkUpload.js"

    override fun onSave(): Parcelable? {
        DirectUploadConfig.code = editor.text.toString()
        finish()
        return null
    }

    override fun getLogger(): Logger = directUploadEngine.logger

    override fun onDebug() {
        val funcList = try {
            directUploadEngine.obtainFunctionList()
        } catch (t: Throwable) {
            displayErrorDialog(t)
            return
        }

        PopupMenu(this, findViewById<View>(R.id.menu_debug)).apply {
            funcList.forEachIndexed { index, v ->
                menu.add(0, index, index, v.funcName)
            }
            setOnMenuItemClickListener { menuItem ->
                val fragment = LoggerBottomSheetFragment(directUploadEngine.logger)
                fragment.show(supportFragmentManager, LoggerBottomSheetFragment.TAG)

                val index = menuItem.itemId
                val function = funcList[index]
                lifecycleScope.runOnIO {
                    kotlin.runCatching {
                        directUploadEngine.logger.d(function.funcName + "...")
                        val result = function.invoke(
                            """ {"a":1, "b":2} """.trimIndent()
                        ) ?: throw Exception("返回值为空")
                        directUploadEngine.logger.d("执行成功，返回值: $result")
                    }.onFailure {
                        directUploadEngine.logger.e(it.readableString)
                    }
                }
                true
            }
            show()
        }

    }

}