package com.github.jing332.tts_server_android.compose.systts.plugin

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Checkbox
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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.github.jing332.tts_server_android.R
import com.github.jing332.tts_server_android.compose.systts.ConfigExportBottomSheet
import com.github.jing332.tts_server_android.compose.widgets.TextCheckBox

@Composable
internal fun PluginExportBottomSheet(
    onDismissRequest: () -> Unit,
    fileName: String,
    onGetJson: (isExportVars: Boolean) -> String,
) {
    var isExportVars by remember { mutableStateOf(false) }
    ConfigExportBottomSheet(
        fileName = fileName,
        json = onGetJson(isExportVars),
        onDismissRequest = onDismissRequest,
        content = {
            TextCheckBox(modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(vertical = 8.dp),
                text = { Text(stringResource(id = R.string.export_vars)) },
                checked = isExportVars,
                onCheckedChange = { isExportVars = !isExportVars })
        }
    )
}