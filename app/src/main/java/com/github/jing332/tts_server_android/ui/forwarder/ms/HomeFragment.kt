package com.github.jing332.tts_server_android.ui.forwarder.ms

import com.github.jing332.tts_server_android.help.config.ServerConfig
import com.github.jing332.tts_server_android.service.forwarder.ms.TtsIntentService
import com.github.jing332.tts_server_android.ui.forwarder.AbsForwarderHomePageFragment

internal class HomeFragment(
) : AbsForwarderHomePageFragment(
    TtsIntentService.ACTION_ON_STARTED,
    TtsIntentService.ACTION_ON_CLOSED,
    TtsIntentService.ACTION_ON_LOG
) {
    override var port: Int
        get() = ServerConfig.port
        set(value) {
            ServerConfig.port = value
        }
    override val isServiceRunning: Boolean get() = TtsIntentService.instance?.isRunning == true
    override val tipInfo: String = ""
}