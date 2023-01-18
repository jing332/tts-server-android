package com.github.jing332.tts_server_android.ui.systts.edit

import android.annotation.SuppressLint
import android.content.Context
import android.text.Html
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.DividerItemDecoration
import com.drake.brv.BindingAdapter
import com.drake.brv.utils.linear
import com.drake.brv.utils.setup
import com.github.jing332.tts_server_android.R
import com.github.jing332.tts_server_android.databinding.SysttsLocalExtraParamsItemBinding
import com.github.jing332.tts_server_android.databinding.SysttsLocalParamsEditViewBinding
import com.github.jing332.tts_server_android.databinding.SysttsLocalParamsExtraEditViewBinding
import com.github.jing332.tts_server_android.model.tts.BaseTTS
import com.github.jing332.tts_server_android.model.tts.LocalTTS
import com.github.jing332.tts_server_android.model.tts.LocalTtsParameter
import com.github.jing332.tts_server_android.ui.custom.AppDialogs
import com.github.jing332.tts_server_android.ui.custom.widget.Seekbar
import com.github.jing332.tts_server_android.ui.custom.widget.spinner.MaterialSpinnerAdapter
import com.github.jing332.tts_server_android.ui.custom.widget.spinner.SpinnerItem
import com.github.jing332.tts_server_android.util.clickWithThrottle
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.lang.Integer.max

@Suppress("DEPRECATION")
@SuppressLint("SetTextI18n")
class LocalTtsParamsEditView(context: Context, attrs: AttributeSet?, defaultStyle: Int) :
    ConstraintLayout(context, attrs, defaultStyle) {
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context) : this(context, null, 0)

    private val binding: SysttsLocalParamsEditViewBinding by lazy {
        SysttsLocalParamsEditViewBinding.inflate(LayoutInflater.from(context), this, true)
    }

    private lateinit var mBrv: BindingAdapter

    private var mTts: LocalTTS? = null

    fun setData(tts: LocalTTS) {
        this.mTts = tts

        binding.seekbarRate.progress = tts.rate
        mBrv.models = tts.extraParams
    }

    init {
        binding.seekbarRate.onSeekBarChangeListener = object : Seekbar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: Seekbar, progress: Int, fromUser: Boolean) {
            }

            override fun onStopTrackingTouch(seekBar: Seekbar) {
                mTts?.rate = seekBar.progress
            }
        }
        binding.seekbarRate.valueFormatter = Seekbar.ValueFormatter { value, _ ->
            if (value == BaseTTS.VALUE_FOLLOW_SYSTEM) context.getString(R.string.follow_system_or_read_aloud_app)
            else value.toString()
        }

        binding.btnAddParams.clickWithThrottle {
            displayExtraParamsEditDialog {
                mTts?.extraParams = mTts?.extraParams ?: mutableListOf()

                mTts?.extraParams?.add(it)
                mBrv.models = mTts?.extraParams
            }
        }

        binding.rvExtraParams.addItemDecoration(
            DividerItemDecoration(context, DividerItemDecoration.VERTICAL)
        )
        mBrv = binding.rvExtraParams.linear().setup {
            addType<LocalTtsParameter>(R.layout.systts_local_extra_params_item)
            onCreate {
                getBinding<SysttsLocalExtraParamsItemBinding>().apply {
                    btnEdit.clickWithThrottle {
                        displayExtraParamsEditDialog(getModel()) {
                            mutable[modelPosition] = it
                            notifyItemChanged(modelPosition)
                        }
                    }
                    btnDelete.clickWithThrottle {
                        AppDialogs.displayDeleteDialog(context, tv.text.toString()) {
                            mBrv.mutable.removeAt(modelPosition)
                            notifyItemRemoved(modelPosition)
                        }
                    }
                }
            }

            onBind {
                getBinding<SysttsLocalExtraParamsItemBinding>().apply {
                    val model = getModel<LocalTtsParameter>()
                    tv.text =
                        Html.fromHtml("<b>${model.type}</b> ${model.key} = <i>${model.value}</i>")
                }
            }
        }

    }

    private fun displayExtraParamsEditDialog(
        data: LocalTtsParameter = LocalTtsParameter("Boolean", "", ""),
        onDone: (p: LocalTtsParameter) -> Unit
    ) {
        val binding =
            SysttsLocalParamsExtraEditViewBinding.inflate(LayoutInflater.from(context), null, false)
                .apply {
                    tilKey.editText?.setText(data.key)
                    tilValue.editText?.setText(data.value)
                }


        val types = LocalTtsParameter.typeList.map { SpinnerItem(it, it) }
        binding.spinnerType.setAdapter(MaterialSpinnerAdapter(context, types))
        binding.spinnerType.selectedPosition =
            max(types.indexOfFirst { it.value.toString() == data.type }, 0)
        binding.spinnerType.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                types[position].value
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }

        MaterialAlertDialogBuilder(context)
            .setTitle(R.string.edit_extra_parameter)
            .setView(binding.root)
            .setPositiveButton(R.string.save) { _, _ ->
                val type = types[binding.spinnerType.selectedPosition].value.toString()
                val key = binding.tilKey.editText?.text.toString()
                val value = binding.tilValue.editText?.text.toString()

                onDone.invoke(LocalTtsParameter(type, key, value))
            }
            .show()
    }
}

