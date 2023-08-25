package com.github.jing332.tts_server_android.compose.nav.systts.edit

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.github.jing332.tts_server_android.R
import com.github.jing332.tts_server_android.compose.widgets.AppDialog
import com.github.jing332.tts_server_android.compose.widgets.LabelSlider
import com.github.jing332.tts_server_android.data.entities.systts.AudioParams
import java.text.DecimalFormat

@Composable
fun AudioParamsDialog(
    onDismissRequest: () -> Unit,
    params: AudioParams,
    onParamsChange: (AudioParams) -> Unit
) {
    AppDialog(
        title = { Text(stringResource(id = R.string.audio_params)) },
        content = {
            Column {
                val str = stringResource(
                    id = R.string.label_speech_rate,
                    if (params.speed == AudioParams.FOLLOW_GLOBAL_VALUE) stringResource(R.string.follow) else params.speed.toString()
                )
                LabelSlider(
                    value = params.speed,
                    onValueChange = {
                        onParamsChange(
                            params.copy(
                                speed = DecimalFormat("#.00").format(it).toFloat()
                            )
                        )
                    },
                    valueRange = 0.0f..3.0f
                ) {
                    Text(str)
                }

                val volStr =
                    stringResource(
                        id = R.string.label_speech_volume,
                        if (params.volume == AudioParams.FOLLOW_GLOBAL_VALUE) stringResource(R.string.follow) else params.volume.toString()
                    )
                LabelSlider(
                    value = params.volume,
                    onValueChange = {
                        onParamsChange(
                            params.copy(
                                volume = DecimalFormat("#.00").format(it).toFloat()
                            )
                        )
                    },
                    valueRange = 0.0f..3.0f
                ) { Text(volStr) }

                val pitchStr =
                    stringResource(
                        id = R.string.label_speech_pitch,
                        if (params.pitch == AudioParams.FOLLOW_GLOBAL_VALUE) stringResource(R.string.follow) else params.pitch.toString()
                    )
                LabelSlider(
                    value = params.pitch,
                    onValueChange = {
                        onParamsChange(
                            params.copy(pitch = DecimalFormat("#.00").format(it).toFloat())
                        )
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
                    enabled = !params.isDefaultValue,
                    onClick = {
                        onParamsChange(AudioParams())
                    }) {
                    Text(stringResource(id = R.string.reset))
                }

                TextButton(onClick = onDismissRequest) {
                    Text(stringResource(id = R.string.close))
                }
            }
        }) {
        onDismissRequest()
    }
}