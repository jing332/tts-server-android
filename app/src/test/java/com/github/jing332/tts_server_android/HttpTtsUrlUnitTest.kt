package com.github.jing332.tts_server_android

import com.github.jing332.tts_server_android.model.AnalyzeUrl
import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class HttpTtsUrlUnitTest {
    @Test
    fun testJs() {
        val url =
            "http://tsn.baidu.com/text2audio,{\"method\": \"POST\", \"body\": \"tex={{java.encodeURI(java.encodeURI(speakText))}}&spd={{(speakSpeed + 5) / 10 + 4}}&per=4114&cuid=baidu_speech_demo&idx=1&cod=2&lan=zh&ctp=1&pdt=220&vol=5&aue=6&pit=5&res_tag=audio\"}"
        val aurl = AnalyzeUrl(url)
        println("baseUrl: " + aurl.eval().toString())
    }
}