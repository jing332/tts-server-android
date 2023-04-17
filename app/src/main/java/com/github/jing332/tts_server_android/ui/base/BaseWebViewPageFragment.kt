package com.github.jing332.tts_server_android.ui.base

import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.*
import androidx.fragment.app.Fragment
import com.github.jing332.tts_server_android.databinding.WebPageFragmentBinding
import com.github.jing332.tts_server_android.utils.toast

open class BaseWebViewPageFragment() : Fragment() {
    private val binding: WebPageFragmentBinding by lazy {
        WebPageFragmentBinding.inflate(layoutInflater)
    }

    val webView: WebView
        get() = binding.webView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return binding.root
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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
                try {
                    if (request?.url?.scheme?.startsWith("http") == false) {
                        val intent = Intent(Intent.ACTION_VIEW, request.url)
                        startActivity(intent)
                        return true
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    toast("跳转APP失败！")
                }
                return super.shouldOverrideUrlLoading(view, request)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.webView.clearHistory()
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
}