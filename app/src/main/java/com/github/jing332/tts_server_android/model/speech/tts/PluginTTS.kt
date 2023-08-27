package com.github.jing332.tts_server_android.model.speech.tts

import androidx.annotation.Keep
import com.github.jing332.tts_server_android.R
import com.github.jing332.tts_server_android.data.appDb
import com.github.jing332.tts_server_android.data.entities.plugin.Plugin
import com.github.jing332.tts_server_android.data.entities.systts.AudioParams
import com.github.jing332.tts_server_android.data.entities.systts.SpeechRuleInfo
import com.github.jing332.tts_server_android.model.rhino.tts.EngineContext
import com.github.jing332.tts_server_android.model.rhino.tts.TtsPluginEngine
import com.script.javascript.RhinoScriptEngine
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Transient
import java.io.InputStream

@Keep
@Parcelize
@kotlinx.serialization.Serializable
@SerialName("plugin")
data class PluginTTS(
    val pluginId: String = "",
    override var locale: String = "",
    var voice: String = "",
    // 插件附加数据
    var data: MutableMap<String, String> = mutableMapOf(),

    override var pitch: Int = 50,
    override var volume: Int = 50,
    override var rate: Int = 50,

    override var audioFormat: BaseAudioFormat = BaseAudioFormat(),
    override var audioPlayer: PlayerParams = PlayerParams(),
    override var audioParams: AudioParams = AudioParams(),
    @Transient
    override var speechRule: SpeechRuleInfo = SpeechRuleInfo(),
    @Transient
    var plugin: Plugin? = null,
) : ITextToSpeechEngine() {
    init {
        if (pluginId.isNotEmpty())
            plugin = appDb.pluginDao.getByPluginId(pluginId)
    }

    val requirePlugin: Plugin
        get() {
            plugin?.let { return it }
            throw Exception(context.getString(R.string.not_found_plugin, pluginId))
        }

    override fun getDescription(): String {
        return "$voice <br>${super.getDescription()}"
    }

    override fun getType(): String {
        return try {
            requirePlugin.name
        } catch (e: Exception) {
            e.message ?: e.cause?.message
        }.toString()
    }

    @IgnoredOnParcel
    @Transient
    var pluginEngine: TtsPluginEngine? = null

    companion object {
        // 复用
        private val engineMap = mutableMapOf<String, RhinoScriptEngine>()
    }

    override fun onLoad() {
        if (engineMap.containsKey(pluginId)) {
        } else {
            engineMap[pluginId] = RhinoScriptEngine()
        }
        pluginEngine = pluginEngine ?: TtsPluginEngine(
            pluginTTS = this,
            context = context,
            rhino = engineMap[pluginId]!!
        )

        pluginEngine?.onLoad()
    }

    override fun onStop() {
        pluginEngine?.onStop()
    }

    override suspend fun getAudio(speakText: String, rate: Int, pitch: Int): InputStream? {
        synchronized(engineMap) {
            // 重新更新 ttsrv.tts 对象
            val ctx = (pluginEngine?.ttsrvObject as EngineContext)
            ctx.tts = this
            pluginEngine?.putDefaultObjects()

            return pluginEngine?.getAudio(
                speakText, rate, pitch
            )
        }
    }
}