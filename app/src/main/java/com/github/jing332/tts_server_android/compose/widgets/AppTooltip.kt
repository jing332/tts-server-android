package com.github.jing332.tts_server_android.compose.widgets

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTooltip(
    modifier: Modifier = Modifier,
    tooltip: String,
    content: @Composable (tooltip: String) -> Unit
) {
    val state = rememberTooltipState()
    TooltipBox(
        modifier = modifier,
        positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
        tooltip = {
            PlainTooltip {
                Text(tooltip)
            }
        },
        state = state,
        content = { content(tooltip) },
    )
}