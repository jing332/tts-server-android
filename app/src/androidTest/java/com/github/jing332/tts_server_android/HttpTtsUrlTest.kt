package com.github.jing332.tts_server_android

import android.util.Log
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.jing332.tts_server_android.constant.AppConst
import com.github.jing332.tts_server_android.model.AnalyzeUrl
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class HttpTtsUrlTest {
    @Test
    fun test() {
        // Context of the app under test.
//        val appContext = InstrumentationRegistry.getInstrumentation().targetContext

        //            "http://tsn.baidu.com/text2audio,{\"method\": \"POST\", \"body\": \"tex={{java.encodeURI(java.encodeURI(speakText))}}&spd={{(speakSpeed + 5) / 10 + 4}}&per=4114&cuid=baidu_speech_demo&idx=1&cod=2&lan=zh&ctp=1&pdt=220&vol=5&aue=6&pit=5&res_tag=audio\"}"
        val url =
            """ http://192.168.0.109:1233/api/ra ,{"method":"POST","body":"<speak xmlns=\"http://www.w3.org/2001/10/synthesis\" xmlns:mstts=\"http://www.w3.org/2001/mstts\" xmlns:emo=\"http://www.w3.org/2009/10/emotionml\" version=\"1.0\" xml:lang=\"en-US\"><voice name=\"zh-CN-XiaoxiaoNeural\"><prosody rate=\"{{(speakSpeed -10) * 2}}%\" pitch=\"+0Hz\">{{String(speakText).replace(/&/g, '&amp;').replace(/\"/g, '&quot;').replace(/'/g, '&apos;').replace(/</g, '&lt;').replace(/>/g, '&gt;')}}</prosody></voice></speak>"} """
        Log.e("TAG", url)
        val a = AnalyzeUrl(url, speakText = "t\\\\est\\测\\\\试")
        Log.e("TAG", "baseUrl: " + a.eval().toString())

        println(AppConst.SCRIPT_ENGINE.eval(""" String("Test\\\\测试") """))
    }
}