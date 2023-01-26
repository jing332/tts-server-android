package com.github.jing332.tts_server_android.ui.systts.edit

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.widget.addTextChangedListener
import com.github.jing332.tts_server_android.R
import com.github.jing332.tts_server_android.constant.ReadAloudTarget
import com.github.jing332.tts_server_android.data.appDb
import com.github.jing332.tts_server_android.data.entities.systts.SystemTts
import com.github.jing332.tts_server_android.data.entities.systts.SystemTtsGroup
import com.github.jing332.tts_server_android.databinding.DialogInAppPlaySettingsBinding
import com.github.jing332.tts_server_android.databinding.SysttsBasicInfoEditViewBinding
import com.github.jing332.tts_server_android.help.SysTtsConfig
import com.github.jing332.tts_server_android.model.tts.AudioPlayer
import com.github.jing332.tts_server_android.ui.custom.widget.Seekbar
import com.github.jing332.tts_server_android.ui.custom.widget.spinner.SpinnerItem
import com.github.jing332.tts_server_android.util.clickWithThrottle
import com.github.jing332.tts_server_android.util.runOnIO
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

            binding.btnGroupRaTarget.check(
                when (value) {
                    ReadAloudTarget.ASIDE -> R.id.btn_aside
                    ReadAloudTarget.DIALOGUE -> R.id.btn_dialogue
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

    private var mData: SystemTts? = null

    @OptIn(DelicateCoroutinesApi::class)
    fun setData(data: SystemTts) {
        GlobalScope.runOnIO {
            val groupList = appDb.systemTtsDao.allGroup
            binding.groupItems = groupList.map { SpinnerItem(it.name, it) }
            binding.groupCurrentPosition = groupList.indexOfFirst { it.id == data.groupId }
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
            btnSetPlayerParams.clickWithThrottle { displayPlayerParamsSettings() }

            cbStandby.setOnClickListener {
                mData?.isStandby = isStandby
            }

            etName.addTextChangedListener {
                mData?.apply { displayName = this@BasicInfoEditView.displayName }
            }

            btnGroupRaTarget.addOnButtonCheckedListener { _, checkedId, isChecked ->
                if (isChecked) {
                    val raTarget = when (checkedId) {
                        R.id.btn_aside -> ReadAloudTarget.ASIDE
                        R.id.btn_dialogue -> ReadAloudTarget.DIALOGUE
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
        }

    }

    private fun displayPlayerParamsSettings() {
        if (mData?.tts?.isDirectPlay() == true) {
            MaterialAlertDialogBuilder(context)
                .setTitle(R.string.warning)
                .setMessage(R.string.please_trun_off_direct_play)
                .setPositiveButton(android.R.string.ok) { _, _ ->
                }
                .show()
        }else if (SysTtsConfig.isInAppPlayAudio) {
            val binding =
                DialogInAppPlaySettingsBinding.inflate(LayoutInflater.from(context), this, false)
            binding.apply {
                val converter = object : Seekbar.ProgressConverter {
                    override fun progressToValue(progress: Int): Any {
                        return (progress * 0.01).toFloat()
                    }

                    override fun valueToProgress(value: Any): Int {
                        return ((value as Float) * 100).toInt()
                    }
                }

                val formatter = Seekbar.ValueFormatter { value, _ ->
                    if (value == AudioPlayer.VALUE_FOLLOW_GLOBAL) context.getString(R.string.follow)
                    else value.toString()
                }

                seekRate.progressConverter = converter
                seekRate.valueFormatter = formatter

                seekPitch.progressConverter = converter
                seekPitch.valueFormatter = formatter

                seekRate.min = 0
                seekPitch.min = 0
                switchOnOff.visibility = View.GONE
                tvTip.text = "启用内置播放器时，使用此处设置覆盖全局。"

                mData?.tts?.audioPlayer?.let {
                    seekRate.value = it.rate
                    seekPitch.value = it.pitch
                }
            }

            MaterialAlertDialogBuilder(context)
                .setTitle(R.string.set_built_in_player_params)
                .setView(binding.root)
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
                .setPositiveButton(android.R.string.ok) { _, _ ->
                }
                .show()
        }
    }

}