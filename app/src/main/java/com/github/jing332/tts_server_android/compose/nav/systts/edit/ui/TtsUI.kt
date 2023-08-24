package com.github.jing332.tts_server_android.compose.nav.systts.edit.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.github.jing332.tts_server_android.data.entities.systts.SystemTts

open class TtsUI {
    @Composable
    open fun ParamsEditScreen(
        modifier: Modifier,
        systts: SystemTts,
        onSysttsChange: (SystemTts) -> Unit
    ) {
    }

    @Composable
    open fun FullEditScreen(
        modifier: Modifier,
        systts: SystemTts,
        onSysttsChange: (SystemTts) -> Unit,
        onSave: () -> Unit
    ) {
    }

    @Composable
    open fun FullEditScreen(
        modifier: Modifier,
        systts: SystemTts,
        onSysttsChange: (SystemTts) -> Unit,
        onSave: () -> Unit,
        onCancel: () -> Unit
    ) {
    }
}