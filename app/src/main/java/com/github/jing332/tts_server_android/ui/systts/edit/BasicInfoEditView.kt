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
import com.github.jing332.tts_server_android.databinding.SysttsBaseInfoEditViewBinding
import com.github.jing332.tts_server_android.ui.custom.widget.spinner.SpinnerItem
import com.github.jing332.tts_server_android.util.runOnIO
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope

class BasicInfoEditView(context: Context, attrs: AttributeSet?, defaultStyle: Int) :
    ConstraintLayout(context, attrs, defaultStyle) {
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context) : this(context, null, 0)

    private val binding: SysttsBaseInfoEditViewBinding by lazy {
        SysttsBaseInfoEditViewBinding.inflate(LayoutInflater.from(context), this, true)
    }

//    var isStandby

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
    }

    fun checkDisplayNameEmpty(): Boolean {
        if (mData?.displayName.isNullOrEmpty()) {
            binding.etName.error = context.getString(R.string.cannot_empty)
            return true
        }
        return false
    }

    init {
        binding.etName.addTextChangedListener {
            mData?.apply { displayName = this@BasicInfoEditView.displayName }
        }

        binding.btnGroupRaTarget.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                val raTarget = when (checkedId) {
                    R.id.btn_aside -> ReadAloudTarget.ASIDE
                    R.id.btn_dialogue -> ReadAloudTarget.DIALOGUE
                    else -> ReadAloudTarget.ALL
                }
                if (this.raTarget != raTarget) this.raTarget = raTarget
            }

        }

        binding.spinnerGroup.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?, view: View?, position: Int, id: Long
            ) {
                mData?.apply { groupId = currentGroup.id }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

}