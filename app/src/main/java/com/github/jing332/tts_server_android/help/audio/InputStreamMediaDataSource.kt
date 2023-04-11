package com.github.jing332.tts_server_android.help.audio

import android.media.MediaDataSource
import android.os.Build
import androidx.annotation.RequiresApi
import okio.buffer
import okio.source
import java.io.InputStream

@RequiresApi(Build.VERSION_CODES.M)
class InputStreamMediaDataSource(private val inputStream: InputStream) : MediaDataSource() {
    private val bufferedInputStream = inputStream.source().buffer()

    override fun close() {
        bufferedInputStream.close()
    }

    override fun readAt(position: Long, buffer: ByteArray, offset: Int, size: Int): Int {
        return bufferedInputStream.read(buffer, offset, size)
    }

    override fun getSize(): Long = inputStream.available().toLong()
}