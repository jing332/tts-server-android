package com.github.jing332.tts_server_android.util

import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.github.jing332.tts_server_android.R
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.net.URLConnection

object FileUtils {
    fun registerResultCreateDocument(
        fragment: Fragment,
        mime: String,
        onGetData: () -> ByteArray?
    ): ActivityResultLauncher<String> {
        fragment.requireContext().apply {
            return fragment.registerForActivityResult(ActivityResultContracts.CreateDocument(mime)) { uri ->
                val saveData = onGetData.invoke()
                if (saveData == null || uri == null)
                    return@registerForActivityResult
                fragment.lifecycleScope.runOnIO {
                    try {
                        fragment.requireContext().contentResolver?.openOutputStream(uri)?.let {
                            it.write(saveData)
                            it.flush()
                            it.close()
                        }
                        toast(R.string.save_success)
                    } catch (e: IOException) {
                        longToast(getString(R.string.file_save_failed, e.message))
                    }
                }
            }
        }
    }

    fun registerResultCreateDocument(
        activity: androidx.activity.ComponentActivity,
        mime: String,
        onGetData: () -> ByteArray?
    ): ActivityResultLauncher<String> {
        activity.apply {
            return registerForActivityResult(ActivityResultContracts.CreateDocument(mime)) { uri ->
                val saveData = onGetData.invoke()
                if (saveData == null || uri == null)
                    return@registerForActivityResult
                lifecycleScope.runOnIO {
                    try {
                        contentResolver?.openOutputStream(uri)?.let {
                            it.write(saveData)
                            it.flush()
                            it.close()
                        }
                        toast(R.string.save_success)
                    } catch (e: IOException) {
                        longToast(getString(R.string.file_save_failed, e.message))
                    }
                }
            }
        }

    }

    /**
     * 按行读取txt
     */
    fun InputStream.readAllText(): String {
        val bufferedReader = this.bufferedReader()
        val buffer = StringBuffer("")
        var str: String?
        while (bufferedReader.readLine().also { str = it } != null) {
            buffer.append(str)
            buffer.append("\n")
        }
        return buffer.toString()
    }

    fun fileExists(file: File): Boolean {
        runCatching {
            if (file.isFile)
                return file.exists()
        }.onFailure {
            it.printStackTrace()
        }
        return false
    }

    fun fileExists(filePath: String): Boolean {
        return fileExists(File(filePath))
    }

    fun saveFile(path: String, data: ByteArray): Boolean {
        return saveFile(File(path), data)
    }

    fun saveFile(file: File, data: ByteArray): Boolean {
        try {
            if (!fileExists(file)) {
                if (file.parentFile?.exists() == false) /* 文件夹不存在则创建 */
                    file.parentFile?.mkdirs()
            }
            val fos = FileOutputStream(file)
            fos.write(data)
            fos.close()
            return true
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }

    fun getAllFilesInFolder(folder: File): List<File> {
        val fileList = mutableListOf<File>()
        val files = folder.listFiles() ?: return fileList
        for (file in files) {
            if (file.isFile) {
                fileList.add(file)
            } else if (file.isDirectory) {
                // 如果是文件夹，则递归调用该方法
                fileList.addAll(getAllFilesInFolder(file))
            }
        }
        return fileList
    }

    fun getMimeType(file: File): String? {
        val fileNameMap = URLConnection.getFileNameMap()
        return fileNameMap.getContentTypeFor(file.name)
    }
}