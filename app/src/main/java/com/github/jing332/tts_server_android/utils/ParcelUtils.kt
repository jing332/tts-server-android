package com.github.jing332.tts_server_android.utils

import android.os.Parcel
import android.os.Parcelable

@Suppress("UNCHECKED_CAST")
fun <T : Parcelable> Parcelable?.clone(): T? {
    val p = Parcel.obtain()
    p.writeValue(this)
    p.setDataPosition(0)
    val c: Class<out Parcelable?> = this!!::class.java
    val newObject = p.readValue(c.classLoader) as T?
    p.recycle()
    return newObject
}