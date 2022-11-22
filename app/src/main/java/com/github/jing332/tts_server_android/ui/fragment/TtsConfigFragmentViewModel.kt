package com.github.jing332.tts_server_android.ui.fragment

import androidx.lifecycle.ViewModel
import com.github.jing332.tts_server_android.App
import com.github.jing332.tts_server_android.constant.ReadAloudTarget
import com.github.jing332.tts_server_android.data.SysTtsConfigItem
import com.github.jing332.tts_server_android.data.appDb
import com.github.jing332.tts_server_android.data.entities.SysTts
import com.github.jing332.tts_server_android.help.SysTtsConfig
import com.github.jing332.tts_server_android.service.systts.help.TtsFormatManger
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import tts_server_lib.Tts_server_lib

class TtsConfigFragmentViewModel : ViewModel() {
    companion object {
        const val TAG = "TtsConfigFragmentViewModel"
    }

    fun onCheckBoxChanged(list: List<SysTts>, position: Int, checked: Boolean): Boolean {
        //检测多语音是否开启
        if (checked && !SysTtsConfig.isMultiVoiceEnabled) {
            val target =
                list.getOrNull(position)?.readAloudTarget ?: ReadAloudTarget.DEFAULT
            if (target == ReadAloudTarget.ASIDE || target == ReadAloudTarget.DIALOGUE) {
                return false
            }
        }

        list[position].let { data ->
            if (checked) { //确保同类型只可单选
                list.forEachIndexed { index, it ->
                    if (it.readAloudTarget == data.readAloudTarget) {
                        it.isEnabled = false
                        appDb.sysTtsDao.update(it)
                    }
                }
            }

            // 更新position
            data.isEnabled = checked
            appDb.sysTtsDao.update(data)
        }
        return true
    }

    /* 添加或编辑Item */
    fun onEditActivityResult(data: SysTts, position: Int) {
        if (position >= 0) { //编辑
            appDb.sysTtsDao.update(data)
        } else { //添加
            appDb.sysTtsDao.insert(data)
        }
    }


    /**
     * 检查多语音音频格式是否一致
     * @return 是否相等
     */
    fun checkMultiVoiceFormat(list: List<SysTts>): Boolean {
        val aside = appDb.sysTtsDao.getByReadAloudTarget(ReadAloudTarget.ASIDE)
        val dialogue = appDb.sysTtsDao.getByReadAloudTarget(ReadAloudTarget.DIALOGUE)
        if (aside == null || dialogue == null) {
            return true
        } else if (aside.isEnabled && dialogue.isEnabled) {
            return TtsFormatManger.isFormatSampleEqual(
                aside.msTtsProperty?.format ?: "",
                dialogue.msTtsProperty?.format ?: ""
            )
        }
        return true
    }


    /* 按API排序 */
    fun sortListByApi() {
    }

    /* 按朗读目标排序 */
    fun sortListByRaTarget() {
    }

    /* 按显示名称排序 */
    fun sortListByDisplayName() {
    }

    /* 上传配置到URL */
    fun uploadConfigToUrl(json: String): Result<String> {
        return try {
            val url = Tts_server_lib.uploadConfig(json)

            Result.success(url)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /* 导出配置 */
    fun exportConfig(): String {
        return try {
            App.jsonBuilder.encodeToString(appDb.sysTtsDao.all)
        } catch (e: Exception) {
            "导出失败：${e.message}"
        }
    }

    /* 从URL导入配置 */
    fun importConfigByUrl(url: String): String? {
        val jsonStr = try {
            Tts_server_lib.httpGet(url, "")
        } catch (e: Exception) {
            return e.message
        }
        return importConfig(jsonStr.decodeToString())
    }

    /* 从json导入配置 */
    fun importConfig(json: String): String? {
        try {
            val list = App.jsonBuilder.decodeFromString<List<SysTts>>(json)
            list.forEach {
                appDb.sysTtsDao.insert(it)
            }
        } catch (e: Exception) {
            return e.message
        }
        return null
    }


    data class ReplacedData(
        var sysTtsConfigItem: SysTtsConfigItem,
        var position: Int,
        val updateUi: Boolean,
    )

}