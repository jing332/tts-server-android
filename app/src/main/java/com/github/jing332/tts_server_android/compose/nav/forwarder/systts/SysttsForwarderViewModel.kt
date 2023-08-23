package com.github.jing332.tts_server_android.compose.nav.forwarder.systts

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import com.github.jing332.tts_server_android.ui.AppLog

class SysttsForwarderViewModel : ViewModel() {
    val logs = mutableStateListOf<AppLog>()
    val logState by lazy { LazyListState() }

}