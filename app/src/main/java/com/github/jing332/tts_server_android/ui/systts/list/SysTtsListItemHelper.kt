package com.github.jing332.tts_server_android.ui.systts.list

import android.content.Context
import android.content.Intent
import android.view.View
import android.view.View.AccessibilityDelegate
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.CheckBox
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.PopupMenu
import androidx.core.app.ActivityOptionsCompat
import androidx.core.view.MenuCompat
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.drake.brv.BindingAdapter
import com.drake.net.utils.withIO
import com.github.jing332.tts_server_android.R
import com.github.jing332.tts_server_android.constant.AppConst
import com.github.jing332.tts_server_android.constant.SpeechTarget
import com.github.jing332.tts_server_android.data.appDb
import com.github.jing332.tts_server_android.data.entities.systts.SystemTts
import com.github.jing332.tts_server_android.databinding.SysttsListItemBinding
import com.github.jing332.tts_server_android.help.audio.AudioPlayer
import com.github.jing332.tts_server_android.help.config.AppConfig
import com.github.jing332.tts_server_android.help.config.SysTtsConfig
import com.github.jing332.tts_server_android.model.speech.tts.ITextToSpeechEngine
import com.github.jing332.tts_server_android.service.systts.SystemTtsService
import com.github.jing332.tts_server_android.ui.systts.base.QuickEditBottomSheet
import com.github.jing332.tts_server_android.ui.systts.edit.BaseParamsEditView
import com.github.jing332.tts_server_android.ui.systts.edit.BaseTtsEditActivity
import com.github.jing332.tts_server_android.ui.view.AppDialogs
import com.github.jing332.tts_server_android.ui.view.AppDialogs.displayErrorDialog
import com.github.jing332.tts_server_android.ui.view.widget.WaitDialog
import com.github.jing332.tts_server_android.utils.clickWithThrottle
import com.github.jing332.tts_server_android.utils.clone
import com.github.jing332.tts_server_android.utils.longToast
import com.github.jing332.tts_server_android.utils.toast
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Suppress("DEPRECATION")
class SysTtsListItemHelper(val fragment: Fragment, val hasGroup: Boolean = false) {
    val context: Context by lazy { fragment.requireContext() }

    // 已经显示长按时的拖拽提示
    private var hasShownTip = false

