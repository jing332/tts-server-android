package com.github.jing332.tts_server_android.compose.systts.replace

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.state.ToggleableState
import com.github.jing332.tts_server_android.R
import com.github.jing332.tts_server_android.compose.systts.GroupItem

@Composable
internal fun Group(
    modifier: Modifier,
    name: String,
    isExpanded: Boolean,
    toggleableState: ToggleableState,
    onToggleableStateChange: (Boolean) -> Unit,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onExport: () -> Unit,
    onSort:()->Unit,
) {
    GroupItem(
        modifier = modifier,
        isExpanded = isExpanded,
        name = name,
        toggleableState = toggleableState,
        onToggleableStateChange = onToggleableStateChange,
        onClick = onClick,
        onExport = onExport,
        onDelete = onDelete,
        actions = { dismiss ->
            DropdownMenuItem(
                leadingIcon = {
                    Icon(
                        Icons.Filled.Edit, "",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                },
                text = { Text(stringResource(R.string.edit)) },
                onClick = {
                    dismiss()
                    onEdit.invoke()
                }
            )

            DropdownMenuItem(
                leadingIcon = {
                    Icon(Icons.Filled.Sort, null)
                },
                text = { Text(stringResource(R.string.sort)) },
                onClick = {
                    dismiss()
                    onSort()
                }
            )
        }
    )
}