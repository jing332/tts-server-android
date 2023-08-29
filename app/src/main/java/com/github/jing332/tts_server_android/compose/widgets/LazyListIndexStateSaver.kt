package com.github.jing332.tts_server_android.compose.widgets

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue

@Composable
fun LazyListIndexStateSaver(
    models: Any?,
    listState: LazyListState,

    onIndexUpdate: suspend (Int, Int) -> Unit = { index, offset ->
        listState.scrollToItem(index, offset)
    },
) {
    var index by rememberSaveable { mutableIntStateOf(0) }
    var offset by rememberSaveable { mutableIntStateOf(0) }

    LaunchedEffect(models) {
        if (models != null && listState.firstVisibleItemIndex <= 0 && listState.firstVisibleItemScrollOffset <= 0) {
            onIndexUpdate(index, offset)
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            index = listState.firstVisibleItemIndex
            offset = listState.firstVisibleItemScrollOffset
        }
    }
}