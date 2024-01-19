package com.github.jing332.tts_server_android.compose.systts.replace

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.github.jing332.tts_server_android.compose.systts.ConfigExportBottomSheet
import com.github.jing332.tts_server_android.constant.AppConst
import com.github.jing332.tts_server_android.data.entities.replace.GroupWithReplaceRule
import kotlinx.serialization.encodeToString

@Composable
fun ReplaceRuleExportBottomSheet(onDismissRequest: () -> Unit, list: List<GroupWithReplaceRule>) {
    val json = remember(list) {
        AppConst.jsonBuilder.encodeToString(list)
    }

    ConfigExportBottomSheet(json = json, onDismissRequest = onDismissRequest, fileName = "ttsrv-replaces.json")
}