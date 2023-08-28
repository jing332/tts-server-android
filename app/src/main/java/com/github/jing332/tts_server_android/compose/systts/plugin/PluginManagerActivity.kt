package com.github.jing332.tts_server_android.compose.systts.plugin

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.github.jing332.tts_server_android.compose.LocalNavController
import com.github.jing332.tts_server_android.compose.theme.AppTheme
import com.github.jing332.tts_server_android.data.appDb
import com.github.jing332.tts_server_android.data.entities.plugin.Plugin
import com.github.jing332.tts_server_android.data.entities.systts.SystemTts
import com.github.jing332.tts_server_android.model.speech.tts.PluginTTS

class PluginManagerActivity : AppCompatActivity() {
    @Suppress("DEPRECATION")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            AppTheme {
                val navController = rememberNavController()
                CompositionLocalProvider(LocalNavController provides navController) {
                    NavHost(
                        navController = navController,
                        startDestination = NavRoutes.PluginManager.id
                    ) {
                        composable(NavRoutes.PluginManager.id) {
                            PluginManagerScreen { finish() }
                        }

                        composable(NavRoutes.PluginEdit.id) { stackEntry ->
                            val plugin: Plugin =
                                stackEntry.arguments?.getParcelable(NavRoutes.PluginEdit.KEY_DATA)
                                    ?: Plugin()

                            PluginEditScreen(plugin, onSave = { appDb.pluginDao.insert(it) })
                        }

                        composable(NavRoutes.PluginPreview.id) {
                            val tts =
                                remember { it.arguments?.getParcelable<PluginTTS>(NavRoutes.PluginPreview.KEY_DATA) }
                                    ?: return@composable
                            var systts by remember { mutableStateOf(SystemTts(tts = tts)) }
                            PluginPreviewScreen(systts = systts, onSysttsChange = { systts = it })
                        }
                    }
                }
            }
        }
    }
}