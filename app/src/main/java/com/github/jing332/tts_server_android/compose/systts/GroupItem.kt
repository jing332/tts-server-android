package com.github.jing332.tts_server_android.compose.systts

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.ExpandCircleDown
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Output
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TriStateCheckbox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.unit.dp
import com.github.jing332.tts_server_android.R

fun Int.sizeToToggleableState(total: Int): ToggleableState = when (this) {
    0 -> ToggleableState.Off
    total -> ToggleableState.On
    else -> ToggleableState.Indeterminate
}

@Composable
fun GroupItem(
    modifier: Modifier,
    isExpanded: Boolean,
    name: String,
    toggleableState: ToggleableState,
    onToggleableStateChange: (Boolean) -> Unit,
    onClick: () -> Unit,
    onExport: () -> Unit,
    onDelete: () -> Unit,
    actions: @Composable ColumnScope.(() -> Unit) -> Unit,
) {
    val view = LocalView.current
    val context = LocalContext.current

    var expandedFirst by remember { mutableStateOf(true) }
    LaunchedEffect(isExpanded) {
        if (expandedFirst) expandedFirst = false
        else {
            val msg =
                if (isExpanded) context.getString(
                    R.string.group_expanded,
                    name
                ) else context.getString(R.string.group_collapsed, name)
            view.announceForAccessibility(msg)
        }
    }

    var checkFirst by remember { mutableStateOf(true) }
    LaunchedEffect(toggleableState) {
        if (checkFirst) checkFirst = false
        else {
            val msg = when (toggleableState) {
                ToggleableState.On -> context.getString(R.string.group_all_enabled, name)
                ToggleableState.Off -> context.getString(R.string.group_all_disabled, name)
                else -> context.getString(R.string.group_part_enabled, name)
            }
            view.announceForAccessibility(msg)
        }
    }

    var showDeleteDialog by remember { mutableStateOf(false) }
    if (showDeleteDialog)
        ConfigDeleteDialog(
            onDismissRequest = { showDeleteDialog = false }, name = name, onConfirm = onDelete
        )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .semantics(true) {
                contentDescription = context.getString(
                    if (isExpanded) R.string.group_expanded
                    else R.string.group_collapsed, " "
                )
            }
            .clickable { onClick() }
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val rotationAngle by animateFloatAsState(
            targetValue = if (isExpanded) 0f else -45f,
            label = ""
        )
        Icon(
            Icons.Default.ExpandCircleDown,
            contentDescription = null,
            modifier = Modifier
                .rotate(rotationAngle)
                .graphicsLayer { rotationZ = rotationAngle }
        )

        Text(
            name,
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier
                .align(Alignment.CenterVertically)
                .weight(1f)
        )
        Row {
            TriStateCheckbox(
                state = toggleableState,
                onClick = {
                    onToggleableStateChange(toggleableState != ToggleableState.On)
                },
                modifier = Modifier.semantics {
                    contentDescription = context.getString(
                        when (toggleableState) {
                            ToggleableState.On -> R.string.group_all_enabled
                            ToggleableState.Off -> R.string.group_all_disabled
                            else -> R.string.group_part_enabled
                        }, name
                    )
                }
            )

            var showOptions by remember { mutableStateOf(false) }
            IconButton(onClick = { showOptions = true }) {
                Icon(
                    Icons.Default.MoreVert,
                    contentDescription = stringResource(id = R.string.more_options_desc, name)
                )

                DropdownMenu(expanded = showOptions, onDismissRequest = { showOptions = false }) {
                    actions { showOptions = false }

                    DropdownMenuItem(
                        text = { Text(stringResource(id = R.string.export_config)) },
                        onClick = {
                            showOptions = false
                            onExport()
                        },
                        leadingIcon = {
                            Icon(Icons.Default.Output, null)
                        }
                    )

                    HorizontalDivider()

                    DropdownMenuItem(text = {
                        Text(
                            stringResource(id = R.string.delete),
                            color = MaterialTheme.colorScheme.error
                        )
                    },
                        leadingIcon = {
                            Icon(
                                Icons.Default.DeleteForever,
                                contentDescription = null,
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