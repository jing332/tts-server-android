package com.github.jing332.tts_server_android.help.audio

import android.media.MediaDataSource
import android.os.Build
import androidx.annotation.RequiresApi

@RequiresApi(api = Build.VERSION_CODES.M)
class ByteArrayMediaDataSource(var data: ByteArray) : MediaDataSource() {
    override fun readAt(position: Long, buffer: ByteArray, offset: Int, size: Int): Int {
        if (position >= data.size) return -1

        val endPosition = (position + size).toInt()
        val size2 = if (endPosition > data.size) size - (endPosition - data.size) else size

        System.arraycopy(data, position.toInt(), buffer, offset, size2)
        return size2
    }

    override fun getSize(): Long {
        return data.size.toLong()
    }

    override fun close() {
    }
}