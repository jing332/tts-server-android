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
import com.github.jing332.tts_server_android.ui.custom.AppDialogs
import com.github.jing332.tts_server_android.ui.systts.edit.HttpTtsEditActivity
import com.github.jing332.tts_server_android.ui.systts.edit.MsTtsEditActivity
import com.github.jing332.tts_server_android.util.clickWithThrottle
import com.github.jing332.tts_server_android.util.clone
import com.github.jing332.tts_server_android.util.toast
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
                            getModelOrNull<CustomGroupModel>(findParentPosition())?.let { group ->
                                // ?????????item??????
                                val subPos = modelPosition - findParentPosition() - 1
                                switchChanged(view, group.itemSublist as List<SystemTts>, subPos)
                            }
                        } else
                            switchChanged(view, models as List<SystemTts>, modelPosition)
                    }

                    cardView.clickWithThrottle { displayQuickEditDialog(itemView, getModel()) }
                    btnDelete.clickWithThrottle { delete(getModel()) }
                    btnEdit.clickWithThrottle { edit(getModel()) }
                    btnEdit.setOnLongClickListener {
                        context.toast(R.string.systts_copied_config)
                        edit(getModel<SystemTts>().copy(id = System.currentTimeMillis()))
                        true
                    }


                    itemView.accessibilityDelegate = object : AccessibilityDelegate() {
                        override fun onInitializeAccessibilityNodeInfo(
                            host: View,
                            info: AccessibilityNodeInfo
                        ) {
                            super.onInitializeAccessibilityNodeInfo(host, info)
                            (getModel() as SystemTts).let {
                                val strEnabled =
                                    if (it.isEnabled) context.getString(R.string.enabled) else ""
                                info.text = "$strEnabled???${tvName.text}???" + context.getString(
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

    // ?????? ????????????????????????
    private val checkMultiVoiceDialog by lazy {
        MaterialAlertDialogBuilder(context)
            .setTitle(R.string.warning)
            .setCancelable(false)
            .setPositiveButton(android.R.string.ok) { _, _ -> }
            .setMessage(R.string.systts_please_check_multi_voice_option).create()
    }

    private fun switchChanged(view: View?, list: List<SystemTts>, position: Int) {
        val checkBox = view as CheckBox
        // ???????????????????????????
        if (setCheckBoxSwitch(list, position, checkBox.isChecked))
            notifyTtsUpdate()
        else {
            checkBox.isChecked = false
            checkMultiVoiceDialog.show()
        }
    }

    private fun setCheckBoxSwitch(list: List<SystemTts>, position: Int, checked: Boolean): Boolean {
        //???????????????????????????
        if (checked && !SysTtsConfig.isMultiVoiceEnabled) {
            val target =
                list.getOrNull(position)?.readAloudTarget ?: ReadAloudTarget.ALL
            if (target == ReadAloudTarget.ASIDE || target == ReadAloudTarget.DIALOGUE) {
                return false
            }
        }

        list[position].let { data ->
            if (checked && !SysTtsConfig.isVoiceMultipleEnabled) { // ??????????????? ???????????????????????????
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
        // ???????????????clone????????????????????????????????????UI?????????
        data.clone<SystemTts>()?.let { clonedData ->
            clonedData.tts.onDescriptionClick(context, v, clonedData) {
                it?.let {
                    appDb.systemTtsDao.updateTts(it)
                    notifyTtsUpdate(clonedData.isEnabled)
                }
            }
        }
    }

    // EditActivity????????????
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
        AppDialogs.displayDeleteDialog(context, data.displayName.toString()) {
            appDb.systemTtsDao.deleteTts(data)
            notifyTtsUpdate(data.isEnabled)
        }
    }
}