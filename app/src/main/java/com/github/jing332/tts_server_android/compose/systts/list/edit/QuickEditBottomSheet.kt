package com.github.jing332.tts_server_android.compose.systts.list.edit

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.github.jing332.tts_server_android.compose.systts.list.edit.ui.LocalSaveCallBack
import com.github.jing332.tts_server_android.compose.systts.list.edit.ui.TtsUiFactory
import com.github.jing332.tts_server_android.compose.systts.list.edit.ui.rememberSaveCallBacks
import com.github.jing332.tts_server_android.compose.widgets.AppBottomSheet
import com.github.jing332.tts_server_android.data.entities.systts.SystemTts
import com.github.jing332.tts_server_android.model.speech.tts.BgmTTS
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun QuickEditBottomSheet(
    onDismissRequest: () -> Unit,
    systts: SystemTts,
    onSysttsChange: (SystemTts) -> Unit
) {
    val ui = remember { TtsUiFactory.from(systts.tts)!! }
    val callbacks = rememberSaveCallBacks()
    val scope = rememberCoroutineScope()

    AppBottomSheet(
        onDismissRequest = {
            scope.launch(Dispatchers.Main) {
                for (callback in callbacks) {
                    if (!callback.onSave()) return@launch
                }
                onDismissRequest()
            }
        },
    ) {
        Column(
            Modifier
                .padding(top = 12.dp)
                .padding(horizontal = 4.dp)
                .verticalScroll(rememberScrollState())
        ) {
            CompositionLocalProvider(LocalSaveCallBack provides callbacks) {
                BasicInfoEditScreen(
                    modifier = Modifier.fillMaxWidth(),
                    systts = systts,
                    onSysttsChange = onSysttsChange,
                    showSpeechTarget = systts.tts !is BgmTTS
                )
            }
            ui.ParamsEditScreen(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                systts = systts,
                onSysttsChange = onSysttsChange
            )
            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}