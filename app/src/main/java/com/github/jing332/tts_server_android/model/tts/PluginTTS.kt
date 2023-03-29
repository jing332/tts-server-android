package com.github.jing332.tts_server_android.model.tts

import android.app.Activity
import android.view.View
import com.github.jing332.tts_server_android.R
import com.github.jing332.tts_server_android.data.appDb
import com.github.jing332.tts_server_android.data.entities.plugin.Plugin
import com.github.jing332.tts_server_android.data.entities.systts.SystemTts
import com.github.jing332.tts_server_android.databinding.SysttsPluginEditBottomSheetBinding
import com.github.jing332.tts_server_android.help.plugin.tts.PluginEngine
import com.github.jing332.tts_server_android.ui.systts.edit.plugin.PluginTtsEditActivity
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Transient

@Parcelize
@kotlinx.serialization.Serializable
@SerialName("plugin")
data class PluginTTS(
    val pluginId: String = "",
    var locale: String = "",
    var voice: String = "",
//    var data: String = "",
    // 插件附加数据
    var data: MutableMap<String, String> = mutableMapOf(),

    override var pitch: Int = 0,
    override var volume: Int = 50,
    override var rate: Int = 50,
    override var audioFormat: BaseAudioFormat = BaseAudioFormat(),
    override var audioPlayer: PlayerParams = PlayerParams(),

    @Transient
    var plugin: Plugin? = null,
) : BaseTTS() {
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

    override fun getEditActivity() = PluginTtsEditActivity::class.java

    override fun getType(): String {
        return try {
            requirePlugin.name
        } catch (e: Exception) {
            e.message ?: e.cause?.message
        }.toString()
    }

    @Suppress("DEPRECATION")
    override fun onDescriptionClick(
        activity: Activity,
        view: View?,
        data: SystemTts,
        done: (modifiedData: SystemTts?) -> Unit
    ) {
        val binding =
            SysttsPluginEditBottomSheetBinding.inflate(activity.layoutInflater, null, false)
        binding.apply {
            basicEdit.setData(data)
            paramsEdit.setData(this@PluginTTS)
            root.minimumHeight = activity.windowManager.defaultDisplay.height
        }

        BottomSheetDialog(activity).apply {
            setContentView(binding.root)
            setOnDismissListener { done(data) }
            show()
        }
    }

    @IgnoredOnParcel
    private val engine: PluginEngine by lazy { PluginEngine(pluginTTS = this) }

    override fun onLoad() {
        engine.eval()
    }

    override fun getAudio(speakText: String): ByteArray? {
        return engine.getAudio(speakText, locale, voice, rate, volume, pitch)
    }
}