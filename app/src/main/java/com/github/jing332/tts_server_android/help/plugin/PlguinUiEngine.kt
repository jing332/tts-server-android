package com.github.jing332.tts_server_android.help.plugin

import android.content.Context
import android.widget.LinearLayout
import com.github.jing332.tts_server_android.constant.AppConst
import com.github.jing332.tts_server_android.model.tts.PluginTTS
import com.github.jing332.tts_server_android.util.dp
import org.mozilla.javascript.NativeObject

class PlguinUiEngine(val pluginTts: PluginTTS) : PluginEngine(
    pluginTTS = pluginTts
) {
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
        val importCode = "importPackage(${AppConst.PACKET_NAME}.help.plugin.ui);" +
                "importPackage(android.view);" +
                "importPackage(android.widget);"

        eval(importCode)
        scriptEngine.get(OBJ_UI_JS).run {
            if (this == null) throw Exception("Object not found: $OBJ_UI_JS")
            else (this as NativeObject)
        }
    }

    fun getSampleRate(locale: String, voice: String): Int? {
        LogOutputter.writeLine("getAudioSampleRate()...")

        return scriptEngine.invokeMethod(
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
        return scriptEngine.invokeMethod(editUiJsObject, FUNC_LOCALES).run {
            if (this == null) emptyList()
            else this as List<String>
        }
    }

    @Suppress("UNCHECKED_CAST")
    fun getVoices(locale: String): Map<String, String> {
        return scriptEngine.invokeMethod(editUiJsObject, FUNC_VOICES, locale).run {
            if (this == null) emptyMap()
            else this as Map<String, String>
        }
    }

    fun onLoadData() {
        LogOutputter.writeLine("onLoadData()...")

        scriptEngine.invokeMethod(
            editUiJsObject,
            FUNC_ON_LOAD_DATA
        )
    }

    fun onLoadUI(context: Context, container: LinearLayout) {
        LogOutputter.writeLine("onLoadUI()...")

        scriptEngine.invokeMethod(
            editUiJsObject,
            FUNC_ON_LOAD_UI,
            context,
            container
        )
    }

    fun onVoiceChanged(locale: String, voice: String) {
        LogOutputter.writeLine("onVoiceChanged()...")

        scriptEngine.invokeMethod(
            editUiJsObject,
            FUNC_ON_VOICE_CHANGED,
            locale,
            voice
        )
    }
}