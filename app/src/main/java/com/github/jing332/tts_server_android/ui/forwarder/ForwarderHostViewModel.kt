package com.github.jing332.tts_server_android.ui.forwarder

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ForwarderHostViewModel : ViewModel() {
    val switchStateLiveData = MutableLiveData<Boolean>()
}