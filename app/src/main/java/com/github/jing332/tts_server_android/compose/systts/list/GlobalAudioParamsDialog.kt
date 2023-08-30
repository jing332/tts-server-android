package com.github.jing332.tts_server_android.compose.systts.list

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import com.github.jing332.tts_server_android.R
import com.github.jing332.tts_server_android.conf.SystemTtsConfig

@Composable
fun GlobalAudioParamsDialog(onDismissRequest: () -> Unit) {
    var speed by remember { SystemTtsConfig.audioParamsSpeed }
    var volume by remember { SystemTtsConfig.audioParamsVolume }
    var pitch by remember { SystemTtsConfig.audioParamsPitch }
    BasicAudioParamsDialog(
        title = { Text(stringResource(id = R.string.audio_params_settings)) },
        onDismissRequest = onDismissRequest,

        speedRange = 0.1f..3f,
        speed = speed,
        onSpeedChange = { speed = it },

        volumeRange = 0.1f..3f,
        volume = volume,
        onVolumeChange = { volume = it },

        pitchRange = 0.1f..3f,
        pitch = pitch,
        onPitchChange = { pitch = it },

        onReset = {
            speed = 1f
            volume = 1f
            pitch = 1f
        }
    )
}