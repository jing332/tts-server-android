package com.github.jing332.tts_server_android.model

import com.drake.net.utils.withDefault
import com.drake.net.utils.withIO
import com.github.jing332.tts_server_android.app
import com.github.jing332.tts_server_android.bean.EdgeVoiceBean
import com.github.jing332.tts_server_android.constant.AppConst
import com.github.jing332.tts_server_android.constant.CnLocalMap
import com.github.jing332.tts_server_android.constant.MsTtsApiType
import com.github.jing332.tts_server_android.utils.FileUtils
import tts_server_lib.Tts_server_lib
import java.io.File
import java.util.*

class MsTtsEditRepository() {
    companion object {
        private val json by lazy { AppConst.jsonBuilder }
        private val EDGE_CACHE_PATH by lazy { "${app.cacheDir.path}/edge/voices.json" }
        private val AZURE_CACHE_PATH by lazy { "${app.cacheDir.path}/azure/voices.json" }
        private val CREATION_CACHE_PATH by lazy { "${app.cacheDir.path}/creation/voices.json" }
    }

    // 缓存
    private val mDataCacheMap: MutableMap<String, List<GeneralVoiceData>> = mutableMapOf()

    /**
     * 根据api获取数据
     */
    @Suppress("UNUSED_PARAMETER")
    suspend fun voicesByApi(@MsTtsApiType api: Int): List<GeneralVoiceData> {
        return withDefault {
            edgeVoices()
        }
    }

    // 帮助 获取并解析数据
    private suspend inline fun <reified T> getVoicesHelper(
        cachePath: String,
        crossinline loadData: () -> ByteArray?
    ): List<T> {
        val file = File(cachePath)
        return if (FileUtils.exists(file)) {
            json.decodeFromString(withIO { file.readText() })
        } else {
            val data = withIO { loadData.invoke() ?: throw Exception("数据为空") }
            FileUtils.saveFile(file, data)
            json.decodeFromString(data.decodeToString())
        }
    }

    private suspend fun edgeVoices(): List<GeneralVoiceData> {
        mDataCacheMap[EDGE_CACHE_PATH]?.let { return it }

        val list = getVoicesHelper<EdgeVoiceBean>(EDGE_CACHE_PATH) {
            Tts_server_lib.getEdgeVoices()
        }

        return list.map {
            GeneralVoiceData(
                gender = it.gender,
                locale = it.locale,
                voiceName = it.shortName,
                _localeName = it.friendlyName.split("-").getOrNull(1)?.trim()
            )
        }.apply { mDataCacheMap[EDGE_CACHE_PATH] = this }
    }
}

// 通用数据
data class GeneralVoiceData(
    /**
    //     * 性别 Male:男, Female:女
     */
    val gender: String,
    /**
     * UUID 仅Creation
     */
    val voiceId: String? = null,
    /**
     * 地区代码 zh-CN
     */
    val locale: String,
    /**
     * Voice zh-CN-XiaoxiaoNeural
     */
    val voiceName: String,

    private val _localeName: String? = null,

    /**
     * 本地化发音人名称 晓晓
     */
    private val _localVoiceName: String? = null,

    // 二级语言（语言技能） 仅限 en-US-JennyMultilingualNeural
    private val _secondaryLocales: Any? = null,

    // 风格列表 azure为List<String>, Creation为string 逗号分割
    private val _styles: Any? = null,
    // 角色列表
    private val _roles: Any? = null,
) {
    /**
     * 地区名
     */
    val localeName: String
        get() = Locale.forLanguageTag(locale).run { getDisplayName(this) }

    /**
     * 获取发音人本地化名称，edge则为汉化
     */
    val localVoiceName: String
        get() {
            _localVoiceName?.let { return _localVoiceName }
            return CnLocalMap.getEdgeVoice(voiceName)
        }

    /**
     * 获取风格列表
     */
    private val styleList: List<String>?
        get() = transformToList(_styles)

    /**
     * 获取本地化的风格列表
     * @return first: 原Key, second: 本地化value
     */
    val localStyleList: List<Pair<String, String>>?
        get() = (
                if (AppConst.isCnLocale) styleList?.map { Pair(it, CnLocalMap.getStyleAndRole(it)) }
                else styleList?.map { Pair(it, it) }
                )
            ?.also { if (it.isEmpty()) return null }

    /**
     * 获取角色列表
     */
    private val roleList: List<String>?
        get() = transformToList(_roles)

    /**
     * 获取汉化的角色列表
     * @return first: 原Key, second: 汉化Value
     */
    val localRoleList: List<Pair<String, String>>?
        get() = (
                if (AppConst.isCnLocale) roleList?.map { Pair(it, CnLocalMap.getStyleAndRole(it)) }
                else roleList?.map { Pair(it, it) }
                )
            ?.also { if (it.isEmpty()) return null }

    /**
     * 二级语言列表
     */
    private val secondaryLocaleList: List<String>?
        get() = transformToList(_secondaryLocales)

    /**
     * 汉化的二级语言列表
     * @return first: 原Key, second: 汉化Value
     */
    val localSecondaryLocaleList: List<Pair<String, String>>?
        get() = secondaryLocaleList?.map { Pair(it, Locale.forLanguageTag(it).displayLanguage) }
            ?.also { if (it.isEmpty()) return null }

    // 根据类型(String或List) 自动转换
    private fun transformToList(obj: Any?): List<String>? {
        obj?.let { v ->
            if (v is List<*>) {
                return v.map { it.toString() }
            } else if (v is String) {
                return v.split(",").filter { it.isNotBlank() }
            }
        }
        return null
    }

}