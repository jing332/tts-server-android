package com.github.jing332.tts_server_android.compose.nav.systts.edit.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf

internal val LocalSaveCallBack =
    staticCompositionLocalOf<MutableList<SaveCallBack>> { mutableListOf() }

internal fun interface SaveCallBack {
    suspend fun onSave(): Boolean
}

@Composable
internal fun rememberSaveCallBacks() = remember { mutableListOf<SaveCallBack>() }

@Composable
internal fun SaveActionHandler(cb: SaveCallBack) {
    val cbs = LocalSaveCallBack.current
    DisposableEffect(Unit) {
        println("add cb")
        cbs.add(cb)
        onDispose {
            println("remove cb")
            cbs.remove(cb)
        }
    }
}