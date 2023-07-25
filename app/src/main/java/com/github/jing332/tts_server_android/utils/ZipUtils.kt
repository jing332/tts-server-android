package com.github.jing332.tts_server_android.utils

import kotlinx.coroutines.isActive
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.util.zip.Deflater
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream
import kotlin.coroutines.coroutineContext


object ZipUtils {
    const val TAG = "ZipUtils"

    suspend fun zipFolder(sourceFolder: File, zipFile: File) {
        try {
            val fos = FileOutputStream(zipFile)
            val zos = ZipOutputStream(BufferedOutputStream(fos))

            zos.setLevel(Deflater.DEFAULT_COMPRESSION)
            zipFolder(sourceFolder, zos, "")
            zos.closeEntry()
            zos.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @Throws(IOException::class)
    private suspend fun zipFolder(folder: File, zos: ZipOutputStream, parentPath: String) {
        val files = folder.listFiles()
        val buffer = ByteArray(4096)
        var bytesRead: Int

        for (file in files!!) {
            if (coroutineContext.isActive) break

            if (file.isDirectory) {
                zipFolder(file, zos, parentPath + file.name + "/")
            } else {
                val fis = FileInputStream(file)
                val bis = BufferedInputStream(fis)
                val entryPath = parentPath + file.name
                val entry = ZipEntry(entryPath)
                zos.putNextEntry(entry)
                while (bis.read(buffer).also { bytesRead = it } != -1) {
                    if (coroutineContext.isActive) break
                    zos.write(buffer, 0, bytesRead)
                }
                bis.close()
                fis.close()
            }
        }
    }

    suspend fun unzipFile(zis: ZipInputStream, destFolder: File) {
        val buffer = ByteArray(4096)
        var bytesRead: Int
        var entry: ZipEntry? = zis.nextEntry
        while (coroutineContext.isActive && entry != null) {
            val entryName = entry.name
            val entryPath = destFolder.absolutePath + File.separator + entryName

            if (entry.isDirectory) {
                val dir = File(entryPath)
                dir.mkdirs()
            } else {
                val file = File(entryPath)
                file.parentFile?.mkdirs()
                file.delete()
                file.createNewFile()
                val fos = FileOutputStream(entryPath)
                val bos = BufferedOutputStream(fos)
                while (zis.read(buffer).also { bytesRead = it } != -1) {
                    bos.write(buffer, 0, bytesRead)
                }
                bos.close()
            }
            zis.closeEntry()

            entry = zis.nextEntry
        }
        zis.close()
    }
}