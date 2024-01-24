package com.github.jing332.tts_server_android.compose.nav

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.DrawerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.RichTooltip
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.github.jing332.tts_server_android.R
import com.github.jing332.tts_server_android.compose.LocalDrawerState
import com.github.jing332.tts_server_android.compose.widgets.AppTooltip
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NavTopAppBar(
    modifier: Modifier = Modifier,
    title: @Composable () -> Unit,
    drawerState: DrawerState = LocalDrawerState.current,
    navigationIcon: @Composable() (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
    windowInsets: WindowInsets = TopAppBarDefaults.windowInsets,
    colors: TopAppBarColors = TopAppBarDefaults.topAppBarColors(),
    scrollBehavior: TopAppBarScrollBehavior? = null
) {
    val scope = rememberCoroutineScope()
    TopAppBar(
        title = title,
        modifier = modifier,
        navigationIcon = {
            if (navigationIcon == null) {
                AppTooltip(tooltip = stringResource(id = R.string.nav_app_bar_open_drawer_description)) {
                    IconButton(onClick = {
                        scope.launch { drawerState.open() }
                    }) {
                        Icon(Icons.Default.Menu, it)
                    }
                }
            } else
                navigationIcon()
        },
        actions = actions,
        windowInsets = windowInsets,
        colors = colors,
        scrollBehavior = scrollBehavior
    )
}