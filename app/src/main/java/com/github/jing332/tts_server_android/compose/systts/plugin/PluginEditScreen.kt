package com.github.jing332.tts_server_android.compose.systts.plugin

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewmodel.compose.viewModel
import com.github.jing332.tts_server_android.R
import com.github.jing332.tts_server_android.compose.LocalNavController
import com.github.jing332.tts_server_android.compose.codeeditor.CodeEditorScreen
import com.github.jing332.tts_server_android.compose.codeeditor.LoggerBottomSheet
import com.github.jing332.tts_server_android.compose.widgets.TextFieldDialog
import com.github.jing332.tts_server_android.conf.PluginConfig
import com.github.jing332.tts_server_android.constant.AppConst
import com.github.jing332.tts_server_android.data.entities.plugin.Plugin
import com.github.jing332.tts_server_android.model.speech.tts.PluginTTS
import com.github.jing332.tts_server_android.ui.view.AppDialogs.displayErrorDialog
import com.github.jing332.tts_server_android.utils.FileUtils.readAllText
import com.github.jing332.tts_server_android.utils.longToast
import io.github.rosemoe.sora.widget.CodeEditor

@Suppress("DEPRECATION")
@Composable
internal fun PluginEditScreen(
    plugin: Plugin,
    onSave: (Plugin) -> Unit,
    vm: PluginEditorViewModel = viewModel()
) {
    val navController = LocalNavController.current
    val context = LocalContext.current

    var codeEditor by remember { mutableStateOf<CodeEditor?>(null) }

//    @Suppress("NAME_SHADOWING")
//    var plugin by remember { mutableStateOf(plugin) }

    val ttsEditRet by navController.currentBackStackEntry?.savedStateHandle?.getStateFlow<PluginTTS?>(
        "ret",
        null
    )?.collectAsState() ?: rememberUpdatedState(null)
    LaunchedEffect(ttsEditRet) {
        ttsEditRet?.let {
            context.longToast("数据仅本次编辑生效")
            vm.updateTTS(it)
        }
    }

    val code by vm.codeLiveData.asFlow().collectAsState(initial = "")
    LaunchedEffect(codeEditor, code) {
        if (codeEditor != null && code.isNotEmpty())
            codeEditor?.setText(code)
    }

    LaunchedEffect(vm) {
        vm.init(plugin, context.assets.open("defaultData/plugin-azure.js").readAllText())
    }

    var showTextParamDialog by remember { mutableStateOf(false) }
    if (showTextParamDialog) {
        var sampleText by remember { mutableStateOf(PluginConfig.textParam.value) }
        TextFieldDialog(
            title = stringResource(id = R.string.set_sample_text_param),
            text = sampleText,
            onTextChange = { sampleText = it },
            onDismissRequest = { showTextParamDialog = false }) {
            PluginConfig.textParam.value = sampleText
        }
    }

    var showDebugLogger by remember { mutableStateOf(false) }
    if (showDebugLogger) {
        LoggerBottomSheet(
            logger = vm.pluginEngine.logger,
            onDismissRequest = { showDebugLogger = false }) {
            runCatching {
                vm.updateCode(codeEditor!!.text.toString())
                vm.debug()
            }.onFailure {
                context.displayErrorDialog(it)
            }
        }
    }

    val previewLauncher =
        rememberLauncherForActivityResult(contract = ActivityResultContracts.StartActivityForResult()) {
            it.data?.let { intent ->
                val tts = intent.getParcelableExtra<PluginTTS>(PluginPreviewActivity.KEY_DATA)
                if (tts == null) {
                    context.longToast("空返回值")
                    return@let
                }

                vm.updateTTS(tts)
            }
        }

    fun previewUi() {
        AppConst.localBroadcast.sendBroadcastSync(Intent(PluginPreviewActivity.ACTION_FINISH))
        vm.updateCode(codeEditor!!.text.toString())
        previewLauncher.launch(Intent(context, PluginPreviewActivity::class.java).apply {
            putExtra(PluginPreviewActivity.KEY_DATA, vm.pluginTTS)
        })
    }

    CodeEditorScreen(
        title = {
            Column {
                Text(
                    stringResource(id = R.string.plugin),
                    style = MaterialTheme.typography.titleLarge
                )
                Text(plugin.name, style = MaterialTheme.typography.bodyMedium)
            }
        },
        onBack = { navController.popBackStack() },
        onDebug = { showDebugLogger = true },

        onSave = {
            if (codeEditor != null) {
                vm.updateCode(codeEditor!!.text.toString())
                runCatching {
                    onSave(vm.pluginEngine.evalPluginInfo())
                    navController.popBackStack()
                }.onFailure {
                    context.displayErrorDialog(it)
                }
            }
        },
        onLongClickSave = { // 仅保存
            onSave(vm.plugin.copy(code = codeEditor!!.text.toString()))
            navController.popBackStack()
        },

        onRemoteAction = { name, _ ->
            when (name) {
                "ui" -> {
                    previewUi()
                }
            }
        },
        onUpdate = { codeEditor = it },
        onSaveFile = {
            "ttsrv-plugin-${vm.plugin.name}.js" to codeEditor!!.text.toString().toByteArray()
        },
        onLongClickMoreLabel = stringResource(id = R.string.plugin_preview_ui),
        onLongClickMore = { previewUi() }
    ) { dismiss ->
        DropdownMenuItem(
            text = { Text(stringResource(id = R.string.plugin_preview_ui)) },
            onClick = {
                dismiss()
                previewUi()
            },
            leadingIcon = {
                Icon(Icons.Default.Settings, null)
            }
        )

        DropdownMenuItem(
            text = { Text(stringResource(id = R.string.set_sample_text_param)) },
            onClick = {
                dismiss()
                showTextParamDialog = true
            },
            leadingIcon = {
                Icon(Icons.Default.TextFields, null)
            }
        )
    }
}