package com.github.jing332.tts_server_android.compose.nav.systts.edit.quick

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.github.jing332.tts_server_android.compose.nav.systts.edit.BasicInfoEditScreen
import com.github.jing332.tts_server_android.compose.nav.systts.edit.ui.TtsUiFactory
import com.github.jing332.tts_server_android.compose.nav.systts.edit.ui.rememberCallbackState
import com.github.jing332.tts_server_android.data.entities.systts.SystemTts

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickEditBottomSheet(
    onDismissRequest: () -> Unit,
    systts: SystemTts,
    onSysttsChange: (SystemTts) -> Unit
) {
    val state = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val ui = remember { TtsUiFactory.from(systts.tts)!! }
    val saveEvent = rememberCallbackState()
    ModalBottomSheet(
        modifier = Modifier.fillMaxSize(),
        sheetState = state, onDismissRequest = {
            saveEvent.value?.invoke()
            onDismissRequest()
        }) {
        Column(Modifier.verticalScroll(rememberScrollState())) {
            BasicInfoEditScreen(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                systts = systts,
                onSysttsChange = onSysttsChange,
                saveEvent = saveEvent
            )
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