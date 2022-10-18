package com.github.jing332.tts_server_android.service.tts

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioFormat
import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import android.os.Build
import android.os.PowerManager
import android.os.SystemClock
import android.speech.tts.SynthesisCallback
import android.speech.tts.SynthesisRequest
import android.speech.tts.TextToSpeech
import android.speech.tts.TextToSpeechService
import android.text.TextUtils
import android.util.Log
import com.github.jing332.tts_server_android.BuildConfig
import com.github.jing332.tts_server_android.service.tts.help.ByteArrayMediaDataSource
import com.github.jing332.tts_server_android.service.tts.help.TtsAudioFormat
import com.github.jing332.tts_server_android.service.tts.help.TtsConfig
import com.github.jing332.tts_server_android.service.tts.help.TtsFormatManger
import com.github.jing332.tts_server_android.utils.GcManger
import com.github.jing332.tts_server_android.utils.NormUtil
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okio.ByteString
import okio.ByteString.Companion.toByteString
import tts_server_lib.Tts_server_lib
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.charset.StandardCharsets
import java.util.*


class TtsService : TextToSpeechService() {
    companion object {
        const val TAG = "TtsService"
        const val ACTION_ON_CONFIG_CHANGED = "action_on_config_changed"
    }

    private val currentLanguage: MutableList<String> = mutableListOf("zho", "CHN", "")

