package com.github.jing332.tts_server_android.compose.systts.list

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.github.jing332.tts_server_android.R
import com.github.jing332.tts_server_android.compose.widgets.AppDialog
import com.github.jing332.tts_server_android.conf.SystemTtsConfig
import com.github.jing332.tts_server_android.utils.clickableRipple

@Composable
fun BgmSettingsDialog(onDismissRequest: () -> Unit) {
    AppDialog(
        onDismissRequest = onDismissRequest,
        title = { Text(stringResource(id = R.string.bgm_settings)) },
        content = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                var shuffle by remember { SystemTtsConfig.isBgmShuffleEnabled }
                Row(
                    Modifier
                        .height(48.dp)
                        .clip(MaterialTheme.shapes.medium)
                        .clickableRipple { shuffle = !shuffle }
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Checkbox(checked = shuffle, onCheckedChange = null)
                    Text(stringResource(id = R.string.shuffle))
                }

                var volume by remember { SystemTtsConfig.bgmVolume }
                val volumeStr = stringResource(id = R.string.label_speech_volume, (volume * 1000f).toInt().toString())
                IntSlider(
                    label = volumeStr,
                    value = volume * 1000f,
                    onValueChange = { volume = it / 1000f },
                    valueRange = 1f..1000f
                )

                Text(
                    stringResource(id = R.string.bgm_settings_tip),
                    modifier = Modifier.padding(4.dp)
                )
            }
        }
    )
}