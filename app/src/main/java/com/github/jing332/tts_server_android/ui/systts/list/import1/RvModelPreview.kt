package com.github.jing332.tts_server_android.ui.systts.list.import1

import androidx.databinding.BaseObservable
import com.github.jing332.tts_server_android.data.entities.systts.SystemTts
import com.github.jing332.tts_server_android.data.entities.systts.SystemTtsGroup

data class RvModelPreview(
    var isChecked: Boolean = true,
    var name: String,
    val groupName: String,
    var data: Pair<SystemTtsGroup, SystemTts>
) : BaseObservable()