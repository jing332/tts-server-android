package com.github.jing332.tts_server_android.compose.systts.list

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DriveFileRenameOutline
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.state.ToggleableState
import com.github.jing332.tts_server_android.R
import com.github.jing332.tts_server_android.compose.systts.GroupItem
import com.github.jing332.tts_server_android.compose.widgets.TextFieldDialog

@Composable
fun Group(
    modifier: Modifier,
    name: String,
    isExpanded: Boolean,
    toggleableState: ToggleableState,
    onToggleableStateChange: (Boolean) -> Unit,
    onClick: () -> Unit,
    onExport: () -> Unit,
    onDelete: () -> Unit,
    onRename: (newName: String) -> Unit,
    onCopy: (newName: String) -> Unit,
    onEditAudioParams: () -> Unit,
    onSort: () -> Unit,
) {

    var showRenameDialog by remember { mutableStateOf(false) }
    if (showRenameDialog) {
        var nameValue by remember { mutableStateOf(name) }
        TextFieldDialog(
            title = stringResource(id = R.string.rename),
            text = nameValue,
            onTextChange = { nameValue = it },
            onDismissRequest = { showRenameDialog = false }) {
            showRenameDialog = false
            onRename(nameValue)
        }
    }

    var showCopyDialog by remember { mutableStateOf(false) }
    if (showCopyDialog) {
        var nameValue by remember { mutableStateOf(name) }
        TextFieldDialog(
            title = stringResource(id = R.string.copy),
            text = nameValue,
            onTextChange = { nameValue = it },
            onDismissRequest = { showCopyDialog = false }) {
            showCopyDialog = false
            onCopy(nameValue)
        }
    }

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
            DropdownMenuItem(text = { Text(stringResource(id = R.string.rename)) },
                onClick = {
                    dismiss()
                    showRenameDialog = true
                },
                leadingIcon = {
                    Icon(Icons.Default.DriveFileRenameOutline, null)
                }
            )

            DropdownMenuItem(text = { Text(stringResource(id = R.string.copy)) },
                onClick = {
                    dismiss()
                    showCopyDialog = true
                },
                leadingIcon = {
                    Icon(Icons.Default.ContentCopy, null)
                }
            )

            DropdownMenuItem(text = { Text(stringResource(id = R.string.audio_params)) },
                onClick = {
                    dismiss()
                    onEditAudioParams()
                },
                leadingIcon = {
                    Icon(Icons.Default.Speed, null)
                }
            )

            DropdownMenuItem(text = { Text(stringResource(id = R.string.sort)) },
                onClick = {
                    dismiss()
                    onSort()
                },
                leadingIcon = {
                    Icon(Icons.Default.Sort, null)
                }
            )
        }
    )

}
