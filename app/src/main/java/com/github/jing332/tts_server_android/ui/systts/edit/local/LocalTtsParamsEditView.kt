package com.github.jing332.tts_server_android.ui.systts.edit.local

import android.annotation.SuppressLint
import android.content.Context
import android.text.Html
import android.text.InputType
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ArrayAdapter
import androidx.appcompat.app.AlertDialog
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.DividerItemDecoration
import com.drake.brv.BindingAdapter
import com.drake.brv.utils.linear
import com.drake.brv.utils.setup
import com.github.jing332.tts_server_android.R
import com.github.jing332.tts_server_android.databinding.SysttsLocalExtraParamsItemBinding
import com.github.jing332.tts_server_android.databinding.SysttsLocalParamsEditViewBinding
import com.github.jing332.tts_server_android.databinding.SysttsLocalParamsExtraEditViewBinding
import com.github.jing332.tts_server_android.model.speech.tts.ITextToSpeechEngine
import com.github.jing332.tts_server_android.model.speech.tts.LocalTTS
import com.github.jing332.tts_server_android.model.speech.tts.LocalTtsParameter
import com.github.jing332.tts_server_android.ui.systts.edit.BaseParamsEditView
import com.github.jing332.tts_server_android.ui.view.AppDialogs
import com.github.jing332.tts_server_android.ui.view.widget.Seekbar
import com.github.jing332.tts_server_android.ui.view.widget.spinner.MaterialSpinnerAdapter
import com.github.jing332.tts_server_android.ui.view.widget.spinner.SpinnerItem
import com.github.jing332.tts_server_android.utils.clickWithThrottle
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.lang.Integer.max

