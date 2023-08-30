package com.github.jing332.tts_server_android.compose.systts.speechrule

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.github.jing332.tts_server_android.compose.LocalNavController
import com.github.jing332.tts_server_android.compose.navigate
import com.github.jing332.tts_server_android.compose.theme.AppTheme
import com.github.jing332.tts_server_android.data.appDb
import com.github.jing332.tts_server_android.data.entities.SpeechRule

class SpeechRuleManagerActivity : AppCompatActivity() {
    private var jsCode by mutableStateOf("")

    @Suppress("DEPRECATION")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (intent != null) importJsCodeFromIntent(intent)

        setContent {
            AppTheme {
                val navController = rememberNavController()
                CompositionLocalProvider(LocalNavController provides navController) {
                    LaunchedEffect(jsCode) {
                        if (jsCode.isNotBlank()) {
                            navController.navigate(NavRoutes.SpeechRuleEdit.id, argsBuilder = {
                                putParcelable(
                                    NavRoutes.SpeechRuleEdit.KEY_DATA, SpeechRule(code = jsCode)
                                )
                            })
                        }
                    }

                    NavHost(
                        navController = navController,
                        startDestination = NavRoutes.SpeechRuleManager.id
                    ) {
                        composable(NavRoutes.SpeechRuleManager.id) {
                            SpeechRuleManagerScreen { finishAfterTransition() }
                        }

                        composable(NavRoutes.SpeechRuleEdit.id) {
                            val rule = remember {
                                it.arguments?.getParcelable(NavRoutes.SpeechRuleEdit.KEY_DATA)
                                    ?: SpeechRule()
                            }
                            SpeechRuleEditScreen(rule, onSave = {
                                appDb.speechRuleDao.insert(it)
                            })
                        }
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)

        if (intent != null) importJsCodeFromIntent(intent)
    }

    private fun importJsCodeFromIntent(intent: Intent) {
        jsCode = intent.getStringExtra("js") ?: return
        intent.removeExtra("js")
    }
}