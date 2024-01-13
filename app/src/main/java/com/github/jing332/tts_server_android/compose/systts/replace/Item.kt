package com.github.jing332.tts_server_android.compose.systts.replace

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.VerticalAlignBottom
import androidx.compose.material.icons.filled.VerticalAlignTop
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.dp
import com.github.jing332.tts_server_android.R
import com.github.jing332.tts_server_android.compose.systts.ConfigDeleteDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun Item(
    name: String,
    modifier: Modifier,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onMoveTop: () -> Unit,
    onMoveBottom: () -> Unit,
    isEnabled: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    val context = LocalContext.current
    var deleteDialog by remember { mutableStateOf(false) }
    if (deleteDialog)
        ConfigDeleteDialog(onDismissRequest = { deleteDialog = false }, name = name) {
            onDelete()
        }

    ElevatedCard(
        onClick = onClick,
        modifier = modifier
    ) {
        Row(modifier = modifier.fillMaxSize()) {
            Checkbox(
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .semantics {
                        role = Role.Switch
                        context
                            .getString(
                                if (isEnabled) R.string.rule_enabled_desc else R.string.rule_disabled_desc,
                                name
                            )
                            .let {
                                contentDescription = it
                                stateDescription = it
                            }
                    },
                checked = isEnabled,
                onCheckedChange = onCheckedChange
            )
            Text(
                name,
                maxLines = 1,
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 4.dp)
                    .fillMaxWidth()
                    .align(Alignment.CenterVertically),
            )
            Row {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, stringResource(id = R.string.edit_desc, name))
                }
                var isMoreOptionsVisible by remember { mutableStateOf(false) }
                IconButton(onClick = {
                    isMoreOptionsVisible = true
                }, modifier = Modifier.padding(end = 10.dp)) {
                    Icon(
                        imageVector = Icons.Filled.MoreVert,
                        contentDescription = stringResource(id = R.string.more_options_desc, name),
                        tint = MaterialTheme.colorScheme.onBackground
                    )

                    DropdownMenu(expanded = isMoreOptionsVisible,
                        onDismissRequest = { isMoreOptionsVisible = false }) {

                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.move_to_top)) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Filled.VerticalAlignTop,
                                    contentDescription = null,
                                )
                            },
                            onClick = {
                                onMoveTop()
                                isMoreOptionsVisible = false
                            }
                        )

                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.move_to_bottom)) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Filled.VerticalAlignBottom,
                                    contentDescription = null,
                                )
                            },
                            onClick = {
                                onMoveBottom()
                                isMoreOptionsVisible = false
                            }
                        )

                        HorizontalDivider()
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.delete)) },
                            leadingIcon = {
                                Icon(
                                    Icons.Filled.DeleteForever,
                                    stringResource(R.string.delete),
                                    tint = MaterialTheme.colorScheme.error
                                )
                            },
                            onClick = {
                                isMoreOptionsVisible = false
                                deleteDialog = true
                            }
                        )

                    }
                }
            }
        }
    }

}
