package com.github.jing332.tts_server_android.model.rhino.core.type

abstract class JClass {
    var onThrowable: ((t: Throwable) -> Unit)? = null

    fun tryBlock(block: () -> Unit) {
        kotlin.runCatching {
            block.invoke()
        }.onFailure {
            if (onThrowable == null)
                it.printStackTrace()
            else onThrowable?.invoke(it)
        }
    }
}