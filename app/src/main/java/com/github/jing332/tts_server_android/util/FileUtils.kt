package com.github.jing332.tts_server_android.util

import java.io.File
import java.io.FileOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream

object FileUtils {
    fun saveObject(obj: Any, filePath: String) {
        try {
            val fos = File(filePath).outputStream()
            val oos = ObjectOutputStream(fos)
            oos.writeObject(obj)
            oos.flush()
            oos.close()
            fos.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @Suppress("UNCHECKED_CAST")
    fun <T> readObject(filePath: String): T? {
        try {
            val fis = File(filePath).inputStream()
            val ois = ObjectInputStream(fis)
            val obj = ois.readObject() as T
            ois.close()
            fis.close()
            return obj
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    fun fileExists(file: File): Boolean {
        runCatching {
            if (file.isFile)
                return file.exists()
        }
        return false
    }

    fun fileExists(filePath: String): Boolean {
        return fileExists(File(filePath))
    }

    fun saveFile(path: String, data: ByteArray): Boolean {
        try {
            val file = File(path)
            if (!fileExists(file)) {
                if (file.parentFile?.exists() == false) /* 文件夹不存在则创建 */
                    file.parentFile?.mkdirs()
            }
            val fos = FileOutputStream(file)
            fos.write(data)
            fos.flush()
            fos.close()
            return true
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }
}