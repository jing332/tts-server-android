package com.github.jing332.tts_server_android.help.audio

import android.media.MediaDataSource
import android.os.Build
import androidx.annotation.RequiresApi
import java.io.InputStream

@RequiresApi(Build.VERSION_CODES.M)
class InputStreamMediaDataSource(val inputStream: InputStream) : MediaDataSource() {
    override fun close() {
        inputStream.close()
    }

    override fun readAt(position: Long, buffer: ByteArray?, offset: Int, size: Int): Int {
        return inputStream.read(buffer, offset, size)
    }

    override fun getSize(): Long = inputStream.available().toLong()
}