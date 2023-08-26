package com.github.jing332.tts_server_android.compose.nav.systts.edit.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AudioFile
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.github.jing332.tts_server_android.R
import com.github.jing332.tts_server_android.compose.nav.systts.edit.BasicInfoEditScreen
import com.github.jing332.tts_server_android.compose.nav.systts.edit.IntSlider
import com.github.jing332.tts_server_android.compose.nav.systts.edit.ui.base.TtsTopAppBar
import com.github.jing332.tts_server_android.constant.SpeechTarget
import com.github.jing332.tts_server_android.data.entities.systts.SpeechRuleInfo
import com.github.jing332.tts_server_android.data.entities.systts.SystemTts
import com.github.jing332.tts_server_android.model.speech.tts.BgmTTS
import com.github.jing332.tts_server_android.ui.AppActivityResultContracts
import com.github.jing332.tts_server_android.ui.FilePickerActivity
import com.github.jing332.tts_server_android.ui.view.AppDialogs.displayErrorDialog
import com.github.jing332.tts_server_android.utils.ASFUriUtils.getPath
import com.github.jing332.tts_server_android.utils.toast

class BgmUI : TtsUI() {
    @Composable
    override fun ParamsEditScreen(
        modifier: Modifier,
        systts: SystemTts,
        onSysttsChange: (SystemTts) -> Unit
    ) {
        val tts = systts.tts as BgmTTS
        LaunchedEffect(Unit) {
            onSysttsChange(systts.copy(speechRule = SpeechRuleInfo(target = SpeechTarget.BGM)))
        }

        val volStr = stringResource(id = R.string.label_speech_volume, tts.volume.toString())
        IntSlider(label = volStr, value = tts.volume.toFloat(), onValueChange = {
            onSysttsChange(systts.copy(tts = tts.copy(volume = it.toInt())))
        }, valueRange = 1f..1000f)
    }

    @Composable
    override fun FullEditScreen(
        modifier: Modifier,
        systts: SystemTts,
        onSysttsChange: (SystemTts) -> Unit,
        onSave: () -> Unit,
        onCancel: () -> Unit
    ) {
        val tts = systts.tts as BgmTTS
        val context = LocalContext.current
        val filePicker =
            rememberLauncherForActivityResult(contract = AppActivityResultContracts.filePickerActivity()) {
                runCatching {
                    val path =
                        context.getPath(it.second, it.first is FilePickerActivity.RequestSelectDir)
                    if (path.isNullOrBlank()) context.toast(R.string.path_is_empty)
                    else {
                        onSysttsChange(
                            systts.copy(
                                tts = tts.copy(
                                    musicList = tts.musicList.toMutableSet().apply { add(path) }
                                )
                            )
                        )
                    }
                }.onFailure {
                    context.displayErrorDialog(it)
                }
            }

        val saveSignal = remember { mutableStateOf<(() -> Unit)?>(null) }
        Scaffold(topBar = {
            TtsTopAppBar(
                title = { Text(text = stringResource(id = R.string.edit_bgm_tts)) },
                onBackAction = onCancel,
                onSaveAction = {
                    saveSignal.value?.invoke()
                    onSave()
                }
            )
        }) { paddingValues ->
            Column(Modifier.padding(paddingValues)) {
                BasicInfoEditScreen(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    systts = systts,
                    onSysttsChange = onSysttsChange,
                    showSpeechTarget = false,
                )

                ParamsEditScreen(
                    modifier = Modifier.fillMaxWidth(),
                    systts = systts,
                    onSysttsChange = onSysttsChange
                )

                OutlinedCard(
                    Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                ) {
                    Row(
                        Modifier.align(Alignment.CenterHorizontally),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = {
                            filePicker.launch(
                                FilePickerActivity.RequestSelectFile(
                                    fileMimes = listOf("audio/*")
                                )
                            )
                        }) {
                            Icon(Icons.Default.AudioFile, null)
                            Text(stringResource(id = R.string.add_file))
                        }
                        VerticalDivider(Modifier.height(16.dp))
                        TextButton(onClick = {
                            filePicker.launch(
                                FilePickerActivity.RequestSelectDir()
                            )
                        }) {
                            Icon(Icons.Default.CreateNewFolder, null)
                            Text(stringResource(id = R.string.add_folder))
                        }
                    }

                    LazyColumn(Modifier.padding(8.dp)) {
                        itemsIndexed(
                            tts.musicList.toList(),
                            key = { index, _ -> index }) { index, item ->
                            Row {
                                Text(
                                    item,
                                    modifier = Modifier.weight(1f),
                                    lineHeight = LocalTextStyle.current.lineHeight * 0.8
                                )
                                IconButton(onClick = {
                                    onSysttsChange(
                                        systts.copy(
                                            tts = tts.copy(
                                                musicList = tts.musicList.toMutableSet()
                                                    .apply { remove(item) }
                                            )
                                        )
                                    )
                                }) {
                                    Icon(
                                        Icons.Default.DeleteForever,
                                        stringResource(id = R.string.delete),
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}