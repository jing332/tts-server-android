package com.github.jing332.tts_server_android.model.rhino.core.ext

import android.content.Context
import cn.hutool.core.lang.UUID
import com.github.jing332.tts_server_android.help.audio.AudioDecoder
import com.github.jing332.tts_server_android.utils.FileUtils
import java.io.File
import java.io.InputStream

@Suppress("unused")
open class JsExtensions(open val context: Context, open val engineId: String) : JsNet(engineId), JsCrypto,
    JsUserInterface {

    @Suppress("MemberVisibilityCanBePrivate")
    fun getAudioSampleRate(audio: ByteArray): Int {
        return AudioDecoder.getSampleRateAndMime(audio).first
    }

    fun getAudioSampleRate(ins: InputStream): Int {
        return getAudioSampleRate(ins.readBytes())
    }

    /* Str转ByteArray */
    fun strToBytes(str: String): ByteArray {
        return str.toByteArray(charset("UTF-8"))
    }

    fun strToBytes(str: String, charset: String): ByteArray {
        return str.toByteArray(charset(charset))
    }

    /* ByteArray转Str */
    fun bytesToStr(bytes: ByteArray): String {
        return String(bytes, charset("UTF-8"))
    }

    fun bytesToStr(bytes: ByteArray, charset: String): String {
        return String(bytes, charset(charset))
    }

    //****************文件操作******************//
    /**
     * 获取本地文件
     * @param path 相对路径
     * @return File
     */
    fun getFile(path: String): File {
        val cachePath = "${context.externalCacheDir!!.absolutePath}/${engineId}"
        if (!FileUtils.exists(cachePath)) File(cachePath).mkdirs()
        val aPath = if (path.startsWith(File.separator)) {
            cachePath + path
        } else {
            cachePath + File.separator + path
        }
        return File(aPath)
    }

    /**
     * 读Bytes文件
     */
    fun readFile(path: String): ByteArray? {
        val file = getFile(path)
        if (file.exists()) {
            return file.readBytes()
        }
        return null
    }

    /**
     * 读取文本文件
     */
    fun readTxtFile(path: String): String {
        val file = getFile(path)
        if (file.exists()) {
            return String(file.readBytes(), charset(charsetDetect(file)))
        }
        return ""
    }

    /**
     * 获取文件编码
     */
    fun charsetDetect(f: File): String = FileUtils.getFileCharsetSimple(f)

    fun readTxtFile(path: String, charsetName: String): String {
        val file = getFile(path)
        if (file.exists()) {
            return String(file.readBytes(), charset(charsetName))
        }
        return ""
    }

    @JvmOverloads
    fun writeTxtFile(path: String, text: String, charset: String = "UTF-8") {
        getFile(path).writeText(text, charset(charset))
    }

    fun fileExist(path: String): Boolean {
        return FileUtils.exists(getFile(path))
    }

    /**
     * 删除本地文件
     * @return 操作是否成功
     */
    fun deleteFile(path: String): Boolean {
        val file = getFile(path)
        return file.delete()
    }

    fun randomUUID(): String = UUID.randomUUID().toString()
}