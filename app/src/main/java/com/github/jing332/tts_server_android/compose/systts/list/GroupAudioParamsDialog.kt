package com.github.jing332.tts_server_android.compose.systts.list

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.github.jing332.tts_server_android.R
import com.github.jing332.tts_server_android.data.entities.systts.AudioParams

@Composable
fun GroupAudioParamsDialog(
    onDismissRequest: () -> Unit,
    params: AudioParams,
    onConfirm: (AudioParams) -> Unit
) {
    var speed by remember { mutableFloatStateOf(params.speed) }
    var volume by remember { mutableFloatStateOf(params.volume) }
    var pitch by remember { mutableFloatStateOf(params.pitch) }

    BasicAudioParamsDialog(
        onDismissRequest = onDismissRequest,
        onReset = {
            speed = AudioParams.FOLLOW_GLOBAL_VALUE
            volume = AudioParams.FOLLOW_GLOBAL_VALUE
            pitch = AudioParams.FOLLOW_GLOBAL_VALUE
        },
        speed = speed,
        onSpeedChange = { speed = it },
        volume = volume,
        onVolumeChange = { volume = it },
        pitch = pitch,
        onPitchChange = { pitch = it },
        buttons = {
            Row {
                TextButton(
                    enabled = speed != AudioParams.FOLLOW_GLOBAL_VALUE || volume != AudioParams.FOLLOW_GLOBAL_VALUE || pitch != AudioParams.FOLLOW_GLOBAL_VALUE,
                    onClick = {
                        speed = AudioParams.FOLLOW_GLOBAL_VALUE
                        volume = AudioParams.FOLLOW_GLOBAL_VALUE
                        pitch = AudioParams.FOLLOW_GLOBAL_VALUE
                    }) {
                    Text(stringResource(id = R.string.reset))
                }
                Spacer(modifier = Modifier.weight(1f))
                Row {
                    TextButton(onClick = onDismissRequest) {
                        Text(stringResource(id = R.string.cancel))
                    }
                    TextButton(onClick = { onConfirm(AudioParams(speed, volume, pitch)) }) {
                        Text(stringResource(id = R.string.confirm))
                    }
                }
            }
        }
    )
}