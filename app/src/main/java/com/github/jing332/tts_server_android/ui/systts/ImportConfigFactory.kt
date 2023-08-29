package com.github.jing332.tts_server_android.ui.systts

import android.content.Context
import android.content.Intent
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import com.github.jing332.tts_server_android.R
import com.github.jing332.tts_server_android.compose.systts.list.ListImportBottomSheet
import com.github.jing332.tts_server_android.compose.systts.plugin.PluginImportBottomSheet
import com.github.jing332.tts_server_android.compose.systts.plugin.PluginManagerActivity
import com.github.jing332.tts_server_android.compose.systts.replace.ReplaceRuleImportBottomSheet
import com.github.jing332.tts_server_android.compose.systts.speechrule.SpeechRuleImportBottomSheet
import com.github.jing332.tts_server_android.compose.systts.speechrule.SpeechRuleManagerActivity

enum class ImportType(val id: String, @StringRes val strResId: Int) {
    LIST("list", R.string.config_list),
    PLUGIN("plugin", R.string.plugin),
    REPLACE_RULE("replaceRule", R.string.replace_rule),
    SPEECH_RULE("speechRule", R.string.speech_rule)
}

object ImportConfigFactory {
    fun getBottomSheet(type: String, onBadFormat: () -> Unit): @Composable (() -> Unit) -> Unit {
        return when (ImportType.values().find { it.id == type }) {
            ImportType.LIST -> {
                { ListImportBottomSheet(it) }
            }

            ImportType.PLUGIN -> {
                { PluginImportBottomSheet(it) }
            }

            ImportType.REPLACE_RULE -> {
                { ReplaceRuleImportBottomSheet(it) }
            }

            ImportType.SPEECH_RULE -> {
                { SpeechRuleImportBottomSheet(it) }
            }

            else -> {
                onBadFormat()

                return { println("bad format") }
            }
        }
    }

    /**
     * @return 是否识别成功
     */
    fun Context.gotoEditorFromJS(js: String): Boolean {
        if (js.contains("PluginJS")) {
            startActivity(Intent(this, PluginManagerActivity::class.java).apply {
                action = Intent.ACTION_VIEW
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                putExtra("js", js)
            })

        } else if (js.contains("SpeechRuleJS")) {
            startActivity(Intent(this, SpeechRuleManagerActivity::class.java).apply {
                action = Intent.ACTION_VIEW
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                putExtra("js", js)
            })
        } else
            return false

        return true
    }
}