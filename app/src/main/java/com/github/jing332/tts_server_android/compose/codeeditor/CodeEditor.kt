package com.github.jing332.tts_server_android.compose.codeeditor

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.github.jing332.text_searcher.ui.plugin.CodeEditorHelper
import com.github.jing332.tts_server_android.conf.CodeEditorConfig
import io.github.rosemoe.sora.widget.CodeEditor

@Composable
fun CodeEditor(modifier: Modifier, onUpdate: (CodeEditor) -> Unit) {
    val context = LocalContext.current

    AndroidView(modifier = modifier, factory = {
        CodeEditor(it).apply {
            val helper = CodeEditorHelper(context, this)
            helper.initEditor()
            tag = helper
        }
    }, update = {
//        val helper = (it.tag as CodeEditorHelper)
        onUpdate(it)
    })
}

fun CodeEditor.helper(): CodeEditorHelper {
    return tag as CodeEditorHelper
}