package com.github.jing332.tts_server_android.ui.systts.replace

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.github.jing332.tts_server_android.App
import com.github.jing332.tts_server_android.constant.AppConst
import com.github.jing332.tts_server_android.data.ReplaceRuleItemData
import com.github.jing332.tts_server_android.util.FileUtils
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import tts_server_lib.Tts_server_lib
import java.io.File

class ReplaceManagerViewModel : ViewModel() {
    val listLiveData: MutableLiveData<ArrayList<ReplaceRuleItemData>> by lazy { MutableLiveData() }

    fun list(): ArrayList<ReplaceRuleItemData> {
        return listLiveData.value!!
    }

    fun load() {
        try {
            val data = File(AppConst.replaceRulesPath).readText()
            listLiveData.value = App.jsonBuilder.decodeFromString(data)
        } catch (e: Exception) {
            e.printStackTrace()
            listLiveData.value = arrayListOf()
        }
    }

    fun save() {
        listLiveData.value?.let {
            FileUtils.saveFile(
                AppConst.replaceRulesPath,
                App.jsonBuilder.encodeToString(it).toByteArray()
            )
        }
    }

    fun switchChanged(position: Int, selected: Boolean) {
        listLiveData.value?.getOrNull(position)?.apply {
            isEnabled = selected
        }
    }

    fun exportConfig(): String {
        return App.jsonBuilder.encodeToString(list())
    }

    fun uploadConfigToUrl(cfg: String): Result<String> {
        return try {
            Result.success(Tts_server_lib.uploadConfig(cfg))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun importConfig(jsonStr: String): String? {
        try {
            val data = App.jsonBuilder.decodeFromString<List<ReplaceRuleItemData>>(jsonStr)
            list().addAll(data)
            listLiveData.value = list()
        } catch (e: Exception) {
            return e.message
        }
        return null
    }

    fun importConfigFromUrl(url: String): String? {
        return try {
            importConfig(Tts_server_lib.httpGet(url, "").decodeToString())
        } catch (e: Exception) {
            e.message
        }
    }
}