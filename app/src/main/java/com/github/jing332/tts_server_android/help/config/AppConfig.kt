package com.github.jing332.tts_server_android.help.config

import com.chibatching.kotpref.KotprefModel
import com.github.jing332.tts_server_android.R

object AppConfig : KotprefModel() {
    override val kotprefName: String
        get() = "app"

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
}