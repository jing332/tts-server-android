package com.github.jing332.tts_server_android.model.tts

import android.app.Activity
import android.view.View
import com.github.jing332.tts_server_android.R
import com.github.jing332.tts_server_android.data.entities.systts.SystemTts
import com.github.jing332.tts_server_android.databinding.SysttsBgmEditBottomSheetBinding
import com.github.jing332.tts_server_android.ui.systts.edit.bgm.BgmTtsEditActivity
import com.github.jing332.tts_server_android.util.toHtmlBold
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Parcelize
@Serializable
@SerialName("bgm")
class BgmTTS(
    var musicList: MutableSet<String> = mutableSetOf(),
    var isShuffleMode: Boolean = false,

    override var pitch: Int = 0,
    override var volume: Int = 0,
    override var rate: Int = 0,
    override var audioFormat: BaseAudioFormat = BaseAudioFormat(),
    override var audioPlayer: PlayerParams = PlayerParams(),
    @Transient
    override var info: TtsInfo = TtsInfo()
) : ITextToSpeechEngine() {
    override fun getEditActivity(): Class<out Activity> = BgmTtsEditActivity::class.java
    override fun getType() = context.getString(R.string.local)

    override fun getDescription(): String {
        val volStr = if (volume == 0) context.getString(R.string.follow) else volume.toString()
        return context.getString(R.string.systts_bgm_description, volStr.toHtmlBold())
    }

    override fun getBottomContent(): String =
        context.getString(R.string.total_n_folders, musicList.size.toString())

    @Suppress("DEPRECATION")
    override fun onDescriptionClick(
        activity: Activity,
        view: View?,
        data: SystemTts,
        done: (modifiedData: SystemTts?) -> Unit
    ) {
        val binding =
            SysttsBgmEditBottomSheetBinding.inflate(activity.layoutInflater, null, false)
        binding.apply {
            basicEdit.liteModeEnabled = true
            basicEdit.setData(data)
            editView.setData(this@BgmTTS)
            root.minimumHeight = activity.windowManager.defaultDisplay.height
        }

        BottomSheetDialog(activity).apply {
            setContentView(binding.root)
            setOnDismissListener { done(data) }
            show()
        }
    }
}