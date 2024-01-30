package com.github.jing332.tts_server_android.compose.systts.replace.edit

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.github.jing332.tts_server_android.R
import com.github.jing332.tts_server_android.compose.theme.AppTheme
import com.github.jing332.tts_server_android.compose.widgets.AppDialog
import com.github.jing332.tts_server_android.data.appDb
import com.github.jing332.tts_server_android.data.entities.systts.SystemTts
import com.github.jing332.tts_server_android.utils.clickableRipple

@Preview
@Composable
private fun PreviewTtsConfigSelectDialog() {
    AppTheme {
        var show by remember { mutableStateOf(true) }
        if (show)
            TtsConfigSelectDialog(onDismissRequest = { show = false }, {})
    }
}

@Composable
internal fun TtsConfigSelectDialog(onDismissRequest: () -> Unit, onClick: (SystemTts) -> Unit) {
    val items = remember {
        appDb.systemTtsDao.allEnabledTts
    }
    AppDialog(
        onDismissRequest = onDismissRequest,
        title = { Text(text = stringResource(id = R.string.choice_item, "")) },
        content = {
            LazyColumn {
                itemsIndexed(items) { index, systts ->
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .clip(MaterialTheme.shapes.medium)
                            .clickableRipple {
                                onClick(systts)
                            }
                    ) {
                        Text(
                            text = systts.displayName ?: "",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = systts.tts.getDescription(),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    )
}