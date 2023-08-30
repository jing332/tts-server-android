package com.github.jing332.tts_server_android.compose.systts.replace

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import com.github.jing332.tts_server_android.R
import com.github.jing332.tts_server_android.compose.systts.ListSortSettingsDialog
import com.github.jing332.tts_server_android.data.appDb
import com.github.jing332.tts_server_android.data.entities.replace.ReplaceRule

internal enum class SortType(@StringRes val strId: Int) {
    CREATE_TIME(R.string.created_time_id),
    NAME(R.string.display_name),
    PATTERN(R.string.replace_rule),
    REPLACEMENT(R.string.replacement),

    ENABLED(R.string.enabled),
    USE_REGEX(R.string.systts_replace_use_regex),
}

@Composable
internal fun SortDialog(onDismissRequest: () -> Unit, list: List<ReplaceRule>) {
    var index by remember { mutableIntStateOf(0) }
    ListSortSettingsDialog(
        name = list.size.toString(),
        onDismissRequest = onDismissRequest,
        index = index,
        onIndexChange = { index = it },
        entries = SortType.values().map { stringResource(id = it.strId) },
        onConfirm = { _, descending ->
            val sortedList = when (SortType.values()[index]) {
                SortType.CREATE_TIME -> list.sortedBy { it.id }
                SortType.NAME -> list.sortedBy { it.name }
                SortType.PATTERN -> list.sortedBy { it.pattern }
                SortType.REPLACEMENT -> list.sortedBy { it.replacement }

                SortType.ENABLED -> list.sortedByDescending { it.isEnabled }
                SortType.USE_REGEX -> list.sortedByDescending { it.isRegex }
            }.run {
                if (descending) this.reversed() else this
            }

            appDb.replaceRuleDao.update(
                *sortedList.mapIndexed { i, rule ->
                    rule.copy(order = i)
                }.toTypedArray()
            )
        }
    )
}