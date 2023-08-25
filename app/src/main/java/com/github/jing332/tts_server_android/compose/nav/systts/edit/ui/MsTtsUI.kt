package com.github.jing332.tts_server_android.compose.nav.systts.edit.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.github.jing332.tts_server_android.R
import com.github.jing332.tts_server_android.compose.nav.systts.edit.BasicInfoEditScreen
import com.github.jing332.tts_server_android.compose.nav.systts.edit.IntSlider
import com.github.jing332.tts_server_android.compose.nav.systts.edit.ui.base.TtsTopAppBar
import com.github.jing332.tts_server_android.compose.widgets.ExposedDropTextField
import com.github.jing332.tts_server_android.constant.MsTtsApiType
import com.github.jing332.tts_server_android.data.entities.systts.SystemTts
import com.github.jing332.tts_server_android.model.speech.tts.MsTTS
import com.github.jing332.tts_server_android.model.speech.tts.MsTtsFormatManger

class MsTtsUI : TtsUI() {
    @Composable
    override fun ParamsEditScreen(
        modifier: Modifier,
        systts: SystemTts,
        onSysttsChange: (SystemTts) -> Unit
    ) {
        val context = LocalContext.current
        val tts = (systts.tts as MsTTS)
        Column {
            val formats = remember { MsTtsFormatManger.getFormatsByApiType(MsTtsApiType.EDGE) }
            ExposedDropTextField(
                label = { Text(stringResource(id = R.string.label_audio_format)) },
                key = tts.format,
                keys = formats,
                values = formats,
                onSelectedChange = { k, v ->
                    onSysttsChange(systts.copy(tts = tts.copy(format = k as String)))
                }
            )

            val speechRate = tts.prosody.rate
            val rateStr = stringResource(
                id = R.string.label_speech_rate,
                if (speechRate == MsTTS.RATE_FOLLOW_SYSTEM) stringResource(id = R.string.follow_system) else speechRate.toString()
            )
            IntSlider(
                value = speechRate.toFloat(),
                onValueChange = {
                    onSysttsChange(
                        systts.copy(tts = tts.copy(prosody = tts.prosody.copy(rate = it.toInt())))
                    )
                },
                valueRange = -100f..100f,
                label = rateStr,
            )

            val volume = tts.prosody.volume
            val volStr = stringResource(
                id = R.string.label_speech_volume, volume.toString()
            )
            IntSlider(
                value = volume.toFloat(),
                onValueChange = {
                    onSysttsChange(
                        systts.copy(tts = tts.copy(prosody = tts.prosody.copy(volume = it.toInt())))
                    )
                },
                valueRange = -50f..50f,
                label = volStr
            )

            val pitch = tts.prosody.pitch
            val pitchStr = stringResource(
                id = R.string.label_speech_pitch,
                if (pitch == MsTTS.PITCH_FOLLOW_SYSTEM) stringResource(id = R.string.follow) else pitch.toString()
            )
            IntSlider(
                value = pitch.toFloat(),
                onValueChange = {
                    onSysttsChange(
                        systts.copy(tts = tts.copy(prosody = tts.prosody.copy(pitch = it.toInt())))
                    )
                },
                valueRange = -50f..50f,
                label = pitchStr
            )

        }
    }

    @Composable
    override fun FullEditScreen(
        modifier: Modifier,
        systts: SystemTts,
        onSysttsChange: (SystemTts) -> Unit,
        onSave: () -> Unit,
        onCancel: () -> Unit
    ) {

        val saveSignal = remember { mutableStateOf<(() -> Unit)?>(null) }
        Scaffold(topBar = {
            TtsTopAppBar(
                title = { Text(text = stringResource(id = R.string.edit_builtin_tts)) },
                onBackAction = onCancel,
                onSaveAction = {
                    saveSignal.value?.invoke()
                    onSave()
                }
            )
        }) { paddingValues ->
            Column(Modifier.padding(paddingValues)) {
                BasicInfoEditScreen(
                    modifier = Modifier.fillMaxWidth(),
                    systts = systts,
                    saveEvent = saveSignal,
                    onSysttsChange = onSysttsChange
                )

                ParamsEditScreen(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    systts = systts,
                    onSysttsChange = onSysttsChange
                )
            }
        }
    }
}