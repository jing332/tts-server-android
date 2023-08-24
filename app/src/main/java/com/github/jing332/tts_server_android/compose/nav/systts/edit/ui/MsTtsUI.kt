package com.github.jing332.tts_server_android.compose.nav.systts.edit.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.github.jing332.tts_server_android.R
import com.github.jing332.tts_server_android.compose.widgets.LabelSlider
import com.github.jing332.tts_server_android.data.entities.systts.SystemTts
import com.github.jing332.tts_server_android.model.speech.tts.MsTTS

class MsTtsUI : TtsUI() {
    @Composable
    override fun FullEditScreen(
        modifier: Modifier,
        systts: SystemTts,
        onSysttsChange: (SystemTts) -> Unit,
        onSave: () -> Unit
    ) {
//        super.FullEditScreen(systts, onUpdate)
    }

    @Composable
    override fun ParamsEditScreen(
        modifier: Modifier,
        systts: SystemTts,
        onUpdate: (SystemTts) -> Unit
    ) {
        val context = LocalContext.current
        val tts = (systts.tts as MsTTS)
        Column {
            val speechRate = tts.prosody.rate
            val rateStr = stringResource(id = R.string.label_speech_rate, speechRate.toString())
            LabelSlider(
                value = speechRate.toFloat(),
                onValueChange = {
//                    println(it.toString() + " -> " + inverseMapValue(it.toDouble()))
                    onUpdate(
                        systts.copy(
                            tts = tts.copy(
                                prosody = tts.prosody.copy(
                                    rate = it.toInt()
                                )
                            )
                        )
                    )
                },
                valueRange = -100f..100f,
                text = { Text(rateStr) },
                a11yDescription = rateStr,
                buttonSteps = 1f,
                buttonLongSteps = 10f
            )
        }
    }
}