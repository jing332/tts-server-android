package com.github.jing332.tts_server_android.compose.widgets

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.MenuItemColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier

@Composable
fun CheckedMenuItem(
    modifier: Modifier = Modifier,
    text: @Composable () -> Unit,
    checked: Boolean,
    onClick: (Boolean) -> Unit,
    onClickCheckBox: (Boolean) -> Unit = onClick,
    leadingIcon: @Composable (() -> Unit)? = null,
    enabled: Boolean = true,
    colors: MenuItemColors = MenuDefaults.itemColors(),
    contentPadding: PaddingValues = MenuDefaults.DropdownMenuItemContentPadding,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
) {
    DropdownMenuItem(
        onClick = { onClick(!checked) },
        modifier = modifier,
        enabled = enabled,
        contentPadding = contentPadding,
        interactionSource = interactionSource,
        colors = colors,
        text = text,
        leadingIcon = leadingIcon,
        trailingIcon = {
            Checkbox(
                checked = checked,
                onCheckedChange = { onClickCheckBox.invoke(it) },
                enabled = enabled,
            )
        }
    )
}