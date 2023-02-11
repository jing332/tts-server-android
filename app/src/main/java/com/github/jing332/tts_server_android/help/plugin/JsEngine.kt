package com.github.jing332.tts_server_android.help.plugin

import com.github.jing332.tts_server_android.R
import com.github.jing332.tts_server_android.app
import com.github.jing332.tts_server_android.data.entities.plugin.Plugin
import com.github.jing332.tts_server_android.help.plugin.JsEngineConfig.Companion.SCRIPT_ENGINE
import com.github.jing332.tts_server_android.help.plugin.ext.JsExtensions
import com.github.jing332.tts_server_android.help.plugin.ext.JsLogger
import org.mozilla.javascript.NativeObject

open class JsEngine(
    context: android.content.Context = app, val plugin: Plugin = Plugin()
) : JsExtensions(context, plugin.pluginId), JsLogger {
    companion object {
        const val OBJ_TTSER = "ttsrv"
        const val OBJ_LOGGER = "logger"
        const val OBJ_PLUGIN_JS = "PluginJS"

        const val FUNC_GET_AUDIO = "getAudio"
    }

    private val pluginJsObject: NativeObject
        get() {
            return SCRIPT_ENGINE.get(OBJ_PLUGIN_JS).run {
                if (this == null) throw Exception("Not found object: $OBJ_PLUGIN_JS")
                else this as NativeObject
            }
        }

    protected fun eval(preCode: String = "") {
        SCRIPT_ENGINE.put(OBJ_TTSER, this@JsEngine)
        SCRIPT_ENGINE.put(OBJ_LOGGER, this@JsEngine)
        val s = SCRIPT_ENGINE.eval(preCode + ";" + plugin.code)

    }

    fun evalPluginInfo(): Plugin {
        LogOutputter.writeLine("\n获取插件信息...")
        eval()

        pluginJsObject.apply {
            plugin.name = get("name").toString()
            plugin.pluginId = get("id").toString()
            plugin.author = get("author").toString()

            runCatching {
                plugin.version = (get("version") as Double).toInt()
            }.onFailure {
                throw NumberFormatException(context.getString(R.string.plugin_bad_format))
            }
        }

        return plugin
    }

    fun getAudio(
        text: String, locale: String, voice: String, rate: Int = 1, volume: Int = 1, pitch: Int = 1
    ): ByteArray? {
        LogOutputter.writeLine(
            "\n执行getAudio()..." +
                    "\ntext: $text, locale: $locale, voice: $voice, rate: $rate, volume: $volume, pitch: $pitch"
        )

        eval()

        return SCRIPT_ENGINE.invokeMethod(
            pluginJsObject, FUNC_GET_AUDIO, text, locale, voice, rate, volume, pitch
        )?.run { this as ByteArray }
    }
}