package com.github.jing332.tts_server_android.compose.forwarder

import android.content.IntentFilter
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.github.jing332.tts_server_android.R
import com.github.jing332.tts_server_android.compose.systts.LogScreen
import com.github.jing332.tts_server_android.compose.widgets.DenseOutlinedField
import com.github.jing332.tts_server_android.compose.widgets.LocalBroadcastReceiver
import com.github.jing332.tts_server_android.compose.widgets.SwitchFloatingButton
import com.github.jing332.tts_server_android.constant.KeyConst
import com.github.jing332.tts_server_android.constant.LogLevel
import com.github.jing332.tts_server_android.ui.AppLog

@Suppress("DEPRECATION")
@Composable
internal fun BasicConfigScreen(
    modifier: Modifier,
    vm: ConfigViewModel,
    intentFilter: IntentFilter,
    actionOnLog: String,
    actionOnClosed: String,
    actionOnStarting: String,
    isRunning: Boolean,
    onRunningChange: (Boolean) -> Unit,
    switch: () -> Unit,
    port: Int,
    onPortChange: (Int) -> Unit
) {
    val context = LocalContext.current
    LocalBroadcastReceiver(intentFilter = intentFilter) { intent ->
        if (intent == null) return@LocalBroadcastReceiver
        when (intent.action) {
            actionOnLog -> {
                intent.getParcelableExtra<AppLog>(KeyConst.KEY_DATA)?.let { log ->
                    vm.logs.add(log)
                }
            }

            actionOnClosed -> {
                onRunningChange(false)
                vm.logs.add(AppLog(LogLevel.INFO, "服务已关闭"))
            }

            actionOnStarting -> {
                onRunningChange(true)
                vm.logs.add(AppLog(LogLevel.INFO, "服务已启动"))
            }
        }
    }

    Column(modifier) {
        LogScreen(
            modifier = Modifier.weight(1f), list = vm.logs, vm.logState
        )

        Row(Modifier.align(Alignment.CenterHorizontally)) {
            DenseOutlinedField(
                label = { Text(stringResource(id = R.string.listen_port)) },
                modifier = Modifier.align(Alignment.CenterVertically),
                value = port.toString(), onValueChange = {
                    kotlin.runCatching {
                        onPortChange(it.toInt())
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