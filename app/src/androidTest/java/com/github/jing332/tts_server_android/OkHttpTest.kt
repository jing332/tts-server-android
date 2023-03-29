package com.github.jing332.tts_server_android

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.drake.net.Net
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class OkHttpTest {
    @Test
    fun postMultiPart() {
        val json = """
            {"11":"11", "22": "22"}
        """.trimIndent()
        val url = "http://v2.jt12.de/up-v2.php"
        val resp: Response = Net.post(url) {
            body = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart(
                    "file",
                    "filename.json",
                    json.toRequestBody("text/javascript".toMediaType())
                )
                .build()
        }.execute()
        println("${resp.code} ${resp.message}: ${resp.body?.string()}")
    }
}