package com.github.jing332.tts_server_android.ui.systts.base

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import com.github.jing332.tts_server_android.data.entities.systts.SystemTts
import com.github.jing332.tts_server_android.databinding.SysttsQuickEditBottomSheetBinding
import com.github.jing332.tts_server_android.model.speech.tts.ITextToSpeechEngine
import com.github.jing332.tts_server_android.ui.systts.edit.BaseParamsEditView
import com.github.jing332.tts_server_android.ui.systts.edit.BasicInfoEditView
import com.github.jing332.tts_server_android.utils.setMarginMatchParent
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class QuickEditBottomSheet @JvmOverloads constructor(
    val data: SystemTts? = null,
    private val isLiteMode: Boolean = false,
    private val editView: BaseParamsEditView<*, ITextToSpeechEngine>? = null,
    private val dismissCallback: ((dialog: DialogInterface) -> Boolean)? = null
) : BottomSheetDialogFragment() {

    companion object {
        const val TAG = "QuickEditBottomSheet"
    }

    private val binding by lazy { SysttsQuickEditBottomSheetBinding.inflate(layoutInflater) }

    val basicEdit: BasicInfoEditView
        get() = binding.basicEdit

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = binding.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (data == null || editView == null) {
            dismiss()
            return
        }

        (view.parent as ViewGroup).setMarginMatchParent()

        basicEdit.liteModeEnabled = isLiteMode
        basicEdit.setData(data, lifecycleScope)
        setContent(editView)
        editView.setData(data.tts)
    }

//    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
//        return BottomSheetDialog(requireContext(), theme).apply {
//            behavior.state = BottomSheetBehavior.STATE_EXPANDED
//        }
//    }

    private fun setContent(view: View) {
        binding.container.removeAllViews()
        binding.container.addView(view)
    }


    override fun onDismiss(dialog: DialogInterface) {
        basicEdit.saveData()

        if (dismissCallback == null) {
            super.onDismiss(dialog)
        } else if (dismissCallback.invoke(dialog))
            super.onDismiss(dialog)
    }
}