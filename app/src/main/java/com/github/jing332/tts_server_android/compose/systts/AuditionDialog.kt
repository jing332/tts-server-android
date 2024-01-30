package com.github.jing332.tts_server_android.compose.systts

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.drake.net.utils.withMain
import com.github.jing332.tts_server_android.R
import com.github.jing332.tts_server_android.compose.widgets.AppDialog
import com.github.jing332.tts_server_android.compose.widgets.LoadingContent
import com.github.jing332.tts_server_android.conf.AppConfig
import com.github.jing332.tts_server_android.data.entities.systts.SystemTts
import com.github.jing332.tts_server_android.help.audio.AudioDecoder
import com.github.jing332.tts_server_android.help.audio.AudioPlayer
import com.github.jing332.tts_server_android.utils.ClipboardUtils
import com.github.jing332.tts_server_android.utils.StringUtils.sizeToReadable
import com.github.jing332.tts_server_android.utils.clickableRipple
import com.github.jing332.tts_server_android.utils.toast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun AuditionDialog(
    systts: SystemTts,
    text: String = AppConfig.testSampleText.value,
    onDismissRequest: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val audioPlayer = remember { AudioPlayer(context) }

    var error by remember { mutableStateOf("") }

    var audioInfo by remember { mutableStateOf<Triple<Int, Int, String>?>(null) }

    LaunchedEffect(systts.id) {
        scope.launch(Dispatchers.IO) {
            kotlin.runCatching {
                systts.tts.onLoad()
                systts.tts.getAudioWithSystemParams(text)
                    ?.use { ins ->
                        val audio = ins.readBytes()
                        val info = AudioDecoder.getSampleRateAndMime(audio)
                        if (audio.isEmpty()) {
                            error = context.getString(R.string.systts_log_audio_empty, "")
                            return@launch
                        }
                        audioInfo = Triple(audio.size, info.first, info.second)

                        if (systts.tts.audioFormat.isNeedDecode)
                            audioPlayer.play(audio)
                        else
                            audioPlayer.play(audio, systts.tts.audioFormat.sampleRate)
                    }
            }.onFailure {
                withMain { error = it.stackTraceToString() }

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

    AppDialog(onDismissRequest = onDismissRequest,
        title = { Text(stringResource(id = R.string.audition)) },
        content = {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                Text(
                    error.ifEmpty { text },
                    color = if (error.isEmpty()) Color.Unspecified else MaterialTheme.colorScheme.error,
//                    maxLines = if (error.isEmpty()) Int.MAX_VALUE else 1,
                    style = MaterialTheme.typography.bodySmall
                )

                val infoStr = stringResource(
                    id = R.string.systts_test_success_info,
                    audioInfo?.first?.toLong()?.sizeToReadable() ?: 0,
                    audioInfo?.second ?: 0,
                    audioInfo?.third ?: ""
                )
                if (error.isEmpty())
                    LoadingContent(
                        modifier = Modifier
                            .padding(top = 8.dp)
                            .fillMaxWidth()
                            .clickableRipple {
                                ClipboardUtils.copyText("TTS Server", infoStr)
                                context.toast(R.string.copied)
                            }, isLoading = audioInfo == null
                    ) {
                        Text(infoStr, style = MaterialTheme.typography.bodyMedium)
                    }

            }
        },
        buttons = {
            TextButton(onClick = onDismissRequest) { Text(stringResource(id = R.string.cancel)) }
        }
    )

}