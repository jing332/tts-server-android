package com.github.jing332.tts_server_android.ui.systts.base

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.github.jing332.tts_server_android.data.entities.systts.SystemTts
import com.github.jing332.tts_server_android.databinding.SysttsQuickEditBottomSheetBinding
import com.github.jing332.tts_server_android.model.tts.ITextToSpeechEngine
import com.github.jing332.tts_server_android.ui.systts.edit.BaseParamsEditView
import com.github.jing332.tts_server_android.ui.systts.edit.BasicInfoEditView
import com.github.jing332.tts_server_android.util.setMarginMatchParent
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class QuickEditBottomSheet(
    val data: SystemTts,
    private val isLiteMode: Boolean = false,
    private val editView: BaseParamsEditView<*, ITextToSpeechEngine>,
    private val dismissCallback: (dialog: DialogInterface) -> Boolean
) :
    BottomSheetDialogFragment() {
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

        basicEdit.liteModeEnabled = isLiteMode
        basicEdit.setData(data)

        setContent(editView)
        editView.setData(data.tts)

        (view.parent as ViewGroup).setMarginMatchParent()
    }

//    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
//        return BottomSheetDialog(requireContext(), theme).apply {
//            behavior.state = BottomSheetBehavior.STATE_EXPANDED
//        }
//    }

    private fun setContent(view: View) {
        binding.content.removeAllViews()
        binding.content.addView(view)
    }


    override fun onDismiss(dialog: DialogInterface) {
        if (dismissCallback.invoke(dialog))
            super.onDismiss(dialog)
    }
}