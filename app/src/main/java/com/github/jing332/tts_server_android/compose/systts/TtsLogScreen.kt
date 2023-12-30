package com.github.jing332.tts_server_android.compose.systts

import android.content.IntentFilter
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import com.github.jing332.tts_server_android.R
import com.github.jing332.tts_server_android.compose.nav.NavTopAppBar
import com.github.jing332.tts_server_android.compose.widgets.LocalBroadcastReceiver
import com.github.jing332.tts_server_android.constant.KeyConst
import com.github.jing332.tts_server_android.service.systts.SystemTtsService
import com.github.jing332.tts_server_android.ui.AppLog

@Suppress("DEPRECATION")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun TtsLogScreen(vm: TtsLogViewModel = viewModel()) {
    LocalBroadcastReceiver(intentFilter = IntentFilter(SystemTtsService.ACTION_ON_LOG)) {
        if (it?.action == SystemTtsService.ACTION_ON_LOG) {
            it.getParcelableExtra<AppLog>(KeyConst.KEY_DATA)?.let { log ->
                println("ACTION_ON_LOG ${log.msg}")
                vm.logs.add(log)
            }
        }
    }

    Scaffold(
        topBar = {
            NavTopAppBar(title = { Text(stringResource(id = R.string.log)) }, actions = {
                IconButton(onClick = { vm.logs.clear() }) {
                    Icon(Icons.Default.DeleteOutline, stringResource(id = R.string.clear_log))
                }
            })
        }
    ) { paddingValues ->
        LogScreen(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = paddingValues.calculateTopPadding()), list = vm.logs
        )
    }
}