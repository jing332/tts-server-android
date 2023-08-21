package com.github.jing332.tts_server_android.compose.nav.systts

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.TextSnippet
import androidx.compose.material3.DrawerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.github.jing332.tts_server_android.R
import com.github.jing332.tts_server_android.compose.LocalNavController
import com.github.jing332.tts_server_android.compose.nav.LogScreen
import com.github.jing332.tts_server_android.compose.nav.NavTopAppBar
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun SystemTtsScreen(drawerState: DrawerState) {
    val navController = LocalNavController.current
    val pagerState = rememberPagerState { 2 }
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            NavTopAppBar(
                drawerState = drawerState,
                title = {
                    Text(stringResource(id = R.string.system_tts))
                }, actions = {
                    IconButton(onClick = {
                    }) {
                        Icon(Icons.Default.MoreVert, stringResource(id = R.string.more_options))
                    }
                }
            )
        },
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
    ) {
        Column(Modifier.padding(it)) {
            HorizontalPager(state = pagerState, userScrollEnabled = false) { index ->
                when (index) {
                    0 -> ListManagerScreen()
                    1 -> LogScreen()
                }
            }
        }
//        AndroidView(factory = )
    }
}