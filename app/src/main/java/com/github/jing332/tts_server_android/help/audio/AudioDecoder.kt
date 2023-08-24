package com.github.jing332.tts_server_android.help.audio

import android.media.MediaCodec
import android.media.MediaCodec.BufferInfo
import android.media.MediaExtractor
import android.media.MediaFormat
import android.os.Build
import android.os.SystemClock
import android.text.TextUtils
import android.util.Log
import com.github.jing332.tts_server_android.help.audio.AudioDecoderException.Companion.ERROR_CODE_NO_AUDIO_TRACK
import com.github.jing332.tts_server_android.utils.GcManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import okio.ByteString
import okio.ByteString.Companion.toByteString
import java.io.IOException
import java.io.InputStream
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets
import kotlin.coroutines.coroutineContext


class AudioDecoder {
    companion object {
        const val TAG = "AudioDecode"

        suspend fun InputStream.readPcmChunk(
            bufferSize: Int = 4096,
            chunkSize: Int = 2048,
            onRead: suspend (ByteArray) -> Unit
        ) {
            var bufferFilledCount = 0
            val buffer = ByteArray(bufferSize)

            while (coroutineContext.isActive) {
                val readLen =
                    this.read(buffer, bufferFilledCount, chunkSize - bufferFilledCount)
                if (readLen == -1) {
                    if (bufferFilledCount > 0) {
                        val chunkData = buffer.copyOfRange(0, bufferFilledCount)
                        onRead.invoke(chunkData)
                    }
                    break
                }
                if (readLen == 0) {
                    delay(100)
                    continue
                }

                bufferFilledCount += readLen
                if (bufferFilledCount >= chunkSize) {
                    val chunkData = buffer.copyOfRange(0, chunkSize)

                    onRead.invoke(chunkData)
                    bufferFilledCount = 0
                }
            }
        }

        /**
         * 获取音频采样率
         */
        private fun getFormats(srcData: ByteArray): List<MediaFormat> {
            kotlin.runCatching {
                val mediaExtractor = MediaExtractor()
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                    mediaExtractor.setDataSource(ByteArrayMediaDataSource(srcData))
                else
                    mediaExtractor.setDataSource(
                        "data:" + "" + ";base64," + srcData.toByteString().base64()
                    )

                val formats = mutableListOf<MediaFormat>()
                for (i in 0 until mediaExtractor.trackCount) {
                    formats.add(mediaExtractor.getTrackFormat(i))
                }
                return formats
            }
            return emptyList()
        }

        /**
         * 获取采样率和MIME
         */
        fun getSampleRateAndMime(audio: ByteArray): Pair<Int, String> {
            val formats = getFormats(audio)

            var sampleRate = 0
            var mime = ""
            if (formats.isNotEmpty()) {
                sampleRate = formats[0].getInteger(MediaFormat.KEY_SAMPLE_RATE)
                mime = formats[0].getString(MediaFormat.KEY_MIME) ?: ""
            }

            return Pair(sampleRate, mime)
        }

    }

    private val currentMime: String = ""
    private var mediaCodec: MediaCodec? = null
    private var oldMime: String? = null

    private fun getMediaCodec(mime: String, mediaFormat: MediaFormat): MediaCodec {
        if (mediaCodec == null || mime != oldMime) {
            if (null != mediaCodec) {
                mediaCodec!!.release()
                GcManager.doGC()
            }
            try {
                mediaCodec = MediaCodec.createDecoderByType(mime)
                oldMime = mime
            } catch (ioException: IOException) {
                //设备无法创建，直接抛出
                ioException.printStackTrace()
                throw RuntimeException(ioException)
            }
        }
        mediaCodec!!.reset()
        mediaCodec!!.configure(mediaFormat, null, null, 0)
        return mediaCodec as MediaCodec
    }

