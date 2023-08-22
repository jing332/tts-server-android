package com.github.jing332.tts_server_android.compose.nav

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardDoubleArrowDown
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.github.jing332.tts_server_android.R
import com.github.jing332.tts_server_android.compose.widgets.htmlcompose.HtmlText
import com.github.jing332.tts_server_android.constant.LogLevel
import com.github.jing332.tts_server_android.ui.AppLog
import kotlinx.coroutines.launch

@Composable
fun LogScreen(
    modifier: Modifier,
    list: List<AppLog>,
    lazyListState: LazyListState = rememberLazyListState()
) {
    val scope = rememberCoroutineScope()
    Box {
        val isAtBottom by remember {
            derivedStateOf {
                val layoutInfo = lazyListState.layoutInfo
                val visibleItemsInfo = layoutInfo.visibleItemsInfo
                if (layoutInfo.totalItemsCount == 0) {
                    false
                } else {
                    val lastVisibleItem = visibleItemsInfo.last()
                    lastVisibleItem.index > list.size - 5
                }
            }
        }

        LaunchedEffect(list) {
            if (!isAtBottom)
                if (list.isNotEmpty())
                    scope.launch {
                        lazyListState.animateScrollToItem(list.size - 1)
                    }
        }

        LazyColumn(modifier, state = lazyListState) {
            itemsIndexed(list, key = { index, _ -> index }) { _, item ->
                val style = MaterialTheme.typography.bodyMedium
                HtmlText(
                    text = item.msg,
                    style = style.copy(color = MaterialTheme.colorScheme.onBackground),
                    lineHeight = style.lineHeight * 0.8f,
                )
            }
        }

        AnimatedVisibility(visible = isAtBottom) {
            FloatingActionButton(onClick = {
                scope.launch {
                    lazyListState.animateScrollToItem(list.size - 1)
                }
            }) {
                Icon(
                    Icons.Default.KeyboardDoubleArrowDown,
                    stringResource(id = R.string.move_to_bottom)
                )
            }
        }
    }
}