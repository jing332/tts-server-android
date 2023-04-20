package com.github.jing332.tts_server_android.ui.forwarder.ms

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.View
import android.webkit.*
import com.github.jing332.tts_server_android.app
import com.github.jing332.tts_server_android.constant.AppConst
import com.github.jing332.tts_server_android.service.forwarder.ms.TtsIntentService
import com.github.jing332.tts_server_android.ui.base.BaseWebViewPageFragment


class MsTtsForwarderWebPage : BaseWebViewPageFragment() {
    private val mReceiver by lazy { MyReceiver() }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        AppConst.localBroadcast.registerReceiver(
            mReceiver,
            IntentFilter(TtsIntentService.ACTION_ON_STARTED)
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val port = TtsIntentService.instance?.cfg?.port ?: 1233
        webView.loadUrl("http://localhost:${port}")
    }

    override fun onStart() {
        super.onStart()
        if (TtsIntentService.instance?.isRunning != true) {
            val i = Intent(app, TtsIntentService::class.java)
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
                TtsIntentService.ACTION_ON_STARTED -> {
                    webView.reload()
                }
            }
        }
    }
}