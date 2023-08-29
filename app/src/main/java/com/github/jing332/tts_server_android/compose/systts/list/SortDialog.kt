package com.github.jing332.tts_server_android.compose.systts.list

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.drake.net.utils.withDefault
import com.drake.net.utils.withIO
import com.drake.net.utils.withMain
import com.github.jing332.tts_server_android.R
import com.github.jing332.tts_server_android.compose.systts.ListSortSettingsDialog
import com.github.jing332.tts_server_android.data.appDb
import com.github.jing332.tts_server_android.data.entities.systts.SystemTts
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

internal enum class SortFields(@StringRes val strResId: Int) {
    NAME(R.string.name),
    TAG_NAME(R.string.tag),
    ENABLE(R.string.enabled),
    ID(R.string.created_time_id)
}

@Composable
internal fun SortDialog(onDismissRequest: () -> Unit, list: List<SystemTts>) {
    var sorting by remember { mutableStateOf(false) }
    ListSortSettingsDialog(
        onDismissRequest = onDismissRequest,
        entries = SortFields.values().map { stringResource(id = it.strResId) },
        sorting = sorting,
        onConfirm = { index, descending ->
            sorting = true
            withIO {
                val sortedList = when (SortFields.values()[index]) {
                    SortFields.NAME -> list.sortedBy { it.displayName }
                    SortFields.TAG_NAME -> list.sortedBy { it.speechRule.tagName }
                    SortFields.ENABLE -> list.sortedBy { it.isEnabled }
                    SortFields.ID -> list.sortedBy { it.id }
                }.run {
                    if (descending) this.reversed() else this
                }
                sortedList.forEachIndexed { i, systemTts ->
                    appDb.systemTtsDao.updateTts(systemTts.copy(order = i))
                }
            }

            sorting = false
        }
    )
}