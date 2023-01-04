package com.github.jing332.tts_server_android.ui.systts

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.jing332.tts_server_android.App
import com.github.jing332.tts_server_android.constant.KeyConst.KEY_DATA
import com.github.jing332.tts_server_android.databinding.SysttsLogFragmentBinding
import com.github.jing332.tts_server_android.service.systts.SystemTtsService.Companion.ACTION_ON_LOG
import com.github.jing332.tts_server_android.ui.AppLog
import com.github.jing332.tts_server_android.ui.custom.adapter.LogListItemAdapter

class SysTtsLogFragment : Fragment() {
    companion object {
        const val TAG = "TtsLogFragment"
    }

    private val binding: SysttsLogFragmentBinding by lazy {
        SysttsLogFragmentBinding.inflate(layoutInflater)
    }
    private val logAdapter: LogListItemAdapter by lazy { LogListItemAdapter(isHtmlText = true) }
    private val mReceiver: MyReceiver by lazy { MyReceiver() }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val layoutManager = LinearLayoutManager(requireContext())
        layoutManager.stackFromEnd = true
        binding.recyclerViewLog.layoutManager = layoutManager
        binding.recyclerViewLog.adapter = logAdapter

        App.localBroadcast.registerReceiver(mReceiver, IntentFilter(ACTION_ON_LOG))
        App.isSysTtsLogEnabled = true
    }

    override fun onDestroy() {
        super.onDestroy()
        App.localBroadcast.unregisterReceiver(mReceiver)
        App.isSysTtsLogEnabled = false
    }

    @Suppress("DEPRECATION")
    inner class MyReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == ACTION_ON_LOG) {
                val log = intent.getParcelableExtra<AppLog>(KEY_DATA) as AppLog
                val layout = binding.recyclerViewLog.layoutManager as LinearLayoutManager
                val isBottom = layout.findLastVisibleItemPosition() == layout.itemCount - 1
                logAdapter.append(log)
                if (isBottom)
                    binding.recyclerViewLog.scrollToPosition(logAdapter.itemCount - 1)
            }
        }
    }
}