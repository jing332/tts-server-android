package com.github.jing332.tts_server_android.compose.systts.list.edit.ui.widgets

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SmartDisplay
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.github.jing332.tts_server_android.R
import com.github.jing332.tts_server_android.compose.systts.list.BasicAudioParamsDialog
import com.github.jing332.tts_server_android.conf.SysTtsConfig
import com.github.jing332.tts_server_android.model.speech.tts.PlayerParams
import com.github.jing332.tts_server_android.utils.longToast

@Composable
fun InternalPlayerDialog(
    onDismissRequest: () -> Unit, params: PlayerParams,
    onParamsChange: (PlayerParams) -> Unit
) {
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        if (!SysTtsConfig.isInAppPlayAudio)
            context.longToast(R.string.built_in_player_not_enabled)
    }

    BasicAudioParamsDialog(
        title = {
            Row(Modifier.padding(bottom = 8.dp)) {
                Icon(imageVector = Icons.Default.SmartDisplay, contentDescription = null)
                Text(stringResource(id = R.string.internal_player))
            }
        },

        onDismissRequest = onDismissRequest,
        speed = params.rate,
        onSpeedChange = { onParamsChange(params.copy(rate = it)) },

        volumeRange = 0f..1f,
        volume = params.volume,
        onVolumeChange = { onParamsChange(params.copy(volume = it)) },

        pitch = params.pitch,
        onPitchChange = { onParamsChange(params.copy(pitch = it)) },

        onReset = {
            onParamsChange(params.copy(rate = 0f, volume = 0f, pitch = 0f))
        }
    )
}