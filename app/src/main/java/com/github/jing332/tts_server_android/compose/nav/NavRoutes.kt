package com.github.jing332.tts_server_android.compose.nav

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.github.jing332.tts_server_android.R

sealed class NavRoutes(
    val id: String,
    @StringRes val strId: Int,
    val icon: @Composable () -> Unit = {},
) {
    companion object {
        val routes by lazy {
            listOf(
                SystemTTS,
                SystemTtsForwarder,
                MsTtsForwarder,
                Settings
            )
        }
    }

    data object SystemTTS : NavRoutes("system_tts", R.string.system_tts, icon = {
        Icon(modifier = Modifier.size(24.dp), painter = painterResource(id = R.drawable.ic_tts), contentDescription  = null)
    })

    data object SystemTtsForwarder :
        NavRoutes("system_tts_forwarder", R.string.forwarder_systts, icon = {
            Icon(modifier = Modifier.size(24.dp), painter = painterResource(id = R.drawable.ic_tts), contentDescription = null)
        })

    data object MsTtsForwarder : NavRoutes("ms_tts_forwarder", R.string.forwarder_ms, icon = {
        Icon(painter = painterResource(id = R.drawable.ic_microsoft), null)
    })

    data object Settings : NavRoutes("settings", R.string.settings, icon = {
        Icon(Icons.Default.Settings, null)
    })

    // =============
    data object TtsEdit : NavRoutes("tts_edit", 0) {
        const val DATA = "data"
    }
}