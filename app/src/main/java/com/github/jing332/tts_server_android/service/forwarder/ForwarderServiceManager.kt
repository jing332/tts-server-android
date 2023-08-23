package com.github.jing332.tts_server_android.service.forwarder

import android.content.Context
import android.content.Intent
import com.github.jing332.tts_server_android.service.forwarder.ms.MsTtsForwarderService
import com.github.jing332.tts_server_android.service.forwarder.system.SysTtsForwarderService

object ForwarderServiceManager {
    fun Context.switchMsTtsForwarder() {
        if (MsTtsForwarderService.isRunning) {
            closeMsTtsForwarder()
        } else {
            startMsTtsForwarder()
        }
    }

    fun Context.switchSysTtsForwarder() {
        if (SysTtsForwarderService.isRunning) {
            closeSysTtsForwarder()
        } else {
            startSysTtsForwarder()
        }
    }

    fun Context.startMsTtsForwarder() {
        startService(Intent(this, MsTtsForwarderService::class.java))
    }

    fun closeMsTtsForwarder() {
        MsTtsForwarderService.instance?.close()
    }

    fun Context.startSysTtsForwarder() {
        startService(Intent(this, SysTtsForwarderService::class.java))
    }

    fun closeSysTtsForwarder() {
        SysTtsForwarderService.instance?.close()
    }
}