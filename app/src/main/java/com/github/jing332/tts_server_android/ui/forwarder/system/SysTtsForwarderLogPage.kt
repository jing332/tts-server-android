package com.github.jing332.tts_server_android.ui.forwarder.system

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
import by.kirich1409.viewbindingdelegate.viewBinding
import com.github.jing332.tts_server_android.R
import com.github.jing332.tts_server_android.constant.AppConst
import com.github.jing332.tts_server_android.constant.KeyConst
import com.github.jing332.tts_server_android.databinding.SysTtsForwarderLogPageFragmentBinding
import com.github.jing332.tts_server_android.help.config.SysTtsForwarderConfig
import com.github.jing332.tts_server_android.service.forwarder.system.SysTtsForwarderService
import com.github.jing332.tts_server_android.ui.AppLog
import com.github.jing332.tts_server_android.ui.LogLevel
import com.github.jing332.tts_server_android.ui.view.adapter.LogListItemAdapter
import com.github.jing332.tts_server_android.utils.clickWithThrottle

class SysTtsForwarderLogPage : Fragment(R.layout.sys_tts_forwarder_log_page_fragment) {
    private val binding by viewBinding(SysTtsForwarderLogPageFragmentBinding::bind)

    private val mReceiver by lazy { MyReceiver() }
    private var logAdapter: LogListItemAdapter? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.etPort.setText(SysTtsForwarderConfig.port.toString())
        binding.fabSwitch.clickWithThrottle {
            SysTtsForwarderConfig.port = binding.etPort.text.toString().toInt()
            if (SysTtsForwarderService.isRunning) {
                SysTtsForwarderService.requestCloseServer()
            } else {
                requireContext().startService(
                    Intent(
                        requireContext(),
                        SysTtsForwarderService::class.java
                    )
                )
                updateSwitch(true)
            }
        }

        logAdapter = LogListItemAdapter()
        binding.rvLog.adapter = logAdapter
        val layoutManager = LinearLayoutManager(requireContext())
        layoutManager.stackFromEnd = true
        binding.rvLog.layoutManager = layoutManager

        if (SysTtsForwarderService.isRunning) {
            updateSwitch(true)
            val address = SysTtsForwarderService.instance?.listenAddress
            logAdapter?.append(
                AppLog(LogLevel.WARN, getString(R.string.server_log_service_running, address))
            )
        } else {
            logAdapter?.append(AppLog(LogLevel.INFO, "启动后点击右上角打开网页, 以进行配置测试和导入阅读。"))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppConst.localBroadcast.registerReceiver(
            mReceiver,
            IntentFilter(SysTtsForwarderService.ACTION_ON_CLOSED).apply {
                addAction(SysTtsForwarderService.ACTION_ON_LOG)
                addAction(SysTtsForwarderService.ACTION_ON_STARTING)
            }
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        AppConst.localBroadcast.unregisterReceiver(mReceiver)
    }

    private fun updateSwitch(isStarted: Boolean) {
        binding.tilPort.isEnabled = !isStarted
        binding.fabSwitch.isChecked = isStarted
    }

    @Suppress("DEPRECATION")
    inner class MyReceiver : BroadcastReceiver() {
        override fun onReceive(ctx: Context?, intent: Intent?) {
            when (intent?.action) {
                SysTtsForwarderService.ACTION_ON_CLOSED -> {
                    updateSwitch(SysTtsForwarderService.isRunning)
                }
                SysTtsForwarderService.ACTION_ON_STARTING -> {
                    updateSwitch(SysTtsForwarderService.isRunning)
                    logAdapter?.removeAll()
                }
                SysTtsForwarderService.ACTION_ON_LOG -> {
                    logAdapter?.let {
                        val log = intent.getParcelableExtra<AppLog>(KeyConst.KEY_DATA) as AppLog
                        val layout = binding.rvLog.layoutManager as LinearLayoutManager
                        val isBottom = layout.findLastVisibleItemPosition() == layout.itemCount - 1
                        it.append(log)
                        /* 判断是否在最底部 */
                        if (isBottom)
                            binding.rvLog.scrollToPosition(it.itemCount - 1)
                    }
                }
            }
        }
    }
}