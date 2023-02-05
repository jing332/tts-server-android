package com.github.jing332.tts_server_android.model.tts

import android.content.Context
import android.view.View
import com.github.jing332.tts_server_android.data.appDb
import com.github.jing332.tts_server_android.data.entities.plugin.Plugin
import com.github.jing332.tts_server_android.data.entities.systts.SystemTts
import com.github.jing332.tts_server_android.help.plugin.JsEngine
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.SerialName

@Parcelize
@kotlinx.serialization.Serializable
@SerialName("plugin")
data class PluginTTS(
    val pluginId: String = "",

    override
    var pitch: Int = 0,
    override var volume: Int = 0,
    override var rate: Int = 0,
    override var audioFormat: BaseAudioFormat = BaseAudioFormat(),
    override var audioPlayer: AudioPlayer = AudioPlayer()
) : BaseTTS() {
    val plugin: Plugin by lazy { appDb.pluginDao.getByPluginId(pluginId)!! }

    override fun isRateFollowSystem() = true
    override fun isPitchFollowSystem() = true

    override fun getType(): String {
        return plugin.name
    }

    override fun getBottomContent(): String {
        return audioFormat.toString()
    }

    override fun getDescription(): String {
        return ""
    }

    override fun onDescriptionClick(
        context: Context,
        view: View?,
        data: SystemTts,
        done: (modifiedData: SystemTts?) -> Unit
    ) {
    }

    @IgnoredOnParcel
    @kotlinx.serialization.Transient
    private var isInitialed = false

    @IgnoredOnParcel
    private val engine: JsEngine by lazy { JsEngine(plugin = plugin) }

    override fun getAudio(speakText: String): ByteArray? {
        return engine.getAudio(speakText, rate, pitch)
    }
}