package com.github.jing332.tts_server_android.compose.widgets

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import com.github.jing332.tts_server_android.R

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LabelSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    steps: Int = 0,
    onValueChangeFinished: (() -> Unit)? = null,
    showButton: Boolean = true,
    onValueRemove: (Boolean) -> Unit = { onValueChange(value - if (it) 0.1f else 0.01f) },
    onValueAdd: (Boolean) -> Unit = { onValueChange(value + if (it) 0.1f else 0.01f) },
    text: @Composable BoxScope.() -> Unit,
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
        Row(Modifier.constrainAs(sliderRef) {
            start.linkTo(parent.start)
            end.linkTo(parent.end)
            top.linkTo(textRef.bottom, margin = (-16).dp)
        }) {
            if (showButton)
                IconButton(
                    onClick = { onValueRemove(false) },
                    enabled = value > valueRange.start,
                    modifier = Modifier.combinedClickable(onLongClick = {
                        onValueRemove(true)
                    }) { onValueRemove(false) }
                ) {
                    Icon(Icons.Default.Remove, stringResource(id = R.string.desc_seekbar_remove))
                }
            Slider(
                modifier = Modifier.weight(1f),
                value = value,
                onValueChange = onValueChange,
                enabled = enabled,
                valueRange = valueRange,
                steps = steps,
                onValueChangeFinished = onValueChangeFinished
            )
            if (showButton)
                IconButton(
                    onClick = { onValueAdd(false) },
                    enabled = value < valueRange.endInclusive,
                    modifier = Modifier.combinedClickable(onLongClick = {
                        onValueAdd(true)
                    }) { onValueAdd(false) }
                ) {
                    Icon(Icons.Default.Add, stringResource(id = R.string.desc_seekbar_add))
                }

        }
    }
}

@Preview
@Composable
fun PreviewSlider() {
    LabelSlider(value = 0f, onValueChange = {}) {
        Text("Hello World")
    }
}