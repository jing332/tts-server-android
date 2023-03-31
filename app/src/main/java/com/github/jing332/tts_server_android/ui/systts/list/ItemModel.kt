package com.github.jing332.tts_server_android.ui.systts.list

import com.drake.brv.annotaion.ItemOrientation
import com.drake.brv.item.ItemDrag
import com.github.jing332.tts_server_android.data.entities.systts.SystemTts
import com.github.jing332.tts_server_android.data.entities.systts.SystemTtsGroup

data class ItemModel(
    val data: SystemTts,
    override var itemOrientationDrag: Int = ItemOrientation.VERTICAL
) : ItemDrag {
}