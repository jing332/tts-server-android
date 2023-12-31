package com.github.jing332.tts_server_android.compose.widgets

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.github.jing332.tts_server_android.R
import com.github.jing332.tts_server_android.compose.asActivity
import com.github.jing332.tts_server_android.compose.asAppCompatActivity
import com.github.jing332.tts_server_android.compose.systts.replace.edit.SoftKeyboardInputToolbar
import com.github.jing332.tts_server_android.utils.SoftKeyboardUtils
import kotlin.math.max

@Preview
@Composable
fun PreviewAppDialog() {
    var show by remember { mutableStateOf(true) }
    if (show) {
        AppDialog(title = {
            Text("Title")
        }, content = {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                for (i in 0..50) {
                    Text("Content")
                }
            }
        }, buttons = {
            TextButton(onClick = {
                show = false
            }) {
                Text("Cancel")
            }
            TextButton(onClick = {
                show = false
            }) {
                Text("OK")
            }
        }, onDismissRequest = {
            show = false
        })
    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppDialog(
    modifier: Modifier = Modifier,
    onDismissRequest: () -> Unit,
    properties: DialogProperties = DialogProperties(),
    title: @Composable () -> Unit,
    content: @Composable BoxScope.() -> Unit,
    dialogContentPadding: PaddingValues = PaddingValues(12.dp),
    buttons: @Composable BoxScope.() -> Unit = {
        TextButton(onClick = onDismissRequest) { Text(stringResource(id = R.string.close)) }
    },
) = BasicAlertDialog(modifier = modifier, onDismissRequest = onDismissRequest, properties = properties) {
    Surface(
        tonalElevation = 8.dp, shadowElevation = 8.dp, shape = MaterialTheme.shapes.extraLarge
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(dialogContentPadding),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(modifier = Modifier.align(Alignment.CenterHorizontally)) {
                CompositionLocalProvider(LocalTextStyle provides MaterialTheme.typography.titleLarge) {
                    title()
                }
            }

            Box(
                Modifier
                    .weight(weight = 1f, fill = false)
                    .align(Alignment.Start)
            ) {
                CompositionLocalProvider(LocalTextStyle provides MaterialTheme.typography.titleMedium) {
                    content()
                }
            }


            Box(modifier = Modifier.align(Alignment.End)) {
                AppDialogFlowRow(
                    mainAxisSpacing = ButtonsMainAxisSpacing,
                    crossAxisSpacing = ButtonsCrossAxisSpacing
                ) {
                    buttons()
                }
            }
        }
    }
}


@Composable
internal fun AppDialogFlowRow(
    mainAxisSpacing: Dp, crossAxisSpacing: Dp, content: @Composable () -> Unit
) {
    Layout(content) { measurables, constraints ->
        val sequences = mutableListOf<List<Placeable>>()
        val crossAxisSizes = mutableListOf<Int>()
        val crossAxisPositions = mutableListOf<Int>()

        var mainAxisSpace = 0
        var crossAxisSpace = 0

        val currentSequence = mutableListOf<Placeable>()
        var currentMainAxisSize = 0
        var currentCrossAxisSize = 0

        // Return whether the placeable can be added to the current sequence.
        fun canAddToCurrentSequence(placeable: Placeable) =
            currentSequence.isEmpty() || currentMainAxisSize + mainAxisSpacing.roundToPx() + placeable.width <= constraints.maxWidth

        // Store current sequence information and start a new sequence.
        fun startNewSequence() {
            if (sequences.isNotEmpty()) {
                crossAxisSpace += crossAxisSpacing.roundToPx()
            }
            // Ensures that confirming actions appear above dismissive actions.
            sequences.add(0, currentSequence.toList())
            crossAxisSizes += currentCrossAxisSize
            crossAxisPositions += crossAxisSpace

            crossAxisSpace += currentCrossAxisSize
            mainAxisSpace = max(mainAxisSpace, currentMainAxisSize)

            currentSequence.clear()
            currentMainAxisSize = 0
            currentCrossAxisSize = 0
        }

        for (measurable in measurables) {
            // Ask the child for its preferred size.
            val placeable = measurable.measure(constraints)

            // Start a new sequence if there is not enough space.
            if (!canAddToCurrentSequence(placeable)) startNewSequence()

            // Add the child to the current sequence.
            if (currentSequence.isNotEmpty()) {
                currentMainAxisSize += mainAxisSpacing.roundToPx()
            }
            currentSequence.add(placeable)
            currentMainAxisSize += placeable.width
            currentCrossAxisSize = max(currentCrossAxisSize, placeable.height)
        }

        if (currentSequence.isNotEmpty()) startNewSequence()

        val mainAxisLayoutSize = max(mainAxisSpace, constraints.minWidth)

        val crossAxisLayoutSize = max(crossAxisSpace, constraints.minHeight)

        val layoutWidth = mainAxisLayoutSize

        val layoutHeight = crossAxisLayoutSize

        layout(layoutWidth, layoutHeight) {
            sequences.forEachIndexed { i, placeables ->
                val childrenMainAxisSizes = IntArray(placeables.size) { j ->
                    placeables[j].width + if (j < placeables.lastIndex) mainAxisSpacing.roundToPx() else 0
                }
                val arrangement = Arrangement.End
                val mainAxisPositions = IntArray(childrenMainAxisSizes.size) { 0 }
                with(arrangement) {
                    arrange(
                        mainAxisLayoutSize,
                        childrenMainAxisSizes,
                        layoutDirection,
                        mainAxisPositions
                    )
                }
                placeables.forEachIndexed { j, placeable ->
                    placeable.place(
                        x = mainAxisPositions[j], y = crossAxisPositions[i]
                    )
                }
            }
        }
    }
}

private val ButtonsMainAxisSpacing = 8.dp
private val ButtonsCrossAxisSpacing = 12.dp