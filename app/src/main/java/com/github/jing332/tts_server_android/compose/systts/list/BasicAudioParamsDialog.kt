package com.github.jing332.tts_server_android.compose.systts.list

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.github.jing332.tts_server_android.R
import com.github.jing332.tts_server_android.compose.widgets.AppDialog
import com.github.jing332.tts_server_android.compose.widgets.LabelSlider
import com.github.jing332.tts_server_android.utils.toScale

@Composable
fun BasicAudioParamsDialog(
    title: @Composable () -> Unit = { Text(stringResource(id = R.string.audio_params)) },
    onDismissRequest: () -> Unit,

    resetValue: Float = 0f,
    onReset: () -> Unit,

    defaultSpeed: Float = 0f,
    speedRange: ClosedFloatingPointRange<Float> = 0f..3f,
    speed: Float,
    onSpeedChange: (Float) -> Unit,

    defaultVolume : Float = 0f,
    volumeRange: ClosedFloatingPointRange<Float> = 0f..3f,
    volume: Float,
    onVolumeChange: (Float) -> Unit,

    defaultPitch: Float = 0f,
    pitchRange: ClosedFloatingPointRange<Float> = 0f..3f,
    pitch: Float,
    onPitchChange: (Float) -> Unit,

    buttons: @Composable (BoxScope.() -> Unit) = {
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
    },
) {
    AppDialog(
        title = title,
        content = {
            Column {
                val str = stringResource(
                    id = R.string.label_speech_rate,
                    if (speed == defaultSpeed) stringResource(R.string.follow) else speed.toString()
                )
                LabelSlider(
                    value = speed,
                    onValueChange = { onSpeedChange(it.toScale(2)) },
                    valueRange = speedRange,
                    text = str
                )

                val volStr =
                    stringResource(
                        id = R.string.label_speech_volume,
                        if (volume == defaultVolume) stringResource(R.string.follow) else volume.toString()
                    )
                LabelSlider(
                    value = volume,
                    onValueChange = { onVolumeChange(it.toScale(2)) },
                    valueRange = volumeRange,
                    text = volStr
                )

                val pitchStr =
                    stringResource(
                        id = R.string.label_speech_pitch,
                        if (pitch == defaultPitch) stringResource(R.string.follow) else pitch.toString()
                    )
                LabelSlider(
                    value = pitch,
                    onValueChange = { onPitchChange(it.toScale(2)) },
                    valueRange = pitchRange,
                    text = pitchStr
                )

            }
        },
        buttons = buttons, onDismissRequest = onDismissRequest
    )
}