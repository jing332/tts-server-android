package com.github.jing332.tts_server_android.compose.plugin

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.CompositionLocalProvider
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.github.jing332.tts_server_android.compose.LocalNavController
import com.github.jing332.tts_server_android.compose.theme.AppTheme
import com.github.jing332.tts_server_android.data.appDb
import com.github.jing332.tts_server_android.data.entities.plugin.Plugin

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
                                    ?: return@composable

                            PluginEditScreen(plugin, onSave = { appDb.pluginDao.insert(it) })
                        }

                        composable(NavRoutes.PluginPreview.id) {
//                            PluginPreviewScreen { finish() }
                        }
                    }
                }
            }
        }
    }
}