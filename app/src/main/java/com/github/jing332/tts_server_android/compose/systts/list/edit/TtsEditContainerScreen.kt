package com.github.jing332.tts_server_android.compose.systts.list.edit

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.github.jing332.tts_server_android.compose.systts.list.edit.ui.LocalSaveCallBack
import com.github.jing332.tts_server_android.compose.systts.list.edit.ui.TtsUiFactory
import com.github.jing332.tts_server_android.compose.systts.list.edit.ui.rememberSaveCallBacks
import com.github.jing332.tts_server_android.data.entities.systts.SystemTts
import com.github.jing332.tts_server_android.model.speech.tts.PluginTTS
import kotlinx.coroutines.launch

@Composable
fun TtsEditContainerScreen(
    modifier: Modifier,
    systts: SystemTts,
    onSysttsChange: (SystemTts) -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit,
) {
    val ui = TtsUiFactory.from(systts.tts)!!
    val callbacks = rememberSaveCallBacks()
    val scope = rememberCoroutineScope()
    CompositionLocalProvider(LocalSaveCallBack provides callbacks) {
        ui.FullEditScreen(
            modifier,
            systts = systts,
            onSysttsChange = onSysttsChange,
            onSave = {
                scope.launch {
                    val iterator = callbacks.listIterator(callbacks.size)
                    while (iterator.hasPrevious()){
                        val element = iterator.previous()
                        if (!element.onSave()) return@launch
                    }

                    onSave()
                }
            },
            onCancel = onCancel
        )
    }
}

@Preview
@Composable
private fun PreviewContainer() {
    var systts by remember { mutableStateOf(SystemTts(tts = PluginTTS())) }
    TtsEditContainerScreen(
        modifier = Modifier.fillMaxSize(),
        systts = systts,
        onSysttsChange = { systts = it },
        onSave = {

        },
        onCancel = {

        }
    )
}