    suspend fun doDecode(
        srcData: ByteArray,
        sampleRate: Int,
        onRead: suspend (pcmData: ByteArray) -> Unit,
    ) {
        val mediaExtractor = MediaExtractor()
        try {
            // RIFF WAVEfmt直接去除文件头即可
            if (srcData.size > 15 && srcData.copyOfRange(0, 15).decodeToString()
                    .endsWith("WAVEfmt")
            ) {
                val data = srcData.copyOfRange(44, srcData.size)
                onRead.invoke(data)
                return
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                mediaExtractor.setDataSource(ByteArrayMediaDataSource(srcData))
            else
                mediaExtractor.setDataSource(
                    "data:" + currentMime + ";base64," + srcData.toByteString().base64()
                )

            decodeInternal(mediaExtractor, sampleRate) {
                onRead.invoke(it)
            }
        } catch (e: Exception) {
            mediaCodec?.reset()
            throw AudioDecoderException(cause = e, message = "音频解码失败")
        } finally {
            mediaExtractor.release()
        }
    }


    /**
     * @param sampleRate opus音频必须设置采样率
     */
    suspend fun doDecode(
        ins: InputStream,
        sampleRate: Int = 0,
        timeoutUs: Long = 5000L,
        onRead: suspend (pcmData: ByteArray) -> Unit,
    ) {
        val mediaExtractor = MediaExtractor()
        try {
            val bytes = ByteArray(15)
            val len = ins.read(bytes)

            if (len == -1) throw AudioDecoderException(message = "读取音频流前15字节失败: len == -1")
            if (bytes.decodeToString().endsWith("WAVEfmt")) {
                ins.buffered().use { buffered ->
                    buffered.skip(29)
                    buffered.readPcmChunk { pcmData ->
                        onRead.invoke(pcmData)
                    }

                    buffered.close()
                }
                return
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                mediaExtractor.setDataSource(InputStreamMediaDataSource(ins))
            else
                throw AudioDecoderException(
                    AudioDecoderException.ERROR_CODE_NOT_SUPPORT_A5,
                    "音频流解码器不支持 Android 5"
                )

            decodeInternal(mediaExtractor, sampleRate, timeoutUs) {
                onRead.invoke(it)
            }
        } catch (e: Exception) {
            mediaCodec?.reset()
            throw AudioDecoderException(cause = e, message = "音频解码失败")
        } finally {
            mediaExtractor.release()
        }
    }

    private suspend fun decodeInternal(
        mediaExtractor: MediaExtractor,
        sampleRate: Int,
        timeoutUs: Long = 5000L,
        onRead: suspend (pcmData: ByteArray) -> Unit
    ) {
        val trackFormat = mediaExtractor.selectAudioTrack()
        val mime = trackFormat.mime

        //opus的音频必须设置这个才能正确的解码
        if ("audio/opus" == mime) trackFormat.compatOpus(sampleRate)

        //创建解码器
        val mediaCodec = getMediaCodec(mime, trackFormat)
        mediaCodec.start()

        val bufferInfo = BufferInfo()
        var inputBuffer: ByteBuffer?
        val startNanos = SystemClock.elapsedRealtimeNanos()
        while (coroutineContext.isActive) {
            //获取可用的inputBuffer，输入参数-1代表一直等到，0代表不等待，10*1000代表10秒超时
            val inputIndex = mediaCodec.dequeueInputBuffer(timeoutUs)
            if (inputIndex < 0) break

            bufferInfo.presentationTimeUs = mediaExtractor.sampleTime

            inputBuffer = mediaCodec.getInputBuffer(inputIndex)
            if (inputBuffer != null) {
                inputBuffer.clear()
            } else
                continue

            //从流中读取的采样数量
            val sampleSize = mediaExtractor.readSampleData(inputBuffer, 0)
            if (sampleSize > 0) {
                bufferInfo.size = sampleSize
                //入队解码
                mediaCodec.queueInputBuffer(inputIndex, 0, sampleSize, 0, 0)
                //移动到下一个采样点
                if (!mediaExtractor.nextSample(startNanos)) {
                    Log.d(TAG, "nextSample(): 已到达流末尾EOF")
                }
            } else
                break

            //取解码后的数据
            var outputIndex = mediaCodec.dequeueOutputBuffer(bufferInfo, timeoutUs)
            //不一定能一次取完，所以要循环取
            var outputBuffer: ByteBuffer?
            val pcmData = ByteArray(bufferInfo.size)

            while (coroutineContext.isActive && outputIndex >= 0) {
                outputBuffer = mediaCodec.getOutputBuffer(outputIndex)
                if (outputBuffer != null) {
                    outputBuffer.get(pcmData)
                    outputBuffer.clear() //用完后清空，复用
                }

                onRead.invoke(pcmData)
                mediaCodec.releaseOutputBuffer(/* index = */ outputIndex, /* render = */ false)
                outputIndex = mediaCodec.dequeueOutputBuffer(bufferInfo, timeoutUs)
            }
        }
    }

    @Suppress("UNUSED_PARAMETER")
    private fun MediaExtractor.nextSample(startNanos: Long): Boolean {
//        while (sampleTime > SystemClock.elapsedRealtimeNanos() - startNanos) {
//            delay(100)
//        }
        return advance()
    }

    private val MediaFormat.mime: String
        get() = getString(MediaFormat.KEY_MIME) ?: ""


    private fun MediaExtractor.selectAudioTrack(): MediaFormat {
        var audioTrackIndex = -1
        var mime: String?
        var trackFormat: MediaFormat? = null
        for (i in 0 until trackCount) {
            trackFormat = getTrackFormat(i)
            mime = trackFormat.getString(MediaFormat.KEY_MIME)
            if (!TextUtils.isEmpty(mime) && mime!!.startsWith("audio")) {
                audioTrackIndex = i
                break
            }
        }
        if (audioTrackIndex == -1)
            throw AudioDecoderException(ERROR_CODE_NO_AUDIO_TRACK, "没有找到音频流")

        selectTrack(audioTrackIndex)
        return trackFormat!!
    }

    private fun MediaFormat.compatOpus(sampleRate: Int) {
        //Log.d(TAG, ByteString.of(trackFormat.getByteBuffer("csd-0")).hex());
        val buf = okio.Buffer()
        // Magic Signature：固定头，占8个字节，为字符串OpusHead
        buf.write("OpusHead".toByteArray(StandardCharsets.UTF_8))
        // Version：版本号，占1字节，固定为0x01
        buf.writeByte(1)
        // Channel Count：通道数，占1字节，根据音频流通道自行设置，如0x02
        buf.writeByte(1)
        // Pre-skip：回放的时候从解码器中丢弃的samples数量，占2字节，为小端模式，默认设置0x00,
        buf.writeShortLe(0)
        // Input Sample Rate (Hz)：音频流的Sample Rate，占4字节，为小端模式，根据实际情况自行设置
        buf.writeIntLe(sampleRate)
        //Output Gain：输出增益，占2字节，为小端模式，没有用到默认设置0x00, 0x00就好
        buf.writeShortLe(0)
        // Channel Mapping Family：通道映射系列，占1字节，默认设置0x00就好
        buf.writeByte(0)
        //Channel Mapping Table：可选参数，上面的Family默认设置0x00的时候可忽略
        val csd1bytes = byteArrayOf(0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00)
        val csd2bytes = byteArrayOf(0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00)
        val hd: ByteString = buf.readByteString()
        val csd0: ByteBuffer = ByteBuffer.wrap(hd.toByteArray())
        setByteBuffer("csd-0", csd0)
        val csd1: ByteBuffer = ByteBuffer.wrap(csd1bytes)
        setByteBuffer("csd-1", csd1)
        val csd2: ByteBuffer = ByteBuffer.wrap(csd2bytes)
        setByteBuffer("csd-2", csd2)
    }
}