package com.github.jing332.tts_server_android.compose.systts.list.edit.ui.widgets

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Headset
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.github.jing332.tts_server_android.R
import com.github.jing332.tts_server_android.conf.AppConfig

@Composable
fun AuditionTextField(modifier: Modifier, onAudition: (String) -> Unit) {
    var text by remember { AppConfig.testSampleText }
    OutlinedTextField(
        modifier = modifier,
        label = {Text(stringResource(id = R.string.audition_text))},
        value = text,
        onValueChange = { text = it },
        trailingIcon = {
            IconButton(onClick = { onAudition(text) }) {
                Icon(Icons.Default.Headset, stringResource(id = R.string.audition))
            }
        }
    )
}