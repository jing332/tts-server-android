package com.github.jing332.tts_server_android.ui.forwarder

import android.app.Activity
import android.os.Bundle
import com.github.jing332.tts_server_android.R
import com.github.jing332.tts_server_android.service.forwarder.ForwarderServiceManager.startMsTtsForwarder
import com.github.jing332.tts_server_android.service.forwarder.ms.MsTtsForwarderService

/* 桌面长按菜单{开关} */
class MsForwarderSwitchActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (MsTtsForwarderService.isRunning)
            MsTtsForwarderService.instance?.close()
        else
            startMsTtsForwarder()

        finish()
    }
}