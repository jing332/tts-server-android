package com.github.jing332.tts_server_android.model.rhino.tts

import android.content.Context
import android.widget.LinearLayout
import com.github.jing332.tts_server_android.constant.AppConst
import com.github.jing332.tts_server_android.model.speech.tts.PluginTTS
import com.github.jing332.tts_server_android.utils.dp
import org.mozilla.javascript.NativeObject
import java.util.Locale

class TtsPluginUiEngine(
    private val pluginTts: PluginTTS,
    val context: Context,
) : TtsPluginEngine(pluginTTS = pluginTts, context = context) {
    companion object {
        const val FUNC_SAMPLE_RATE = "getAudioSampleRate"
        const val FUNC_IS_NEED_DECODE = "isNeedDecode"

        const val FUNC_LOCALES = "getLocales"
        const val FUNC_VOICES = "getVoices"

        const val FUNC_ON_LOAD_UI = "onLoadUI"
        const val FUNC_ON_LOAD_DATA = "onLoadData"
        const val FUNC_ON_VOICE_CHANGED = "onVoiceChanged"

        const val OBJ_UI_JS = "EditorJS"
    }

    fun dp(px: Int): Int {
        return px.dp
    }

    private val editUiJsObject: NativeObject by lazy {
        val importCode = "importPackage(${AppConst.PACKET_NAME}.model.rhino.core.type.ui);" +
                "importPackage(android.view);" +
                "importPackage(android.widget);"

        eval(importCode)
        findObject(OBJ_UI_JS)
    }


    fun getSampleRate(locale: String, voice: String): Int? {
        logger.d("getSampleRate()...")

        return rhino.invokeMethod(
            editUiJsObject,
            FUNC_SAMPLE_RATE,
            locale,
            voice
        )?.run {
            return if (this is Int) this
            else (this as Double).toInt()
        }
    }

    fun isNeedDecode(locale: String, voice: String): Boolean {
        logger.d("isNeedDecode()...")

        return try {
            rhino.invokeMethod(
                editUiJsObject,
                FUNC_IS_NEED_DECODE,
                locale,
                voice
            )?.run {
                if (this is Boolean) this
                else (this as Double).toInt() == 1
            } ?: true
        } catch (_: NoSuchMethodException) {
            true
        }
    }

    fun getLocales(): Map<String, String> {
        return rhino.invokeMethod(editUiJsObject, FUNC_LOCALES).run {
            when (this) {
                is List<*> -> this.associate {
                    it.toString() to Locale.forLanguageTag(it.toString()).run {
                        this.getDisplayName(this)
                    }
                }

                is Map<*, *> -> {
                    this.map { (key, value) ->
                        key.toString() to value.toString()
                    }.toMap()
                }

                else -> emptyMap()
            }
        }
    }

    fun getVoices(locale: String): Map<String, String> {
        return rhino.invokeMethod(editUiJsObject, FUNC_VOICES, locale).run {
            when (this) {
                is Map<*, *> -> {
                    this.map { (key, value) ->
                        key.toString() to value.toString()
                    }.toMap()
                }
                /* is NativeMap -> {
                     val entries = NativeMap::class.java.getDeclaredField("entries").apply {
                         isAccessible = true
                     }.get(this) as Hashtable
                     entries.forEach {
                         println(it)
                     }

                     emptyMap()
                 }*/
                else -> emptyMap()
            }
        }
    }

    fun onLoadData() {
        logger.d("onLoadData()...")

        try {
            rhino.invokeMethod(
                editUiJsObject,
                FUNC_ON_LOAD_DATA
            )
        } catch (_: NoSuchMethodException) {
        }
    }

    fun onLoadUI(context: Context, container: LinearLayout) {
        logger.d("onLoadUI()...")

        try {
            rhino.invokeMethod(
                editUiJsObject,
                FUNC_ON_LOAD_UI,
                context,
                container
            )
        } catch (_: NoSuchMethodException) {
        }
    }

    fun onVoiceChanged(locale: String, voice: String) {
        logger.d("onVoiceChanged()...")

        rhino.invokeMethod(
            editUiJsObject,
            FUNC_ON_VOICE_CHANGED,
            locale,
            voice
        )
    }
}