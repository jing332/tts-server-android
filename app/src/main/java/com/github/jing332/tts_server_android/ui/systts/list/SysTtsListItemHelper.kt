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
import androidx.core.view.MenuCompat
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.drake.brv.BindingAdapter
import com.drake.net.utils.withIO
import com.github.jing332.tts_server_android.R
import com.github.jing332.tts_server_android.constant.ReadAloudTarget
import com.github.jing332.tts_server_android.data.appDb
import com.github.jing332.tts_server_android.data.entities.systts.SystemTts
import com.github.jing332.tts_server_android.databinding.SysttsListItemBinding
import com.github.jing332.tts_server_android.help.AppConfig
import com.github.jing332.tts_server_android.help.AudioPlayer
import com.github.jing332.tts_server_android.help.SysTtsConfig
import com.github.jing332.tts_server_android.model.tts.BaseTTS
import com.github.jing332.tts_server_android.service.systts.SystemTtsService
import com.github.jing332.tts_server_android.ui.systts.edit.BaseTtsEditActivity
import com.github.jing332.tts_server_android.ui.view.AppDialogs
import com.github.jing332.tts_server_android.ui.view.widget.WaitDialog
import com.github.jing332.tts_server_android.util.clickWithThrottle
import com.github.jing332.tts_server_android.util.clone
import com.github.jing332.tts_server_android.util.toast
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

@Suppress("DEPRECATION")
class SysTtsListItemHelper(val fragment: Fragment, val isGroupList: Boolean = false) {
    val context: Context by lazy { fragment.requireContext() }

