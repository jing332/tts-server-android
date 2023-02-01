package com.github.jing332.tts_server_android.ui.forwarder.system

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.View
import android.webkit.*
import com.github.jing332.tts_server_android.App
import com.github.jing332.tts_server_android.help.SysTtsForwarderConfig
import com.github.jing332.tts_server_android.service.sysforwarder.SysTtsForwarderService
import com.github.jing332.tts_server_android.ui.base.BaseWebViewPageFragment

class SysTtsForwarderWebPage : BaseWebViewPageFragment() {
    private val mReceiver by lazy { MyReceiver() }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        App.localBroadcast.registerReceiver(
            mReceiver, IntentFilter(SysTtsForwarderService.ACTION_ON_STARTING)
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        webView.loadUrl("http://localhost:${SysTtsForwarderConfig.port}")
        if (SysTtsForwarderService.instance?.isRunning != true) {
            val i = Intent(requireContext(), SysTtsForwarderService::class.java)
            requireContext().startService(i)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        App.localBroadcast.unregisterReceiver(mReceiver)
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