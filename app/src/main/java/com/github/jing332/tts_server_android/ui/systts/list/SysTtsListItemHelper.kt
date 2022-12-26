package com.github.jing332.tts_server_android.ui.systts.list

import android.content.Context
import android.content.Intent
import android.view.View
import android.view.View.AccessibilityDelegate
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.CheckBox
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.drake.brv.BindingAdapter
import com.github.jing332.tts_server_android.R
import com.github.jing332.tts_server_android.constant.KeyConst
import com.github.jing332.tts_server_android.constant.ReadAloudTarget
import com.github.jing332.tts_server_android.data.appDb
import com.github.jing332.tts_server_android.data.entities.systts.SystemTts
import com.github.jing332.tts_server_android.databinding.SysttsListItemBinding
import com.github.jing332.tts_server_android.help.SysTtsConfig
import com.github.jing332.tts_server_android.model.tts.MsTTS
import com.github.jing332.tts_server_android.service.systts.SystemTtsService
import com.github.jing332.tts_server_android.ui.systts.edit.BaseInfoEditView
import com.github.jing332.tts_server_android.ui.systts.edit.HttpTtsEditActivity
import com.github.jing332.tts_server_android.ui.systts.edit.MsTtsEditActivity
import com.github.jing332.tts_server_android.ui.systts.list.my_group.RvGroupModel
import com.github.jing332.tts_server_android.util.clone
import com.github.jing332.tts_server_android.util.setFadeAnim
import com.google.android.material.dialog.MaterialAlertDialogBuilder

@Suppress("UNCHECKED_CAST", "DEPRECATION")
class SysTtsListItemHelper(val fragment: Fragment, val isGroupList: Boolean = false) {
    val context: Context by lazy { fragment.requireContext() }

    fun init(adapter: BindingAdapter, holder: BindingAdapter.BindingViewHolder) {
        adapter.apply {
            holder.apply {
                getBindingOrNull<SysttsListItemBinding>()?.apply {
                    checkBoxSwitch.setOnClickListener { view ->
                        if (isGroupList) {
                            getModelOrNull<RvGroupModel>(findParentPosition())?.let { group ->
                                // 组中的item位置
                                val subPos = modelPosition - findParentPosition() - 1
                                switchChanged(view, group.itemSublist as List<SystemTts>, subPos)
                            }
                        } else
                            switchChanged(view, models as List<SystemTts>, modelPosition)
                    }
                    btnEdit.setOnClickListener { edit(getModel()) }
                    btnDelete.setOnClickListener { delete(getModel()) }
                    itemView.setOnLongClickListener {
                        displayBaseEditDialog(getModel())
                        true
                    }

                    itemView.setOnClickListener { displayQuickEditDialog(it, getModel()) }
                    itemView.accessibilityDelegate = object : AccessibilityDelegate() {
                        override fun onInitializeAccessibilityNodeInfo(
                            host: View,
                            info: AccessibilityNodeInfo
                        ) {
                            super.onInitializeAccessibilityNodeInfo(host, info)
                            (getModel() as SystemTts).let {
                                val strEnabled =
                                    if (it.isEnabled) context.getString(R.string.enabled) else ""
                                info.text = "$strEnabled，${tvName.text}，" + context.getString(
                                    R.string.systts_list_item_desc,
                                    tvRaTarget.text,
                                    tvApiType.text,
                                    tvDescription.text,
                                    tvBottomContent.text,
                                )
                            }
                        }
                    }

                }
            }
        }
    }

    // 警告 多语音选项未开启
    private val checkMultiVoiceDialog by lazy {
        MaterialAlertDialogBuilder(context)
            .setTitle(R.string.warning)
            .setCancelable(false)
            .setPositiveButton(android.R.string.ok) { _, _ -> }
            .setMessage(R.string.systts_please_check_multi_voice_option).create()
            .apply { window?.setWindowAnimations(R.style.dialogFadeStyle) }
    }

    private fun switchChanged(view: View?, list: List<SystemTts>, position: Int) {
        val checkBox = view as CheckBox
        // 检测是否开启多语音
        if (setCheckBoxSwitch(list, position, checkBox.isChecked))
            notifyTtsUpdate()
        else {
            checkBox.isChecked = false
            checkMultiVoiceDialog.show()
        }
    }

    private fun setCheckBoxSwitch(list: List<SystemTts>, position: Int, checked: Boolean): Boolean {
        //检测多语音是否开启
        if (checked && !SysTtsConfig.isMultiVoiceEnabled) {
            val target =
                list.getOrNull(position)?.readAloudTarget ?: ReadAloudTarget.ALL
            if (target == ReadAloudTarget.ASIDE || target == ReadAloudTarget.DIALOGUE) {
                return false
            }
        }

        list[position].let { data ->
            if (checked && !SysTtsConfig.isVoiceMultipleEnabled) { // 多选关闭下 确保同类型只可单选
                list.forEach {
                    if (it.readAloudTarget == data.readAloudTarget) {
                        appDb.systemTtsDao.updateTts(it.copy(isEnabled = false))
                    }
                }
            }

            appDb.systemTtsDao.updateTts(data.copy(isEnabled = checked))
        }
        return true
    }

    private fun displayQuickEditDialog(v: View, data: SystemTts) {
        // 修改数据要clone，不然对比时数据相同导致UI不更新
        data.clone<SystemTts>()?.let { clonedData ->
            clonedData.tts.onDescriptionClick(context, v, data.displayName.toString()) {
                it?.let {
                    appDb.systemTtsDao.updateTts(clonedData)
                    notifyTtsUpdate(clonedData.isEnabled)
                }
            }
        }
    }

    private fun displayBaseEditDialog(data: SystemTts) {
        data.copy().let { copiedData ->
            val editView = BaseInfoEditView(context).apply {
                setData(copiedData, appDb.systemTtsDao.allGroup)
            }
            editView.setPadding(16, 8, 16, 24)
            MaterialAlertDialogBuilder(context)
                .setTitle(R.string.base_info_edit)
                .setView(editView)
                .setOnDismissListener {
                    appDb.systemTtsDao.updateTts(copiedData)
                    notifyTtsUpdate(copiedData.isEnabled)
                }
                .setFadeAnim()
                .show()
        }
    }

    // EditActivity的返回值
    private val startForResult =
        fragment.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            val data = result.data?.getParcelableExtra<SystemTts>(KeyConst.KEY_DATA)
            data?.let {
                appDb.systemTtsDao.insertTts(data)
                notifyTtsUpdate(data.isEnabled)
            }
        }

    private fun notifyTtsUpdate(isUpdate: Boolean = true) {
        if (isUpdate) SystemTtsService.notifyUpdateConfig()
    }

    fun edit(data: SystemTts) {
        val cls = when (data.tts) {
            is MsTTS -> MsTtsEditActivity::class.java
            else -> HttpTtsEditActivity::class.java
        }
        startForResult.launch(Intent(context, cls).apply {
            putExtra(
                KeyConst.KEY_DATA,
                data
            )
        })
    }

    fun delete(data: SystemTts) {
        MaterialAlertDialogBuilder(context).setTitle(R.string.is_confirm_delete)
            .setMessage(data.displayName)
            .setPositiveButton(R.string.delete) { _, _ ->
                appDb.systemTtsDao.deleteTts(data)
                notifyTtsUpdate(data.isEnabled)
            }
            .setFadeAnim().show()
    }
}