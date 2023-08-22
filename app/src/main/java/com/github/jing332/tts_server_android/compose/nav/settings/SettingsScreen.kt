package com.github.jing332.tts_server_android.compose.nav.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DrawerState
import androidx.compose.material3.ExperimentalMaterial3Api
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
                onCheckedChange = { wrapButton = it })
        }
    }
}