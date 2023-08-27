package com.github.jing332.tts_server_android.compose.plugin

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.github.jing332.tts_server_android.R
import com.github.jing332.tts_server_android.compose.LocalNavController
import com.github.jing332.tts_server_android.compose.nav.systts.edit.ui.PluginTtsUI
import com.github.jing332.tts_server_android.data.entities.systts.SystemTts

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun PluginPreviewScreen(systts: SystemTts, onSysttsChange: (SystemTts) -> Unit) {
    val navController = LocalNavController.current
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(title = { stringResource(id = R.string.plugin_preview_ui) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, stringResource(id = R.string.nav_back))
                    }
                },
                actions = {
                    IconButton(onClick = {
                        navController.previousBackStackEntry!!.savedStateHandle["ret"] = systts.tts
                        navController.popBackStack()
                    }) {
                        Icon(Icons.Default.Save, stringResource(id = R.string.save))
                    }
                }
            )
        }) { paddingValues ->
        PluginTtsUI().EditContentScreen(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            systts = systts,
            onSysttsChange = onSysttsChange,
            showBasicInfo = false
        )
    }
}