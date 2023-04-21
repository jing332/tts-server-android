package com.github.jing332.tts_server_android.ui.forwarder.sys

import com.github.jing332.tts_server_android.help.config.SysTtsForwarderConfig
import com.github.jing332.tts_server_android.service.forwarder.system.SysTtsForwarderService
import com.github.jing332.tts_server_android.ui.forwarder.AbsForwarderWebPageFragment

internal class WebFragment() :
    AbsForwarderWebPageFragment(SysTtsForwarderService.ACTION_ON_STARTING) {
    override var isServiceRunning: Boolean = SysTtsForwarderService.isRunning
    override val port: Int = SysTtsForwarderConfig.port
}