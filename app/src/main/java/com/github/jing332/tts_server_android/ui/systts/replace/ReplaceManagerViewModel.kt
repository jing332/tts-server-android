package com.github.jing332.tts_server_android.ui.systts.replace

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.github.jing332.tts_server_android.App
import com.github.jing332.tts_server_android.data.ReplaceRuleItemData
import com.github.jing332.tts_server_android.util.FileUtils
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import java.io.File

class ReplaceManagerViewModel : ViewModel() {
    val listLiveData: MutableLiveData<ArrayList<ReplaceRuleItemData>> by lazy { MutableLiveData() }

    companion object {
        val filepath by lazy { App.context.filesDir.absolutePath + "/replace_rule.json" }
    }

    fun list(): ArrayList<ReplaceRuleItemData> {
        return listLiveData.value!!
    }

    fun load() {
        try {
            val data = File(filepath).readText()
            listLiveData.value = App.jsonBuilder.decodeFromString(data)
        } catch (e: Exception) {
            e.printStackTrace()
            listLiveData.value = arrayListOf()
        }
    }

    fun save() {
        listLiveData.value?.let {
            FileUtils.saveFile(filepath, App.jsonBuilder.encodeToString(it).toByteArray())
        }
    }

    fun switchChanged(position: Int, selected: Boolean) {
        listLiveData.value?.getOrNull(position)?.apply {
            isEnabled = selected
        }
    }
}