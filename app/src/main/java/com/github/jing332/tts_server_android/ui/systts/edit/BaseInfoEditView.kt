package com.github.jing332.tts_server_android.ui.systts.edit

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.RadioButton
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.children
import androidx.core.widget.addTextChangedListener
import com.github.jing332.tts_server_android.R
import com.github.jing332.tts_server_android.constant.ReadAloudTarget
import com.github.jing332.tts_server_android.data.appDb
import com.github.jing332.tts_server_android.data.entities.systts.SystemTts
import com.github.jing332.tts_server_android.data.entities.systts.SystemTtsGroup
import com.github.jing332.tts_server_android.databinding.SysttsBaseInfoEditViewBinding
import com.github.jing332.tts_server_android.ui.custom.adapter.initAccessibilityDelegate
import com.github.jing332.tts_server_android.ui.custom.widget.ConvenientSeekbar
import com.github.jing332.tts_server_android.ui.custom.widget.spinner.SpinnerItem

class BaseInfoEditView(context: Context, attrs: AttributeSet?, defaultStyle: Int) :
    ConstraintLayout(context, attrs, defaultStyle), ConvenientSeekbar.OnSeekBarChangeListener {
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context) : this(context, null, 0)

    private val binding: SysttsBaseInfoEditViewBinding by lazy {
        SysttsBaseInfoEditViewBinding.inflate(LayoutInflater.from(context), this, true)
    }

    @ReadAloudTarget
    var raTarget: Int = ReadAloudTarget.ALL
        set(value) {
            field = value
            binding.radioGroup.children.forEach { (it as RadioButton).isChecked = false }
            (binding.radioGroup.getChildAt(field) as RadioButton).isChecked = true
            mData?.apply { readAloudTarget = field }
        }

    var displayName: String
        get() = binding.displayName ?: ""
        set(value) {
            binding.displayName = value
        }

    val currentGroup: SystemTtsGroup
        get() = binding.groupItems!![binding.groupCurrentPosition].value as SystemTtsGroup

    private var mData: SystemTts? = null

    fun setData(data: SystemTts, groupList: List<SystemTtsGroup> = appDb.systemTtsDao.allGroup) {
        this.mData = data
        this.displayName = data.displayName ?: ""
        raTarget = data.readAloudTarget
        binding.groupItems = groupList.map { SpinnerItem(it.name, it) }
        binding.groupCurrentPosition = groupList.indexOfFirst { it.id == data.groupId }
    }

    fun checkDisplayNameEmpty(): Boolean {
        if (mData?.displayName.isNullOrEmpty()) {
            binding.etName.error = context.getString(R.string.cannot_empty)
            return true
        }
        return false
    }

    init {
        binding.tilGroup.initAccessibilityDelegate()
        binding.tilName.initAccessibilityDelegate()

        binding.etName.addTextChangedListener {
            mData?.apply { displayName = this@BaseInfoEditView.displayName }
        }
        binding.radioGroup.setOnCheckedChangeListener { _, checkedId ->
            val raTarget = when (checkedId) {
                R.id.radioBtn_ra_all -> ReadAloudTarget.ALL
                R.id.radioBtn_only_ra_aside -> ReadAloudTarget.ASIDE
                R.id.radioBtn_only_ra_dialogue -> ReadAloudTarget.DIALOGUE
                else -> -1
            }
            if (this.raTarget != raTarget) this.raTarget = raTarget
        }

        binding.spinnerGroup.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                mData?.apply { groupId = currentGroup.id }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

}