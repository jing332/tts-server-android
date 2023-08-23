package com.github.jing332.tts_server_android.compose

import android.content.Context
import android.os.Bundle
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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.github.jing332.tts_server_android.BuildConfig
import com.github.jing332.tts_server_android.R
import com.github.jing332.tts_server_android.compose.nav.MsTtsForwarderScreen
import com.github.jing332.tts_server_android.compose.nav.NavRoutes
import com.github.jing332.tts_server_android.compose.nav.SystemTtsForwarderScreen
import com.github.jing332.tts_server_android.compose.nav.settings.SettingsScreen
import com.github.jing332.tts_server_android.compose.nav.systts.SystemTtsScreen
import com.github.jing332.tts_server_android.compose.theme.AppTheme
import com.github.jing332.tts_server_android.utils.longToast
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

val LocalNavController = compositionLocalOf<NavController> { error("No nav controller") }
val LocalDrawerState = compositionLocalOf<DrawerState> { error("No drawer state") }

fun Context.asAppCompatActivity(): AppCompatActivity {
    return this as? AppCompatActivity ?: error("Context is not an AppCompatActivity")
}

class ComposeMainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            AppTheme {
                NavHostScreen()
            }
        }
    }
}

@Composable
private fun NavHostScreen() {
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val snackbarState = remember { SnackbarHostState() }

    CompositionLocalProvider(
        LocalNavController provides navController,
        LocalDrawerState provides drawerState,
    ) {
        ModalNavigationDrawer(
            drawerState = drawerState,
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
                composable(NavRoutes.SystemTTS.id) { SystemTtsScreen(drawerState) }
                composable(NavRoutes.SystemTtsForwarder.id) {
                    SystemTtsForwarderScreen(
                        drawerState
                    )
                }
                composable(NavRoutes.MsTtsForwarder.id) { MsTtsForwarderScreen(drawerState) }
                composable(NavRoutes.Settings.id) { SettingsScreen(drawerState) }
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
    var selected by rememberSaveable { mutableStateOf(NavRoutes.SystemTTS.id) }

    @Composable
    fun drawerItem(
        icon: @Composable () -> Unit,
        targetScreen: NavRoutes,
        onClick: () -> Unit = {
            scope.launch {
                drawerState.close()
            }
            navController.navigate(targetScreen.id)
            selected = targetScreen.id
        }
    ) {
        val isSelected = selected == targetScreen.id
        NavigationDrawerItem(
            icon = { icon() },
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
            drawerItem(icon = { }, targetScreen = route)
            Spacer(modifier = Modifier.height(4.dp))
        }
    }
}