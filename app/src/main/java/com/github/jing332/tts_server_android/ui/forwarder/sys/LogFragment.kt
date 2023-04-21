package com.github.jing332.tts_server_android.ui.forwarder.sys

import com.github.jing332.tts_server_android.help.config.SysTtsForwarderConfig
import com.github.jing332.tts_server_android.service.forwarder.system.SysTtsForwarderService
import com.github.jing332.tts_server_android.ui.forwarder.AbsForwarderHomePageFragment

internal class LogFragment() : AbsForwarderHomePageFragment(
    SysTtsForwarderService.ACTION_ON_STARTING,
    SysTtsForwarderService.ACTION_ON_CLOSED,
    SysTtsForwarderService.ACTION_ON_LOG
) {
    override var port: Int
        get() = SysTtsForwarderConfig.port
        set(value) {
            SysTtsForwarderConfig.port = value
        }


    override val isServiceRunning: Boolean = SysTtsForwarderService.isRunning
    override val tipInfo: String = ""
}