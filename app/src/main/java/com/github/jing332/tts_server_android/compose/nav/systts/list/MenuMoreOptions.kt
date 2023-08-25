package com.github.jing332.tts_server_android.compose.nav.systts.list

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Input
import androidx.compose.material.icons.filled.ManageSearch
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Output
import androidx.compose.material.icons.filled.SmartDisplay
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.github.jing332.tts_server_android.R
import com.github.jing332.tts_server_android.compose.asAppCompatActivity
import com.github.jing332.tts_server_android.compose.nav.systts.GlobalAudioParamsDialog
import com.github.jing332.tts_server_android.compose.nav.systts.InternalPlayerDialog
import com.github.jing332.tts_server_android.compose.widgets.CheckedMenuItem
import com.github.jing332.tts_server_android.conf.SystemTtsConfig
import com.github.jing332.tts_server_android.ui.systts.ConfigExportBottomSheetFragment
import com.github.jing332.tts_server_android.ui.systts.list.ImportConfigBottomSheetFragment
import com.github.jing332.tts_server_android.ui.systts.plugin.PluginManagerActivity
import com.github.jing332.tts_server_android.ui.systts.replace.ReplaceManagerActivity
import com.github.jing332.tts_server_android.ui.systts.speech_rule.SpeechRuleManagerActivity
import com.github.jing332.tts_server_android.utils.startActivity

@Composable
internal fun MenuMoreOptions(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    vm: ListManagerViewModel
) {
    var showInternalPlayerDialog by remember { mutableStateOf(false) }
    if (showInternalPlayerDialog)
        InternalPlayerDialog {
            showInternalPlayerDialog = false
        }

    var showAudioParamsDialog by remember { mutableStateOf(false) }
    if (showAudioParamsDialog)
        GlobalAudioParamsDialog {
            showAudioParamsDialog = false
        }

    val context = LocalContext.current
    val activity = remember { context.asAppCompatActivity() }
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismissRequest
    ) {

        var isSplit by remember { SystemTtsConfig.isSplitEnabled }
        CheckedMenuItem(
            text = { Text(stringResource(id = R.string.systts_split_long_sentences)) },
            checked = isSplit,
            onClick = {
                isSplit = it
            },
            leadingIcon = {
                Icon(Icons.Default.ContentCut, null)
            }
        )

        var isMultiVoice by remember { SystemTtsConfig.isMultiVoiceEnabled }
        CheckedMenuItem(
            text = { Text(stringResource(id = R.string.systts_multi_voice_option)) },
            checked = isMultiVoice,
            onClick = {
                isMultiVoice = it
            },
            leadingIcon = {
                Icon(Icons.Default.Group, null)
            },
        )
        HorizontalDivider()

        var isInternalPlayer by remember { SystemTtsConfig.isInternalPlayerEnabled }
        CheckedMenuItem(
            text = { Text(stringResource(id = R.string.systts_use_internal_audio_player)) },
            checked = isInternalPlayer,
            onClick = { showInternalPlayerDialog = true },
            onClickCheckBox = { isInternalPlayer = it },
            leadingIcon = {
                Icon(Icons.Default.SmartDisplay, null)
            }
        )

        DropdownMenuItem(
            text = { Text(stringResource(id = R.string.audio_params)) },
            onClick = { showAudioParamsDialog = true },
            leadingIcon = {
                Icon(Icons.Default.Speed, null)
            }
        )

        HorizontalDivider()
        DropdownMenuItem(
            text = { Text(stringResource(id = R.string.speech_rule_manager)) },
            onClick = {
                onDismissRequest()
                context.startActivity(SpeechRuleManagerActivity::class.java)
            },
            leadingIcon = {
                Icon(Icons.Default.MenuBook, null)
            }
        )

        DropdownMenuItem(
            text = { Text(stringResource(id = R.string.plugin_manager)) },
            onClick = {
                onDismissRequest()
                context.startActivity(PluginManagerActivity::class.java)
            },
            leadingIcon = {
                Icon(painterResource(id = R.drawable.ic_plugin), null)
            }
        )

        CheckedMenuItem(
            text = { Text(stringResource(id = R.string.replace_rule_manager)) },
            checked = SystemTtsConfig.isReplaceEnabled.value,
            onClick = {
                onDismissRequest()
                context.startActivity(ReplaceManagerActivity::class.java)
            },
            onClickCheckBox = {
                SystemTtsConfig.isReplaceEnabled.value = it
            },
            leadingIcon = {
                Icon(Icons.Default.ManageSearch, null)
            }
        )

        HorizontalDivider()
        DropdownMenuItem(text = {
            Text(stringResource(id = R.string.import_config))
        }, onClick = {
            val fragment = ImportConfigBottomSheetFragment()
            fragment.show(
                activity.supportFragmentManager,
                ImportConfigBottomSheetFragment.TAG
            )
        }, leadingIcon = {
            Icon(Icons.Default.Input, null)
        })

        DropdownMenuItem(text = {
            Text(stringResource(id = R.string.export_config))
        }, onClick = {
            val fragment = ConfigExportBottomSheetFragment(
                onGetConfig = { vm.export().getOrElse { it.toString() } },
                onGetName = { "ttsrv-list.json" }
            )
            fragment.show(
                activity.supportFragmentManager,
                ConfigExportBottomSheetFragment.TAG
            )
        }, leadingIcon = {
            Icon(Icons.Default.Output, null)
        })
    }
}