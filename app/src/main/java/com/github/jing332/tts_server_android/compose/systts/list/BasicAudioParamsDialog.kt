package com.github.jing332.tts_server_android.compose.systts.list

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.github.jing332.tts_server_android.R
import com.github.jing332.tts_server_android.compose.widgets.AppDialog
import com.github.jing332.tts_server_android.compose.widgets.LabelSlider
import java.text.DecimalFormat

@Composable
fun BasicAudioParamsDialog(
    title: @Composable () -> Unit = { Text(stringResource(id = R.string.audio_params)) },
    onDismissRequest: () -> Unit,

    resetValue: Float = 0f,
    onReset: () -> Unit,

    speedRange: ClosedFloatingPointRange<Float> = 0f..3f,
    speed: Float,
    onSpeedChange: (Float) -> Unit,

    volumeRange: ClosedFloatingPointRange<Float> = 0f..3f,
    volume: Float,
    onVolumeChange: (Float) -> Unit,

    pitchRange: ClosedFloatingPointRange<Float> = 0f..3f,
    pitch: Float,
    onPitchChange: (Float) -> Unit,
) {

    fun Float.to2Dic() = DecimalFormat("#.00").format(this).toFloat()

    AppDialog(
        title = title,
        content = {
            Column {
                val str = stringResource(
                    id = R.string.label_speech_rate,
                    if (speed == 0f) stringResource(R.string.follow) else speed.toString()
                )
                LabelSlider(
                    value = speed,
                    onValueChange = { onSpeedChange(it.to2Dic()) },
                    valueRange = speedRange
                ) {
                    Text(str)
                }

                val volStr =
                    stringResource(
                        id = R.string.label_speech_volume,
                        if (volume == 0f) stringResource(R.string.follow) else volume.toString()
                    )
                LabelSlider(
                    value = volume,
                    onValueChange = { onVolumeChange(it.to2Dic()) },
                    valueRange = volumeRange
                ) { Text(volStr) }

                val pitchStr =
                    stringResource(
                        id = R.string.label_speech_pitch,
                        if (pitch == 0f) stringResource(R.string.follow) else pitch.toString()
                    )
                LabelSlider(
                    value = pitch,
                    onValueChange = { onPitchChange(it.to2Dic()) },
                    valueRange = pitchRange
                ) {
                    Text(pitchStr)
                }

            }
        },
        buttons = {
            Row {
                TextButton(
                    enabled = speed != resetValue || volume != resetValue || pitch != resetValue,
                    onClick = {
                        onReset()
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