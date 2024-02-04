package com.github.jing332.tts_server_android.compose.systts.list

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.github.jing332.tts_server_android.compose.widgets.LabelSlider

@Composable
fun IntSlider(
    modifier: Modifier = Modifier,
    label: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>
) {
    LabelSlider(
        modifier = modifier,
        value = value,
        onValueChange = onValueChange,
        valueRange = valueRange,
        text = label,
        buttonSteps = 1f,
        buttonLongSteps = 10f
    )
}