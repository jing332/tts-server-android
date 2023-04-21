package com.github.jing332.tts_server_android.ui.forwarder

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.view.View
import android.webkit.*
import androidx.fragment.app.Fragment
import by.kirich1409.viewbindingdelegate.viewBinding
import com.github.jing332.tts_server_android.R
import com.github.jing332.tts_server_android.constant.AppConst
import com.github.jing332.tts_server_android.databinding.WebPageFragmentBinding
import com.github.jing332.tts_server_android.utils.toast

abstract class AbsForwarderWebPageFragment(val startedAction: String) :
    Fragment(R.layout.web_page_fragment) {
    private val binding by viewBinding(WebPageFragmentBinding::bind)
    val webView: WebView get() = binding.webView
    private val mReceiver = MyReceiver()

    abstract val port: Int
    abstract val isServiceRunning: Boolean

    @SuppressLint("SetJavaScriptEnabled")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        AppConst.localBroadcast.registerReceiver(mReceiver, IntentFilter(startedAction))

        binding.swipeRefresh.setOnRefreshListener {
            binding.webView.reload()
        }
        binding.webView.settings.javaScriptEnabled = true
        binding.webView.settings.domStorageEnabled = true

        binding.webView.webChromeClient = WebChromeClient()
        binding.webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView, url: String) {
                binding.swipeRefresh.isRefreshing = false
            }

            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
            ): Boolean {
                kotlin.runCatching {
                    if (request?.url?.scheme?.startsWith("http") == false) {
                        val intent = Intent(Intent.ACTION_VIEW, request.url)
                        startActivity(intent)
                        return true
                    }
                }.onFailure {
                    toast("跳转APP失败！")
                }

                return super.shouldOverrideUrlLoading(view, request)
            }
        }

        webView.loadUrl("http://localhost:$port")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        AppConst.localBroadcast.unregisterReceiver(mReceiver)
        binding.webView.clearHistory()
        binding.webView.destroy()
    }

    @Suppress("DEPRECATION")
    override fun onResume() {
        super.onResume()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            binding.webView.settings.forceDark =
                if (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK ==
                    Configuration.UI_MODE_NIGHT_YES
                ) WebSettings.FORCE_DARK_ON
                else WebSettings.FORCE_DARK_OFF
        }
    }

    fun onBackKeyDown(): Boolean {
        if (binding.webView.canGoBack()) {
            binding.webView.goBack()
            return true
        }

        return false
    }


    inner class MyReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == startedAction) {
                webView.reload()
            }
        }

    }
}