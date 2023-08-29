package com.github.jing332.tts_server_android.compose.systts.speechrule

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.github.jing332.tts_server_android.compose.systts.ConfigImportBottomSheet
import com.github.jing332.tts_server_android.compose.systts.ConfigModel
import com.github.jing332.tts_server_android.compose.systts.SelectImportConfigDialog
import com.github.jing332.tts_server_android.constant.AppConst
import com.github.jing332.tts_server_android.data.appDb
import com.github.jing332.tts_server_android.data.entities.SpeechRule

@Composable
fun SpeechRuleImportBottomSheet(onDismissRequest: () -> Unit) {
    var showSelectDialog by remember { mutableStateOf<List<SpeechRule>?>(null) }
    if (showSelectDialog != null) {
        val list = showSelectDialog!!
        SelectImportConfigDialog(
            onDismissRequest = { showSelectDialog = null },
            models = list.map { ConfigModel(true, it.name, "${it.author} - v${it.version}", it) },
            onSelectedList = {
                 appDb.speechRuleDao.insert(*it.map { speechRule -> speechRule as SpeechRule }.toTypedArray())

                it.size
            }
        )
    }

    ConfigImportBottomSheet(onDismissRequest = onDismissRequest, onImport = {
        showSelectDialog = AppConst.jsonBuilder.decodeFromString<List<SpeechRule>>(it)
    })
}