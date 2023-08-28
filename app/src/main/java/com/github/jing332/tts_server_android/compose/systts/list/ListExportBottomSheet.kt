package com.github.jing332.tts_server_android.compose.systts.list

import androidx.compose.runtime.Composable
import com.github.jing332.tts_server_android.compose.ConfigExportBottomSheet
import com.github.jing332.tts_server_android.constant.AppConst
import com.github.jing332.tts_server_android.data.entities.systts.GroupWithSystemTts
import com.github.jing332.tts_server_android.data.entities.systts.SystemTtsGroup
import kotlinx.serialization.encodeToString

@Composable
fun ListExportBottomSheet(onDismissRequest: () -> Unit, list: List<GroupWithSystemTts>) {
    ConfigExportBottomSheet(
        json = AppConst.jsonBuilder.encodeToString(list),
        onDismissRequest = onDismissRequest
    )
}