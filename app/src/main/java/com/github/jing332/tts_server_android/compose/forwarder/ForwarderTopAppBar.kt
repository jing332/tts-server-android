package com.github.jing332.tts_server_android.compose.forwarder

import android.content.Intent
import android.webkit.CookieManager
import android.webkit.WebStorage
import android.webkit.WebView
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddBusiness
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.core.net.toUri
import com.github.jing332.tts_server_android.R
import com.github.jing332.tts_server_android.compose.nav.NavTopAppBar
import com.github.jing332.tts_server_android.compose.widgets.CheckedMenuItem
import com.github.jing332.tts_server_android.utils.toast

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ForwarderTopAppBar(
    title: @Composable () -> Unit,
    wakeLockEnabled: Boolean,
    onWakeLockEnabledChange: (Boolean) -> Unit,

    actions: @Composable ColumnScope.(onDismissRequest: () -> Unit) -> Unit = { },
    onClearWebData: (() -> Unit)? = null,
    onOpenWeb: () -> String,
    onAddDesktopShortCut: () -> Unit,
) {
    val context = LocalContext.current
    NavTopAppBar(
        title = title,
        actions = {
            IconButton(onClick = {
                val url = onOpenWeb.invoke()
                if (url.isNotEmpty()) {
                    context.startActivity(Intent(Intent.ACTION_VIEW, url.toUri()))
                }
            }) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_web),
                    contentDescription = stringResource(
                        id = R.string.open_web
                    )
                )
            }

            var showOptions by remember { mutableStateOf(false) }

            IconButton(onClick = { showOptions = true }) {
                Icon(Icons.Default.MoreVert, stringResource(id = R.string.more_options))

                DropdownMenu(expanded = showOptions, onDismissRequest = { showOptions = false }) {
                    CheckedMenuItem(
                        text = { Text(text = stringResource(id = R.string.wake_lock)) },
                        checked = wakeLockEnabled,
                        onClick = onWakeLockEnabledChange,
                        leadingIcon = {
                            Icon(Icons.Default.Lock, null)
                        }
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(id = R.string.clear_web_data)) },
                        onClick = {
                            showOptions = false
                            if (onClearWebData == null) {
                                WebView(context).apply {
                                    clearCache(true)
                                    clearFormData()
                                    clearSslPreferences()
                                }
                                CookieManager.getInstance().apply {
                                    removeAllCookies(null)
                                    flush()
                                }
                                WebStorage.getInstance().deleteAllData()
                                context.toast(R.string.cleared)
                            } else
                                onClearWebData.invoke()
                        },
                        leadingIcon = {
                            Icon(Icons.Default.CleaningServices, null)
                        }
                    )

                    DropdownMenuItem(
                        text = { Text(stringResource(id = R.string.desktop_shortcut)) },
                        onClick = {
                            showOptions = false
                            onAddDesktopShortCut()
                        },
                        leadingIcon = {
                            Icon(Icons.Default.AddBusiness, null)
                        }
                    )

                    actions { showOptions = false }
                }
            }

        }
    )
}