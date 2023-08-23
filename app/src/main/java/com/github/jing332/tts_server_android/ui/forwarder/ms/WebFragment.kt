package com.github.jing332.tts_server_android.ui.forwarder.ms

import com.github.jing332.tts_server_android.help.config.ServerConfig
import com.github.jing332.tts_server_android.service.forwarder.ms.MsTtsForwarderService
import com.github.jing332.tts_server_android.ui.forwarder.AbsForwarderWebPageFragment

class WebFragment() : AbsForwarderWebPageFragment(MsTtsForwarderService.ACTION_ON_STARTING) {
    override val port: Int get() = ServerConfig.port
    override val isServiceRunning: Boolean get() = MsTtsForwarderService.isRunning
}