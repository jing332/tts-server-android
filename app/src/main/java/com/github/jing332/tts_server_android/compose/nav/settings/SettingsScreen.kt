package com.github.jing332.tts_server_android.compose.nav.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowCircleUp
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Headset
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material3.DrawerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.github.jing332.tts_server_android.R
import com.github.jing332.tts_server_android.compose.nav.NavTopAppBar
import com.github.jing332.tts_server_android.conf.AppConfig
import com.github.jing332.tts_server_android.conf.SystemTtsConfig

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
        Column(Modifier.padding(paddingValues)) {
            var wrapButton by remember { AppConfig.isSwapListenAndEditButton }
            PreferenceSwitch(
                title = { Text(stringResource(id = R.string.pref_swap_listen_and_edit_button)) },
                subTitle = {},
                checked = wrapButton,
                onCheckedChange = { wrapButton = it },
                icon = {
                    Icon(Icons.Default.Headset, contentDescription = null)
                }
            )

            PreferenceDivider {
                Text(stringResource(id = R.string.systts_interface_preference))
            }
            var autoCheck by remember { AppConfig.isAutoCheckUpdateEnabled }
            PreferenceSwitch(
                title = { Text(stringResource(id = R.string.auto_check_update)) },
                subTitle = { Text(stringResource(id = R.string.check_update_summary)) },
                checked = autoCheck,
                onCheckedChange = { autoCheck = it },
                icon = {
                    Icon(Icons.Default.ArrowCircleUp, contentDescription = null)
                }
            )

            var targetMultiple by remember { SystemTtsConfig.isVoiceMultipleEnabled }
            PreferenceSwitch(
                title = { Text(stringResource(id = R.string.voice_multiple_option)) },
                subTitle = { Text(stringResource(id = R.string.voice_multiple_summary)) },
                checked = targetMultiple,
                onCheckedChange = { targetMultiple = it },
                icon = {
                    Icon(Icons.Default.SelectAll, contentDescription = null)
                }
            )

            var groupMultiple by remember { SystemTtsConfig.isGroupMultipleEnabled }
            PreferenceSwitch(
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