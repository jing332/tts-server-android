package com.github.jing332.tts_server_android.service.tts.help

import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import android.os.Build
import android.text.TextUtils
import android.util.Log
import com.github.jing332.tts_server_android.BuildConfig
import com.github.jing332.tts_server_android.service.tts.SystemTtsService
import com.github.jing332.tts_server_android.utils.GcManager
import okio.ByteString
import okio.ByteString.Companion.toByteString
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.charset.StandardCharsets

class AudioDecode {
    companion object {
        const val TAG = "AudioDecode"
    }

    private var isDecoding = false
    private val currentMime: String = ""
    private var mediaCodec: MediaCodec? = null
    private var oldMime: String? = null

    fun stop() {
        isDecoding = false
    }

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


    @Synchronized
    fun doDecode(
        srcData: ByteArray,
        sampleRate: Int,
        onRead: (pcmData: ByteArray) -> Unit,
        error: (reason: String) -> Unit
    ) {
        isDecoding = true
        try {
            val mediaExtractor = MediaExtractor()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                mediaExtractor.setDataSource(ByteArrayMediaDataSource(srcData))
            else
                mediaExtractor.setDataSource(
                    "data:" + currentMime + ";base64," + srcData.toByteString().base64()
                )

            //找到音频流的索引
            var audioTrackIndex = -1
            var mime: String? = null
            var trackFormat: MediaFormat? = null
            for (i in 0 until mediaExtractor.trackCount) {
                trackFormat = mediaExtractor.getTrackFormat(i)
                mime = trackFormat.getString(MediaFormat.KEY_MIME)
                if (!TextUtils.isEmpty(mime) && mime!!.startsWith("audio")) {
                    audioTrackIndex = i
                    Log.d(TAG, "找到音频流的index：$audioTrackIndex, mime：$mime")
                    break
                }
            }
            //没有找到音频流的情况下
            if (audioTrackIndex == -1) {
                error.invoke("initAudioDecoder: 没有找到音频流")
                stop()
                return
            }

            //opus的音频必须设置这个才能正确的解码
            if ("audio/opus" == mime) {
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
                if (BuildConfig.DEBUG) {
                    Log.e(
                        SystemTtsService.TAG,
                        trackFormat!!.getByteBuffer("csd-1")!!
                            .order(ByteOrder.nativeOrder()).long.toString() + ""
                    )
                    Log.e(
                        SystemTtsService.TAG,
                        trackFormat.getByteBuffer("csd-2")!!
                            .order(ByteOrder.nativeOrder()).long.toString() + ""
                    )
                    Log.e(
                        SystemTtsService.TAG,
                        ByteString.of(*trackFormat.getByteBuffer("csd-2")!!.array()).hex()
                    )
                }
                val csd1bytes = byteArrayOf(0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00)
                val csd2bytes = byteArrayOf(0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00)
                val hd: ByteString = buf.readByteString()
                val csd0: ByteBuffer = ByteBuffer.wrap(hd.toByteArray())
                trackFormat!!.setByteBuffer("csd-0", csd0)
                val csd1: ByteBuffer = ByteBuffer.wrap(csd1bytes)
                trackFormat.setByteBuffer("csd-1", csd1)
                val csd2: ByteBuffer = ByteBuffer.wrap(csd2bytes)
                trackFormat.setByteBuffer("csd-2", csd2)
            }

            //选择此音轨
            mediaExtractor.selectTrack(audioTrackIndex)
            //创建解码器
            val mediaCodec: MediaCodec =
                getMediaCodec(
                    mime.toString(),
                    trackFormat!!
                ) //MediaCodec.createDecoderByType(mime);
            mediaCodec.start()
            val bufferInfo = MediaCodec.BufferInfo()
            var inputBuffer: ByteBuffer?
            val timeoutUs: Long = 10000
            while (isDecoding) {
                //获取可用的inputBuffer，输入参数-1代表一直等到，0代表不等待，10*1000代表10秒超时
                //超时时间10秒
                val inputIndex = mediaCodec.dequeueInputBuffer(timeoutUs)
                if (inputIndex < 0) {
                    break
                }
                bufferInfo.presentationTimeUs = mediaExtractor.sampleTime
                //bufferInfo.flags=mediaExtractor.getSampleFlags();
                inputBuffer = mediaCodec.getInputBuffer(inputIndex)
                if (inputBuffer != null) {
                    inputBuffer.clear()
                } else {
                    continue
                }
                //从流中读取的采用数据的大小
                val sampleSize = mediaExtractor.readSampleData(inputBuffer, 0)
                if (sampleSize > 0) {
                    bufferInfo.size = sampleSize
                    //入队解码
                    mediaCodec.queueInputBuffer(inputIndex, 0, sampleSize, 0, 0)
                    //移动到下一个采样点
                    mediaExtractor.advance()
                } else {
                    break
                }

                //取解码后的数据
                var outputIndex = mediaCodec.dequeueOutputBuffer(bufferInfo, timeoutUs)
                //不一定能一次取完，所以要循环取
                var outputBuffer: ByteBuffer?
                var pcmData: ByteArray
                while (outputIndex >= 0) {
                    outputBuffer = mediaCodec.getOutputBuffer(outputIndex)
                    pcmData = ByteArray(bufferInfo.size)
                    if (outputBuffer != null) {
                        outputBuffer.get(pcmData)
                        outputBuffer.clear() //用完后清空，复用
                    }
                    /* 回调 传出数据 */
                    onRead.invoke(pcmData)
                    mediaCodec.releaseOutputBuffer(outputIndex, false)
                    outputIndex = mediaCodec.dequeueOutputBuffer(bufferInfo, timeoutUs)
                }
            }
        } catch (e: Exception) {
            mediaCodec?.reset()
            stop()
            error.invoke(e.toString())
        }
    }
}