    fun init(adapter: BindingAdapter, holder: BindingAdapter.BindingViewHolder) {
        adapter.apply {
            holder.apply {
                getBindingOrNull<SysttsListItemBinding>()?.apply {
                    checkBoxSwitch.setOnClickListener {
                        if (hasGroup) {
                            getModelOrNull<GroupModel>(findParentPosition())?.let { group ->
                                // 组中的item位置
                                val subPos = modelPosition - findParentPosition() - 1
                                switchChanged(
                                    checkBoxSwitch,
                                    (group.itemSublist!![subPos] as ItemModel).data
                                )
                            }
                        } else
                            switchChanged(checkBoxSwitch, getModel<ItemModel>().data)
                    }
                    if (hasGroup)
                        checkBoxSwitch.setOnLongClickListener {
                            itemTouchHelper?.startDrag(holder)
                            true
                        }

                    cardView.clickWithThrottle {
                        displayQuickEditBottomSheet(
                            itemView,
                            getModel<ItemModel>().data
                        )
                    }
                    cardView.setOnLongClickListener {
                        if (hasGroup && !hasShownTip) {
                            hasShownTip = true
                            context.longToast(R.string.systts_drag_tip_msg)
                        }

                        val model = getModel<ItemModel>().data.copy()
                        if (model.speechTarget == SpeechTarget.BGM) return@setOnLongClickListener true

                        if (model.speechTarget == SpeechTarget.CUSTOM_TAG)
                            appDb.speechRule.getByRuleId(model.speechRule.tagRuleId)?.let {
                                val keys = it.tags.keys.toList()
                                val idx = keys.indexOf(model.speechRule.tag)

                                val newTag = keys.getOrNull(idx + 1)
                                if (newTag == null) {
                                    model.speechRule.target = SpeechTarget.ALL
                                } else {
                                    model.speechRule.tag = newTag
                                }
                            }
                        else {
                            appDb.speechRule.getByRuleId(model.speechRule.tagRuleId)?.let {
                                model.speechRule.target = SpeechTarget.CUSTOM_TAG
                                model.speechRule.tag = it.tags.keys.first()
                            }
                        }

                        appDb.systemTtsDao.updateTts(model)
                        notifyTtsUpdate(model.isEnabled)

                        true
                    }

                    // 交换按钮
                    btnListen.isVisible = AppConfig.isSwapListenAndEditButton
                    btnEdit.isGone = AppConfig.isSwapListenAndEditButton

                    btnListen.clickWithThrottle { listen(getModel<ItemModel>().data) }
                    btnListen.setOnLongClickListener {
                        edit(root, getModel<ItemModel>().data)
                        true
                    }
                    btnEdit.clickWithThrottle { edit(root, getModel<ItemModel>().data) }
                    btnEdit.setOnLongClickListener {
                        listen(getModel<ItemModel>().data)
                        true
                    }

                    btnMore.clickWithThrottle {
                        displayMoreOptions(
                            root, it, getModel<ItemModel>().data
                        )
                    }
                    btnMore.setOnLongClickListener {
                        context.toast(R.string.systts_copied_config)
                        edit(
                            itemView,
                            getModel<ItemModel>().data.copy(id = System.currentTimeMillis())
                        )

                        true
                    }

                    itemView.accessibilityDelegate = object : AccessibilityDelegate() {
                        override fun onInitializeAccessibilityNodeInfo(
                            host: View, info: AccessibilityNodeInfo
                        ) {
                            super.onInitializeAccessibilityNodeInfo(host, info)

                            val data = getSystemTTS(holder)
                            var str =
                                if (data.isEnabled) context.getString(R.string.enabled) else ""
                            if (data.speechRule.isStandby) str += ", " + context.getString(R.string.as_standby)
                            info.text = "$str, ${tvName.text}, " + context.getString(
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

    fun getSystemTTS(holder: BindingAdapter.BindingViewHolder): SystemTts {
        return holder.getModelOrNull<SystemTts>() ?: holder.getModel<ItemModel>().data
    }

    private val audioPlayer by lazy { AudioPlayer(context) }
    private val waitDialog by lazy { WaitDialog(context) }

    private fun listen(model: SystemTts) {
        waitDialog.show()

        fragment.lifecycleScope.launch(Dispatchers.Main) {
            val tts = model.tts
            val audio = try {
                withIO {
                    tts.onLoad()
                    tts.getAudioWithSystemParams(AppConfig.testSampleText)
                }
            } catch (e: Exception) {
                context.displayErrorDialog(e)
                return@launch
            } finally {
                waitDialog.dismiss()
                model.tts.onDestroy()
            }

            if (audio == null) {
                context.displayErrorDialog(
                    Exception(
                        context.getString(
                            R.string.systts_log_audio_empty,
                            AppConfig.testSampleText
                        )
                    )
                )
                return@launch
            }

            val dlg = MaterialAlertDialogBuilder(context)
                .setTitle(R.string.playing)
                .setMessage(AppConfig.testSampleText)
                .setPositiveButton(R.string.cancel, null)
                .setOnDismissListener {
                    audioPlayer.stop()
                    tts.onDestroy()
                }.show()

            withIO { audioPlayer.play(audio.readBytes()) }
            dlg.dismiss()
        }
    }

    private fun displayMoreOptions(itemView: View, anchorView: View, model: SystemTts) {
        PopupMenu(context, anchorView).apply {
            menuInflater.inflate(R.menu.sysstts_item_more_options, menu)
            setForceShowIcon(true)
            MenuCompat.setGroupDividerEnabled(menu, true)

            menu.findItem(R.id.menu_edit).isVisible = AppConfig.isSwapListenAndEditButton
            menu.findItem(R.id.menu_listen).isVisible = !AppConfig.isSwapListenAndEditButton

            setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.menu_edit -> {
                        edit(itemView, model)
                        true
                    }

                    R.id.menu_listen -> {
                        listen(model)
                        true
                    }

                    R.id.menu_copy_config -> {
                        context.toast(R.string.systts_copied_config)
                        edit(itemView, model.copy(id = System.currentTimeMillis()))
                        true
                    }

                    R.id.menu_delete -> {
                        delete(model)
                        true
                    }

                    else -> {
                        AppConst.localBroadcast.sendBroadcast(Intent(SysTtsListFragment.ACTION_ADD_TTS).apply {
                            putExtra(SysTtsListFragment.KEY_SYSTEM_TTS_DATA, model)
                            putExtra(SysTtsListFragment.KEY_MENU_ID, it.itemId)
                        })
                        true
                    }
                }
            }
            show()
        }

    }

    // 警告 多语音选项未开启
    private val checkMultiVoiceDialog by lazy {
        MaterialAlertDialogBuilder(context)
            .setTitle(R.string.warning)
            .setCancelable(false)
            .setPositiveButton(android.R.string.ok, null)
            .setMessage(R.string.systts_please_check_multi_voice_option).create()
    }

    private fun switchChanged(checkBox: CheckBox, current: SystemTts) {
        // 检测是否开启多语音
        if (setCheckBoxSwitch(appDb.systemTtsDao.allEnabledTts, current, checkBox.isChecked))
            notifyTtsUpdate()
        else {
            checkBox.isChecked = false
            checkMultiVoiceDialog.show()
        }
    }

    private fun setCheckBoxSwitch(
        list: List<SystemTts>,
        current: SystemTts,
        checked: Boolean
    ): Boolean {
        if (checked) {
            // 多语音未开启 但勾选了自定义TAG
            if (!SysTtsConfig.isMultiVoiceEnabled && current.speechTarget == SpeechTarget.CUSTOM_TAG)
                return false

            if (current.isStandby) {
                list.forEach {
                    if (it.speechRule.isStandby) {
                        if (it.speechRule.target == current.speechRule.target && it.speechRule.tag == current.speechRule.tag) {
                            appDb.systemTtsDao.updateTts(it.copy(isEnabled = false))
                        }
                    }
                }
            } else if (!SysTtsConfig.isVoiceMultipleEnabled) { // 多选关闭下 确保同目标只可单选
                list.forEach {
                    if (!it.isStandby && it.speechTarget == current.speechTarget && it.speechRule.tag == current.speechRule.tag)
                        appDb.systemTtsDao.updateTts(it.copy(isEnabled = false))
                }
            }
        }
        appDb.systemTtsDao.updateTts(current.copy(isEnabled = checked))

        return true
    }

    @Suppress("UNCHECKED_CAST", "UNUSED_PARAMETER")
    private fun displayQuickEditBottomSheet(v: View, data: SystemTts) {
        // 修改数据要clone，不然对比时数据相同导致UI不更新
        data.clone<SystemTts>()?.let { clonedData ->
            val paramsEdit = clonedData.tts.getParamsEditView(context)
            val quickEdit = QuickEditBottomSheet(
                clonedData,
                paramsEdit.second,
                paramsEdit.first as BaseParamsEditView<*, ITextToSpeechEngine>
            ) {
                appDb.systemTtsDao.updateTts(clonedData)
                notifyTtsUpdate(clonedData.isEnabled)
                true
            }
            quickEdit.show(
                fragment.requireActivity().supportFragmentManager,
                QuickEditBottomSheet.TAG
            )
        }
    }

    // EditActivity的返回值
    private val startForResult =
        fragment.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            val data = result.data?.getParcelableExtra<SystemTts>(BaseTtsEditActivity.KEY_DATA)
            data?.let {
                appDb.systemTtsDao.insertTts(data)
                notifyTtsUpdate(data.isEnabled)
            }
        }

    private fun notifyTtsUpdate(isUpdate: Boolean = true) {
        if (isUpdate) SystemTtsService.notifyUpdateConfig()
    }

    fun edit(itemView: View, data: SystemTts) {
        val options = ActivityOptionsCompat.makeSceneTransitionAnimation(
            fragment.requireActivity(), itemView,
            context.getString(R.string.key_activity_shared_container_trans)
        )
        startForResult.launch(
            Intent(context, data.tts.getEditActivity()).apply {
                putExtra(BaseTtsEditActivity.KEY_DATA, data)
            },
            options
        )
    }

    fun delete(data: SystemTts) {
        AppDialogs.displayDeleteDialog(context, data.displayName.toString()) {
            appDb.systemTtsDao.deleteTts(data)
            notifyTtsUpdate(data.isEnabled)
        }
    }
}