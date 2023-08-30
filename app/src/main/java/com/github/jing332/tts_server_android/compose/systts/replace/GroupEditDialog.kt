package com.github.jing332.tts_server_android.compose.systts.replace

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.github.jing332.tts_server_android.R
import com.github.jing332.tts_server_android.compose.widgets.AppDialog
import com.github.jing332.tts_server_android.compose.widgets.TextCheckBox
import com.github.jing332.tts_server_android.constant.ReplaceExecution
import com.github.jing332.tts_server_android.data.entities.replace.ReplaceRuleGroup
import com.github.jing332.tts_server_android.utils.clickableRipple

@Composable
internal fun GroupEditDialog(
    onDismissRequest: () -> Unit,
    group: ReplaceRuleGroup,
    onGroupChange: (ReplaceRuleGroup) -> Unit,
    onConfirm: () -> Unit
) {
    AppDialog(onDismissRequest = onDismissRequest,
        title = { Text(stringResource(id = R.string.group)) },
        content = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                OutlinedTextField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    label = { Text(stringResource(id = R.string.group_name)) },
                    value = group.name,
                    onValueChange = {
                        onGroupChange(group.copy(name = it))
                    }
                )

                TextCheckBox(text = {
                    Text(stringResource(id = R.string.replace_rule_after_execute))
                }, checked = group.onExecution == ReplaceExecution.AFTER, onCheckedChange = {
                    onGroupChange(
                        group.copy(onExecution = if (group.onExecution == ReplaceExecution.BEFORE) ReplaceExecution.AFTER else ReplaceExecution.BEFORE)
                    )
                })
            }
        }, buttons = {
            Row {
                TextButton(onClick = onDismissRequest) {
                    Text(stringResource(id = R.string.cancel))
                }
                TextButton(onClick = onConfirm) {

                    Text(stringResource(id = R.string.confirm))
                }
            }
        }
    )
}