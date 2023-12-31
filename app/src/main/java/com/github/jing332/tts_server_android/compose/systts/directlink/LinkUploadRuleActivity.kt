package com.github.jing332.tts_server_android.compose.systts.directlink

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import com.github.jing332.tts_server_android.R
import com.github.jing332.tts_server_android.compose.codeeditor.CodeEditorScreen
import com.github.jing332.tts_server_android.compose.codeeditor.LoggerBottomSheet
import com.github.jing332.tts_server_android.compose.theme.AppTheme
import com.github.jing332.tts_server_android.conf.DirectUploadConfig
import com.github.jing332.tts_server_android.model.rhino.core.Logger
import com.github.jing332.tts_server_android.model.rhino.direct_link_upload.DirectUploadEngine
import com.github.jing332.tts_server_android.model.rhino.direct_link_upload.DirectUploadFunction
import com.github.jing332.tts_server_android.ui.view.AppDialogs.displayErrorDialog
import io.github.rosemoe.sora.widget.CodeEditor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LinkUploadRuleActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            AppTheme {
                LinkUploadRuleScreen()
            }
        }
    }


    @Composable
    private fun LinkUploadRuleScreen() {
        var editor by remember { mutableStateOf<CodeEditor?>(null) }
        val logger by remember { mutableStateOf(Logger()) }
        var targets by remember { mutableStateOf<List<DirectUploadFunction>?>(null) }
        val scope = rememberCoroutineScope()

        LaunchedEffect(editor) {
            editor?.setText(DirectUploadConfig.code.value)
        }

        var showDebugLogger by remember { mutableStateOf("") }
        if (showDebugLogger.isNotEmpty())
            LoggerBottomSheet(
                logger = logger,
                onDismissRequest = { showDebugLogger = "" }) {
                scope.launch(Dispatchers.IO) {
                    runCatching {
                        val engine = DirectUploadEngine(
                            context = this@LinkUploadRuleActivity,
                            logger = logger,
                            code = editor!!.text.toString()
                        )
                        val func =
                            engine.obtainFunctionList().find { it.funcName == showDebugLogger }

                        val url = func?.invoke(""" {"test":"test"} """)
                        logger.i("url: $url")
                    }.onFailure {
                        this@LinkUploadRuleActivity.displayErrorDialog(it)
                    }
                }
            }

        fun obtainFunctionList(): List<DirectUploadFunction> {
            val engine = DirectUploadEngine(
                context = this,
                logger = logger,
                code = editor!!.text.toString()
            )
            return engine.obtainFunctionList()
        }


        CodeEditorScreen(
            title = { Text(stringResource(id = R.string.direct_link_settings)) },
            onBack = { finishAfterTransition() },
            onSave = {
                runCatching {
                    obtainFunctionList()
                    DirectUploadConfig.code.value = editor!!.text.toString()

                    finishAfterTransition()
                }.onFailure {
                    this.displayErrorDialog(it)
                }
            },
            onUpdate = { editor = it },
            onDebug = {
                kotlin.runCatching {
                    targets = obtainFunctionList()
                }.onFailure {
                    this.displayErrorDialog(it)
                }
            },
            debugIconContent = {
                DropdownMenu(expanded = targets != null, onDismissRequest = { targets = null }) {
                    targets?.forEach {
                        DropdownMenuItem(text = { Text(it.funcName) }, onClick = {
                            targets = null
                            showDebugLogger = it.funcName
                        })
                    }
                }
            },
            onSaveFile = {
                "ttsrv-directLink.js" to editor!!.text.toString().toByteArray()
            }
        )
    }

}