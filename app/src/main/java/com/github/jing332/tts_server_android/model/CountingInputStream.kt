package com.github.jing332.tts_server_android.model

import java.io.IOException
import java.io.InputStream

class CountingInputStream(private val inputStream: InputStream) : InputStream() {
    private var bytesRead: Int = 0

    @Throws(IOException::class)
    override fun read(): Int {
        val data = inputStream.read()
        if (data != -1) {
            bytesRead++
        }
        return data
    }

    @Throws(IOException::class)
    override fun read(b: ByteArray): Int {
        val bytesRead = inputStream.read(b)
        if (bytesRead != -1) {
            this.bytesRead += bytesRead
        }
        return bytesRead
    }

    @Throws(IOException::class)
    override fun read(b: ByteArray, off: Int, len: Int): Int {
        val bytesRead = inputStream.read(b, off, len)
        if (bytesRead != -1) {
            this.bytesRead += bytesRead
        }
        return bytesRead
    }

    override fun available(): Int {
        return inputStream.available()
    }

    override fun close() {
        inputStream.close()
    }

    override fun mark(readlimit: Int) {
        inputStream.mark(readlimit)
    }

    override fun markSupported(): Boolean {
        return inputStream.markSupported()
    }

    override fun reset() {
        inputStream.reset()
    }

    override fun skip(n: Long): Long {
        return inputStream.skip(n)
    }

    fun getBytesRead(): Int {
        return bytesRead
    }
}
