package com.github.jing332.tts_server_android.ui.systts.edit

import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import com.github.jing332.tts_server_android.databinding.SysttsLocalEditActivityBinding
import com.github.jing332.tts_server_android.model.tts.LocalTTS
import com.github.jing332.tts_server_android.ui.custom.widget.spinner.MaterialSpinnerAdapter
import com.github.jing332.tts_server_android.ui.custom.widget.spinner.SpinnerItem
import kotlin.math.max

class LocalTtsEditActivity : BaseTtsEditActivity<LocalTTS>({ LocalTTS() }) {
    private val binding: SysttsLocalEditActivityBinding by lazy {
        SysttsLocalEditActivityBinding.inflate(layoutInflater)
    }

    private lateinit var engineItems: List<SpinnerItem>
    override fun onSave() {
        if (binding.basicEdit.displayName.isEmpty())
            systemTts.displayName = engineItems[binding.spinnerEngine.selectedPosition].displayText
        super.onSave()

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.apply {
            basicEdit.setData(systemTts)
            paramsEdit.setData(tts)

            engineItems = getEngines().map { SpinnerItem("${it.second} (${it.first})", it.first) }
            spinnerEngine.setAdapter(
                MaterialSpinnerAdapter(
                    this@LocalTtsEditActivity,
                    engineItems
                )
            )

            spinnerEngine.onItemSelectedListener = object : OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    tts.engine = engineItems[position].value.toString()
                    basicEdit.displayName = ""
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                }
            }
            spinnerEngine.selectedPosition = max(
                engineItems.indexOfFirst { it.value.toString() == tts.engine }, 0
            )

        }

    }


    // return Pair.first=packageName, Pair.second=labelName
    @Suppress("DEPRECATION")
    private fun getEngines(): List<Pair<String, String>> {
        val intent = Intent(TextToSpeech.Engine.INTENT_ACTION_TTS_SERVICE)
        return packageManager.queryIntentServices(intent, PackageManager.MATCH_DEFAULT_ONLY).map {
            val res = packageManager.getResourcesForApplication(it.serviceInfo.applicationInfo)
            val packageName = it.serviceInfo.packageName
            val labelName = try {
                res.getString(it.serviceInfo.labelRes)
            } catch (_: Resources.NotFoundException) {
                ""
            }

            return@map Pair(packageName, labelName)
        }
    }
}