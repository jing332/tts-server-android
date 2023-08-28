package com.github.jing332.tts_server_android.compose.codeeditor

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.github.jing332.tts_server_android.R
import com.github.jing332.tts_server_android.compose.widgets.AppSelectionDialog
import com.github.jing332.tts_server_android.conf.CodeEditorConfig
import com.github.jing332.tts_server_android.constant.CodeEditorTheme

@Composable
internal fun ThemeSettingsDialog(onDismissRequest: () -> Unit) {
    AppSelectionDialog(
        onDismissRequest = onDismissRequest,
        title = { Text(stringResource(id = R.string.theme)) },
        value = CodeEditorConfig.theme.value,
        values = CodeEditorTheme.values().toList(),
        entries = CodeEditorTheme.values().map { it.id.ifBlank { stringResource(id = R.string.theme_default) } },
        onClick = { value, _ ->
            CodeEditorConfig.theme.value = value as CodeEditorTheme
            onDismissRequest()
        }
    )
}

@Preview
@Composable
fun PreviewEditorThemeDialog() {
    var show by remember { mutableStateOf(true) }
    if (show) {
        ThemeSettingsDialog {
            show = false
        }
    }
}