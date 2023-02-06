package com.github.jing332.tts_server_android.help.plugin

import com.github.jing332.tts_server_android.R
import com.github.jing332.tts_server_android.app
import com.github.jing332.tts_server_android.data.entities.plugin.Plugin
import com.github.jing332.tts_server_android.help.plugin.JsEngineConfig.Companion.SCRIPT_ENGINE
import com.github.jing332.tts_server_android.help.plugin.ext.JsExtensions
import com.github.jing332.tts_server_android.help.plugin.ext.JsLogger
import org.mozilla.javascript.NativeObject

open class JsEngine(
    private val context: android.content.Context = app, val plugin: Plugin = Plugin()
) : JsExtensions(), JsLogger {
    companion object {
        const val OBJ_TTSER = "ttsrv"
        const val OBJ_LOGGER = "logger"
        const val OBJ_PLUGIN_JS = "PluginJS"

        const val FUNC_GET_AUDIO = "getAudio"
    }


    protected val pluginJsObject: NativeObject
        get() {
            return SCRIPT_ENGINE.get(OBJ_PLUGIN_JS).run {
                if (this == null) throw Exception(context.getString(R.string.systts_plugin_no_obj_engine_js))
                else this as NativeObject
            }
        }

    protected fun eval() {
        SCRIPT_ENGINE.put(OBJ_TTSER, this@JsEngine)
        SCRIPT_ENGINE.put(OBJ_LOGGER, this@JsEngine as JsLogger)
        SCRIPT_ENGINE.eval(plugin.code)
    }

    fun evalPluginInfo(): Plugin {
        LogOutputter.writeLine("\n获取插件信息...")
        eval()

        pluginJsObject.apply {
            plugin.name = get("name").toString()
            plugin.pluginId = get("id").toString()
            plugin.author = get("author").toString()
            plugin
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