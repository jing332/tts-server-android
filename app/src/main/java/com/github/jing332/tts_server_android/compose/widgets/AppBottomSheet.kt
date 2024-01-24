package com.github.jing332.tts_server_android.compose.widgets

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import com.dokar.sheets.BottomSheetValue
import com.dokar.sheets.m3.BottomSheet
import com.dokar.sheets.rememberBottomSheetState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun AppBottomSheet(
    value: BottomSheetValue = BottomSheetValue.Peeked,
    onDismissRequest: () -> Unit,
    content: @Composable () -> Unit
) {
    val scope = rememberCoroutineScope()
    val state = rememberBottomSheetState(confirmValueChange = {
        if (it == BottomSheetValue.Collapsed) {
            scope.launch(Dispatchers.Main) {
                delay(200)
                onDismissRequest()
            }
        }

        true
    })
    LaunchedEffect(value) {
        when (value) {
            BottomSheetValue.Collapsed -> state.collapse()
            BottomSheetValue.Expanded -> state.expand()
            BottomSheetValue.Peeked -> state.peek()
        }
    }
    BottomSheet(
        modifier = Modifier.fillMaxSize(),
        state = state,
        content = content,
    )
}