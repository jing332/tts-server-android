package com.github.jing332.tts_server_android

import android.content.Context
import com.github.jing332.tts_server_android.constant.AppConst
import com.github.jing332.tts_server_android.utils.ClipboardUtils
import com.github.jing332.tts_server_android.utils.longToast
import com.github.jing332.tts_server_android.utils.runOnUI
import tts_server_lib.Tts_server_lib
import java.time.LocalDateTime


class CrashHandler(var context: Context) : Thread.UncaughtExceptionHandler {
    private var mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler()

    init {
        Thread.setDefaultUncaughtExceptionHandler(this)
    }

    override fun uncaughtException(t: Thread, e: Throwable) {
        handleException(e)
        mDefaultHandler?.uncaughtException(t, e)
    }

    private fun handleException(e: Throwable) {
        context.longToast("TTS Server已崩溃 上传日志中 稍后将会复制到剪贴板")
        val log = "\n${LocalDateTime.now()}" +
                "\n版本代码：${AppConst.appInfo.versionCode}， 版本名称：${AppConst.appInfo.versionName}\n" +
                "崩溃详情：\n${e.stackTraceToString()}"
        val copyContent: String = try {
            if (BuildConfig.DEBUG)
                log
            else
                Tts_server_lib.uploadLog(log)
        } catch (e: Exception) {
            e.printStackTrace()
            log
        }

        runOnUI {
            ClipboardUtils.copyText("TTS-Server崩溃日志", copyContent)
            context.longToast("已将日志复制到剪贴板")
        }
    }
}