package com.github.jing332.tts_server_android.compose.systts

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.github.jing332.tts_server_android.R
import com.github.jing332.tts_server_android.compose.widgets.AppDialog
import com.github.jing332.tts_server_android.compose.widgets.LoadingContent
import com.github.jing332.tts_server_android.utils.clickableRipple

@Composable
fun ListSortSettingsDialog(
    onDismissRequest: () -> Unit,
    entries: List<String>,
    sorting: Boolean,
    onConfirm: (index: Int, descending: Boolean) -> Unit
) {
    var descending by remember { mutableStateOf(false) }
    var current by remember { mutableIntStateOf(0) }
    AppDialog(
        onDismissRequest = onDismissRequest,
        title = { Text(stringResource(id = R.string.list_soft_settings)) },
        content = {
            LoadingContent(isLoading = sorting) {
                LazyColumn {
                    items(entries.size) { index ->
                        val selected = index == current
                        Row(
                            Modifier
                                .height(48.dp)
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
                                color = if (selected) MaterialTheme.colorScheme.primary else Color.Unspecified
                            )
                        }
                    }
                }
            }
        },
        buttons = {
            Row(
                Modifier
                    .align(Alignment.CenterStart)
                    .height(48.dp)
                    .clip(MaterialTheme.shapes.medium)
                    .clickableRipple(role = Role.Checkbox) { descending = !descending }
            ) {
                Checkbox(checked = descending, onCheckedChange = { descending = it })
                Text(stringResource(id = R.string.descending))
            }

            Row {
                TextButton(onClick = onDismissRequest) {
                    Text(stringResource(id = R.string.cancel))
                }

                TextButton(onClick = { onConfirm(current, descending) }) {
                    Text(stringResource(id = R.string.confirm))
                }
            }
        }
    )

}