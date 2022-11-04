package com.github.jing332.tts_server_android.ui.fragment

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.jing332.tts_server_android.LogLevel
import com.github.jing332.tts_server_android.MyLog
import com.github.jing332.tts_server_android.databinding.FragmentTtsLogBinding
import com.github.jing332.tts_server_android.service.systts.SystemTtsService.Companion.ACTION_ON_LOG
import com.github.jing332.tts_server_android.ui.LogViewAdapter

class TtsLogFragment : Fragment() {
    companion object {
        const val TAG = "TtsLogFragment"
    }

    private val binding: FragmentTtsLogBinding by lazy {
        FragmentTtsLogBinding.inflate(
            layoutInflater
        )
    }
    private val logAdapter: LogViewAdapter by lazy {
        LogViewAdapter(
            arrayListOf(
                MyLog(
                    LogLevel.WARN,
                    "请保持此页面存活以监听实时日志"
                )
            )
        )
    }
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

        requireContext().registerReceiver(mReceiver, IntentFilter(ACTION_ON_LOG))
    }

    override fun onDestroy() {
        super.onDestroy()
        requireContext().unregisterReceiver(mReceiver)
    }

    @Suppress("DEPRECATION")
    inner class MyReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent!!.action == ACTION_ON_LOG) {
                val log = intent.getSerializableExtra("data") as MyLog
                Log.i(TAG, "接收到Log: ${log.level}, ${log.msg}")

                val layout = binding.recyclerViewLog.layoutManager as LinearLayoutManager
                val isBottom = layout.findLastVisibleItemPosition() == layout.itemCount - 1
                logAdapter.append(log)
                if (isBottom)
                    binding.recyclerViewLog.scrollToPosition(logAdapter.itemCount - 1)
            }
        }

    }
}