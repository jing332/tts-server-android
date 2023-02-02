package com.github.jing332.tts_server_android.ui.forwarder.system

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.github.jing332.tts_server_android.service.forwarder.system.SysTtsForwarderService

class ScSwitchActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (SysTtsForwarderService.isRunning)
            SysTtsForwarderService.requestCloseServer()
        else
            startService(Intent(this, SysTtsForwarderService::class.java))

        finish()
    }
}