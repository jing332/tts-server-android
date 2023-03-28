package com.github.jing332.tts_server_android.help.plugin

import com.github.jing332.tts_server_android.R
import com.github.jing332.tts_server_android.app
import com.github.jing332.tts_server_android.data.entities.plugin.Plugin
import com.github.jing332.tts_server_android.help.plugin.ext.JsExtensions
import com.github.jing332.tts_server_android.help.plugin.ext.JsLogger
import com.github.jing332.tts_server_android.model.tts.PluginTTS
import com.script.javascript.RhinoScriptEngine
import org.mozilla.javascript.NativeObject

open class PluginEngine(
    context: android.content.Context = app,
    private val pluginTTS: PluginTTS,
    protected val scriptEngine: RhinoScriptEngine = RhinoScriptEngine()
) : JsExtensions(context, pluginTTS.plugin!!.pluginId), JsLogger {
    private var mPlugin: Plugin
        inline get() = pluginTTS.plugin!!
        inline set(value) {
            pluginTTS.plugin = value
        }

    companion object {
        const val OBJ_TTSRV = "ttsrv"
        const val OBJ_LOGGER = "logger"
        const val OBJ_PLUGIN_JS = "PluginJS"
        const val OBJ_TTS_DATA = "ttsData"

        const val FUNC_GET_AUDIO = "getAudio"
    }

    // 已弃用, 占位
    @Suppress("unused")
    var extraData: String = ""

    val tts: PluginTTS
        get() = pluginTTS

    private val pluginJsObject: NativeObject
        get() {
            return scriptEngine.get(OBJ_PLUGIN_JS).run {
                if (this == null) throw Exception("Not found object: $OBJ_PLUGIN_JS")
                else this as NativeObject
            }
        }

    fun eval(preCode: String = "") {
        scriptEngine.put(OBJ_TTSRV, this@PluginEngine)
        scriptEngine.put(OBJ_LOGGER, this@PluginEngine)
        scriptEngine.eval(preCode + ";" + mPlugin.code)
    }

    fun evalPluginInfo(): Plugin {
        LogOutputter.writeLine("\n获取插件信息...")
        eval()

        pluginJsObject.apply {
            mPlugin.name = get("name").toString()
            mPlugin.pluginId = get("id").toString()
            mPlugin.author = get("author").toString()

            runCatching {
                mPlugin.version = (get("version") as Double).toInt()
            }.onFailure {
                throw NumberFormatException(context.getString(R.string.plugin_bad_format))
            }
        }

        return mPlugin
    }

    fun getAudio(
        text: String, locale: String, voice: String, rate: Int = 1, volume: Int = 1, pitch: Int = 1
    ): ByteArray? {
        LogOutputter.writeLine(
            "\n执行getAudio()..." +
                    "\ntext: $text, locale: $locale, voice: $voice, rate: $rate, volume: $volume, pitch: $pitch"
        )

        return scriptEngine.invokeMethod(
            pluginJsObject,
            FUNC_GET_AUDIO,
            text,
            pluginTTS.locale,
            pluginTTS.voice,
            pluginTTS.rate,
            pluginTTS.volume,
            pluginTTS.pitch
        )?.run { this as ByteArray }
    }
}