package com.github.jing332.tts_server_android.compose.forwarder

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import com.github.jing332.tts_server_android.ui.AppLog

class ConfigViewModel : ViewModel() {
    val logs = mutableStateListOf<AppLog>()
    val logState by lazy { LazyListState() }
}