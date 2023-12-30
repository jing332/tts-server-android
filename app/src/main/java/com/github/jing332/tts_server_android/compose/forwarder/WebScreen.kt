package com.github.jing332.tts_server_android.compose.forwarder

import android.annotation.SuppressLint
import android.content.Intent
import android.webkit.JsResult
import android.webkit.WebResourceRequest
import android.webkit.WebView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pullrefresh.PullRefreshIndicator
import androidx.compose.material3.pullrefresh.pullRefresh
import androidx.compose.material3.pullrefresh.rememberPullRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.github.jing332.tts_server_android.utils.longToast
import com.google.accompanist.web.AccompanistWebChromeClient
import com.google.accompanist.web.AccompanistWebViewClient
import com.google.accompanist.web.LoadingState
import com.google.accompanist.web.WebView
import com.google.accompanist.web.WebViewNavigator
import com.google.accompanist.web.WebViewState
import com.google.accompanist.web.rememberWebViewNavigator
import com.google.accompanist.web.rememberWebViewState

@SuppressLint("SetJavaScriptEnabled")
@Composable
internal fun WebScreen(
    modifier: Modifier,
    url: String = "",
    state: WebViewState = rememberWebViewState(url),
    navigator: WebViewNavigator = rememberWebViewNavigator(),
) {
    var showAlertDialog by remember { mutableStateOf<Triple<String, String, JsResult>?>(null) }
    if (showAlertDialog != null) {
        val webUrl = showAlertDialog!!.first
        val msg = showAlertDialog!!.second
        val result = showAlertDialog!!.third
        AlertDialog(onDismissRequest = {
            result.cancel()
            showAlertDialog = null
        },
            title = { Text(webUrl) },
            text = { Text(msg) },
            confirmButton = {
                TextButton(
                    onClick = {
                        result.confirm()
                        showAlertDialog = null
                    }) {
                    Text(stringResource(id = android.R.string.ok))
                }
            }, dismissButton = {
                result.cancel()
                showAlertDialog = null
            })
    }

    val context = LocalContext.current
    val chromeClient = remember {
        object : AccompanistWebChromeClient() {
            override fun onJsConfirm(
                view: WebView?,
                url: String?,
                message: String?,
                result: JsResult?
            ): Boolean {
                if (result == null) return false
                showAlertDialog = Triple(url ?: "", message ?: "", result)
                return true
            }

            override fun onJsAlert(
                view: WebView?,
                url: String?,
                message: String?,
                result: JsResult?
            ): Boolean {
                if (result == null) return false
                showAlertDialog = Triple(url ?: "", message ?: "", result)

                return true
            }
        }
    }

    val client = remember {
        object : AccompanistWebViewClient() {
            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
            ): Boolean {
                kotlin.runCatching {
                    if (request?.url?.scheme?.startsWith("http") == false) {
                        val intent = Intent(Intent.ACTION_VIEW, request.url)
                        context.startActivity(Intent.createChooser(intent, request.url.toString()))
                        return true
                    }
                }.onFailure {
                    context.longToast("跳转APP失败: ${request?.url}")
                }

                return super.shouldOverrideUrlLoading(view, request)
            }
        }
    }

    Column(modifier = modifier) {
        val process =
            if (state.loadingState is LoadingState.Loading) (state.loadingState as LoadingState.Loading).progress else 0f

        if (process > 0)
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth(),
                progress = process
            )

        var lastTitle by remember { mutableStateOf("") }
        val refreshState = rememberPullRefreshState(refreshing = state.isLoading, onRefresh = {
            navigator.reload()
        })
        Text(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .pullRefresh(refreshState),
            text = state.pageTitle?.apply { lastTitle = this } ?: lastTitle,
            maxLines = 1,
            style = MaterialTheme.typography.titleMedium,
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            WebView(
                modifier = Modifier.fillMaxSize(),
                state = state,
                navigator = navigator,
                onCreated = {
                    it.settings.javaScriptEnabled = true
                },
                client = client,
                chromeClient = chromeClient,
            )

            Column(Modifier.fillMaxWidth()) {
                PullRefreshIndicator(
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    refreshing = refreshState.refreshing,
                    state = refreshState
                )
            }
        }
    }
}