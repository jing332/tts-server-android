package com.github.jing332.tts_server_android.model.tts

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import com.github.jing332.tts_server_android.data.appDb
import com.github.jing332.tts_server_android.data.entities.plugin.Plugin
import com.github.jing332.tts_server_android.data.entities.systts.SystemTts
import com.github.jing332.tts_server_android.databinding.SysttsPluginEditBottomSheetBinding
import com.github.jing332.tts_server_android.help.plugin.JsEngine
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.SerialName

@Parcelize
@kotlinx.serialization.Serializable
@SerialName("plugin")
data class PluginTTS(
    val pluginId: String = "",
    var locale: String = "",
    var voice: String = "",

    override
    var pitch: Int = 0,
    override var volume: Int = 50,
    override var rate: Int = 50,
    override var audioFormat: BaseAudioFormat = BaseAudioFormat(),
    override var audioPlayer: AudioPlayer = AudioPlayer()
) : BaseTTS() {
    @IgnoredOnParcel
    val plugin: Plugin by lazy {
        appDb.pluginDao.getByPluginId(pluginId).run {
            if (this == null) throw Exception("未找到插件: $pluginId")
            this
        }
    }

    override fun isPitchFollowSystem() = true

    override fun getType(): String {
        return try {
            plugin.name
        } catch (e: Exception) {
            e.message ?: e.cause?.message
        }.toString()
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
        val binding =
            SysttsPluginEditBottomSheetBinding.inflate(LayoutInflater.from(context), null, false)

        binding.apply {
            basicEdit.setData(data)
            paramsEdit.setData(this@PluginTTS)
        }

        BottomSheetDialog(context).apply {
            setContentView(binding.root)
            setOnDismissListener { done(data) }
            show()
        }
    }

    @IgnoredOnParcel
    @kotlinx.serialization.Transient
    private var isInitialed = false

    @IgnoredOnParcel
    private val engine: JsEngine by lazy { JsEngine(plugin = plugin) }

    override fun getAudio(speakText: String): ByteArray? {
        return engine.getAudio(speakText, rate, volume, pitch)
    }
}