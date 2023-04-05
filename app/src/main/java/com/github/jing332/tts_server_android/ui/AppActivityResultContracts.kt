package com.github.jing332.tts_server_android.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.result.contract.ActivityResultContract
import com.github.jing332.tts_server_android.constant.KeyConst

object AppActivityResultContracts {
    fun filePickerActivity() =
        object :
            ActivityResultContract<FilePickerActivity.RequestData, Pair<FilePickerActivity.RequestData?, Uri?>>() {
            override fun createIntent(
                context: Context,
                input: FilePickerActivity.RequestData
            ): Intent {
                return Intent(context, FilePickerActivity::class.java).apply {
                    putExtra(KeyConst.KEY_DATA, input)
                }
            }

            @Suppress("DEPRECATION")
            override fun parseResult(
                resultCode: Int,
                intent: Intent?
            ): Pair<FilePickerActivity.RequestData?, Uri?> {
                return intent?.getParcelableExtra<FilePickerActivity.RequestData>(KeyConst.KEY_DATA) to intent?.data
            }

        }
}