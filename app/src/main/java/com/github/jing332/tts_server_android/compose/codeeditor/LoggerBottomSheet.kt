package com.github.jing332.tts_server_android.compose.codeeditor

import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.github.jing332.tts_server_android.compose.widgets.AppBottomSheet
import com.github.jing332.tts_server_android.constant.LogLevel
import com.github.jing332.tts_server_android.model.rhino.core.Logger

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoggerBottomSheet(
    logger: Logger,
    onDismissRequest: () -> Unit,
    onLaunched: () -> Unit
) {
    var logText by remember { mutableStateOf(AnnotatedString("")) }

    val listener = remember {
        Logger.LogListener { text, level ->
            logText = buildAnnotatedString {
                append(logText)
                val color = LogLevel.toColor(level)
                withStyle(SpanStyle(color = Color(color))) {
                    appendLine(text)
                }
            }
        }
    }

    LaunchedEffect(logger) {
        logger.addListener(listener)
        onLaunched()
    }

    DisposableEffect(logger) {
        onDispose {
            logger.removeListener(listener)
        }
    }

    AppBottomSheet(onDismissRequest = onDismissRequest) {
        SelectionContainer(modifier = Modifier.verticalScroll(rememberScrollState())) {
            Text(
                logText, modifier = Modifier
                    .fillMaxHeight()
                    .padding(horizontal = 8.dp)
                    .padding(bottom = 8.dp)
            )
        }
    }
}