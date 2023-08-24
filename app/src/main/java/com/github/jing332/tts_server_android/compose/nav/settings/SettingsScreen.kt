package com.github.jing332.tts_server_android.compose.nav.settings

import android.content.Intent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowCircleUp
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Headset
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material.icons.filled.SettingsBackupRestore
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
import com.github.jing332.tts_server_android.compose.nav.NavTopAppBar
import com.github.jing332.tts_server_android.conf.AppConfig
import com.github.jing332.tts_server_android.conf.SystemTtsConfig
import com.github.jing332.tts_server_android.ui.preference.backup_restore.BackupRestoreActivity
import com.github.jing332.tts_server_android.ui.systts.direct_upload.DirectUploadSettingsActivity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(drawerState: DrawerState) {
    Scaffold(
        topBar = {
            NavTopAppBar(
                title = { Text(stringResource(R.string.settings)) },
                drawerState = drawerState
            )
        }
    ) { paddingValues ->
        val context = LocalContext.current
        Column(Modifier.padding(paddingValues)) {
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
                            context,
                            DirectUploadSettingsActivity::class.java
                        ).apply { action = Intent.ACTION_VIEW })
                },
                title = { Text(stringResource(id = R.string.direct_link_settings)) },
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
                            AppLocale.getSetLocale(context).displayName
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

            DividerPreference {
                Text(stringResource(id = R.string.systts_interface_preference))
            }
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