package com.github.jing332.tts_server_android.compose.nav.forwarder.systts

import android.content.IntentFilter
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.TextSnippet
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import com.github.jing332.tts_server_android.R
import com.github.jing332.tts_server_android.compose.nav.NavTopAppBar
import com.github.jing332.tts_server_android.compose.nav.forwarder.WebScreen
import com.github.jing332.tts_server_android.compose.widgets.LocalBroadcastReceiver
import com.github.jing332.tts_server_android.conf.SysttsForwarderConfig
import com.github.jing332.tts_server_android.service.forwarder.system.SysTtsForwarderService
import com.google.accompanist.web.rememberWebViewNavigator
import com.google.accompanist.web.rememberWebViewState
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun SystemTtsForwarderScreen(vm: SysttsForwarderViewModel = viewModel()) {
    val pages = remember { listOf(R.string.log, R.string.web) }
    val state = rememberPagerState { pages.size }
    val scope = rememberCoroutineScope()
    Scaffold(
        topBar = {
            NavTopAppBar(title = { Text(stringResource(id = R.string.forwarder_systts)) })
        },
        bottomBar = {
            NavigationBar {
                pages.forEachIndexed { index, strId ->
                    val selected = state.currentPage == index
                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            scope.launch {
                                state.animateScrollToPage(index)
                            }
                        },
                        icon = {
                            if (index == 0)
                                Icon(Icons.Default.TextSnippet, null)
                            else
                                Icon(painter = painterResource(R.drawable.ic_web), null)
                        },
                        alwaysShowLabel = false,
                        label = {
                            Text(stringResource(id = strId))
                        }
                    )
                }
            }
        }) { paddingValues ->
        HorizontalPager(
            modifier = Modifier.padding(paddingValues),
            state = state,
            userScrollEnabled = false
        ) {
            when (it) {
                0 -> ForwarderConfigScreen(Modifier.fillMaxSize(), vm)
                1 -> {
                    fun url() = "http://localhost:${SysttsForwarderConfig.port.value}"

                    val webState = rememberWebViewState(url = url())
                    val navigator = rememberWebViewNavigator()
                    LocalBroadcastReceiver(intentFilter = IntentFilter(SysTtsForwarderService.ACTION_ON_STARTING)) {
                        navigator.loadUrl(url())
                    }

                    WebScreen(
                        modifier = Modifier.fillMaxSize(),
                        state = webState,
                        navigator = navigator
                    )
                }
            }
        }
    }
}