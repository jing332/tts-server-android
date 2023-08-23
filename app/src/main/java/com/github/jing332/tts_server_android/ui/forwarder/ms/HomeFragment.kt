package com.github.jing332.tts_server_android.ui.forwarder.ms

import com.github.jing332.tts_server_android.help.config.ServerConfig
import com.github.jing332.tts_server_android.service.forwarder.ms.MsTtsForwarderService
import com.github.jing332.tts_server_android.ui.forwarder.AbsForwarderHomePageFragment

internal class HomeFragment(
) : AbsForwarderHomePageFragment(
    MsTtsForwarderService.ACTION_ON_STARTING,
    MsTtsForwarderService.ACTION_ON_CLOSED,
    MsTtsForwarderService.ACTION_ON_LOG
) {
    override var port: Int
        get() = ServerConfig.port
        set(value) {
            ServerConfig.port = value
        }
    override val isServiceRunning: Boolean get() = MsTtsForwarderService.isRunning
    override val tipInfo: String = ""
}