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
import androidx.recyclerview.widget.RecyclerView
import com.github.jing332.tts_server_android.MyLog
import com.github.jing332.tts_server_android.databinding.FragmentTtsLogBinding
import com.github.jing332.tts_server_android.service.tts.SystemTtsService.Companion.ACTION_ON_LOG
import com.github.jing332.tts_server_android.ui.LogViewAdapter


class TtsLogFragment : Fragment() {
    companion object {
        const val TAG = "TtsLogFragment"
    }

    private var mLastPosition = -1
    private var mLastItemCount = -1

    private val logAdapter: LogViewAdapter by lazy { LogViewAdapter(arrayListOf()) }

    private val mReceiver: MyReceiver by lazy { MyReceiver() }

    private val binding: FragmentTtsLogBinding by lazy {
        FragmentTtsLogBinding.inflate(
            layoutInflater
        )
    }

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

        /* 用来判断是否在日志列表最底部 以确认是否自动滚动 */
        binding.recyclerViewLog.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                mLastItemCount = recyclerView.layoutManager!!.itemCount
                /* 当前状态为停止滑动状态SCROLL_STATE_IDLE时 */
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    if (recyclerView.layoutManager is LinearLayoutManager) {
                        mLastPosition = layoutManager.findLastVisibleItemPosition()
                    }
                }
            }
        })

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
                logAdapter.append(log)
                /* 判断是否在最底部 */
                if (mLastPosition == mLastItemCount - 1 || mLastPosition == mLastItemCount - 2|| mLastPosition == mLastItemCount - 3) {
                    binding.recyclerViewLog.scrollToPosition(logAdapter.itemCount - 1)
                }
            }
        }

    }

}