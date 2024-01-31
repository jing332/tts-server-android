package com.github.jing332.tts_server_android.compose.systts.list.edit.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.github.jing332.tts_server_android.R
import com.github.jing332.tts_server_android.compose.systts.AuditionDialog
import com.github.jing332.tts_server_android.compose.systts.list.IntSlider
import com.github.jing332.tts_server_android.compose.systts.list.edit.BasicInfoEditScreen
import com.github.jing332.tts_server_android.compose.systts.list.edit.ui.widgets.AuditionTextField
import com.github.jing332.tts_server_android.compose.systts.list.edit.ui.widgets.TtsTopAppBar
import com.github.jing332.tts_server_android.compose.widgets.AppSpinner
import com.github.jing332.tts_server_android.compose.widgets.LoadingContent
import com.github.jing332.tts_server_android.constant.MsTtsApiType
import com.github.jing332.tts_server_android.data.entities.systts.SystemTts
import com.github.jing332.tts_server_android.model.GeneralVoiceData
import com.github.jing332.tts_server_android.model.speech.tts.MsTTS
import com.github.jing332.tts_server_android.model.speech.tts.MsTtsFormatManger
import com.github.jing332.tts_server_android.ui.view.AppDialogs.displayErrorDialog

class MsTtsUI : TtsUI() {
    @Composable
    override fun ParamsEditScreen(
        modifier: Modifier,
        systts: SystemTts,
        onSysttsChange: (SystemTts) -> Unit
    ) {
        val tts = (systts.tts as MsTTS)
        Column(modifier) {
            val formats = remember { MsTtsFormatManger.getFormatsByApiType(MsTtsApiType.EDGE) }
            AppSpinner(
                label = { Text(stringResource(id = R.string.label_audio_format)) },
                value = tts.format,
                values = formats,
                entries = formats,
                onSelectedChange = { k, v ->
                    onSysttsChange(systts.copy(tts = tts.copy(format = k as String)))
                },
                modifier = Modifier,
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
            val volStr = stringResource(id = R.string.label_speech_volume, volume.toString())
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
        Scaffold(
            modifier = modifier,
            topBar = {
                TtsTopAppBar(
                    title = { Text(text = stringResource(id = R.string.edit_builtin_tts)) },
                    onBackAction = onCancel,
                    onSaveAction = {
                        onSave()
                    }
                )
            }) { paddingValues ->
            Column(
                Modifier
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
            ) {
                Content(
                    modifier = Modifier
                        .padding(8.dp),
                    systts = systts,
                    onSysttsChange = onSysttsChange,
                )
            }
        }
    }

    @Composable
    private fun Content(
        modifier: Modifier,
        systts: SystemTts,
        onSysttsChange: (SystemTts) -> Unit,

        vm: MsTtsViewModel = viewModel()
    ) {
        val context = LocalContext.current
        var displayName by remember { mutableStateOf("") }

        @Suppress("NAME_SHADOWING")
        val systts by rememberUpdatedState(newValue = systts)
        val tts = systts.tts as MsTTS
        SaveActionHandler {
            if (systts.displayName.isNullOrBlank())
                onSysttsChange(
                    systts.copy(
                        displayName = displayName
                    )
                )

            true
        }

        var showAudition by remember { mutableStateOf(false) }
        if (showAudition) {
            AuditionDialog(systts = systts) { showAudition = false }
        }

        Column(modifier) {
            BasicInfoEditScreen(
                modifier = Modifier
                    .fillMaxWidth(),
                systts = systts,
                onSysttsChange = onSysttsChange
            )

            AuditionTextField(modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp), onAudition = {
                showAudition = true
            })

            val apis =
                remember { listOf(R.string.systts_api_edge, R.string.systts_api_edge_okhttp) }

            LaunchedEffect(vm) {
                runCatching {
                    vm.load()
                    vm.updateLocales()
                }.onFailure {
                    context.displayErrorDialog(it)
                }
            }

            AppSpinner(
                label = { Text(stringResource(id = R.string.label_api)) },
                value = tts.api,
                values = listOf(MsTtsApiType.EDGE, MsTtsApiType.EDGE_OKHTTP),
                entries = apis.map { stringResource(id = it) },
                onSelectedChange = { api, _ ->
                    onSysttsChange(systts.copy(tts = tts.copy(api = api as Int)))
                },
                modifier = Modifier.padding(top = 4.dp)
            )

            LoadingContent(isLoading = vm.isLoading) {
                Column {
                    AppSpinner(
                        label = { Text(stringResource(id = R.string.language)) },
                        value = tts.locale,
                        values = vm.locales.map { it.first },
                        entries = vm.locales.map { it.second },
                        onSelectedChange = { lang, _ ->
                            onSysttsChange(systts.copy(tts = tts.copy(locale = lang as String)))
                            vm.onLocaleChanged(lang)
                        },
                        modifier = Modifier.padding(top = 4.dp)
                    )

                    LaunchedEffect(tts.locale) {
                        vm.onLocaleChanged(tts.locale)
                    }

                    fun GeneralVoiceData.name() = localVoiceName + " (${voiceName})"

                    AppSpinner(
                        label = { Text(stringResource(id = R.string.label_voice)) },
                        value = tts.voiceName,
                        values = vm.voices.map { it.voiceName },
                        entries = vm.voices.map { it.name() },
                        onSelectedChange = { voice, name ->
                            val lastName =
                                vm.voices.find { it.voiceName == tts.voiceName }?.name() ?: ""
                            onSysttsChange(
                                systts.copy(
                                    displayName =
                                    if (systts.displayName.isNullOrBlank() || lastName == systts.displayName) name
                                    else systts.displayName,
                                    tts = tts.copy(voiceName = voice as String)
                                )
                            )

                            displayName = name
                        },
                        modifier = Modifier.padding(top = 4.dp)
                    )

                }
            }

            ParamsEditScreen(
                modifier = Modifier
                    .fillMaxWidth().padding(top = 8.dp),
                systts = systts,
                onSysttsChange = onSysttsChange
            )
        }

    }
}