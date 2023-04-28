package com.github.jing332.tts_server_android.ui.systts

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import by.kirich1409.viewbindingdelegate.viewBinding
import com.github.jing332.tts_server_android.R
import com.github.jing332.tts_server_android.constant.AppConst
import com.github.jing332.tts_server_android.constant.KeyConst.KEY_DATA
import com.github.jing332.tts_server_android.databinding.SysttsLogFragmentBinding
import com.github.jing332.tts_server_android.service.systts.SystemTtsService.Companion.ACTION_ON_LOG
import com.github.jing332.tts_server_android.ui.AppLog
import com.github.jing332.tts_server_android.ui.view.BrvLogHelper
import com.github.jing332.tts_server_android.ui.view.adapter.LogListItemAdapter

class SysTtsLogPageFragment : Fragment(R.layout.systts_log_fragment) {
    companion object {
        const val TAG = "TtsLogFragment"
    }

    private val binding by viewBinding(SysttsLogFragmentBinding::bind)
    private val logAdapter: LogListItemAdapter by lazy { LogListItemAdapter(isHtmlText = true) }
    private val mReceiver: MyReceiver by lazy { MyReceiver() }
    private var mLogHelper: BrvLogHelper? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mLogHelper = BrvLogHelper(binding.recyclerViewLog, isHtmlEnabled = true)
        mLogHelper?.bindFloatingActionButton(binding.btnToBottom)

        AppConst.localBroadcast.registerReceiver(mReceiver, IntentFilter(ACTION_ON_LOG))
        AppConst.isSysTtsLogEnabled = true
    }

    override fun onDestroyView() {
        super.onDestroyView()
        AppConst.localBroadcast.unregisterReceiver(mReceiver)
        AppConst.isSysTtsLogEnabled = false
    }

    @Suppress("DEPRECATION")
    inner class MyReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == ACTION_ON_LOG && view != null) {
                val log = intent.getParcelableExtra<AppLog>(KEY_DATA) as AppLog
                mLogHelper?.append(log)
            }
        }
    }
}