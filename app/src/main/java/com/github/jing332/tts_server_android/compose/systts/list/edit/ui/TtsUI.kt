package com.github.jing332.tts_server_android.compose.systts.list.edit.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.github.jing332.tts_server_android.data.entities.systts.SystemTts
import kotlinx.coroutines.CoroutineScope

typealias CallbackState = MutableState<(suspend () -> Unit)?>
typealias CallbackStateWithRet = MutableState<(suspend CoroutineScope.() -> Boolean)?>

@Composable
fun rememberCallbackState() =
    remember { mutableStateOf<(suspend () -> Unit)?>(null) }

@Composable
fun rememberCallbackStateWithRet() =
    remember { mutableStateOf<(suspend CoroutineScope.() -> Boolean)?>(null) }

open class TtsUI {
    @Composable
    open fun ParamsEditScreen(
        modifier: Modifier,
        systts: SystemTts,
        onSysttsChange: (SystemTts) -> Unit
    ) {
    }


    @Composable
    open fun FullEditScreen(
        modifier: Modifier,
        systts: SystemTts,
        onSysttsChange: (SystemTts) -> Unit,
        onSave: () -> Unit,
        onCancel: () -> Unit
    ) {
    }
}