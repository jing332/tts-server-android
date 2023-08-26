package com.github.jing332.tts_server_android.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Parcelable
import androidx.activity.result.contract.ActivityResultContract
import com.github.jing332.tts_server_android.constant.KeyConst
import com.github.jing332.tts_server_android.help.ByteArrayBinder
import com.github.jing332.tts_server_android.utils.setBinder

object AppActivityResultContracts {
    /**
     * 用于传递Parcelable数据
     */
    @Suppress("DEPRECATION")
    fun <T : Parcelable> parcelableDataActivity(clz: Class<out Activity>) =
        object : ActivityResultContract<T?, T?>() {
            override fun createIntent(context: Context, input: T?): Intent {
                return Intent(context, clz).apply {
                    if (input != null) putExtra(KeyConst.KEY_DATA, input)
                }
            }

            override fun parseResult(resultCode: Int, intent: Intent?): T? {
                return intent?.getParcelableExtra(KeyConst.KEY_DATA)
            }
        }

    fun filePickerActivity() =
        object :
            ActivityResultContract<FilePickerActivity.IRequestData, Pair<FilePickerActivity.IRequestData?, Uri?>>() {
            override fun createIntent(
                context: Context,
                input: FilePickerActivity.IRequestData
            ): Intent {
                return Intent(context, FilePickerActivity::class.java).apply {
                    if (input is FilePickerActivity.RequestSaveFile)
                        setBinder(ByteArrayBinder(input.fileBytes!!))

                    putExtra(FilePickerActivity.KEY_REQUEST_DATA, input)
                }
            }

            @Suppress("DEPRECATION")
            override fun parseResult(
                resultCode: Int,
                intent: Intent?
            ): Pair<FilePickerActivity.IRequestData?, Uri?> {
                return intent?.getParcelableExtra<FilePickerActivity.IRequestData>(
                    FilePickerActivity.KEY_REQUEST_DATA
                ) to intent?.data
            }

        }
}