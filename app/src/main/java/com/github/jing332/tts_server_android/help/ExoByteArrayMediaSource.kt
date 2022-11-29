/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.jing332.tts_server_android.help

import android.net.Uri
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.PlaybackException
import com.google.android.exoplayer2.upstream.BaseDataSource
import com.google.android.exoplayer2.upstream.DataSourceException
import com.google.android.exoplayer2.upstream.DataSpec
import com.google.android.exoplayer2.util.Assertions
import java.io.IOException
import kotlin.math.min

/** ByteArray 数据源 用于音频播放  */
class ExoByteArrayMediaSource(data: ByteArray) : BaseDataSource( /* isNetwork = */false) {
    private val data: ByteArray
    private var uri: Uri? = null
    private var readPosition = 0
    private var bytesRemaining = 0
    private var opened = false

    init {
        Assertions.checkNotNull(data)
        Assertions.checkArgument(data.isNotEmpty())
        this.data = data
    }

    @Throws(IOException::class)
    override fun open(dataSpec: DataSpec): Long {
        uri = dataSpec.uri
        transferInitializing(dataSpec)
        if (dataSpec.position > data.size) {
            throw DataSourceException(PlaybackException.ERROR_CODE_IO_READ_POSITION_OUT_OF_RANGE)
        }
        readPosition = dataSpec.position.toInt()
        bytesRemaining = data.size - dataSpec.position.toInt()
        if (dataSpec.length != C.LENGTH_UNSET.toLong()) {
            bytesRemaining = min(bytesRemaining.toLong(), dataSpec.length).toInt()
        }
        opened = true
        transferStarted(dataSpec)

        return if (dataSpec.length != C.LENGTH_UNSET.toLong()) dataSpec.length else bytesRemaining.toLong()
    }

    @Suppress("NAME_SHADOWING")
    override fun read(buffer: ByteArray, offset: Int, length: Int): Int {
        var len = length
        if (len == 0) {
            return 0
        } else if (bytesRemaining == 0) {
            return C.RESULT_END_OF_INPUT
        }
        len = min(len, bytesRemaining)
        System.arraycopy(data, readPosition, buffer, offset, len)
        readPosition += len
        bytesRemaining -= len
        bytesTransferred(len)

        return len
    }

    override fun getUri(): Uri? {
        return uri
    }

    override fun close() {
        if (opened) {
            opened = false
            transferEnded()
        }
        uri = null
    }
}