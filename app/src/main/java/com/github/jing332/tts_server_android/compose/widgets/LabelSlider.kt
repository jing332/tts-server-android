package com.github.jing332.tts_server_android.compose.widgets

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout

@Composable
fun LabelSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    steps: Int = 0,
    onValueChangeFinished: (() -> Unit)? = null,
    text: @Composable BoxScope.() -> Unit
) {
    ConstraintLayout(modifier) {
        val (textRef, sliderRef) = createRefs()
        Box(
            modifier = Modifier
                .constrainAs(textRef) {
                    start.linkTo(parent.start)
                    top.linkTo(parent.top)
                    end.linkTo(parent.end)
                }
                .wrapContentHeight()
        ) { text() }
        Slider(
            modifier = Modifier
                .fillMaxWidth()
                .constrainAs(sliderRef) {
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)

                    top.linkTo(textRef.bottom, margin = (-16).dp)
                },
            value = value,
            onValueChange = onValueChange,
            enabled = enabled,
            valueRange = valueRange,
            steps = steps,
            onValueChangeFinished = onValueChangeFinished
        )
    }
}

@Preview
@Composable
fun PreviewSlider() {
    LabelSlider(value = 0f, onValueChange = {}) {
        Text("Hello World")
    }
}