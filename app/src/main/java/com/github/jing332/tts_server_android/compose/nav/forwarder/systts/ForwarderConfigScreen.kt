package com.github.jing332.tts_server_android.compose.nav.forwarder.systts

import android.content.IntentFilter
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.github.jing332.tts_server_android.R
import com.github.jing332.tts_server_android.compose.nav.LogScreen
import com.github.jing332.tts_server_android.compose.widgets.DenseOutlinedField
import com.github.jing332.tts_server_android.compose.widgets.LocalBroadcastReceiver
import com.github.jing332.tts_server_android.compose.widgets.SwitchFloatingButton
import com.github.jing332.tts_server_android.conf.SysttsForwarderConfig
import com.github.jing332.tts_server_android.constant.KeyConst
import com.github.jing332.tts_server_android.constant.LogLevel
import com.github.jing332.tts_server_android.service.forwarder.ForwarderServiceManager
import com.github.jing332.tts_server_android.service.forwarder.ForwarderServiceManager.startSysTtsForwarder
import com.github.jing332.tts_server_android.service.forwarder.system.SysTtsForwarderService
import com.github.jing332.tts_server_android.ui.AppLog
import com.github.jing332.tts_server_android.utils.longToast

@Suppress("DEPRECATION")
@Composable
internal fun ForwarderConfigScreen(modifier: Modifier, vm: SysttsForwarderViewModel) {
    val context = LocalContext.current
    var isRunning by remember { mutableStateOf(SysTtsForwarderService.isRunning) }

    LocalBroadcastReceiver(intentFilter = IntentFilter(SysTtsForwarderService.ACTION_ON_LOG).apply {
        addAction(SysTtsForwarderService.ACTION_ON_CLOSED)
        addAction(SysTtsForwarderService.ACTION_ON_STARTING)
    }) { intent ->
        if (intent == null) return@LocalBroadcastReceiver
        when (intent.action) {
            SysTtsForwarderService.ACTION_ON_LOG -> {
                intent.getParcelableExtra<AppLog>(KeyConst.KEY_DATA)?.let { log ->
                    vm.logs.add(log)
                }
            }

            SysTtsForwarderService.ACTION_ON_CLOSED -> {
                isRunning = false
                vm.logs.add(AppLog(LogLevel.INFO, "服务已关闭"))
            }

            SysTtsForwarderService.ACTION_ON_STARTING -> {
                isRunning = true
                vm.logs.add(AppLog(LogLevel.INFO, "服务已启动"))
            }
        }
    }

    fun switch() {
        if (SysTtsForwarderService.isRunning)
            runCatching {
                ForwarderServiceManager.closeSysTtsForwarder()
            }.onFailure {
                context.longToast(it.toString())
            }
        else
            context.startSysTtsForwarder()
    }

    Column(modifier) {
        LogScreen(
            modifier = Modifier.weight(1f), list = vm.logs, vm.logState
        )

        var port by remember { SysttsForwarderConfig.port }
        Row(Modifier.align(Alignment.CenterHorizontally)) {
            DenseOutlinedField(
                label = { Text(stringResource(id = R.string.listen_port)) },
                modifier = Modifier.align(Alignment.CenterVertically),
                value = port.toString(), onValueChange = {
                    kotlin.runCatching {
                        port = it.toInt()
                    }
                }
            )

            SwitchFloatingButton(
                modifier = Modifier.padding(8.dp),
                switch = isRunning,
                onSwitchChange = { switch() }
            )
        }
    }
}