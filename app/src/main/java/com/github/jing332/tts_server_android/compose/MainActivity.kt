@file:Suppress("DEPRECATION")

package com.github.jing332.tts_server_android.compose

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.SystemClock
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowCircleUp
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.navigation.NavController
import androidx.navigation.NavDeepLinkRequest
import androidx.navigation.NavDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavOptions
import androidx.navigation.Navigator
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.github.jing332.tts_server_android.BuildConfig
import com.github.jing332.tts_server_android.R
import com.github.jing332.tts_server_android.ShortCuts
import com.github.jing332.tts_server_android.compose.systts.list.edit.TtsEditContainerScreen
import com.github.jing332.tts_server_android.compose.systts.nav.NavRoutes
import com.github.jing332.tts_server_android.compose.systts.nav.forwarder.ms.MsTtsForwarderScreen
import com.github.jing332.tts_server_android.compose.systts.nav.forwarder.systts.SystemTtsForwarderScreen
import com.github.jing332.tts_server_android.compose.systts.nav.settings.SettingsScreen
import com.github.jing332.tts_server_android.compose.systts.nav.systts.SystemTtsScreen
import com.github.jing332.tts_server_android.compose.theme.AppTheme
import com.github.jing332.tts_server_android.conf.AppConfig
import com.github.jing332.tts_server_android.constant.AppConst
import com.github.jing332.tts_server_android.data.appDb
import com.github.jing332.tts_server_android.data.entities.systts.SystemTts
import com.github.jing332.tts_server_android.model.speech.tts.ITextToSpeechEngine
import com.github.jing332.tts_server_android.ui.AppHelpDocumentActivity
import com.github.jing332.tts_server_android.utils.MyTools.killBattery
import com.github.jing332.tts_server_android.utils.clone
import com.github.jing332.tts_server_android.utils.longToast
import com.github.jing332.tts_server_android.utils.toast
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

val LocalNavController = compositionLocalOf<NavHostController> { error("No nav controller") }
val LocalDrawerState = compositionLocalOf<DrawerState> { error("No drawer state") }

fun Context.asAppCompatActivity(): AppCompatActivity {
    return this as? AppCompatActivity ?: error("Context is not an AppCompatActivity")
}

private var updateCheckTrigger by mutableStateOf(false)

class ComposeMainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        ShortCuts.buildShortCuts(this)
        setContent {
            AppTheme {
                var showAutoCheckUpdaterDialog by remember { mutableStateOf(false) }
                if (showAutoCheckUpdaterDialog) {
                    val fromUser by remember { mutableStateOf(updateCheckTrigger) }
                    AutoUpdateCheckerDialog(fromUser) {
                        showAutoCheckUpdaterDialog = false
                        updateCheckTrigger = false
                    }
                }

                LaunchedEffect(Unit) {
                    showAutoCheckUpdaterDialog = AppConfig.isAutoCheckUpdateEnabled.value
                }

                LaunchedEffect(updateCheckTrigger) {
                    if (updateCheckTrigger) showAutoCheckUpdaterDialog = true
                }

                MainScreen { finish() }
            }
        }
    }
}


