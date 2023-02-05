package com.github.jing332.tts_server_android

import androidx.test.ext.junit.runners.AndroidJUnit4
import cn.hutool.crypto.symmetric.SymmetricCrypto
import com.github.jing332.tts_server_android.help.plugin.ext.JsNet
import com.script.javascript.RhinoScriptEngine
import org.junit.Test
import org.junit.runner.RunWith
import org.mozilla.javascript.NativeObject

@RunWith(AndroidJUnit4::class)
class RhinoEngineTest {
    @Test
    fun Aes() {
        val aesKey = """G%.g7"Y&Nf^40Ee<""".toByteArray()
        val c = SymmetricCrypto("AES/ECB/PKCS5Padding", aesKey)

        val text = "qwq123"
        println(c.encryptBase64(text))
    }

    @Test
    fun net() {
        val resp = object : JsNet {}.httpGet("https://baidu.com")
        println(resp!!.body!!.bytes().size)
    }

    @Test
    fun script() {
        val jsCode = """
            var tts = {
                "name": "讯飞TTS",
                "id": "xunfei.tts",
                "author": "jing",
                "description": "提取讯飞官网TTS",
                "version": 1,
                "getAudio": function(text, rate, volume, pitch){
                      println(text, rate, volume, pitch)   
                      println(rate)   
                },
            }
            
            function s(){
                println("damn it")
            }
            
            function getAudio(){
                return "你好啊"            
            }
            
        """.trimIndent()

        RhinoScriptEngine().apply {
            val compiledScript = compile(jsCode)
            compiledScript.eval()
            println((get("tts") as NativeObject).get("name"))
        }

//        PluginEngine().apply {
//            println(runScript(jsCode, "测试文本", 1))
//        }
    }
}