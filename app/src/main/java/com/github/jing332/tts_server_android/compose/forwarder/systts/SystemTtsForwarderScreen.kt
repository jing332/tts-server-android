package com.github.jing332.tts_server_android.compose.forwarder.systts

import android.content.Intent
import android.content.IntentFilter
import androidx.compose.foundation.layout.fillMaxSize
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
import com.github.jing332.tts_server_android.conf.SysttsForwarderConfig
import com.github.jing332.tts_server_android.service.forwarder.ForwarderServiceManager.switchSysTtsForwarder
import com.github.jing332.tts_server_android.service.forwarder.system.SysTtsForwarderService
import com.github.jing332.tts_server_android.ui.forwarder.SystemForwarderSwitchActivity
import com.github.jing332.tts_server_android.utils.MyTools

@Composable
fun SystemTtsForwarderScreen(cfgVM: ConfigViewModel = viewModel()) {
    val context = LocalContext.current
    var port by remember { SysttsForwarderConfig.port }
    BasicForwarderScreen(
        topBar = {
            var wakeLockEnabled by remember { SysttsForwarderConfig.isWakeLockEnabled }
            ForwarderTopAppBar(
                title = { Text(text = stringResource(id = R.string.forwarder_systts)) },
                wakeLockEnabled = wakeLockEnabled,
                onWakeLockEnabledChange = { wakeLockEnabled = it },
                onOpenWeb = { "http://localhost:${port}" }
            ) {
                MyTools.addShortcut(
                    ctx = context,
                    name = context.getString(R.string.forwarder_systts),
                    id = "switch_systts_forwarder",
                    iconResId = R.mipmap.ic_app_launcher_round,
                    launcherIntent = Intent(context, SystemForwarderSwitchActivity::class.java)
                )
            }
        },
        configScreen = {
            var isRunning by remember { mutableStateOf(SysTtsForwarderService.isRunning) }
            BasicConfigScreen(
                modifier = Modifier.fillMaxSize(),
                vm = cfgVM,
                intentFilter = IntentFilter().apply {
                    addAction(SysTtsForwarderService.ACTION_ON_LOG)
                    addAction(SysTtsForwarderService.ACTION_ON_CLOSED)
                    addAction(SysTtsForwarderService.ACTION_ON_STARTING)
                },
                actionOnLog = SysTtsForwarderService.ACTION_ON_LOG,
                actionOnClosed = SysTtsForwarderService.ACTION_ON_CLOSED,
                actionOnStarting = SysTtsForwarderService.ACTION_ON_STARTING,
                isRunning = isRunning,
                onRunningChange = { isRunning = it },
                switch = { context.switchSysTtsForwarder() },
                port = port,
                onPortChange = { port = it }
            )
        }) {
        "http://localhost:${port}"
    }
}