package com.github.jing332.tts_server_android.ui.systts.edit

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import com.github.jing332.tts_server_android.R
import com.github.jing332.tts_server_android.constant.ReadAloudTarget
import com.github.jing332.tts_server_android.data.appDb
import com.github.jing332.tts_server_android.data.entities.systts.SystemTts
import com.github.jing332.tts_server_android.data.entities.systts.SystemTtsGroup
import com.github.jing332.tts_server_android.databinding.SysttsBasicInfoEditViewBinding
import com.github.jing332.tts_server_android.databinding.SysttsBuiltinPlayerSettingsBinding
import com.github.jing332.tts_server_android.help.config.SysTtsConfig
import com.github.jing332.tts_server_android.model.tts.PlayerParams
import com.github.jing332.tts_server_android.ui.view.widget.Seekbar
import com.github.jing332.tts_server_android.ui.view.widget.spinner.SpinnerItem
import com.github.jing332.tts_server_android.util.clickWithThrottle
import com.github.jing332.tts_server_android.util.runOnIO
import com.github.jing332.tts_server_android.util.toast
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope

class BasicInfoEditView(context: Context, attrs: AttributeSet?, defaultStyle: Int) :
    ConstraintLayout(context, attrs, defaultStyle) {
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context) : this(context, null, 0)

    private val binding: SysttsBasicInfoEditViewBinding by lazy {
        SysttsBasicInfoEditViewBinding.inflate(LayoutInflater.from(context), this, true)
    }

    var isStandby: Boolean
        get() = binding.cbStandby.isChecked
        set(value) {
            binding.cbStandby.isChecked = value
        }

    @ReadAloudTarget
    var raTarget: Int = ReadAloudTarget.ALL
        set(value) {
            field = value
            mData?.apply { readAloudTarget = field }

            binding.tilCustomTag.isVisible = value == ReadAloudTarget.CUSTOM_TAG
            binding.btnGroupRaTarget.check(
                when (value) {
                    ReadAloudTarget.ASIDE -> R.id.btn_aside
                    ReadAloudTarget.DIALOGUE -> R.id.btn_dialogue
                    ReadAloudTarget.CUSTOM_TAG -> R.id.btn_custom_tag
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
        }


    @OptIn(DelicateCoroutinesApi::class)
    fun setData(data: SystemTts) {
        GlobalScope.runOnIO {
            val groupList = appDb.systemTtsDao.allGroup
            binding.groupItems = groupList.map { SpinnerItem(it.name, it) }
            binding.groupCurrentPosition = groupList.indexOfFirst { it.id == data.groupId }

//            appDb.readRuleDao.allEnabled.getOrNull(0)?.tags?.let { tags ->
//                binding.tagItems = tags.map { SpinnerItem("${it.value} (${it.key})", it) }
//                binding.tagCurrentPosition = tags.keys.indexOf(data.speechRule.tag)
//            }
        }

        this.mData = data
        this.displayName = data.displayName ?: ""
        raTarget = data.readAloudTarget
        isStandby = data.isStandby
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
                        R.id.btn_aside -> ReadAloudTarget.ASIDE
                        R.id.btn_dialogue -> ReadAloudTarget.DIALOGUE
                        R.id.btn_custom_tag -> ReadAloudTarget.CUSTOM_TAG
                        else -> ReadAloudTarget.ALL
                    }
                    if (this@BasicInfoEditView.raTarget != raTarget)
                        this@BasicInfoEditView.raTarget = raTarget
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

            spinnerTag.onItemSelectedListener = object : OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    mData?.apply {
                        tag = currentTag.key
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                    TODO("Not yet implemented")
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

                seekRate.min = 0
                seekPitch.min = 0
                switchOnOff.visibility = View.GONE
                tvTip.setText(R.string.builtin_player_settings_tip_msg)

                mData?.tts?.audioPlayer?.let {
                    seekRate.value = it.rate
                    seekPitch.value = it.pitch
                }
            }

            MaterialAlertDialogBuilder(context)
                .setTitle(R.string.set_built_in_player_params)
                .setView(binding.root)
                .setNeutralButton(R.string.reset) { _, _ ->
                    mData?.tts?.audioPlayer?.let {
                        it.rate = 1f
                        it.pitch = 1f
                    }
                    context.toast(R.string.ok_reset)
                }
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(android.R.string.ok) { _, _ ->
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

}