package com.github.jing332.tts_server_android.compose.systts.list

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.drake.net.utils.withMain
import com.github.jing332.tts_server_android.R
import com.github.jing332.tts_server_android.data.entities.systts.SystemTts
import com.github.jing332.tts_server_android.help.audio.AudioPlayer
import com.github.jing332.tts_server_android.help.config.AppConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun AuditionDialog(systts: SystemTts, onDismissRequest: () -> Unit) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val audioPlayer = remember { AudioPlayer(context) }

    var error by remember { mutableStateOf("") }

    LaunchedEffect(systts.id) {
        scope.launch(Dispatchers.IO) {
            kotlin.runCatching {
                systts.tts.onLoad()
                val audio =
                    systts.tts.getAudioWithSystemParams(AppConfig.testSampleText) ?: return@launch
                if (systts.tts.audioFormat.isNeedDecode)
                    audioPlayer.play(audio)
                else
                    audioPlayer.play(audio, systts.tts.audioFormat.sampleRate)
            }.onFailure {
                withMain { error = it.toString() }
                return@launch
            }
            withMain { onDismissRequest() }
        }
    }

    DisposableEffect(systts.id) {
        onDispose {
            audioPlayer.release()
            systts.tts.onDestroy()
        }
    }

    AlertDialog(onDismissRequest = onDismissRequest,
        title = { Text(stringResource(id = R.string.audition)) },
        text = {
            Text(error.ifEmpty { AppConfig.testSampleText },
                color = if (error.isEmpty()) Color.Unspecified else MaterialTheme.colorScheme.error)
        },
        confirmButton = {
            TextButton(onClick = {
                onDismissRequest()
            }) {
                Text(stringResource(id = R.string.cancel))
            }
        }
    )

}