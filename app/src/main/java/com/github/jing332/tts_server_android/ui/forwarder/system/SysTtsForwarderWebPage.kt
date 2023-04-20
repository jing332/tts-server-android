package com.github.jing332.tts_server_android.ui.forwarder.system

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.View
import android.webkit.*
import com.github.jing332.tts_server_android.constant.AppConst
import com.github.jing332.tts_server_android.help.config.SysTtsForwarderConfig
import com.github.jing332.tts_server_android.service.forwarder.system.SysTtsForwarderService
import com.github.jing332.tts_server_android.ui.base.BaseWebViewPageFragment

class SysTtsForwarderWebPage : BaseWebViewPageFragment() {
    private val mReceiver = MyReceiver()
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        AppConst.localBroadcast.registerReceiver(
            mReceiver, IntentFilter(SysTtsForwarderService.ACTION_ON_STARTING)
        )

        webView.loadUrl("http://localhost:${SysTtsForwarderConfig.port}")
    }

    override fun onStart() {
        super.onStart()
        if (SysTtsForwarderService.instance?.isRunning != true) {
            val i = Intent(requireContext(), SysTtsForwarderService::class.java)
            requireContext().startService(i)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        AppConst.localBroadcast.unregisterReceiver(mReceiver)
    }


    inner class MyReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                SysTtsForwarderService.ACTION_ON_STARTING -> {
                    webView.reload()
                }
            }
        }
    }
}