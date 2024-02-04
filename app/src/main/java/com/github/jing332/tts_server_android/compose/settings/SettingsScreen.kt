package com.github.jing332.tts_server_android.compose.settings

import android.content.Intent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuOpen
import androidx.compose.material.icons.automirrored.filled.TextSnippet
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.ArrowCircleUp
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.FileOpen
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Headset
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.PlayCircleOutline
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material.icons.filled.SettingsBackupRestore
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.TextSnippet
import androidx.compose.material.icons.filled.Waves
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.github.jing332.tts_server_android.AppLocale
import com.github.jing332.tts_server_android.R
import com.github.jing332.tts_server_android.app
import com.github.jing332.tts_server_android.compose.backup.BackupRestoreActivity
import com.github.jing332.tts_server_android.compose.nav.NavTopAppBar
import com.github.jing332.tts_server_android.compose.systts.directlink.LinkUploadRuleActivity
import com.github.jing332.tts_server_android.compose.theme.getAppTheme
import com.github.jing332.tts_server_android.compose.theme.setAppTheme
import com.github.jing332.tts_server_android.conf.AppConfig
import com.github.jing332.tts_server_android.conf.SystemTtsConfig
import com.github.jing332.tts_server_android.constant.FilePickerMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(drawerState: DrawerState) {
    var showThemeDialog by remember { mutableStateOf(false) }
    if (showThemeDialog)
        ThemeSelectionDialog(
            onDismissRequest = { showThemeDialog = false },
            currentTheme = getAppTheme(),
            onChangeTheme = {
                setAppTheme(it)
            }
        )

    Scaffold(
        topBar = {
            NavTopAppBar(
                title = { Text(stringResource(R.string.settings)) },
                drawerState = drawerState
            )
        }
    ) { paddingValues ->
        val context = LocalContext.current
        Column(
            Modifier
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            DividerPreference { Text(stringResource(id = R.string.app_name)) }

            BasePreferenceWidget(
                icon = {
                    Icon(Icons.Default.SettingsBackupRestore, null)
                },
                onClick = {
                    context.startActivity(
                        Intent(
                            context,
                            BackupRestoreActivity::class.java
                        ).apply { action = Intent.ACTION_VIEW })
                },
                title = { Text(stringResource(id = R.string.backup_restore)) },
            )

            BasePreferenceWidget(
                icon = {
                    Icon(Icons.Default.Link, null)
                },
                onClick = {
                    context.startActivity(
                        Intent(
                            context, LinkUploadRuleActivity::class.java
                        ).apply { action = Intent.ACTION_VIEW })
                },
                title = { Text(stringResource(id = R.string.direct_link_settings)) },
            )

            BasePreferenceWidget(
                icon = { Icon(Icons.Default.ColorLens, null) },
                onClick = { showThemeDialog = true },
                title = { Text(stringResource(id = R.string.theme)) },
                subTitle = { Text(stringResource(id = getAppTheme().stringResId)) },
            )

            val languageKeys = remember {
                mutableListOf("").apply { addAll(AppLocale.localeMap.keys.toList()) }
            }

            val languageNames = remember {
                AppLocale.localeMap.map { "${it.value.displayName} - ${it.value.getDisplayName(it.value)}" }
                    .toMutableList()
                    .apply { add(0, context.getString(R.string.follow_system)) }
            }

            var langMenu by remember { mutableStateOf(false) }
            DropdownPreference(
                Modifier.minimumInteractiveComponentSize(),
                expanded = langMenu,
                onExpandedChange = { langMenu = it },
                icon = {
                    Icon(Icons.Default.Language, null)
                },
                title = { Text(stringResource(id = R.string.language)) },
                subTitle = {
                    Text(
                        if (AppLocale.getLocaleCodeFromFile(context).isEmpty()) {
                            stringResource(id = R.string.follow_system)
                        } else {
                            AppLocale.getLocaleFromFile(context).displayName
                        }
                    )
                }) {
                languageNames.forEachIndexed { index, name ->
                    DropdownMenuItem(
                        text = {
                            Text(name)
                        }, onClick = {
                            langMenu = false

                            AppLocale.saveLocaleCodeToFile(context, languageKeys[index])
                            AppLocale.setLocale(app)
                        }
                    )
                }
            }

            var filePickerMode by remember { AppConfig.filePickerMode }
            var expanded by remember { mutableStateOf(false) }
            DropdownPreference(
                expanded = expanded,
                onExpandedChange = { expanded = it },
                icon = { Icon(Icons.Default.FileOpen, null) },
                title = { Text(stringResource(id = R.string.file_picker_mode)) },
                subTitle = {
                    Text(
                        when (filePickerMode) {
                            FilePickerMode.PROMPT -> stringResource(id = R.string.file_picker_mode_prompt)
                            FilePickerMode.BUILTIN -> stringResource(id = R.string.file_picker_mode_builtin)
                            else -> stringResource(id = R.string.file_picker_mode_system)
                        }
                    )
                },
                actions = {
                    DropdownMenuItem(
                        text = { Text(stringResource(id = R.string.file_picker_mode_prompt)) },
                        onClick = {
                            expanded = false
                            filePickerMode = FilePickerMode.PROMPT
                        }
                    )

                    DropdownMenuItem(
                        text = { Text(stringResource(id = R.string.file_picker_mode_builtin)) },
                        onClick = {
                            expanded = false
                            filePickerMode = FilePickerMode.BUILTIN
                        }
                    )

                    DropdownMenuItem(
                        text = { Text(stringResource(id = R.string.file_picker_mode_system)) },
                        onClick = {
                            expanded = false
                            filePickerMode = FilePickerMode.SYSTEM
                        }
                    )
                }
            )

            var autoCheck by remember { AppConfig.isAutoCheckUpdateEnabled }
            SwitchPreference(
                title = { Text(stringResource(id = R.string.auto_check_update)) },
                subTitle = { Text(stringResource(id = R.string.check_update_summary)) },
                checked = autoCheck,
                onCheckedChange = { autoCheck = it },
                icon = {
                    Icon(Icons.Default.ArrowCircleUp, contentDescription = null)
                }
            )

            var maxDropdownCount by remember { AppConfig.spinnerMaxDropDownCount }
            SliderPreference(
                title = { Text(stringResource(id = R.string.spinner_drop_down_max_count)) },
                subTitle = { Text(stringResource(id = R.string.spinner_drop_down_max_count_summary)) },
                value = maxDropdownCount.toFloat(),
                onValueChange = {
                    maxDropdownCount = it.toInt()
                },
                label = if (maxDropdownCount == 0) stringResource(id = R.string.unlimited) else maxDropdownCount.toString(),
                valueRange = 0f..50f,
                icon = { Icon(Icons.AutoMirrored.Filled.MenuOpen, null) }
            )

            DividerPreference {
                Text(stringResource(id = R.string.system_tts))
            }

            var useExoDecoder by remember { SystemTtsConfig.isExoDecoderEnabled }
            SwitchPreference(
                title = { Text(stringResource(id = R.string.use_exo_decoder)) },
                subTitle = { Text(stringResource(id = R.string.use_exo_decoder_summary)) },
                checked = useExoDecoder,
                onCheckedChange = { useExoDecoder = it },
                icon = { Icon(Icons.Default.PlayCircleOutline, null) }
            )

            var streamPlay by remember { SystemTtsConfig.isStreamPlayModeEnabled }
            SwitchPreference(
                title = { Text(stringResource(id = R.string.stream_audio_mode)) },
                subTitle = { Text(stringResource(id = R.string.stream_audio_mode_summary)) },
                checked = streamPlay,
                onCheckedChange = { streamPlay = it },
                icon = { Icon(Icons.Default.Waves, null) }
            )

            var skipSilentText by remember { SystemTtsConfig.isSkipSilentText }
            SwitchPreference(
                title = { Text(stringResource(id = R.string.skip_request_silent_text)) },
                subTitle = { Text(stringResource(id = R.string.skip_request_silent_text_summary)) },
                checked = skipSilentText,
                onCheckedChange = { skipSilentText = it },
                icon = { Icon(Icons.AutoMirrored.Filled.TextSnippet, null) }
            )

            var foregroundService by remember { SystemTtsConfig.isForegroundServiceEnabled }
            SwitchPreference(
                title = { Text(stringResource(id = R.string.foreground_service_and_notification)) },
                subTitle = { Text(stringResource(id = R.string.foreground_service_and_notification_summary)) },
                checked = foregroundService,
                onCheckedChange = { foregroundService = it },
                icon = { Icon(Icons.Default.NotificationsNone, null) }
            )

            var wakeLock by remember { SystemTtsConfig.isWakeLockEnabled }
            SwitchPreference(
                title = { Text(stringResource(id = R.string.wake_lock)) },
                subTitle = { Text(stringResource(id = R.string.wake_lock_summary)) },
                checked = wakeLock,
                onCheckedChange = { wakeLock = it },
                icon = { Icon(Icons.Default.Lock, null) }
            )

            var maxRetry by remember { SystemTtsConfig.maxRetryCount }
            val maxRetryValue =
                if (maxRetry == 0) stringResource(id = R.string.no_retries) else maxRetry.toString()
            SliderPreference(
                title = { Text(stringResource(id = R.string.max_retry_count)) },
                subTitle = { Text(stringResource(id = R.string.max_retry_count_summary)) },
                value = maxRetry.toFloat(),
                onValueChange = { maxRetry = it.toInt() },
                valueRange = 0f..10f,
                icon = { Icon(Icons.Default.Repeat, null) },
                label = maxRetryValue,
            )

            var emptyAudioCount by remember { SystemTtsConfig.maxEmptyAudioRetryCount }
            val emptyAudioCountValue =
                if (emptyAudioCount == 0) stringResource(id = R.string.no_retries) else emptyAudioCount.toString()
            SliderPreference(
                title = { Text(stringResource(id = R.string.retry_count_when_audio_empty)) },
                subTitle = { Text(stringResource(id = R.string.retry_count_when_audio_empty_summary)) },
                value = emptyAudioCount.toFloat(),
                onValueChange = { emptyAudioCount = it.toInt() },
                valueRange = 0f..10f,
                icon = { Icon(Icons.Default.Audiotrack, null) },
                label = emptyAudioCountValue
            )

            var standbyTriggeredIndex by remember { SystemTtsConfig.standbyTriggeredRetryIndex }
            val standbyTriggeredIndexValue = standbyTriggeredIndex.toString()
            SliderPreference(
                title = { Text(stringResource(id = R.string.systts_standby_triggered_retry_index)) },
                subTitle = { Text(stringResource(id = R.string.systts_standby_triggered_retry_index_summary)) },
                value = standbyTriggeredIndex.toFloat(),
                onValueChange = { standbyTriggeredIndex = it.toInt() },
                valueRange = 0f..10f,
                icon = { Icon(Icons.Default.Repeat, null) },
                label = standbyTriggeredIndexValue
            )


            var requestTimeout by remember { SystemTtsConfig.requestTimeout }
            val requestTimeoutValue = "${requestTimeout / 1000}s"
            SliderPreference(
                title = { Text(stringResource(id = R.string.request_timeout)) },
                subTitle = { Text(stringResource(id = R.string.request_timeout_summary)) },
                value = (requestTimeout / 1000).toFloat(),
                onValueChange = { requestTimeout = it.toInt() * 1000 },
                valueRange = 1f..30f,
                icon = { Icon(Icons.Default.AccessTime, null) },
                label = requestTimeoutValue
            )

            DividerPreference {
                Text(stringResource(id = R.string.systts_interface_preference))
            }

            var limitTagLen by remember { AppConfig.limitTagLength }
            val limitTagLenString =
                if (limitTagLen == 0) stringResource(id = R.string.unlimited) else limitTagLen.toString()
            SliderPreference(
                title = { Text(stringResource(id = R.string.limit_tag_length)) },
                subTitle = { Text(stringResource(id = R.string.limit_tag_length_summary)) },
                value = limitTagLen.toFloat(),
                onValueChange = { limitTagLen = it.toInt() },
                valueRange = 0f..50f,
                icon = { Icon(Icons.Default.Tag, null) },
                label = limitTagLenString
            )

            var limitNameLen by remember { AppConfig.limitNameLength }
            val limitNameLenString =
                if (limitNameLen == 0) stringResource(id = R.string.unlimited) else limitNameLen.toString()
            SliderPreference(
                title = { Text(stringResource(id = R.string.limit_name_length)) },
                subTitle = { Text(stringResource(id = R.string.limit_name_length_summary)) },
                value = limitNameLen.toFloat(),
                onValueChange = { limitNameLen = it.toInt() },
                valueRange = 0f..50f,
                icon = { Icon(Icons.Default.TextFields, null) },
                label = limitNameLenString
            )

            var wrapButton by remember { AppConfig.isSwapListenAndEditButton }
            SwitchPreference(
                title = { Text(stringResource(id = R.string.pref_swap_listen_and_edit_button)) },
                subTitle = {},
                checked = wrapButton,
                onCheckedChange = { wrapButton = it },
                icon = {
                    Icon(Icons.Default.Headset, contentDescription = null)
                }
            )

            var targetMultiple by remember { SystemTtsConfig.isVoiceMultipleEnabled }
            SwitchPreference(
                title = { Text(stringResource(id = R.string.voice_multiple_option)) },
                subTitle = { Text(stringResource(id = R.string.voice_multiple_summary)) },
                checked = targetMultiple,
                onCheckedChange = { targetMultiple = it },
                icon = {
                    Icon(Icons.Default.SelectAll, contentDescription = null)
                }
            )

            var groupMultiple by remember { SystemTtsConfig.isGroupMultipleEnabled }
            SwitchPreference(
                title = { Text(stringResource(id = R.string.groups_multiple)) },
                subTitle = { Text(stringResource(id = R.string.groups_multiple_summary)) },
                checked = groupMultiple,
                onCheckedChange = { groupMultiple = it },
                icon = {
                    Icon(Icons.Default.Groups, contentDescription = null)
                }
            )
        }
    }
}