package com.github.jing332.tts_server_android.ui.forwarder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.View
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import by.kirich1409.viewbindingdelegate.viewBinding
import com.github.jing332.tts_server_android.R
import com.github.jing332.tts_server_android.constant.AppConst
import com.github.jing332.tts_server_android.constant.KeyConst
import com.github.jing332.tts_server_android.constant.LogLevel
import com.github.jing332.tts_server_android.databinding.ForwarderHomeFragmentBinding
import com.github.jing332.tts_server_android.ui.AppLog
import com.github.jing332.tts_server_android.ui.view.adapter.LogListItemAdapter
import com.github.jing332.tts_server_android.utils.clickWithThrottle
import tts_server_lib.Tts_server_lib

abstract class AbsForwarderHomePageFragment(
    private val startedAction: String,
    private val closedAction: String,
    private val logAction: String,
) : Fragment(R.layout.forwarder_home_fragment) {

    abstract var port: Int
    abstract val isServiceRunning: Boolean
    abstract val tipInfo: String

    private val binding by viewBinding(ForwarderHomeFragmentBinding::bind)
    private val vm: ForwarderHostViewModel by viewModels(ownerProducer = { requireParentFragment() })

    private lateinit var mLogAdapter: LogListItemAdapter
    private var mReceiver = MyReceiver()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        AppConst.localBroadcast.registerReceiver(
            mReceiver,
            IntentFilter().apply {
                addAction(startedAction)
                addAction(closedAction)
                addAction(logAction)
            }
        )

        mLogAdapter = LogListItemAdapter()
        binding.rvLog.adapter = mLogAdapter
        val layoutManager = LinearLayoutManager(requireContext())
        layoutManager.stackFromEnd = true
        binding.rvLog.layoutManager = layoutManager

        binding.switchOnOff.clickWithThrottle {
            vm.switchStateLiveData.value = binding.switchOnOff.isChecked
        }

        binding.tilPort.editText?.apply {
            setText(port.toString())
            addTextChangedListener {
                try {
                    port = it.toString().toInt()
                } catch (_: NumberFormatException) {
                }
            }
        }


        if (isServiceRunning) {
            val localIp = Tts_server_lib.getOutboundIP()
            mLogAdapter.append(
                AppLog(
                    LogLevel.WARN,
                    getString(R.string.server_log_service_running, "${localIp}:${port}")
                )
            )
        } else {
            mLogAdapter.append((AppLog(LogLevel.INFO, tipInfo)))
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        AppConst.localBroadcast.unregisterReceiver(mReceiver)
    }

    fun updateState(isRunning: Boolean = isServiceRunning) {
        if (binding.switchOnOff.isChecked != isRunning)
            binding.switchOnOff.isChecked = isRunning

        binding.tilPort.isEnabled = !isRunning
    }

    inner class MyReceiver : BroadcastReceiver() {
        @Suppress("DEPRECATION")
        override fun onReceive(ctx: Context?, intent: Intent?) {
            when (intent?.action) {
                logAction -> {
                    mLogAdapter.let {
                        val log = intent.getParcelableExtra<AppLog>(KeyConst.KEY_DATA) as AppLog
                        val layout = binding.rvLog.layoutManager as LinearLayoutManager
                        val isBottom =
                            layout.findLastVisibleItemPosition() == layout.itemCount - 1
                        it.append(log)
                        /* 判断是否在最底部 */
                        if (isBottom)
                            binding.rvLog.scrollToPosition(it.itemCount - 1)
                    }
                }
                startedAction -> {
                    mLogAdapter.removeAll() /* 清空日志 */
                    updateState(true)
                }
                closedAction -> {
                    updateState(false)
                }
            }
        }
    }
}