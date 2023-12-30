package com.github.jing332.tts_server_android.compose.forwarder.ms

import android.content.Intent
import android.content.IntentFilter
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Token
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import com.github.jing332.tts_server_android.R
import com.github.jing332.tts_server_android.compose.forwarder.BasicConfigScreen
import com.github.jing332.tts_server_android.compose.forwarder.BasicForwarderScreen
import com.github.jing332.tts_server_android.compose.forwarder.ConfigViewModel
import com.github.jing332.tts_server_android.compose.forwarder.ForwarderTopAppBar
import com.github.jing332.tts_server_android.compose.widgets.TextFieldDialog
import com.github.jing332.tts_server_android.conf.MsForwarderConfig
import com.github.jing332.tts_server_android.service.forwarder.ForwarderServiceManager.switchMsTtsForwarder
import com.github.jing332.tts_server_android.service.forwarder.ms.MsTtsForwarderService
import com.github.jing332.tts_server_android.ui.forwarder.MsForwarderSwitchActivity
import com.github.jing332.tts_server_android.utils.MyTools

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MsTtsForwarderScreen(
    cfgVM: ConfigViewModel = viewModel()
) {
    var showTokenDialog by remember { mutableStateOf(false) }
    if (showTokenDialog) {
        var text by remember { mutableStateOf(MsForwarderConfig.token.value) }
        TextFieldDialog(
            title = stringResource(id = R.string.server_set_token),
            text = text,
            onTextChange = { text = it },
            onDismissRequest = { showTokenDialog = false }) {
            MsForwarderConfig.token.value = text
        }
    }

    var wakeLockEnabled by remember { MsForwarderConfig.isWakeLockEnabled }
    var port by remember { MsForwarderConfig.port }
    val context = LocalContext.current
    BasicForwarderScreen(topBar = {
        ForwarderTopAppBar(
            title = { Text(stringResource(id = R.string.forwarder_ms)) },
            wakeLockEnabled = wakeLockEnabled,
            onWakeLockEnabledChange = { wakeLockEnabled = it },
            onOpenWeb = { "http://localhost:${port}" },
            onAddDesktopShortCut = {
                MyTools.addShortcut(
                    ctx = context,
                    name = context.getString(R.string.forwarder_ms),
                    id = "switch_ms_forwarder",
                    iconResId = R.mipmap.ic_app_launcher_round,
                    launcherIntent = Intent(context, MsForwarderSwitchActivity::class.java)
                )
            },
            actions = {
                DropdownMenuItem(
                    text = { Text(stringResource(id = R.string.server_set_token)) },
                    onClick = { showTokenDialog = true },
                    leadingIcon = {
                        Icon(Icons.Default.Token, null)
                    }
                )
            }
        )

    },
        configScreen = {
            var isRunning by remember { mutableStateOf(MsTtsForwarderService.isRunning) }
            BasicConfigScreen(modifier = Modifier.fillMaxSize(),
                vm = cfgVM,
                intentFilter = IntentFilter().apply {
                    addAction(MsTtsForwarderService.ACTION_ON_LOG)
                    addAction(MsTtsForwarderService.ACTION_ON_CLOSED)
                    addAction(MsTtsForwarderService.ACTION_ON_STARTING)
                },
                actionOnLog = MsTtsForwarderService.ACTION_ON_LOG,
                actionOnClosed = MsTtsForwarderService.ACTION_ON_CLOSED,
                actionOnStarting = MsTtsForwarderService.ACTION_ON_STARTING,
                isRunning = isRunning,
                onRunningChange = { isRunning = it },
                switch = { context.switchMsTtsForwarder() },
                port = port,
                onPortChange = { port = it })
        }) {

        "http://localhost:${port}"
    }

}