package com.github.jing332.tts_server_android.compose.nav.systts

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import com.github.jing332.tts_server_android.R
import com.github.jing332.tts_server_android.compose.widgets.AppDialog
import com.github.jing332.tts_server_android.compose.widgets.LabelSlider
import com.github.jing332.tts_server_android.conf.SystemTtsConfig
import java.text.DecimalFormat

@Composable
fun GlobalAudioParamsDialog(onDismissRequest: () -> Unit) {
    var speed by remember { SystemTtsConfig.audioParamsSpeed }
    var volume by remember { SystemTtsConfig.audioParamsVolume }
    var pitch by remember { SystemTtsConfig.audioParamsPitch }
    AppDialog(
        title = { Text(stringResource(id = R.string.audio_params)) },
        content = {
            Column {
                LabelSlider(
                    value = speed,
                    onValueChange = {
                        speed = DecimalFormat("#.00").format(it).toFloat()
                    },
                    valueRange = 0.1f..3.0f
                ) {
                    val str = stringResource(id = R.string.label_speed) + speed
                    Text(str)
                }


                LabelSlider(
                    value = volume,
                    onValueChange = {
                        volume = DecimalFormat("#.00").format(it).toFloat()
                    },
                    valueRange = 0.1f..3.0f
                ) {
                    val str = stringResource(id = R.string.label_volume) + volume
                    Text(str)
                    str
                }

                LabelSlider(
                    value = pitch,
                    onValueChange = {
                        pitch = DecimalFormat("#.00").format(it).toFloat()
                    },
                    valueRange = 0.1f..3.0f
                ) {
                    val str = stringResource(id = R.string.label_pitch) + pitch
                    Text(str)
                }

            }
        },
        buttons = {
            Row {
                TextButton(
                    enabled = speed != 1f || volume != 1f || pitch != 1f,
                    onClick = {
                        speed = 1f
                        volume = 1f
                        pitch = 1f
                    }) {
                    Text(stringResource(id = R.string.reset))
                }

                TextButton(onClick = onDismissRequest) {
                    Text(stringResource(id = R.string.close))
                }
            }
        }, onDismissRequest = onDismissRequest
    )
}