    fun init(adapter: BindingAdapter, holder: BindingAdapter.BindingViewHolder) {
        adapter.apply {
            holder.apply {
                getBindingOrNull<SysttsListItemBinding>()?.apply {
                    checkBoxSwitch.setOnClickListener { view ->
                        if (isGroupList) {
                            getModelOrNull<SysTtsGroupModel>(findParentPosition())?.let { group ->
                                // 组中的item位置
                                val subPos = modelPosition - findParentPosition() - 1
                                switchChanged(view, group.itemSublist!![subPos] as SystemTts)
                            }
                        } else
                            switchChanged(view, getModel())
                    }

                    cardView.clickWithThrottle { displayLiteEditDialog(itemView, getModel()) }
                    cardView.setOnLongClickListener {
                        val model = getModel<SystemTts>().copy()
                        if (model.readAloudTarget == ReadAloudTarget.ASIDE)
                            model.readAloudTarget = ReadAloudTarget.DIALOGUE
                        else
                            model.readAloudTarget = ReadAloudTarget.ASIDE

                        appDb.systemTtsDao.updateTts(model)
                        notifyTtsUpdate(model.isEnabled)

                        true
                    }

                    btnListen.isGone = AppConfig.isSwapListenAndEditButton
                    btnEdit.isVisible = AppConfig.isSwapListenAndEditButton

                    btnListen.clickWithThrottle {
                        listen(getModel())
                    }

                    btnEdit.clickWithThrottle {
                        edit(getModel())
                    }

                    btnListen.setOnLongClickListener {
                        edit(getModel())
                        true
                    }

                    btnMore.clickWithThrottle {
                        displayMoreOptions(it, getModel())
                    }
                    itemView.accessibilityDelegate = object : AccessibilityDelegate() {
                        override fun onInitializeAccessibilityNodeInfo(
                            host: View,
                            info: AccessibilityNodeInfo
                        ) {
                            super.onInitializeAccessibilityNodeInfo(host, info)
                            (getModel() as SystemTts).let {
                                var str =
                                    if (it.isEnabled) context.getString(R.string.enabled) else ""

                                if (it.isStandby) str += ", " + context.getString(R.string.as_standby)
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
    }


    private val audioPlayer by lazy { AudioPlayer(context) }
    private val waitDialog by lazy { WaitDialog(context) }

    @OptIn(DelicateCoroutinesApi::class)
    private fun listen(model: SystemTts) {
        waitDialog.show()

        GlobalScope.launch(Dispatchers.Main) {
            val tts = model.tts.clone<BaseTTS>()!!
            val audio = try {
                withIO {
                    tts.onLoad()
                    if (tts.isRateFollowSystem()){
                        tts.rate = 50
                    }
                    if (tts.isPitchFollowSystem()){
                        tts.pitch = 0
                    }
                    tts.getAudio(AppConfig.testSampleText)
                }
            } catch (e: Exception) {
                AppDialogs.displayErrorDialog(context, e.stackTraceToString())
                return@launch
            } finally {
                waitDialog.dismiss()
                model.tts.onDestroy()
            }

            if (audio == null) {
                AppDialogs.displayErrorDialog(
                    context,
                    context.getString(R.string.systts_log_audio_empty, AppConfig.testSampleText)
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

            withIO { audioPlayer.play(audio) }
            dlg.dismiss()
        }
    }

    private fun displayMoreOptions(v: View, model: SystemTts) {
        PopupMenu(context, v).apply {
            menuInflater.inflate(R.menu.sysstts_more_options, menu)
            setForceShowIcon(true)
            MenuCompat.setGroupDividerEnabled(menu, true)

            menu.findItem(R.id.menu_listen).isVisible = AppConfig.isSwapListenAndEditButton
            menu.findItem(R.id.menu_edit).isVisible = !AppConfig.isSwapListenAndEditButton

            setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.menu_edit -> edit(model)
                    R.id.menu_listen -> listen(model)
                    R.id.menu_copy_config -> {
                        context.toast(R.string.systts_copied_config)
                        edit(model.copy(id = System.currentTimeMillis()))
                    }

                    R.id.menu_delete -> delete(model)
                }
                true
            }
            show()
        }

    }

    // 警告 多语音选项未开启
    private val checkMultiVoiceDialog by lazy {
        MaterialAlertDialogBuilder(context)
            .setTitle(R.string.warning)
            .setCancelable(false)
            .setPositiveButton(android.R.string.ok) { _, _ -> }
            .setMessage(R.string.systts_please_check_multi_voice_option).create()
    }

    private fun switchChanged(view: View?, current: SystemTts) {
        val checkBox = view as CheckBox
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
            //检测多语音是否开启
            if (!SysTtsConfig.isMultiVoiceEnabled) {
                val target = current.readAloudTarget
                if (target == ReadAloudTarget.ASIDE || target == ReadAloudTarget.DIALOGUE)
                    return false
            }

            if (current.isStandby) {
                list.forEach {
                    if (it.isStandby && it.readAloudTarget == current.readAloudTarget)
                        appDb.systemTtsDao.updateTts(it.copy(isEnabled = false))
                }
            } else if (!SysTtsConfig.isVoiceMultipleEnabled) { // 多选关闭下 确保同目标只可单选
                list.forEach {
                    if (!it.isStandby && it.readAloudTarget == current.readAloudTarget)
                        appDb.systemTtsDao.updateTts(it.copy(isEnabled = false))
                }
            }
        }
        appDb.systemTtsDao.updateTts(current.copy(isEnabled = checked))

        return true
    }

    private fun displayLiteEditDialog(v: View, data: SystemTts) {
        // 修改数据要clone，不然对比时数据相同导致UI不更新
        data.clone<SystemTts>()?.let { clonedData ->
            clonedData.tts.onDescriptionClick(context, v, clonedData) {
                it?.let {
                    appDb.systemTtsDao.updateTts(it)
                    notifyTtsUpdate(clonedData.isEnabled)
                }
            }
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

    fun edit(data: SystemTts) {
        startForResult.launch(Intent(context, data.tts.getEditActivity()).apply {
            putExtra(BaseTtsEditActivity.KEY_DATA, data)
        })
    }

    fun delete(data: SystemTts) {
        AppDialogs.displayDeleteDialog(context, data.displayName.toString()) {
            appDb.systemTtsDao.deleteTts(data)
            notifyTtsUpdate(data.isEnabled)
        }
    }
}