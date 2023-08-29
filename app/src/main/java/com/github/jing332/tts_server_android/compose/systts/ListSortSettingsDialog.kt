package com.github.jing332.tts_server_android.compose.systts

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.github.jing332.tts_server_android.R
import com.github.jing332.tts_server_android.compose.widgets.AppDialog
import com.github.jing332.tts_server_android.compose.widgets.LoadingContent
import com.github.jing332.tts_server_android.utils.clickableRipple
import com.github.jing332.tts_server_android.utils.toast
import kotlinx.coroutines.launch
import kotlin.system.measureTimeMillis

@Composable
fun ListSortSettingsDialog(
    onDismissRequest: () -> Unit,
    entries: List<String>,
    sorting: Boolean,
    onConfirm: suspend (index: Int, descending: Boolean) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var descending by remember { mutableStateOf(false) }
    var current by remember { mutableIntStateOf(0) }
    AppDialog(
        onDismissRequest = onDismissRequest,
        title = { Text(stringResource(id = R.string.sort)) },
        content = {
            LoadingContent(isLoading = sorting) {
                LazyColumn(Modifier.padding(vertical = 8.dp)) {
                    items(entries.size) { index ->
                        val selected = index == current
                        Row(
                            Modifier
                                .height(48.dp)
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                                .clip(MaterialTheme.shapes.medium)
                                .clickableRipple {
                                    current = index
                                },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                entries[index],
                                maxLines = 1,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                color = if (selected) MaterialTheme.colorScheme.primary else Color.Unspecified,
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )
                        }
                    }
                }
            }
        },
        buttons = {
            Row(
                Modifier
                    .height(48.dp)
                    .padding(end = 16.dp)
                    .clip(MaterialTheme.shapes.medium)
                    .clickableRipple(role = Role.Checkbox) { descending = !descending },
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Checkbox(checked = descending, onCheckedChange = { descending = it })
                Text(
                    stringResource(id = R.string.descending),
                    modifier = Modifier.padding(end = 8.dp)
                )
            }

            Row {
                TextButton(onClick = onDismissRequest) {
                    Text(stringResource(id = R.string.cancel))
                }

                TextButton(onClick = {
                    scope.launch {
                        val cost = measureTimeMillis { onConfirm(current, descending) }
                        context.toast(R.string.sorting_complete_msg, cost)
                    }
                }) {
                    Text(stringResource(id = R.string.start))
                }
            }
        }
    )

}