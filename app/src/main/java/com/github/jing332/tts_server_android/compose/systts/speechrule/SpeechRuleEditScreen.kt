package com.github.jing332.tts_server_android.compose.systts.speechrule

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import com.github.jing332.tts_server_android.conf.SpeechRuleConfig
import com.github.jing332.tts_server_android.data.entities.SpeechRule
import com.github.jing332.tts_server_android.ui.view.AppDialogs.displayErrorDialog
import com.github.jing332.tts_server_android.utils.FileUtils.readAllText
import io.github.rosemoe.sora.widget.CodeEditor

@Composable
internal fun SpeechRuleEditScreen(
    rule: SpeechRule,
    onSave: (SpeechRule) -> Unit,
    vm: SpeechRuleEditViewModel = viewModel()
) {
    val navController = LocalNavController.current
    val context = LocalContext.current
    var codeEditor by remember { mutableStateOf<CodeEditor?>(null) }

    LaunchedEffect(vm, codeEditor) {
        vm.init(rule, context.assets.open("defaultData/speech_rule.js").readAllText())
    }

    val code by vm.codeLiveData.asFlow().collectAsState(initial = "")
    LaunchedEffect(code, codeEditor) {
        if (codeEditor != null && code.isNotEmpty())
            codeEditor?.setText(code)
    }

    var showTextParamDialog by remember { mutableStateOf(false) }
    if (showTextParamDialog) {
        var textParam by remember { mutableStateOf(SpeechRuleConfig.textParam.value) }
        TextFieldDialog(
            title = stringResource(id = R.string.set_sample_text_param),
            text = textParam,
            onDismissRequest = { showTextParamDialog = false },
            onTextChange = { textParam = it },
            onConfirm = {
                SpeechRuleConfig.textParam.value = textParam
                showTextParamDialog = false
            }
        )
    }

    var showDebugLogger by remember { mutableStateOf(false) }
    if (showDebugLogger) {
        LoggerBottomSheet(logger = vm.logger, onDismissRequest = { showDebugLogger = false }) {
            runCatching {
                vm.code = codeEditor!!.text.toString()
                vm.debug(SpeechRuleConfig.textParam.value)
            }.onFailure {
                context.displayErrorDialog(it)
            }
        }
    }

    CodeEditorScreen(
        title = { Text(stringResource(id = R.string.speech_rule)) },
        onBack = { navController.popBackStack() },
        onDebug = { showDebugLogger = true },
        onSave = {
            runCatching {
                vm.code = codeEditor!!.text.toString()
                vm.evalRuleInfo()

                onSave(vm.speechRule)
                navController.popBackStack()
            }.onFailure {
                context.displayErrorDialog(it)
            }
        },
        onUpdate = { codeEditor = it },
        onSaveFile = {
            "ttsrv-speechRule-${vm.speechRule.name}.js" to codeEditor!!.text.toString().toByteArray()
        }
    ) { dismiss ->
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