package com.github.jing332.tts_server_android.ui.systts.edit

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import com.github.jing332.tts_server_android.R
import com.github.jing332.tts_server_android.constant.AppConst
import com.github.jing332.tts_server_android.constant.SpeechTarget
import com.github.jing332.tts_server_android.data.appDb
import com.github.jing332.tts_server_android.data.entities.SpeechRule
import com.github.jing332.tts_server_android.data.entities.systts.SystemTts
import com.github.jing332.tts_server_android.data.entities.systts.SystemTtsGroup
import com.github.jing332.tts_server_android.databinding.SysttsBasicInfoEditViewBinding
import com.github.jing332.tts_server_android.databinding.SysttsBuiltinPlayerSettingsBinding
import com.github.jing332.tts_server_android.help.config.SysTtsConfig
import com.github.jing332.tts_server_android.model.rhino.speech_rule.SpeechRuleEngine
import com.github.jing332.tts_server_android.model.speech.tts.AudioParams
import com.github.jing332.tts_server_android.model.speech.tts.PlayerParams
import com.github.jing332.tts_server_android.ui.systts.AudioParamsSettingsView
import com.github.jing332.tts_server_android.ui.view.AppDialogs
import com.github.jing332.tts_server_android.ui.view.AppDialogs.displayErrorDialog
import com.github.jing332.tts_server_android.ui.view.AppMaterialSpinner
import com.github.jing332.tts_server_android.ui.view.MaterialTextInput
import com.github.jing332.tts_server_android.ui.view.widget.Seekbar
import com.github.jing332.tts_server_android.ui.view.widget.spinner.SpinnerItem
import com.github.jing332.tts_server_android.utils.clickWithThrottle
import com.github.jing332.tts_server_android.utils.layoutInflater
import com.github.jing332.tts_server_android.utils.runOnUI
import com.github.jing332.tts_server_android.utils.toast
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.*
import kotlinx.serialization.decodeFromString
import java.lang.Integer.max

