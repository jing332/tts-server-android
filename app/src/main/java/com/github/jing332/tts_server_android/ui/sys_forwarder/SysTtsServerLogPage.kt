package com.github.jing332.tts_server_android.ui.sys_forwarder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.jing332.tts_server_android.App
import com.github.jing332.tts_server_android.R
import com.github.jing332.tts_server_android.constant.KeyConst
import com.github.jing332.tts_server_android.databinding.SysTtsServerLogPageFragmentBinding
import com.github.jing332.tts_server_android.help.SysTtsForwarderConfig
import com.github.jing332.tts_server_android.service.sysforwarder.SysTtsForwarderService
import com.github.jing332.tts_server_android.ui.AppLog
import com.github.jing332.tts_server_android.ui.LogLevel
import com.github.jing332.tts_server_android.ui.MainActivity
import com.github.jing332.tts_server_android.ui.custom.adapter.LogListItemAdapter
import com.github.jing332.tts_server_android.util.clickWithThrottle
import com.github.jing332.tts_server_android.util.toast

class SysTtsServerLogPage : Fragment() {
    private val binding: SysTtsServerLogPageFragmentBinding by lazy {
        SysTtsServerLogPageFragmentBinding.inflate(layoutInflater)
    }

    private val mReceiver by lazy { MyReceiver() }
    private val logAdapter: LogListItemAdapter by lazy { LogListItemAdapter() }

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

        binding.rvLog.adapter = logAdapter
        val layoutManager = LinearLayoutManager(requireContext())
        layoutManager.stackFromEnd = true
        binding.rvLog.layoutManager = layoutManager

        logAdapter.append(AppLog(LogLevel.INFO, "启动后点击右上角打开网页, 以进行配置测试和导入阅读。"))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        App.localBroadcast.registerReceiver(
            mReceiver,
            IntentFilter(SysTtsForwarderService.ACTION_ON_CLOSED).apply {
                addAction(SysTtsForwarderService.ACTION_ON_LOG)
                addAction(MainActivity.ACTION_OPTION_ITEM_SELECTED_ID)
            }
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        App.localBroadcast.unregisterReceiver(mReceiver)
    }

    private fun updateSwitch(isStarted: Boolean) {
        if (isStarted)
            binding.fabSwitch.setImageResource(R.drawable.ic_baseline_check_circle_outline_24)
        else
            binding.fabSwitch.setImageResource(R.drawable.ic_baseline_do_disturb_alt_24)
    }


    fun optionsItemSelected(itemId: Int) {
        when (itemId) {
            R.id.menu_openWeb -> { /* {打开网页版} 按钮 */
                if (SysTtsForwarderService.instance?.isRunning == true) {
                    val intent = Intent(Intent.ACTION_VIEW)
                    intent.data =
                        Uri.parse("http://localhost:${SysTtsForwarderConfig.port}")
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intent)
                } else {
                    toast(R.string.server_please_start_service)
                }
            }
        }
    }

    @Suppress("DEPRECATION")
    inner class MyReceiver : BroadcastReceiver() {
        override fun onReceive(ctx: Context?, intent: Intent?) {
            when (intent?.action) {
                MainActivity.ACTION_OPTION_ITEM_SELECTED_ID -> {
                    optionsItemSelected(intent.getIntExtra(MainActivity.KEY_MENU_ITEM_ID, -1))
                }
                SysTtsForwarderService.ACTION_ON_CLOSED -> {
                    updateSwitch(SysTtsForwarderService.instance?.isRunning == true)
                }
                SysTtsForwarderService.ACTION_ON_LOG -> {
                    val log = intent.getParcelableExtra<AppLog>(KeyConst.KEY_DATA) as AppLog
                    val layout = binding.rvLog.layoutManager as LinearLayoutManager
                    val isBottom = layout.findLastVisibleItemPosition() == layout.itemCount - 1
                    logAdapter.append(log)
                    /* 判断是否在最底部 */
                    if (isBottom)
                        binding.rvLog.scrollToPosition(logAdapter.itemCount - 1)
                }
            }
        }
    }
}