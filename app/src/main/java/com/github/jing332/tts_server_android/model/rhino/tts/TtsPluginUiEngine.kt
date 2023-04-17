package com.github.jing332.tts_server_android.model.rhino.tts

import android.content.Context
import android.widget.LinearLayout
import com.github.jing332.tts_server_android.constant.AppConst
import com.github.jing332.tts_server_android.model.speech.tts.PluginTTS
import com.github.jing332.tts_server_android.util.dp
import com.google.common.collect.HashBasedTable
import org.mozilla.javascript.Hashtable
import org.mozilla.javascript.NativeMap
import org.mozilla.javascript.NativeObject

class TtsPluginUiEngine(
    private val pluginTts: PluginTTS,
    val context: Context,
) :
    TtsPluginEngine(pluginTTS = pluginTts, context = context) {
    companion object {
        const val FUNC_SAMPLE_RATE = "getAudioSampleRate"
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

    @Suppress("UNCHECKED_CAST")
    fun getLocales(): List<String> {
        return rhino.invokeMethod(editUiJsObject, FUNC_LOCALES).run {
            if (this == null) emptyList()
            else this as List<String>
        }
    }

    @Suppress("UNCHECKED_CAST")
    fun getVoices(locale: String): Map<String, String> {
        val nati = NativeMap()

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

        rhino.invokeMethod(
            editUiJsObject,
            FUNC_ON_LOAD_DATA
        )
    }

    fun onLoadUI(context: Context, container: LinearLayout) {
        logger.d("onLoadUI()...")

        rhino.invokeMethod(
            editUiJsObject,
            FUNC_ON_LOAD_UI,
            context,
            container
        )
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