@Composable
private fun MainScreen(finish: () -> Unit) {
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
//    val snackbarState = remember { SnackbarHostState() }
    val gesturesEnabled = !drawerState.isClosed
    val context = LocalContext.current

    var lastBackDownTime by remember { mutableLongStateOf(0L) }
    BackHandler(enabled = drawerState.isClosed) {
        val duration = 2000
        SystemClock.elapsedRealtime().let {
            if (it - lastBackDownTime <= duration) {
                finish()
            } else {
                lastBackDownTime = it
                context.toast(R.string.app_down_again_to_exit)
            }
        }
    }
    CompositionLocalProvider(
        LocalNavController provides navController,
        LocalDrawerState provides drawerState,
    ) {
        ModalNavigationDrawer(
            drawerState = drawerState,
            gesturesEnabled = gesturesEnabled,
            drawerContent = {
                NavDrawerContent(
                    navController,
                    drawerState,
                    Modifier
                        .fillMaxHeight()
                        .width(300.dp)
                        .background(MaterialTheme.colorScheme.background)
                        .padding(12.dp)
                )
            }) {
            NavHost(
                navController = navController,
                startDestination = NavRoutes.SystemTTS.id
            ) {
                composable(NavRoutes.SystemTTS.id) { SystemTtsScreen() }
                composable(NavRoutes.SystemTtsForwarder.id) {
                    SystemTtsForwarderScreen()
                }
                composable(NavRoutes.MsTtsForwarder.id) { MsTtsForwarderScreen() }
                composable(NavRoutes.Settings.id) { SettingsScreen(drawerState) }

                composable(NavRoutes.TtsEdit.id) { stackEntry ->
                    val systts: SystemTts =
                        stackEntry.arguments?.getParcelable(NavRoutes.TtsEdit.DATA)
                            ?: return@composable
                    var stateSystts by rememberSaveable {
                        mutableStateOf(systts.run {
                            if (tts.locale.isBlank()) {
                                copy(
                                    tts = tts.clone<ITextToSpeechEngine>()!!
                                        .apply { locale = AppConst.localeCode }
                                )
                            } else
                                this
                        })
                    }
                    TtsEditContainerScreen(
                        modifier = Modifier
                            .fillMaxSize(),
                        systts = stateSystts,
                        onSysttsChange = {
                            stateSystts = it
                            println("UpdateSystemTTS: $it")
                        },
                        onSave = {
                            navController.popBackStack()
                            appDb.systemTtsDao.insertTts(stateSystts)
                        },
                        onCancel = {
                            navController.popBackStack()
                        }
                    )
                }
            }
        }

    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun NavDrawerContent(
    navController: NavHostController,
    drawerState: DrawerState,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    BackHandler(enabled = drawerState.isOpen) {
        scope.launch { drawerState.close() }
    }

    @Composable
    fun DrawerItem(
        selected: Boolean = false,
        icon: @Composable () -> Unit,
        label: @Composable () -> Unit,
        onClick: () -> Unit
    ) {
        NavigationDrawerItem(
            modifier = Modifier.padding(vertical = 2.dp),
            icon = icon,
            label = label,
            selected = selected,
            onClick = onClick,
            colors = NavigationDrawerItemDefaults.colors(unselectedContainerColor = Color.Transparent)
        )
    }


    @Composable
    fun NavDrawerItem(
        icon: @Composable () -> Unit,
        targetScreen: NavRoutes,
        onClick: () -> Unit = {
            scope.launch { drawerState.close() }
            navController.navigateSingleTop(targetScreen.id, popUpToMain = true)
        }
    ) {
        val isSelected = navController.currentDestination?.route == targetScreen.id
        DrawerItem(
            icon = icon,
            label = { Text(text = stringResource(id = targetScreen.strId)) },
            selected = isSelected,
            onClick = onClick,
        )
    }

    Column(modifier = modifier.verticalScroll(rememberScrollState())) {
        Spacer(modifier = Modifier.height(24.dp))
        val context = LocalContext.current
        val clipboardManager = LocalClipboardManager.current
//        val snackBarState = LocalSnackbarHostState.current

        var isBuildTimeExpanded by remember { mutableStateOf(false) }
        val versionNameText = remember {
            "${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})"
        }
        Column(modifier = Modifier
            .padding(end = 4.dp)
            .clip(MaterialTheme.shapes.small)
            .combinedClickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = rememberRipple(bounded = true),
                onClick = {
                    isBuildTimeExpanded = !isBuildTimeExpanded
                },
                onLongClick = {
                    clipboardManager.setText(AnnotatedString(versionNameText))
                    context.longToast(R.string.copied)
                }
            )) {
            Row {
                Image(
                    painterResource(id = R.mipmap.ic_launcher_round),
                    stringResource(id = R.string.app_name),
                    modifier = Modifier.size(64.dp)
                )
                Column(modifier = Modifier.align(Alignment.CenterVertically)) {
                    Text(
                        text = stringResource(id = R.string.app_name),
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = versionNameText,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            AnimatedVisibility(visible = isBuildTimeExpanded) {
                Text(
                    text = SimpleDateFormat(
                        "yyyy-MM-dd HH:mm:ss", Locale.getDefault()
                    ).format(BuildConfig.BUILD_TIME * 1000),
                    modifier = Modifier.padding(4.dp)
                )
            }
        }
        HorizontalDivider(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp, horizontal = 4.dp)
        )

        for (route in NavRoutes.routes) {
            NavDrawerItem(icon = route.icon, targetScreen = route)
        }

        HorizontalDivider(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp, horizontal = 4.dp)
        )

        DrawerItem(
            icon = { Icon(Icons.Default.BatteryFull, null) },
            label = { Text(stringResource(id = R.string.battery_optimization_whitelist)) },
            selected = false,
            onClick = { context.killBattery() }
        )

        DrawerItem(
            icon = { Icon(Icons.Default.ArrowCircleUp, null) },
            label = { Text(stringResource(id = R.string.check_update)) },
            selected = false,
            onClick = {
                scope.launch {
                    drawerState.close()
                    updateCheckTrigger = true
                }
            },
        )

        DrawerItem(
            icon = { Icon(Icons.Default.HelpOutline, null) },
            label = { Text(stringResource(id = R.string.app_help_document)) },
            selected = false,
            onClick = {
                context.startActivity(Intent(context, AppHelpDocumentActivity::class.java).apply {
                    action = Intent.ACTION_VIEW
                })
            },
        )

        var showAboutDialog by remember { mutableStateOf(false) }
        if (showAboutDialog)
            AboutDialog { showAboutDialog = false }

        DrawerItem(
            icon = { Icon(Icons.Default.Info, null) },
            label = { Text(stringResource(id = R.string.about)) },
            selected = false,
            onClick = { showAboutDialog = true }
        )
    }
}

/*
* 可传递 Bundle 到 Navigation
* */
@SuppressLint("RestrictedApi")
fun NavController.navigate(
    route: String,
    args: Bundle,
    navOptions: NavOptions? = null,
    navigatorExtras: Navigator.Extras? = null
) {
    val routeLink = NavDeepLinkRequest
        .Builder
        .fromUri(NavDestination.createRoute(route).toUri())
        .build()

    val deepLinkMatch = graph.matchDeepLink(routeLink)
    if (deepLinkMatch != null) {
        val destination = deepLinkMatch.destination
        val id = destination.id
        navigate(id, args, navOptions, navigatorExtras)
    } else {
        navigate(route, navOptions, navigatorExtras)
    }
}

/**
 * 单例并清空其他栈
 */
fun NavHostController.navigateSingleTop(
    route: String,
    args: Bundle? = null,
    popUpToMain: Boolean = false
) {
    val navController = this
    val navOptions = NavOptions.Builder()
        .setLaunchSingleTop(true)
        .apply {
            if (popUpToMain) setPopUpTo(
                navController.graph.startDestinationId,
                inclusive = false,
                saveState = true
            )
        }
        .setRestoreState(true)
        .build()
    if (args == null)
        navController.navigate(route, navOptions)
    else
        navController.navigate(route, args, navOptions)
}