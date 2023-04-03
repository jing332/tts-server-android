package com.github.jing332.tts_server_android

import com.github.jing332.tts_server_android.model.AnalyzeUrl
import com.github.jing332.tts_server_android.service.systts.help.ResultTextTag
import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class HttpTtsUrlUnitTest {
    @Test
    fun testJs() {
//        val url =
//            "http://tsn.baidu.com/text2audio,{\"method\": \"POST\", \"body\": \"tex={{java.encodeURI(java.encodeURI(speakText))}}&spd={{(speakSpeed + 5) / 10 + 4}}&per=4114&cuid=baidu_speech_demo&idx=1&cod=2&lan=zh&ctp=1&pdt=220&vol=5&aue=6&pit=5&res_tag=audio\"}"
        val url =
            """  http://192.168.0.109:1233/api/ra,{"method":"POST","body":"<speak xmlns=\"http://www.w3.org/2001/10/synthesis\" xmlns:mstts=\"http://www.w3.org/2001/mstts\" xmlns:emo=\"http://www.w3.org/2009/10/emotionml\" version=\"1.0\" xml:lang=\"en-US\"><voice name=\"zh-CN-XiaoxiaoNeural\"><prosody rate=\"{{(speakSpeed -10) * 2}}%\" pitch=\"+0Hz\">{{speakText}}</prosody></voice></speak>"} """
        val aurl = AnalyzeUrl(url)
        println("baseUrl: " + aurl.eval().toString())
    }
}