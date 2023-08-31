package com.github.jing332.tts_server_android.compose.systts.list

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import com.drake.net.utils.withIO
import com.github.jing332.tts_server_android.R
import com.github.jing332.tts_server_android.compose.systts.ListSortSettingsDialog
import com.github.jing332.tts_server_android.data.appDb
import com.github.jing332.tts_server_android.data.entities.systts.SystemTts

internal enum class SortFields(@StringRes val strResId: Int) {
    NAME(R.string.name),
    TAG_NAME(R.string.tag),
    TYPE(R.string.type),
    ENABLE(R.string.enabled),
    ID(R.string.created_time_id)
}

@Composable
internal fun SortDialog(onDismissRequest: () -> Unit, list: List<SystemTts>) {
    var index by remember { mutableIntStateOf(0) }
    ListSortSettingsDialog(
        name = list.size.toString(),
        index = index,
        onIndexChange = { index = it },
        onDismissRequest = onDismissRequest,
        entries = SortFields.values().map { stringResource(id = it.strResId) },
        onConfirm = { _, descending ->
            withIO {
                val sortedList = when (SortFields.values()[index]) {
                    SortFields.NAME -> list.sortedBy { it.displayName }
                    SortFields.TAG_NAME -> list.sortedBy { it.speechRule.tagName }
                    SortFields.TYPE -> list.sortedBy { it.tts.getType() }
                    SortFields.ENABLE -> list.sortedBy { it.isEnabled }
                    SortFields.ID -> list.sortedBy { it.id }
                }.run {
                    if (descending) this.reversed() else this
                }
                sortedList.forEachIndexed { i, systemTts ->
                    appDb.systemTtsDao.updateTts(systemTts.copy(order = i))
                }
            }
        }
    )
}