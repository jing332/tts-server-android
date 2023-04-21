package com.github.jing332.tts_server_android.utils

import kotlin.reflect.KProperty

class VarDelegate(var value: Any?) {
    operator fun getValue(thisRef: Any?, property: KProperty<*>): Any? = value
    operator fun setValue(thisRef: Any?, property: KProperty<*>, newValue: Any?) {
        value = newValue
    }
}