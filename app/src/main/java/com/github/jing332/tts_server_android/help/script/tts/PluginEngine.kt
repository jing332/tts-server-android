package com.github.jing332.tts_server_android.help.script.tts

import android.content.Context
import com.github.jing332.tts_server_android.R
import com.github.jing332.tts_server_android.app
import com.github.jing332.tts_server_android.data.entities.plugin.Plugin
import com.github.jing332.tts_server_android.help.script.core.BaseScriptEngine
import com.github.jing332.tts_server_android.help.script.core.LogOutputter
import com.github.jing332.tts_server_android.help.script.core.Logger
import com.github.jing332.tts_server_android.help.script.core.ext.JsExtensions
import com.github.jing332.tts_server_android.help.script.core.ext.JsLogger
import com.github.jing332.tts_server_android.model.tts.PluginTTS
import com.script.javascript.RhinoScriptEngine
import org.mozilla.javascript.NativeObject

open class PluginEngine(
    private val pluginTTS: PluginTTS,
    override val context: Context,
    override val rhino: RhinoScriptEngine = RhinoScriptEngine(),
    override val logger: Logger = Logger(),
) : BaseScriptEngine(
    rhino = rhino, context = context, id = pluginTTS.pluginId, logger = logger,
    code = pluginTTS.requirePlugin.code
) {
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
        get() = findObject(OBJ_PLUGIN_JS)

    fun eval(preCode: String = "") {
        rhino.put(OBJ_TTSRV, this@PluginEngine)
        rhino.put(OBJ_LOGGER, this@PluginEngine)
        rhino.eval(preCode + ";" + mPlugin.code)
    }

    fun evalPluginInfo(): Plugin {
        logger.d("evalPluginInfo()...")
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
        logger.d(
            "getAudio()..." +
                    "\ntext: $text, locale: $locale, voice: $voice, rate: $rate, volume: $volume, pitch: $pitch"
        )

        return rhino.invokeMethod(
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