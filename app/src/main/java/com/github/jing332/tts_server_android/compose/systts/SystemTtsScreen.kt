package com.github.jing332.tts_server_android.compose.systts

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.TextSnippet
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.github.jing332.tts_server_android.R
import com.github.jing332.tts_server_android.compose.systts.list.ListManagerScreen
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SystemTtsScreen( vm: SystemTtsViewModel = viewModel()) {
    val pagerState = rememberPagerState { 2 }
    val scope = rememberCoroutineScope()

    Scaffold(
        bottomBar = {
            NavigationBar {
                (0 until 2).forEach {
                    when (it) {
                        0 -> {
                            val isSelected = pagerState.currentPage == it
                            NavigationBarItem(
                                selected = isSelected,
                                onClick = {
                                    scope.launch {
                                        pagerState.animateScrollToPage(it)
                                    }
                                },
                                icon = {
                                    Icon(
                                        painterResource(id = R.drawable.ic_config),
                                        null,
                                        Modifier.size(24.dp)
                                    )
                                },
                                label = {
                                    Text(stringResource(id = R.string.config))
                                }
                            )
                        }

                        1 -> {
                            val isSelected = pagerState.currentPage == it
                            NavigationBarItem(
                                selected = isSelected,
                                onClick = {
                                    scope.launch {
                                        pagerState.animateScrollToPage(it)
                                    }
                                },
                                icon = {
                                    Icon(Icons.Default.TextSnippet, null)
                                },
                                label = {
                                    Text(stringResource(id = R.string.log))
                                }
                            )
                        }
                    }

                }
            }
        }
    ) { paddingValues ->
        HorizontalPager(
            modifier = Modifier
                .padding(bottom = paddingValues.calculateBottomPadding())
                .fillMaxSize(),
            state = pagerState,
            userScrollEnabled = false
        ) { index ->
            when (index) {
                0 -> ListManagerScreen()
                1 -> TtsLogScreen()
            }
        }
    }
}