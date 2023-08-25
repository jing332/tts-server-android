package com.github.jing332.tts_server_android.compose.widgets

import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.github.jing332.tts_server_android.R
import kotlinx.coroutines.delay

//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun LoadingDialog(onDismissRequest: () -> Unit) {
//    AlertDialog(
//        onDismissRequest = onDismissRequest,
//        properties = DialogProperties(dismissOnClickOutside = false)
//    ) {
//        Surface(
//            tonalElevation = 8.dp,
//            shape = MaterialTheme.shapes.small,
//            modifier = Modifier.wrapContentSize()
//        ) {
//            Column(Modifier.padding(horizontal = 48.dp, vertical = 24.dp)) {
//                LoadingAnimation()
//                Text(
//                    stringResource(id = R.string.loading),
//                    Modifier
//                        .padding(top = 8.dp)
//                        .align(Alignment.CenterHorizontally),
//                    style = MaterialTheme.typography.titleMedium
//                )
//            }
//        }
//    }
//}

//@Preview
//@Composable
//fun PreviewLoadingDialog() {
//    var show by remember { mutableStateOf(true) }
//    if (show) {
//        LoadingDialog {
//            show = false
//        }
//    }
//}

// https://github.com/JustAmalll/LoadingAnimation
@Composable
fun LoadingAnimation(
    modifier: Modifier = Modifier,
    circleSize: Dp = 16.dp,
    circleColor: Color = MaterialTheme.colorScheme.primary,
    spaceBetween: Dp = 6.dp,
    travelDistance: Dp = 10.dp
) {
    val circle = listOf(
        remember {
            androidx.compose.animation.core.Animatable(initialValue = 0f)
        },
        remember {
            androidx.compose.animation.core.Animatable(initialValue = 0f)
        },
        remember {
            androidx.compose.animation.core.Animatable(initialValue = 0f)
        }
    )

    circle.forEachIndexed { index, animatable ->
        LaunchedEffect(key1 = animatable) {
            delay(index * 100L)
            animatable.animateTo(
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = keyframes {
                        durationMillis = 1200
                        0.0f at 0 with LinearOutSlowInEasing
                        1.0f at 300 with LinearOutSlowInEasing
                        0.0f at 600 with LinearOutSlowInEasing
                        0.0f at 1200 with LinearOutSlowInEasing
                    },
                    repeatMode = RepeatMode.Restart
                )
            )
        }
    }
    val circleValues = circle.map { it.value }
    val distance = with(LocalDensity.current) { travelDistance.toPx() }
    val lastCircle = circleValues.size - 1

    Row(modifier = modifier) {
        circleValues.forEachIndexed { index, value ->
            Box(
                modifier = Modifier
                    .size(circleSize)
                    .graphicsLayer {
                        translationY = -value * distance
                    }
                    .background(
                        color = circleColor,
                        shape = CircleShape
                    )
            )
            if (index != lastCircle) {
                Spacer(modifier = Modifier.width(spaceBetween))
            }
        }
    }

}