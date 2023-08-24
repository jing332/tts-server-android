package com.github.jing332.tts_server_android.compose.nav.systts.edit.quick

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.github.jing332.tts_server_android.compose.nav.systts.edit.BasicInfoEditScreen
import com.github.jing332.tts_server_android.compose.nav.systts.edit.ui.TtsUiFactory
import com.github.jing332.tts_server_android.data.entities.systts.SystemTts

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickEditBottomSheet(
    onDismissRequest: () -> Unit,
    systts: SystemTts,
    onUpdate: (SystemTts) -> Unit
) {
    val ui = remember { TtsUiFactory.from(systts.tts) }
    ModalBottomSheet(onDismissRequest = onDismissRequest) {
//        BasicInfoEditScreen(systts = systts, onSysttsChange = onUpdate)
//        Spacer(modifier = Modifier.height(12.dp))

//        ui?.ParamsEditScreen(systts = systts, onUpdate = onUpdate)
    }
}