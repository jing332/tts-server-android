package com.github.jing332.tts_server_android.compose.systts.list.edit.ui

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AudioFile
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material3.FilledTonalButton
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.github.jing332.tts_server_android.R
import com.github.jing332.tts_server_android.compose.systts.list.IntSlider
import com.github.jing332.tts_server_android.compose.systts.list.edit.BasicInfoEditScreen
import com.github.jing332.tts_server_android.compose.systts.list.edit.ui.widgets.TtsTopAppBar
import com.github.jing332.tts_server_android.compose.widgets.AppSelectionDialog
import com.github.jing332.tts_server_android.constant.SpeechTarget
import com.github.jing332.tts_server_android.data.entities.systts.SpeechRuleInfo
import com.github.jing332.tts_server_android.data.entities.systts.SystemTts
import com.github.jing332.tts_server_android.model.speech.tts.BgmTTS
import com.github.jing332.tts_server_android.ui.AppActivityResultContracts
import com.github.jing332.tts_server_android.ui.ExoPlayerActivity
import com.github.jing332.tts_server_android.ui.FilePickerActivity
import com.github.jing332.tts_server_android.ui.view.AppDialogs.displayErrorDialog
import com.github.jing332.tts_server_android.utils.ASFUriUtils.getPath
import com.github.jing332.tts_server_android.utils.FileUtils.audioList
import com.github.jing332.tts_server_android.utils.clickableRipple
import com.github.jing332.tts_server_android.utils.toast
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import java.io.File

class BgmTtsUI : TtsUI() {
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

        val volStr =
            stringResource(
                id = R.string.label_speech_volume,
                if (tts.volume == 0) stringResource(id = R.string.follow) else tts.volume.toString()
            )
        IntSlider(
            modifier = Modifier.padding(top = 8.dp),
            label = volStr, value = tts.volume.toFloat(),
            onValueChange = {
                onSysttsChange(systts.copy(tts = tts.copy(volume = it.toInt())))
            }, valueRange = 0f..1000f
        )
    }

    @Composable
    override fun FullEditScreen(
        modifier: Modifier,
        systts: SystemTts,
        onSysttsChange: (SystemTts) -> Unit,
        onSave: () -> Unit,
        onCancel: () -> Unit
    ) {
        LaunchedEffect(Unit) {

        }

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

        var showMusicList by remember { mutableStateOf("") }
        if (showMusicList != "") {
            val audioFiles = remember(showMusicList) {
                try {
                    File(showMusicList).audioList()
                } catch (e: Exception) {
                    context.displayErrorDialog(e)
                    null
                }
            } ?: return

            AppSelectionDialog(
                onDismissRequest = { showMusicList = "" },
                title = { Text(showMusicList) },
                value = Any(),
                values = audioFiles,
                entries = audioFiles.map { it.name },
                onClick = { value, _ ->
                    context.startActivity(Intent(context, ExoPlayerActivity::class.java).apply {
                        action = Intent.ACTION_VIEW
                        data = (value as File).toUri()
                    })
                }
            )
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
                    FilesAccessPermissionContent(Modifier.fillMaxWidth())

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
                        items(tts.musicList.toList()) { item ->
                            Row(
                                Modifier
                                    .clip(MaterialTheme.shapes.small)
                                    .clickableRipple {
                                        showMusicList = item
                                    }
                            ) {
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

@OptIn(ExperimentalPermissionsApi::class)
@Composable
private fun FilesAccessPermissionContent(modifier: Modifier = Modifier) {
    val context = LocalContext.current

    @Composable
    fun ColumnScope.warnButton(text: String, onClick: () -> Unit) {
        FilledTonalButton(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(top = 8.dp),
            onClick = onClick,
            content = {
                Text(
                    text,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        )
    }

    Column(modifier) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) { // A11
            var isGranted by remember { mutableStateOf(Environment.isExternalStorageManager()) }
            val permissionCheckerObserver = remember {
                LifecycleEventObserver { _, event ->
                    if (event == Lifecycle.Event.ON_RESUME) {
                        isGranted = Environment.isExternalStorageManager()
                    }
                }
            }
            val lifecycle = LocalLifecycleOwner.current.lifecycle
            DisposableEffect(lifecycle, permissionCheckerObserver) {
                lifecycle.addObserver(permissionCheckerObserver)
                onDispose { lifecycle.removeObserver(permissionCheckerObserver) }
            }

            if (!isGranted) {
                warnButton(text = stringResource(id = R.string.grant_permission_all_file)) {
                    context.startActivity(Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION).apply {
                        setData(Uri.parse("package:${context.packageName}"))
                    })
                }
            }
        }

        val storagePermission = rememberPermissionState(Manifest.permission.READ_EXTERNAL_STORAGE)
        if (!storagePermission.status.isGranted)
            warnButton(text = stringResource(R.string.grant_permission_storage_file)) {
                storagePermission.launchPermissionRequest()
            }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // A13
            val audioPermission = rememberPermissionState(Manifest.permission.READ_MEDIA_AUDIO)

            if (!audioPermission.status.isGranted)
                warnButton(text = stringResource(R.string.grant_permission_audio_file)) {
                    audioPermission.launchPermissionRequest()
                }
        }


    }
}