package com.github.jing332.tts_server_android.compose.nav.systts

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.DriveFileRenameOutline
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TriStateCheckbox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.unit.dp
import com.github.jing332.tts_server_android.R
import com.github.jing332.tts_server_android.compose.widgets.TextFieldDialog


@Composable
fun Group(
    name: String,
    isExpanded: Boolean,
    toggleableState: ToggleableState,
    onCheckedChange: () -> Unit,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    onRename: (newName: String) -> Unit,
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    if (showDeleteDialog) {
        ConfigDeleteDialog(onDismissRequest = { showDeleteDialog = false }, name = name) {
            showDeleteDialog = false
            onDelete()
        }
    }

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

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                onClickLabel = stringResource(
                    id = if (isExpanded) R.string.desc_collapse_group
                    else R.string.desc_expand_group, name
                )
            ) { onClick() }
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painterResource(id = if (isExpanded) R.drawable.ic_arrow_expand else R.drawable.ic_arrow_collapse),
            contentDescription = stringResource(id = if (isExpanded) R.string.group_expanded else R.string.group_collapsed)
        )

        Text(
            name,
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier
                .align(Alignment.CenterVertically)
                .weight(1f)
        )
        Row {
            TriStateCheckbox(state = toggleableState, onClick = {
                onCheckedChange()
            })

            var showOptions by remember { mutableStateOf(false) }
            IconButton(onClick = { showOptions = true }) {
                Icon(
                    Icons.Default.MoreVert,
                    contentDescription = stringResource(id = R.string.more_options)
                )

                DropdownMenu(expanded = showOptions, onDismissRequest = { showOptions = false }) {
                    DropdownMenuItem(text = { Text(stringResource(id = R.string.rename)) },
                        onClick = {
                            showOptions = false
                            showRenameDialog = true
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Default.DriveFileRenameOutline,
                                contentDescription = stringResource(id = R.string.rename)
                            )
                        }
                    )

                    Divider()
                    DropdownMenuItem(text = {
                        Text(
                            stringResource(id = R.string.delete),
                            color = MaterialTheme.colorScheme.error
                        )
                    },
                        leadingIcon = {
                            Icon(
                                Icons.Default.DeleteForever,
                                contentDescription = stringResource(id = R.string.delete),
                                tint = MaterialTheme.colorScheme.error
                            )
                        },
                        onClick = {
                            showOptions = false
                            showDeleteDialog = true
                        }
                    )
                }
            }
        }

    }
}