    private val ttsConfig: TtsConfig by lazy { TtsConfig().loadConfig(this) }
    private val mReceiver: MyReceiver by lazy { MyReceiver() }
    private val mWakeLock by lazy {
        val powerManager = getSystemService(POWER_SERVICE) as PowerManager
        return@lazy powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK or PowerManager.ON_AFTER_RELEASE,
            "tts-server:tts"
        )
    }

    private var isSynthesizing = false

    override fun onCreate() {
        super.onCreate()
        val intentFilter = IntentFilter(ACTION_ON_CONFIG_CHANGED)
        registerReceiver(mReceiver, intentFilter)
        mWakeLock.acquire(60 * 20 * 100)
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(mReceiver)
        mWakeLock.release()
    }

    override fun onIsLanguageAvailable(lang: String?, country: String?, variant: String?): Int {
        return if (Locale.SIMPLIFIED_CHINESE.isO3Language == lang || Locale.US.isO3Language == lang) {
            if (Locale.SIMPLIFIED_CHINESE.isO3Country == country || Locale.US.isO3Country == country) TextToSpeech.LANG_COUNTRY_AVAILABLE else TextToSpeech.LANG_AVAILABLE
        } else TextToSpeech.LANG_NOT_SUPPORTED
    }

    override fun onGetLanguage(): Array<String> {
        return currentLanguage.toTypedArray()
    }

    override fun onLoadLanguage(lang: String?, country: String?, variant: String?): Int {
        val result = onIsLanguageAvailable(lang, country, variant)
        currentLanguage.clear()
        currentLanguage.addAll(
            mutableListOf(
                lang.toString(),
                country.toString(),
                variant.toString()
            )
        )

        return result
    }

    override fun onStop() {
        Log.d(TAG, "onStop")
        isSynthesizing = false
    }

    private val norm: NormUtil by lazy {
        return@lazy NormUtil(500F, 0F, 100F, 0F)
    }

    @OptIn(DelicateCoroutinesApi::class)
    override fun onSynthesizeText(request: SynthesisRequest?, callback: SynthesisCallback?) {
        val startTime = SystemClock.elapsedRealtime()
        synchronized(this) {
            reNewWakeLock()

            val text = request?.charSequenceText.toString()
            Log.d(TAG, "接收到文本: $text")
            if (text.isEmpty()) {
                Log.d(TAG, "文本为空，跳过")
                callback!!.start(
                    16000,
                    AudioFormat.ENCODING_PCM_16BIT, 1
                )
                callback.done()
                return
            }

            GlobalScope.launch {
                while (isSynthesizing) {
                    try {
                        delay(100)
                    } catch (e: InterruptedException) {
                        e.printStackTrace()
                    }
                    val time = SystemClock.elapsedRealtime() - startTime
                    //超时15秒后跳过,保证长句不会被跳过
                    if (time > 15000) {
                        callback!!.error(TextToSpeech.ERROR_NETWORK_TIMEOUT)
                        isSynthesizing = false
                    }
                }
            }
            synthesizeText(request, callback)
        }
    }

    private fun synthesizeText(request: SynthesisRequest?, callback: SynthesisCallback?) {
        val rate = "${norm.normalize(request?.speechRate?.toFloat()!!) - 50}%"
        val text = request.charSequenceText.toString()
        val pitch = "${request.pitch - 100}%"
        Log.e(TAG, "pitch: $pitch")

        val format = TtsFormatManger.getFormat(ttsConfig.format)
        if (format == null) {
            Log.e(TAG, "不支持解码此格式: ${ttsConfig.format}")
            callback!!.start(
                16000,
                AudioFormat.ENCODING_PCM_16BIT, 1
            )
            callback.error(TextToSpeech.ERROR_INVALID_REQUEST)
            return
        }

        callback?.start(format.hz, format.bitRate, 1)
        try {
            val audio = getAudio(ttsConfig.api, text, rate, pitch)
            if (audio != null) {
                Log.i(TAG, "获取音频成功, size: ${audio.size}")
                doDecode(callback!!, audio)
            } else {
                callback?.error()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun getAudio(api: Int, text: String, rate: String, pitch: String): ByteArray? {
        Log.e(TAG, "api:$api")
        when (api) {
            TtsAudioFormat.API_EDGE -> {
                return Tts_server_lib.getEdgeAudio(
                    ttsConfig.voiceName,
                    text,
                    rate,
                    pitch,
                    ttsConfig.format
                )
            }
            TtsAudioFormat.API_CREATION -> {
                val arg = tts_server_lib.CreationArg()
                arg.text = text
                arg.voiceName = ttsConfig.voiceName
                arg.voiceId = ttsConfig.voiceId
                arg.style = "general"
                arg.styleDegree = "1.0"
                arg.role = "default"
                arg.rate = rate
                arg.volume = ttsConfig.volumeToPctString()
                arg.format = ttsConfig.format
                Log.d(TAG, arg.toString())
                return Tts_server_lib.getCreationAudio(arg)
            }
        }
        return null
    }

    private fun reNewWakeLock() {
        if (!mWakeLock.isHeld) {
            mWakeLock.acquire(60 * 20 * 1000)
            GcManger.doGC()
            Log.i(TAG, "刷新WakeLock 20分钟")
        }
    }


    private val currentMime: String = ""
    private var mediaCodec: MediaCodec? = null
    private var oldMime: String? = null

    /**
     * 根据mime创建MediaCodec
     * 当Mime未变化时复用MediaCodec
     *
     * @param mime mime
     * @return MediaCodec
     */
    private fun getMediaCodec(mime: String, mediaFormat: MediaFormat): MediaCodec {
        if (mediaCodec == null || mime != oldMime) {
            if (null != mediaCodec) {
                mediaCodec!!.release()
                GcManger.doGC()
                System.gc()
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
    private fun doDecode(cb: SynthesisCallback, data: ByteArray) {
        isSynthesizing = true
        try {
            val mediaExtractor = MediaExtractor()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                //在高版本上使用自定义MediaDataSource
                mediaExtractor.setDataSource(ByteArrayMediaDataSource(data))
            } else {
                //在低版本上使用Base64音频数据/ Base64.encodeToString(data, Base64.DEFAULT)
                mediaExtractor.setDataSource(
                    "data:" + currentMime + ";base64," + data.toByteString().base64()
                )
            }

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
                Log.e(TAG, "initAudioDecoder: 没有找到音频流")
                cb.done()
                isSynthesizing = false
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
                buf.writeIntLe(TtsFormatManger.getFormat(ttsConfig.format)?.hz!!)
                //Output Gain：输出增益，占2字节，为小端模式，没有用到默认设置0x00, 0x00就好
                buf.writeShortLe(0)
                // Channel Mapping Family：通道映射系列，占1字节，默认设置0x00就好
                buf.writeByte(0)
                //Channel Mapping Table：可选参数，上面的Family默认设置0x00的时候可忽略
                if (BuildConfig.DEBUG) {
                    Log.e(
                        TAG,
                        trackFormat!!.getByteBuffer("csd-1")!!
                            .order(ByteOrder.nativeOrder()).long.toString() + ""
                    )
                    Log.e(
                        TAG,
                        trackFormat.getByteBuffer("csd-2")!!
                            .order(ByteOrder.nativeOrder()).long.toString() + ""
                    )
                    Log.e(TAG, ByteString.of(*trackFormat.getByteBuffer("csd-2")!!.array()).hex())
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
            while (isSynthesizing) {
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
                    writeToCallBack(cb, pcmData)
//                    cb.audioAvailable(pcmData, 0, bufferInfo.size)
                    //释放
                    mediaCodec.releaseOutputBuffer(outputIndex, false)
                    //再次获取数据
                    outputIndex = mediaCodec.dequeueOutputBuffer(bufferInfo, timeoutUs)
                }
            }
            mediaCodec.reset()
            cb.done()
            isSynthesizing = false
            Log.d(TAG, "播放完毕")
        } catch (e: Exception) {
            Log.e(TAG, "doDecode", e)
            cb.error()
            isSynthesizing = false
            //GcManger.getInstance().doGC();
        }
    }

    private fun writeToCallBack(callback: SynthesisCallback, audio: ByteArray) {
        try {
            val maxBufferSize: Int = callback.maxBufferSize
            var offset = 0
            while (offset < audio.size) {
                val bytesToWrite = maxBufferSize.coerceAtMost(audio.size - offset)
                callback.audioAvailable(audio, offset, bytesToWrite)
                offset += bytesToWrite
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    /* @Synchronized
     private fun doUnDecode(cb: SynthesisCallback, format: String, data: ByteString) {
         isSynthesizing = true
         val length: Int = data.toByteArray().size
         //最大BufferSize
         val maxBufferSize = cb.maxBufferSize
         var offset = 0
         while (offset < length && isSynthesizing) {
             val bytesToWrite = Math.min(maxBufferSize, length - offset)
             cb.audioAvailable(data.toByteArray(), offset, bytesToWrite)
             offset += bytesToWrite
         }
         cb.done()
         isSynthesizing = false
     }*/

    inner class MyReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == ACTION_ON_CONFIG_CHANGED) {
                ttsConfig.loadConfig(this@TtsService)
            }
        }
    }

}