@Suppress("DEPRECATION")
@SuppressLint("SetTextI18n")
class LocalTtsParamsEditView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defaultStyle: Int = 0
) : BaseParamsEditView<SysttsLocalParamsEditViewBinding, LocalTTS>(context, attrs, defaultStyle) {
    private lateinit var mBrv: BindingAdapter

    override var tts: LocalTTS? = null

    override fun setData(tts: LocalTTS) {
        super.setData(tts)

        binding.seekbarRate.progress = tts.rate
        binding.seekbarPitch.progress = tts.pitch
        binding.cbDirectPlay.isChecked = tts.isDirectPlayMode
        binding.spinnerSampleRate.setText(tts.audioFormat.sampleRate.toString())

        mBrv.models = tts.extraParams
    }

    init {
        binding.seekbarRate.onSeekBarChangeListener = object : Seekbar.OnSeekBarChangeListener {
            override fun onStopTrackingTouch(seekBar: Seekbar) {
                tts?.rate = seekBar.progress
            }
        }
        binding.seekbarRate.valueFormatter = Seekbar.ValueFormatter { value, _ ->
            if (value == ITextToSpeechEngine.VALUE_FOLLOW_SYSTEM) context.getString(R.string.follow_system_or_read_aloud_app)
            else value.toString()
        }

        binding.seekbarRate.valueFormatter = Seekbar.ValueFormatter { value, _ ->
            if (value == ITextToSpeechEngine.VALUE_FOLLOW_SYSTEM) context.getString(R.string.follow_system_or_read_aloud_app)
            else value.toString()
        }

        binding.seekbarPitch.setFloatType(2)
        binding.seekbarPitch.valueFormatter = Seekbar.ValueFormatter { value, _ ->
            if (value == 0f) context.getString(R.string.follow_system_or_read_aloud_app)
            else value.toString()
        }
        binding.seekbarPitch.onSeekBarChangeListener = object : Seekbar.OnSeekBarChangeListener {
            override fun onStopTrackingTouch(seekBar: Seekbar) {
                tts?.pitch = ((seekBar.value as Float) * 100).toInt()
            }
        }

        binding.btnAddParams.clickWithThrottle {
            displayExtraParamsEditDialog {
                tts?.extraParams = tts?.extraParams ?: mutableListOf()

                tts?.extraParams?.add(it)
                mBrv.models = tts?.extraParams
            }
        }

        binding.btnHelpDirectPlay.clickWithThrottle {
            MaterialAlertDialogBuilder(context)
                .setTitle(R.string.systts_direct_play_help)
                .setMessage(R.string.systts_direct_play_help_msg)
                .show()
        }

        binding.tilSampleRate.setStartIconOnClickListener {
            MaterialAlertDialogBuilder(context)
                .setTitle(R.string.systts_sample_rate)
                .setMessage(R.string.systts_help_sample_rate)
                .show()
        }

        binding.spinnerSampleRate.addTextChangedListener {
            tts?.audioFormat?.sampleRate = it.toString().toInt()
        }

        // 采样率
        val adapter = ArrayAdapter<String>(context, android.R.layout.simple_spinner_dropdown_item)
        adapter.addAll(resources.getStringArray(R.array.sample_rate_list).toList())
        binding.spinnerSampleRate.setAdapter(adapter)
        // 不过滤
        binding.spinnerSampleRate.threshold = Int.MAX_VALUE

        binding.cbDirectPlay.setOnClickListener {
            tts?.isDirectPlayMode = binding.cbDirectPlay.isChecked
        }

        binding.rvExtraParams.addItemDecoration(
            DividerItemDecoration(context, DividerItemDecoration.VERTICAL)
        )
        mBrv = binding.rvExtraParams.linear().setup {
            addType<LocalTtsParameter>(R.layout.systts_local_extra_params_item)
            onCreate {
                getBinding<SysttsLocalExtraParamsItemBinding>().apply {
                    itemView.clickWithThrottle {
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
        data: LocalTtsParameter = LocalTtsParameter("Boolean", "", true.toString()),
        onDone: (p: LocalTtsParameter) -> Unit
    ) {
        val types = LocalTtsParameter.typeList.map { SpinnerItem(it, it) }
        val binding =
            SysttsLocalParamsExtraEditViewBinding.inflate(LayoutInflater.from(context), null, false)
                .apply {
                    spinnerType.setAdapter(MaterialSpinnerAdapter(context, types))
                    spinnerType.selectedPosition =
                        max(types.indexOfFirst { it.value.toString() == data.type }, 0)
                    spinnerType.onItemSelectedListener = object : OnItemSelectedListener {
                        override fun onItemSelected(
                            parent: AdapterView<*>?,
                            view: View?,
                            position: Int,
                            id: Long
                        ) {
                            tilValue.editText?.setText("")
                            tilValue.visibility =
                                if (position == 0) View.GONE else View.VISIBLE
                            groupBoolValue.visibility =
                                if (position == 0) View.VISIBLE else View.GONE

                            tilValue.editText?.inputType = when (types[position].value) {
                                "Int" -> InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_SIGNED
                                "Float" -> InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_SIGNED or InputType.TYPE_NUMBER_FLAG_DECIMAL
                                else -> InputType.TYPE_CLASS_TEXT
                            }
                        }

                        override fun onNothingSelected(parent: AdapterView<*>?) {
                        }
                    }

                    spinnerType.onItemSelectedListener.onItemSelected(null, null, 0, 0)
                    tilKey.editText?.setText(data.key)
                    if (data.type == "Boolean") {
                        if (data.value.toBoolean()) {
                            groupBoolValue.check(R.id.btn_true)
                        } else
                            groupBoolValue.check(R.id.btn_false)
                    } else {
                        tilValue.visibility = View.VISIBLE
                        groupBoolValue.visibility = View.GONE
                        tilValue.editText?.setText(data.value)
                    }
                }


        val dlg = MaterialAlertDialogBuilder(context)
            .setTitle(R.string.edit_extra_parameter)
            .setView(binding.root)
            .setPositiveButton(R.string.save, null)
            .create()

        dlg.show()
        dlg.getButton(AlertDialog.BUTTON_POSITIVE).clickWithThrottle {
            val type = types[binding.spinnerType.selectedPosition].value.toString()
            val key = binding.tilKey.editText?.text.toString()

            if (key.isBlank()) {
                binding.tilKey.editText?.error = context.getString(R.string.cannot_empty)
                binding.tilKey.requestFocus()
                return@clickWithThrottle
            }

            val value = if (type == "Boolean") {
                (binding.groupBoolValue.checkedButtonId == R.id.btn_true).toString()
            } else {
                val text = binding.tilValue.editText?.text.toString()
                if (text.isBlank()) {
                    binding.tilValue.editText?.error = context.getString(R.string.cannot_empty)
                    binding.tilValue.requestFocus()
                    return@clickWithThrottle
                }

                text
            }

            onDone.invoke(LocalTtsParameter(type, key, value))
            dlg.dismiss()
        }
    }
}

