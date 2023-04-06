package com.github.jing332.tts_server_android.help

import android.os.Binder

// Binder 用于传递大数据
class ByteArrayBinder(val data: ByteArray) : Binder()