package com.github.jing332.tts_server_android.compose.widgets

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.github.jing332.tts_server_android.R
import com.github.jing332.tts_server_android.utils.clickableRipple

@Composable
fun AppSelectionDialog(
    onDismissRequest: () -> Unit, title: @Composable () -> Unit,
    value: Any,
    values: List<Any>,
    entries: List<String>,
    isLoading: Boolean = false,

    itemContent: @Composable RowScope.(Boolean, String, Any) -> Unit = { isSelected, entry, _ ->
        Text(
            entry,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(4.dp),
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
        )
    },

    buttons: @Composable BoxScope.() -> Unit = {
        TextButton(onClick = onDismissRequest) { Text(stringResource(id = R.string.close)) }
    },

    onValueSame: (Any, Any) -> Boolean = { a, b -> a == b },
    onClick: (Any, String) -> Unit,
) {
    AppDialog(
        title = title,
        content = {
            val state = rememberLazyListState()
            LaunchedEffect(Unit) {
                val index = values.indexOfFirst { onValueSame(it, value) }
                if (index >= 0 && index < entries.size)
                    state.animateScrollToItem(index)
            }
            LoadingContent(modifier = Modifier.padding(vertical = 16.dp), isLoading = isLoading) {
                LazyColumn(state = state) {
                    itemsIndexed(entries) { i, entry ->
                        val current = values[i]
                        val isSelected = onValueSame(value, current)
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .clip(MaterialTheme.shapes.medium)
                                .background(if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Unspecified)
                                .clickableRipple { onClick(current, entry) }
                                .minimumInteractiveComponentSize(),
                        ) {
                            itemContent(isSelected, entry, value)
                        }
                    }
                }
            }
        },
        buttons = buttons, onDismissRequest = onDismissRequest,
    )
}
