package com.github.jing332.tts_server_android.compose.nav.systts.edit.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.github.jing332.tts_server_android.R
import com.github.jing332.tts_server_android.compose.nav.systts.edit.BasicInfoEditScreen
import com.github.jing332.tts_server_android.compose.nav.systts.edit.IntSlider
import com.github.jing332.tts_server_android.data.entities.systts.SystemTts
import com.github.jing332.tts_server_android.model.speech.tts.PluginTTS

class PluginTtsUI : TtsUI() {

    @Composable
    override fun ParamsEditScreen(
        modifier: Modifier,
        systts: SystemTts,
        onSysttsChange: (SystemTts) -> Unit,
    ) {
        val context = LocalContext.current
        val tts = (systts.tts as PluginTTS)
        Column(modifier) {
            val rateStr =
                if (tts.volume == 0) stringResource(id = R.string.follow_system_or_read_aloud_app)
                else stringResource(id = R.string.label_speech_rate, tts.rate.toString())
            IntSlider(
                label = rateStr,
                value = tts.rate.toFloat(),
                onValueChange = {
                    onSysttsChange(
                        systts.copy(
                            tts = tts.copy(rate = it.toInt())
                        )
                    )
                },
                valueRange = 0f..100f
            )

            val volumeStr =
                if (tts.volume == 0) stringResource(id = R.string.follow_system_or_read_aloud_app)
                else stringResource(id = R.string.label_speech_volume, tts.volume.toString())
            IntSlider(
                label = volumeStr, value = tts.volume.toFloat(), onValueChange = {
                    onSysttsChange(
                        systts.copy(
                            tts = tts.copy(volume = it.toInt())
                        )
                    )
                }, valueRange = 0f..100f
            )

            val pitchStr =
                if (tts.pitch == 0) stringResource(id = R.string.follow_system_or_read_aloud_app)
                else stringResource(id = R.string.label_speech_pitch, tts.pitch.toString())
            IntSlider(
                label = pitchStr, value = tts.pitch.toFloat(), onValueChange = {
                    onSysttsChange(
                        systts.copy(
                            tts = tts.copy(pitch = it.toInt())
                        )
                    )
                }, valueRange = 0f..100f
            )
        }
    }

    @Preview
    @Composable
    private fun PreviewParamsEditScreen() {
        var systts by remember { mutableStateOf(SystemTts(tts = PluginTTS())) }
        ParamsEditScreen(Modifier, systts = systts, onSysttsChange = { systts = it })
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun FullEditScreen(
        modifier: Modifier,
        systts: SystemTts,
        onSysttsChange: (SystemTts) -> Unit,
        onSave: () -> Unit,
        onCancel: () -> Unit,
    ) {
        Scaffold(
            modifier = modifier,
            topBar = {
                TopAppBar(
                    title = { Text(stringResource(id = R.string.edit_plugin_tts)) },
                    actions = {
                        IconButton(onClick = onSave) {
                            Icon(Icons.Default.Save, stringResource(id = R.string.save))
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onCancel) {
                            Icon(Icons.Default.ArrowBack, stringResource(id = R.string.nav_back))
                        }
                    }
                )
            }
        ) { paddingValues ->
            Column(Modifier.padding(paddingValues)) {
                BasicInfoEditScreen(
                    Modifier.fillMaxWidth(),
                    systts = systts,
                    onSysttsChange = onSysttsChange
                )
                ParamsEditScreen(
                    Modifier.fillMaxWidth(),
                    systts = systts,
                    onSysttsChange = onSysttsChange
                )
            }
        }
    }
}