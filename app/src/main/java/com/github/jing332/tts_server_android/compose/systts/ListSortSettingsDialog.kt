package com.github.jing332.tts_server_android.compose.systts

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.github.jing332.tts_server_android.R
import com.github.jing332.tts_server_android.compose.widgets.AppDialog
import com.github.jing332.tts_server_android.compose.widgets.LoadingContent
import com.github.jing332.tts_server_android.compose.widgets.TextCheckBox
import com.github.jing332.tts_server_android.utils.toast
import kotlinx.coroutines.launch
import kotlin.system.measureTimeMillis

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ListSortSettingsDialog(
    name: String,
    onDismissRequest: () -> Unit,
    index: Int,
    onIndexChange: (Int) -> Unit,
    entries: List<String>,
    onConfirm: suspend (index: Int, descending: Boolean) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var descending by remember { mutableStateOf(false) }
    var sorting by remember { mutableStateOf(false) }
    AppDialog(
        onDismissRequest = onDismissRequest,
        title = { Text(stringResource(id = R.string.sort)) },
        content = {
            LoadingContent(Modifier.padding(vertical = 8.dp), isLoading = sorting) {
                Column(Modifier.fillMaxWidth()) {
                    Text(
                        name,
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(vertical = 4.dp),
                        style = MaterialTheme.typography.titleMedium
                    )
                    FlowRow(Modifier.align(Alignment.CenterHorizontally)) {
                        entries.forEachIndexed { i, s ->
                            val selected = i == index
                            FilterChip(
                                selected,
                                modifier = Modifier.padding(horizontal = 4.dp),
                                onClick = { onIndexChange(i) },
                                label = {
                                    Text(
                                        s,
                                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            )
                        }
                    }
                }
            }
        },
        buttons = {
            Row(Modifier.fillMaxWidth()) {
                TextCheckBox(
                    text = {
                        Text(stringResource(id = R.string.descending), modifier = Modifier.padding(end = 8.dp))
                    }, checked = descending, onCheckedChange = { descending = it }
                )

                Row(Modifier.weight(1f)) {
                    Spacer(modifier = Modifier.weight(1f))
                    TextButton(onClick = onDismissRequest) {
                        Text(stringResource(id = R.string.cancel))
                    }

                    TextButton(onClick = {
                        scope.launch {
                            sorting = true
                            val cost = measureTimeMillis { onConfirm(index, descending) }
                            context.toast(R.string.sorting_complete_msg, cost)
                            sorting = false
                        }
                    }) {
                        Text(stringResource(id = R.string.start))
                    }
                }
            }
        }
    )

}