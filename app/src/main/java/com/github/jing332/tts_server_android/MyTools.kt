package com.github.jing332.tts_server_android


class MyTools {
    companion object {
    }

    private fun httpGet(url: String): String? {


        /*client.newCall(request).enqueue(object : okhttp3.Callback {

            override fun onFailure(call: okhttp3.Call, e: IOException) {
                Log.d("HttpGet", "onFailure: ${e.message}")
            }

            override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                val string = response.body?.string()
                Log.d("HttpGet", "正常请求 onResponse: $string")
                //切换到主线程
//                    runOnUiThread {
//                        tvResult.text = "正常请求 onResponse: $string"
//                    }
            }
        })*/
//            return resp.body?.string()
        return null
    }
}


//@JsonClass(generateAdapter = true)
//data class ApkMetaData(
//    public val elements: List<Element>,
//)
//
//@JsonClass(generateAdapter = true)
//data class Element(
//    val outputFile: String,
//    val versionCode: Int,
//    val versionName: String
//)