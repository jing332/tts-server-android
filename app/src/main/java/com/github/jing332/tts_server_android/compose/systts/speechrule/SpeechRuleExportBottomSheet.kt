package com.github.jing332.tts_server_android.compose.systts.speechrule

import androidx.compose.runtime.Composable
import com.github.jing332.tts_server_android.compose.systts.ConfigExportBottomSheet
import com.github.jing332.tts_server_android.constant.AppConst
import com.github.jing332.tts_server_android.data.entities.SpeechRule
import kotlinx.serialization.encodeToString

@Composable
fun SpeechRuleExportBottomSheet(onDismissRequest: () -> Unit, list: List<SpeechRule>) {
    ConfigExportBottomSheet(
        onDismissRequest = onDismissRequest,
        json = AppConst.jsonBuilder.encodeToString(list),
        fileName = if (list.size == 1) "ttsrv-speechRule-${list[0].name}.json" else "ttsrv-speechRules.json"
    )
}