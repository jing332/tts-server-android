package com.github.jing332.tts_server_android.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.result.contract.ActivityResultContract

object AppActivityResultContracts {
    fun filePickerActivity() =
        object : ActivityResultContract<FilePickerActivity.RequestData, Uri?>() {
            override fun createIntent(
                context: Context,
                input: FilePickerActivity.RequestData
            ): Intent {
                return Intent(context, FilePickerActivity::class.java).apply {
                    putExtra(FilePickerActivity.KEY_TYPE, input.type)
                    input.fileName?.let { fileName ->
                            putExtra(FilePickerActivity.KEY_FILE_NAME, fileName)
                        input.fileData?.let {
                            putExtra(FilePickerActivity.KEY_FILE_DATA, it)
                        }
                    }
                }
            }

            override fun parseResult(resultCode: Int, intent: Intent?): Uri? {
                return intent?.data
            }

        }
}