package com.github.jing332.tts_server_android.utils

import cn.hutool.core.exceptions.ExceptionUtil.getThrowableList


object ThrowableUtils {
    fun getRootCause(throwable: Throwable?): Throwable? {
        val list = getThrowableList(throwable)
        return if (list.size < 2) null else list[list.size - 1] as Throwable
    }
}

val Throwable.rootCause: Throwable?
    get() = ThrowableUtils.getRootCause(this)

val Throwable.readableString: String
    get() = "${rootCause}\n⬇ More:\n${stackTraceToString()}"
