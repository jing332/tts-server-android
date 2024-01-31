package com.github.jing332.tts_server_android.compose.systts.replace.edit

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.github.jing332.tts_server_android.R
import com.github.jing332.tts_server_android.compose.theme.AppTheme
import com.github.jing332.tts_server_android.compose.widgets.AppBottomSheet
import com.github.jing332.tts_server_android.compose.widgets.HtmlText
import com.github.jing332.tts_server_android.data.appDb
import com.github.jing332.tts_server_android.data.entities.systts.SystemTts
import com.github.jing332.tts_server_android.utils.clickableRipple

@Preview
@Composable
private fun PreviewTtsConfigSelectDialog() {
    AppTheme {
        var show by remember { mutableStateOf(true) }
        if (show)
            SysttsSelectBottomSheet(onDismissRequest = { show = false }, {})
    }
}

@Composable
internal fun SysttsSelectBottomSheet(onDismissRequest: () -> Unit, onClick: (SystemTts) -> Unit) {
    val items = remember { appDb.systemTtsDao.allEnabledTts }
    AppBottomSheet(onDismissRequest = onDismissRequest) {
        Column(Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = stringResource(id = R.string.choice_item, ""), style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(8.dp))
            LazyColumn(Modifier.weight(1f)) {
                itemsIndexed(items) { _, systts ->
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .clip(MaterialTheme.shapes.small)
                            .clickableRipple {
                                onClick(systts)
                            }
                    ) {
                        Text(
                            text = systts.displayName ?: "",
                            style = MaterialTheme.typography.titleMedium
                        )
                        HtmlText(
                            text = systts.tts.getDescription(),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }
}
