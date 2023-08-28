package com.github.jing332.tts_server_android.compose.systts.list

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.github.jing332.tts_server_android.R
import com.github.jing332.tts_server_android.compose.widgets.AppDialog
import com.github.jing332.tts_server_android.compose.widgets.LabelSlider
import com.github.jing332.tts_server_android.data.entities.systts.AudioParams
import java.text.DecimalFormat

@Composable
fun GroupAudioParamsDialog(
    onDismissRequest: () -> Unit,
    params: AudioParams,
    onConfirm: (AudioParams) -> Unit
) {
    var speed by remember { mutableFloatStateOf(params.speed) }
    var volume by remember { mutableFloatStateOf(params.volume) }
    var pitch by remember { mutableFloatStateOf(params.pitch) }

    AppDialog(
        title = { Text(stringResource(id = R.string.audio_params)) },
        content = {
            Column {
                val str = stringResource(
                    id = R.string.label_speech_rate,
                    if (speed == AudioParams.FOLLOW_GLOBAL_VALUE) stringResource(R.string.follow) else speed.toString()
                )
                LabelSlider(
                    value = speed,
                    onValueChange = {
                        speed = DecimalFormat("#.00").format(it).toFloat()
                    },
                    valueRange = 0.0f..3.0f
                ) {
                    Text(str)
                }

                val volStr =
                    stringResource(
                        id = R.string.label_speech_volume,
                        if (volume == AudioParams.FOLLOW_GLOBAL_VALUE) stringResource(R.string.follow) else volume.toString()
                    )
                LabelSlider(
                    value = volume,
                    onValueChange = {
                        volume = DecimalFormat("#.00").format(it).toFloat()

                    },
                    valueRange = 0.0f..3.0f
                ) { Text(volStr) }

                val pitchStr =
                    stringResource(
                        id = R.string.label_speech_pitch,
                        if (pitch == AudioParams.FOLLOW_GLOBAL_VALUE) stringResource(R.string.follow) else pitch.toString()
                    )
                LabelSlider(
                    value = pitch,
                    onValueChange = {
                        pitch = DecimalFormat("#.00").format(it).toFloat()
                    },
                    valueRange = 0.0f..3.0f
                ) {
                    Text(pitchStr)
                }

            }
        },
        buttons = {
            Row {
                TextButton(
                    enabled = speed != AudioParams.FOLLOW_GLOBAL_VALUE ||
                            volume != AudioParams.FOLLOW_GLOBAL_VALUE ||
                            pitch != AudioParams.FOLLOW_GLOBAL_VALUE,
                    onClick = {
                        speed = AudioParams.FOLLOW_GLOBAL_VALUE
                        volume = AudioParams.FOLLOW_GLOBAL_VALUE
                        pitch = AudioParams.FOLLOW_GLOBAL_VALUE
                    }) {
                    Text(stringResource(id = R.string.reset))
                }

                Spacer(modifier = Modifier.width(24.dp))

                TextButton(
                    onClick = onDismissRequest
                ) {
                    Text(stringResource(id = R.string.cancel))
                }
                TextButton(onClick = { onConfirm(AudioParams(speed, volume, pitch)) }) {
                    Text(stringResource(id = R.string.confirm))
                }
            }
        }, onDismissRequest = onDismissRequest)
}