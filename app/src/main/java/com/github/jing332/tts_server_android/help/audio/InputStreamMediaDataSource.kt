package com.github.jing332.tts_server_android.help.audio

import android.media.MediaDataSource
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import okio.buffer
import okio.source
import java.io.InputStream

@RequiresApi(Build.VERSION_CODES.M)
class InputStreamMediaDataSource(private val inputStream: InputStream) : MediaDataSource() {
    companion object {
        const val TAG = "InputStreamDataSource"
    }

    private val bufferedInputStream = inputStream.source().buffer()

    override fun close() {
        Log.d(TAG, "close")
        bufferedInputStream.close()
    }

    override fun readAt(position: Long, buffer: ByteArray, offset: Int, size: Int): Int {
        Log.d(TAG, "readAt: pos=$position, offset=$offset, size=$size")
        kotlin.runCatching {
            return bufferedInputStream.read(buffer, offset, size).apply {
                Log.d(TAG, "readAt: readLen=$this")
            }
        }.onFailure {
            Log.d(TAG, it.stackTraceToString())
        }
        return -1
    }

    override fun getSize(): Long {
        return inputStream.available().toLong().apply {
            Log.d(TAG, "getSize: $this")
        }
    }

}