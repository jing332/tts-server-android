package com.github.jing332.tts_server_android.model.tts

import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
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
    override var audioPlayer: PlayerParams = PlayerParams()
) : BaseTTS() {
    override fun getEditActivity(): Class<out Activity> = BgmTtsEditActivity::class.java
    override fun getType() = context.getString(R.string.bgm)

    override fun getDescription(): String {
        val volStr = if (volume == 0) context.getString(R.string.follow) else volume.toString()
        return "音量:${volStr.toHtmlBold()}"
    }

    override fun getBottomContent(): String {
        return "共${musicList.size.toString()}个文件夹"
    }

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
            basicEdit.setData(data)
            basicEdit.liteModeEnabled = true
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