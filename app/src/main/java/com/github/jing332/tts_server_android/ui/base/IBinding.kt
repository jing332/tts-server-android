package com.github.jing332.tts_server_android.ui.base

import androidx.viewbinding.ViewBinding

internal sealed interface IBinding<VB : ViewBinding> {
    val binding: VB
}