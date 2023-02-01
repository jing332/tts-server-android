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
import com.github.jing332.tts_server_android.App
import com.github.jing332.tts_server_android.R
import com.github.jing332.tts_server_android.constant.KeyConst
import com.github.jing332.tts_server_android.databinding.SysTtsForwarderLogPageFragmentBinding
import com.github.jing332.tts_server_android.help.SysTtsForwarderConfig
import com.github.jing332.tts_server_android.service.sysforwarder.SysTtsForwarderService
import com.github.jing332.tts_server_android.ui.AppLog
import com.github.jing332.tts_server_android.ui.LogLevel
import com.github.jing332.tts_server_android.ui.custom.adapter.LogListItemAdapter
import com.github.jing332.tts_server_android.util.clickWithThrottle

class SysTtsForwarderLogPage : Fragment() {
    private val binding: SysTtsForwarderLogPageFragmentBinding by lazy {
        SysTtsForwarderLogPageFragmentBinding.inflate(layoutInflater)
    }

    private val mReceiver by lazy { MyReceiver() }
    private var logAdapter: LogListItemAdapter? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = binding.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.etPort.setText(SysTtsForwarderConfig.port.toString())
        binding.fabSwitch.clickWithThrottle {
            SysTtsForwarderConfig.port = binding.etPort.text.toString().toInt()
            if (SysTtsForwarderService.instance?.isRunning == true) {
                App.localBroadcast.sendBroadcast(Intent(SysTtsForwarderService.ACTION_REQUEST_CLOSE_SERVER))
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

        if (SysTtsForwarderService.instance?.isRunning == true) {
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
        App.localBroadcast.registerReceiver(
            mReceiver,
            IntentFilter(SysTtsForwarderService.ACTION_ON_CLOSED).apply {
                addAction(SysTtsForwarderService.ACTION_ON_LOG)
                addAction(SysTtsForwarderService.ACTION_ON_STARTING)
            }
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        App.localBroadcast.unregisterReceiver(mReceiver)
    }

    private fun updateSwitch(isStarted: Boolean) {
        binding.tilPort.isEnabled = !isStarted
        if (isStarted) {
            binding.fabSwitch.setImageResource(R.drawable.ic_baseline_check_circle_outline_24)
        } else
            binding.fabSwitch.setImageResource(R.drawable.ic_baseline_do_disturb_alt_24)
    }



    @Suppress("DEPRECATION")
    inner class MyReceiver : BroadcastReceiver() {
        override fun onReceive(ctx: Context?, intent: Intent?) {
            when (intent?.action) {
                SysTtsForwarderService.ACTION_ON_CLOSED -> {
                    updateSwitch(SysTtsForwarderService.instance?.isRunning == true)
                }
                SysTtsForwarderService.ACTION_ON_STARTING -> logAdapter?.removeAll()
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