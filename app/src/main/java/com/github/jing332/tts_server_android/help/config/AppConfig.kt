package com.github.jing332.tts_server_android.help.config

import com.chibatching.kotpref.KotprefModel
import com.chibatching.kotpref.enumpref.enumValuePref
import com.github.jing332.tts_server_android.R
import com.github.jing332.tts_server_android.constant.AppTheme
import com.github.jing332.tts_server_android.constant.FilePickerMode

@Suppress("DEPRECATION")
object AppConfig : KotprefModel() {


    override val kotprefName: String
        get() = "app"

    var theme by enumValuePref(AppTheme.Default)

    var isAutoCheckUpdateEnabled by booleanPref(true)

    /**
     * 是否 Edge接口使用DNS解析IP
     */
    var isEdgeDnsEnabled by booleanPref(true)

    var isSwapListenAndEditButton by booleanPref(false)

    private var mTestSampleText by stringPref(key = "testSampleText")

    /**
     * 如果为空则设置默认
     */
    var testSampleText: String
        get() {
            if (mTestSampleText.isBlank()) mTestSampleText =
                context.getString(R.string.systts_sample_test_text)
            return mTestSampleText
        }
        set(value) {
            mTestSampleText = value
        }

    var fragmentIndex by intPref(0)

    var filePickerMode by intPref(FilePickerMode.PROMPT)
    var spinnerMaxDropDownCount by intPref(20)

    var lastReadHelpDocumentVersion by intPref(0)
}