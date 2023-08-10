package com.github.jing332.tts_server_android.help.audio.exo

import android.annotation.SuppressLint
import android.net.Uri
import androidx.media3.common.C
import androidx.media3.datasource.BaseDataSource
import androidx.media3.datasource.DataSpec
import okio.buffer
import okio.source
import java.io.EOFException
import java.io.IOException
import java.io.InputStream
import kotlin.math.min


@SuppressLint("UnsafeOptInUsageError")
class InputStreamDataSource(
    private val inputStream: InputStream,
) : BaseDataSource(/* isNetwork = */ false) {
    private val bufferedSource = inputStream.source().buffer()
    private var dataSpec: DataSpec? = null
    private var bytesRemaining: Long = 0
    private var opened = false

    @Throws(IOException::class)
    override fun open(dataSpec: DataSpec): Long {
        this.dataSpec = dataSpec
        bufferedSource.skip(dataSpec.position)

        if (dataSpec.length == C.LENGTH_UNSET.toLong()) {
            bytesRemaining = inputStream.available().toLong()
            if (bytesRemaining == 0L) bytesRemaining = C.LENGTH_UNSET.toLong()
        } else {
            bytesRemaining = dataSpec.length
        }

        opened = true
        return bytesRemaining
    }

    override fun getUri(): Uri? = dataSpec?.uri

    @Throws(IOException::class)
    override fun read(buffer: ByteArray, offset: Int, readLength: Int): Int {
        if (readLength == 0) {
            return 0
        } else if (bytesRemaining == 0L) {
            return C.RESULT_END_OF_INPUT
        }

        val bytesToRead =
            if (bytesRemaining == C.LENGTH_UNSET.toLong()) readLength
            else min(bytesRemaining, readLength.toLong()).toInt()

        val bytesRead = bufferedSource.read(buffer, offset, bytesToRead)
        if (bytesRead == -1) {
            if (bytesRemaining != C.LENGTH_UNSET.toLong()) {
                // End of stream reached having not read sufficient data.
                throw IOException(EOFException())
            }
            return C.RESULT_END_OF_INPUT
        }
        if (bytesRemaining != C.LENGTH_UNSET.toLong())
            bytesRemaining -= bytesRead.toLong()

        return bytesRead
    }

    @Throws(IOException::class)
    override fun close() {
        try {
            bufferedSource.close()
            inputStream.close()
        } finally {
            opened = false
        }
    }
}