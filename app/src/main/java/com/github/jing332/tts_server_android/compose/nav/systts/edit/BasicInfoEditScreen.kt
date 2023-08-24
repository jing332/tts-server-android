package com.github.jing332.tts_server_android.compose.nav.systts.edit

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SmartDisplay
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.github.jing332.tts_server_android.R
import com.github.jing332.tts_server_android.compose.widgets.DenseOutlinedField
import com.github.jing332.tts_server_android.compose.widgets.ExposedDropTextField
import com.github.jing332.tts_server_android.data.appDb
import com.github.jing332.tts_server_android.data.entities.AbstractListGroup.Companion.DEFAULT_GROUP_ID
import com.github.jing332.tts_server_android.data.entities.systts.SystemTts
import com.github.jing332.tts_server_android.data.entities.systts.SystemTtsGroup

@Composable
fun BasicInfoEditScreen(
    modifier: Modifier,
    systts: SystemTts,
    onSysttsChange: (SystemTts) -> Unit,

    group: SystemTtsGroup = appDb.systemTtsDao.getGroup(systts.groupId)
        ?: SystemTtsGroup(id = DEFAULT_GROUP_ID, name = ""),
    groups: List<SystemTtsGroup> = appDb.systemTtsDao.allGroup
) {
    var showParamsDialog by remember { mutableStateOf(false) }
    if (showParamsDialog)
        AudioParamsDialog(
            onDismissRequest = { showParamsDialog = false },
            params = systts.tts.audioParams,
            onParamsChange = {
                onSysttsChange(systts.copy(tts = systts.tts.apply {
                    audioParams = it
                }))
            }
        )

    Column(modifier) {
        Row(
            Modifier
                .align(Alignment.CenterHorizontally)
                .horizontalScroll(rememberScrollState())
        ) {
            TextButton(onClick = { showParamsDialog = true }) {
                Row {
                    Icon(Icons.Default.Speed, null)
                    Text(stringResource(id = R.string.audio_params))
                }
            }

            TextButton(onClick = { }) {
                Row {
                    Icon(Icons.Default.SmartDisplay, null)
                    Text(stringResource(id = R.string.internal_player))
                }
            }
        }

        ExposedDropTextField(
            modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(id = R.string.group)) },
            key = group,
            keys = groups,
            values = groups.map { it.name },
            onKeyChange = {
                onSysttsChange(systts.copy(groupId = (it as SystemTtsGroup).id))
            }
        )
        var name by remember { mutableStateOf(systts.displayName ?: "") }
        DenseOutlinedField(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            value = name, onValueChange = {
                name = it
                onSysttsChange(systts.copy(displayName = it))
            }
        )
    }
}