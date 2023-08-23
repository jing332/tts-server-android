package com.github.jing332.tts_server_android.compose.widgets

import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.github.jing332.tts_server_android.R


@Composable
fun SwitchFloatingButton(modifier: Modifier, switch: Boolean, onSwitchChange: (Boolean) -> Unit) {
    val scope = rememberCoroutineScope()

    val targetIcon =
        if (switch) Icons.Filled.Stop else Icons.Filled.Send
    val rotationAngle by animateFloatAsState(targetValue = if (switch) 360f else 0f, label = "")

    val color =
        animateColorAsState(
            targetValue = if (switch) MaterialTheme.colorScheme.inversePrimary else MaterialTheme.colorScheme.primaryContainer,
            label = "",
            animationSpec = tween(500, 0, LinearEasing)
        )

    FloatingActionButton(
        modifier = modifier,
        elevation = FloatingActionButtonDefaults.elevation(8.dp),
        shape = CircleShape,
        containerColor = color.value,
        onClick = { onSwitchChange(!switch) }) {

        Crossfade(targetState = targetIcon, label = "") {
            Icon(
                imageVector = it,
                contentDescription = stringResource(id = if (switch) R.string.close else R.string.start),
                modifier = Modifier
                    .rotate(rotationAngle)
                    .graphicsLayer {
                        rotationZ = rotationAngle
                    }
                    .size(if (switch) 42.dp else 32.dp)
            )
        }

    }
}