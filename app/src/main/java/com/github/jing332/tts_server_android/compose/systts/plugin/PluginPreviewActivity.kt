package com.github.jing332.tts_server_android.compose.systts.plugin

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.github.jing332.tts_server_android.R
import com.github.jing332.tts_server_android.compose.LocalNavController
import com.github.jing332.tts_server_android.compose.systts.list.edit.ui.PluginTtsUI
import com.github.jing332.tts_server_android.compose.theme.AppTheme
import com.github.jing332.tts_server_android.data.entities.systts.SystemTts
import com.github.jing332.tts_server_android.model.speech.tts.PluginTTS

@Suppress("DEPRECATION")
class PluginPreviewActivity : AppCompatActivity() {
    companion object {
        const val KEY_DATA = "data"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val tts = intent.getParcelableExtra<PluginTTS>(KEY_DATA)
        if (tts == null) {
            finish()
            return
        }
        setContent {
            AppTheme {
                var systts by remember { mutableStateOf(SystemTts(tts = tts)) }
                PluginPreviewScreen(systts = systts, onSysttsChange = { systts = it }, onSave = {
                    intent.putExtra(KEY_DATA, systts.tts)
                    setResult(RESULT_OK, intent)
                    finish()
                })
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun PluginPreviewScreen(
        systts: SystemTts,
        onSysttsChange: (SystemTts) -> Unit,
        onSave: () -> Unit
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                TopAppBar(title = { Text(stringResource(id = R.string.plugin_preview_ui)) },
                    navigationIcon = {
                        IconButton(onClick = { finish() }) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                stringResource(id = R.string.nav_back)
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = {
                            onSave()
                        }) {
                            Icon(Icons.Default.Save, stringResource(id = R.string.save))
                        }
                    }
                )
            }) { paddingValues ->
            PluginTtsUI().EditContentScreen(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState()),
                systts = systts,
                onSysttsChange = onSysttsChange,
                showBasicInfo = false
            )
        }
    }
}