class BasicInfoEditView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defaultStyle: Int = 0
) : ConstraintLayout(context, attrs, defaultStyle) {

    private val binding: SysttsBasicInfoEditViewBinding by lazy {
        SysttsBasicInfoEditViewBinding.inflate(context.layoutInflater, this, true)
    }

    var isStandby: Boolean
        get() = binding.cbStandby.isChecked
        set(value) {
            binding.cbStandby.isChecked = value
        }

    @SpeechTarget
    var raTarget: Int = SpeechTarget.ALL
        set(value) {
            field = value
            mData?.apply { speechTarget = field }

            binding.layoutSpeechRule.isVisible = value == SpeechTarget.CUSTOM_TAG

            if (binding.layoutSpeechRule.isVisible)
                binding.spinnerTag.callItemSelected()
            else
                mData?.apply {
                    speechRule.tag = ""
                    speechRule.tagRuleId = ""
                }

            binding.btnGroupRaTarget.check(
                when (value) {
                    SpeechTarget.CUSTOM_TAG -> R.id.btn_custom_tag
                    else -> R.id.btn_ra_all
                }
            )
        }

    var displayName: String
        get() = binding.displayName ?: ""
        set(value) {
            binding.displayName = value
        }

    val currentGroup: SystemTtsGroup
        get() = binding.groupItems!![binding.groupCurrentPosition].value as SystemTtsGroup

    val currentRule: SpeechRule?
        get() {
            return binding.ruleItems?.getOrNull(binding.ruleCurrentPosition)?.let {
                return it.value as SpeechRule
            }
        }

    // {key = dialogue, value = 对话}
    @Suppress("UNCHECKED_CAST")
    val currentTag: Map.Entry<String, String>
        get() {
            binding.tagItems!![binding.tagCurrentPosition].let {
                return it.value as Map.Entry<String, String>
            }
        }

    private var mData: SystemTts? = null

    var liteModeEnabled: Boolean = false
        set(value) {
            field = value
            binding.btnGroupRaTarget.isGone = value
            binding.btnStandbyHelp.isGone = value
            binding.cbStandby.isGone = value
            binding.btnPlayerSettings.isGone = value
            binding.btnAudioParams.isGone = value
        }

    fun saveData() {
        kotlin.runCatching {
            currentRule?.let {
                val engine = SpeechRuleEngine(context, it)
                engine.eval()
                try {
                    mData?.speechRule?.tagName =
                        engine.getTagName(currentTag.key, mData?.speechRule?.tagData ?: emptyMap())
                } catch (_: NoSuchMethodException) {
                }
            }
        }.onFailure {
            context.displayErrorDialog(it, "获取标签名失败")
        }
    }

    fun setData(data: SystemTts, coroutineScope: CoroutineScope) {
        coroutineScope.launch(Dispatchers.IO) {
            val groupList = appDb.systemTtsDao.allGroup
            binding.groupItems = groupList.map { SpinnerItem(it.name, it) }
            binding.groupCurrentPosition = groupList.indexOfFirst { it.id == data.groupId }

            if (!liteModeEnabled) {
                val ruleList = appDb.speechRule.allEnabled
                binding.ruleItems = ruleList.map { SpinnerItem(it.name, it) }
                binding.ruleCurrentPosition =
                    max(0, ruleList.indexOfFirst { it.ruleId == data.speechRule.tagRuleId })
                binding.spinnerSpeechRule.callItemSelected()
            }
        }


        mData = data
        displayName = data.displayName ?: ""
        raTarget = data.speechRule.target

        isStandby = data.speechRule.isStandby
    }

    fun checkDisplayNameEmpty(): Boolean {
        if (mData?.displayName.isNullOrEmpty()) {
            binding.etName.error = context.getString(R.string.cannot_empty)
            return true
        }
        return false
    }

    init {
        binding.apply {
            btnAudioParams.clickWithThrottle { displayAudioParamsSettings() }
            btnPlayerSettings.clickWithThrottle { displayPlayerParamsSettings() }
            btnStandbyHelp.clickWithThrottle {
                MaterialAlertDialogBuilder(context).setTitle(R.string.systts_as_standby_help)
                    .setMessage(R.string.systts_standby_help_msg)
                    .setPositiveButton(android.R.string.ok, null)
                    .show()
            }

            cbStandby.setOnClickListener {
                if (!liteModeEnabled) mData?.isStandby = isStandby
            }

            etName.addTextChangedListener {
                mData?.apply { displayName = this@BasicInfoEditView.displayName }
            }

            btnGroupRaTarget.addOnButtonCheckedListener { _, checkedId, isChecked ->
                if (!liteModeEnabled && isChecked) {
                    val raTarget = when (checkedId) {
                        R.id.btn_custom_tag -> SpeechTarget.CUSTOM_TAG
                        else -> SpeechTarget.ALL
                    }
                    if (this@BasicInfoEditView.raTarget != raTarget)
                        this@BasicInfoEditView.raTarget = raTarget

                    if (raTarget == SpeechTarget.ALL) {
                        binding.layoutTagData.removeAllViews()
                        if (mData?.speechRule?.tagData?.isNotEmpty() == true) {
                            AppDialogs.displayDeleteDialog(
                                context,
                                "当前标签已有数据 ${mData?.speechRule?.tagData}，\n是否删除？"
                            ) {
                                mData?.speechRule?.resetTag()
                            }
                        }
                    }
                }
            }

            spinnerGroup.onItemSelectedListener = object : OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?, view: View?, position: Int, id: Long
                ) {
                    mData?.apply { groupId = currentGroup.id }
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }

            spinnerSpeechRule.onItemSelectedListener = object : OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    currentRule?.let { rule ->
                        binding.tagItems =
                            rule.tags.map { SpinnerItem("${it.value} (${it.key})", it) }
                        binding.tagCurrentPosition =
                            max(0, rule.tags.keys.indexOf(mData?.speechRule?.tag))
                        binding.spinnerTag.callItemSelected()
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {

                }

            }

            spinnerTag.onItemSelectedListener = object : OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    kotlin.runCatching {
                        updateTagUI()
                    }.onFailure {
                        context.displayErrorDialog(it)
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {

                }

            }
        }
    }

    private fun updateTagUI() {
        mData?.apply {
            if (raTarget != SpeechTarget.CUSTOM_TAG) return
            if (currentRule == null) return
            currentRule?.let {
                speechRule.tagRuleId = it.ruleId
                speechRule.tag = currentTag.key

                binding.layoutTagData.removeAllViews()
                runOnUI {
                    it.tagsData[speechRule.tag]?.forEach { defTag ->
                        val key = defTag.key ?: ""
                        val label = defTag.value["label"]
                        val hint = defTag.value["hint"]
                        var view: View? = null
                        val items = defTag.value["items"]
                        if (items.isNullOrEmpty()) {
                            val textInput = MaterialTextInput(context)
                            textInput.hint = label
                            textInput.isExpandedHintEnabled = true
                            textInput.placeholderText = hint
                            textInput.editText?.setText(
                                speechRule.tagData[key] ?: ""
                            )
                            textInput.editText?.addTextChangedListener { txt ->
                                speechRule.tagData[key] = txt.toString()
                            }
                            view = textInput
                        } else {
                            val spinenr = AppMaterialSpinner(context)
                            spinenr.hint = label
                            if (hint?.isNotEmpty() == true) {
                                spinenr.til.setStartIconDrawable(R.drawable.ic_baseline_help_outline_24)
                                spinenr.til.setStartIconContentDescription(R.string.tip)
                                spinenr.til.setStartIconOnClickListener {
                                    MaterialAlertDialogBuilder(context)
                                        .setTitle(label)
                                        .setMessage(hint)
                                        .setPositiveButton(android.R.string.ok, null)
                                        .show()
                                }
                            }

                            val itemsMap: Map<String, String> =
                                AppConst.jsonBuilder.decodeFromString(items)
                            val models = itemsMap.map { SpinnerItem(it.value, it.key) }
                            spinenr.setListModel(models)

                            val value = speechRule.tagData[key]
                            val defaultValue = defTag.value["default"] ?: ""

                            val index = models.indexOfFirst { it.value.toString() == value }
                            val pos =
                                if (index == -1) models.indexOfFirst { it.value.toString() == defaultValue } else index
                            spinenr.selectedPosition = max(0, pos)

                            spinenr.spinner.onItemSelectedListener =
                                object : OnItemSelectedListener {
                                    override fun onItemSelected(
                                        parent: AdapterView<*>?,
                                        view: View?,
                                        position: Int,
                                        id: Long
                                    ) {
                                        speechRule.tagData[key] =
                                            models.getOrNull(position)?.value?.toString() ?: ""
                                    }

                                    override fun onNothingSelected(parent: AdapterView<*>?) {
                                    }

                                }

                            view = spinenr
                        }

                        binding.layoutTagData.addView(
                            view,
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT
                        )
                    }
                }

            }
        }
    }

    private fun displayPlayerParamsSettings() {
        if (mData?.tts?.isDirectPlay() == true) {
            MaterialAlertDialogBuilder(context)
                .setTitle(R.string.warning)
                .setMessage(R.string.please_turn_off_direct_play)
                .setPositiveButton(android.R.string.ok, null)
                .show()
        } else if (SysTtsConfig.isInAppPlayAudio) {
            val binding =
                SysttsBuiltinPlayerSettingsBinding.inflate(
                    LayoutInflater.from(context), this, false
                )
            binding.apply {
                val formatter = Seekbar.ValueFormatter { value, _ ->
                    if (value == PlayerParams.VALUE_FOLLOW_GLOBAL) context.getString(R.string.follow)
                    else value.toString()
                }

                seekRate.setFloatType(2)
                seekRate.valueFormatter = formatter

                seekPitch.setFloatType(2)
                seekPitch.valueFormatter = formatter

                seekVolume.setFloatType(2)
                seekVolume.valueFormatter = formatter

                switchOnOff.visibility = View.GONE
                tvTip.setText(R.string.builtin_player_settings_tip_msg)

                mData?.tts?.audioPlayer?.let {
                    seekRate.value = it.rate
                    seekVolume.value = it.volume
                    seekPitch.value = it.pitch
                }
            }

            MaterialAlertDialogBuilder(context)
                .setTitle(R.string.set_built_in_player_params)
                .setView(binding.root)
                .setNegativeButton(R.string.reset) { _, _ ->
                    binding.apply {
                        seekRate.value = 1f
                        seekVolume.value = 1f
                        seekPitch.value = 1f
                    }
                    context.toast(R.string.ok_reset)
                }
                .setPositiveButton(R.string.close, null)
                .setOnDismissListener {
                    mData?.tts?.audioPlayer?.let {
                        it.rate = binding.seekRate.value as Float
                        it.pitch = binding.seekPitch.value as Float
                    }
                }
                .show()

        } else {
            MaterialAlertDialogBuilder(context)
                .setTitle(R.string.warning)
                .setMessage(R.string.built_in_player_not_enabled)
                .setPositiveButton(android.R.string.ok, null)
                .show()
        }
    }

    private fun displayAudioParamsSettings() {
        val view = AudioParamsSettingsView(context)
        view.setData(mData!!.tts.audioParams)

        MaterialAlertDialogBuilder(context)
            .setTitle(R.string.audio_params_settings)
            .apply {
                if (SysTtsConfig.isInAppPlayAudio) setMessage("⚠️内置播放器开启时此处参数无效。")
            }
            .setView(view)
            .setPositiveButton(R.string.close, null)
            .setNegativeButton(R.string.reset) { _, _ ->
                mData?.tts?.audioParams?.reset(AudioParams.FOLLOW_GLOBAL_VALUE)
                context.toast(R.string.ok_reset)
            }
            .show()
    }

}