package com.github.jing332.tts_server_android.ui.systts.directupload

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.view.menu.MenuBuilder
import androidx.appcompat.widget.PopupMenu
import androidx.lifecycle.lifecycleScope
import com.github.jing332.tts_server_android.R
import com.github.jing332.tts_server_android.constant.CodeEditorTheme
import com.github.jing332.tts_server_android.databinding.SysttsDirectUploadSettingsActivityBinding
import com.github.jing332.tts_server_android.help.config.AppConfig
import com.github.jing332.tts_server_android.help.config.DirectUploadConfig
import com.github.jing332.tts_server_android.help.plugin.directupload.DirectUploadEngine
import com.github.jing332.tts_server_android.ui.base.BackActivity
import com.github.jing332.tts_server_android.ui.view.AppDialogs
import com.github.jing332.tts_server_android.ui.view.CodeEditorHelper
import com.github.jing332.tts_server_android.util.readableString
import com.github.jing332.tts_server_android.util.runOnIO

class DirectUploadSettingsActivity : BackActivity() {
    private val binding by lazy { SysttsDirectUploadSettingsActivityBinding.inflate(layoutInflater) }

    private val directUploadEngine by lazy { DirectUploadEngine(context = this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.editor.setText(DirectUploadConfig.code)
        binding.editor.isWordwrap = AppConfig.isCodeEditorWordWrapEnabled
        val editorHelper = CodeEditorHelper(this, binding.editor)
        editorHelper.initEditor()
        editorHelper.setTheme(AppConfig.codeEditorTheme)
    }

    @SuppressLint("RestrictedApi")
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.direct_upload_settings, menu)
        if (menu is MenuBuilder) menu.setOptionalIconsVisible(true)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_save -> {
                DirectUploadConfig.code = binding.editor.text.toString()
                finish()
                true
            }

            R.id.menu_debug -> {
                debug()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun debug() {
        directUploadEngine.code = binding.editor.text.toString()

        val funcList = try {
            directUploadEngine.obtainFunctionList()
        } catch (t: Throwable) {
            AppDialogs.displayErrorDialog(this, t.stackTraceToString())
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