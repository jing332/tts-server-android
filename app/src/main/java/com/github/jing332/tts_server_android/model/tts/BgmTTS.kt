package com.github.jing332.tts_server_android.model.tts

import android.app.Activity
import android.content.Context
import android.view.View
import com.github.jing332.tts_server_android.data.entities.systts.SystemTts
import com.github.jing332.tts_server_android.ui.systts.edit.bgm.BgmTtsEditActivity
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
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
    override fun getType() = "BGM"

    override fun getDescription(): String {
        return "音量:$volume"
    }

    override fun getBottomContent(): String {
        return "共${musicList.size}个文件夹"
    }

    override fun onDescriptionClick(
        context: Context,
        view: View?,
        data: SystemTts,
        done: (modifiedData: SystemTts?) -> Unit
    ) {

    }
}