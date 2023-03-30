package com.github.jing332.tts_server_android

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.jing332.tts_server_android.help.script.directupload.DirectUploadEngine
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DirectUploadEngineTest {

    @Test
    fun testJS() {
        val code = """
            let DirectUploadJS = {
                "XX网盘(永久有效)": function(config){
                    println("from js: " + config)
                    return {'url':'https://xxx.com/111.json', 'summary':'永久有效'}                                                                                        
                },
            }
        """.trimIndent()
        val engine = DirectUploadEngine(context = app, code = code)
        val list = engine.obtainFunctionList()
        println(list)
        list.forEach {
            it.invoke("jsonsjosnsjkosnsojsn").apply {
//                println(keys)
            }
        }
    }

}