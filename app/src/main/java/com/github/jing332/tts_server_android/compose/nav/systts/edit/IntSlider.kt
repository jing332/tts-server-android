package com.github.jing332.tts_server_android.compose.nav.systts.edit

import androidx.compose.material3.Text
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
        text = { Text(label) },
        a11yDescription = label,
        buttonSteps = 1f,
        buttonLongSteps = 10f
    )
}