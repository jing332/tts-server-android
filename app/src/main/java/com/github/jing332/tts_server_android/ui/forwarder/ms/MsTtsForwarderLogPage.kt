package com.github.jing332.tts_server_android.ui.forwarder.ms

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import by.kirich1409.viewbindingdelegate.viewBinding
import com.github.jing332.tts_server_android.*
import com.github.jing332.tts_server_android.constant.AppConst
import com.github.jing332.tts_server_android.constant.KeyConst
import com.github.jing332.tts_server_android.databinding.MsTtsForwarderLogFragmentBinding
import com.github.jing332.tts_server_android.help.config.ServerConfig
import com.github.jing332.tts_server_android.service.forwarder.ms.TtsIntentService
import com.github.jing332.tts_server_android.ui.AppLog
import com.github.jing332.tts_server_android.ui.LogLevel
import com.github.jing332.tts_server_android.ui.view.adapter.LogListItemAdapter
import com.github.jing332.tts_server_android.utils.clickWithThrottle
import tts_server_lib.Tts_server_lib

class MsTtsForwarderLogPage : Fragment(R.layout.ms_tts_forwarder_log_fragment) {
    private val binding by viewBinding(MsTtsForwarderLogFragmentBinding::bind)

    private val mReceiver = MyReceiver()
    private var mLogAdapter: LogListItemAdapter? = null

    private var port = 1233
        get() {
            return try {
                binding.tilPort.editText!!.text.toString().toInt()
            } catch (e: Exception) {
                field
            }
        }
        set(value) {
            field = value
            binding.tilPort.editText!!.setText(value.toString())
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mLogAdapter = LogListItemAdapter()
        binding.rvLog.adapter = mLogAdapter
        val layoutManager = LinearLayoutManager(requireContext())
        layoutManager.stackFromEnd = true
        binding.rvLog.layoutManager = layoutManager


        binding.switchOnOff.clickWithThrottle {
            if (binding.switchOnOff.isChecked) {
                ServerConfig.port = port
                val i = Intent(app, TtsIntentService::class.java)
                requireContext().startService(i)
            } else {
                if (TtsIntentService.instance?.isRunning == true) { /*服务运行中*/
                    TtsIntentService.instance?.closeServer() /*关闭服务 然后将通过广播通知MainActivity*/
                }
            }
        }

        port = ServerConfig.port
        if (TtsIntentService.instance?.isRunning == true) {
            val localIp = Tts_server_lib.getOutboundIP()
            mLogAdapter?.append(
                AppLog(
                    LogLevel.WARN,
                    getString(R.string.server_log_service_running, "${localIp}:${port}")
                )
            )
        } else {
            mLogAdapter?.append((AppLog(LogLevel.INFO, getString(R.string.server_log_tips))))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        /*注册广播*/
        IntentFilter(TtsIntentService.ACTION_ON_LOG).apply {
            addAction(TtsIntentService.ACTION_ON_STARTED)
            addAction(TtsIntentService.ACTION_ON_CLOSED)
            AppConst.localBroadcast.registerReceiver(mReceiver, this)
        }
        AppConst.isServerLogEnabled = true
    }

    override fun onDestroy() {
        super.onDestroy()
        AppConst.localBroadcast.unregisterReceiver(mReceiver)
        AppConst.isServerLogEnabled = false
    }

    fun updateState(isRunning: Boolean) {
        binding.switchOnOff.isChecked = isRunning
        binding.tilPort.isEnabled = !isRunning
    }

    /* 监听广播 */
    inner class MyReceiver : BroadcastReceiver() {
        @Suppress("DEPRECATION")
        override fun onReceive(ctx: Context?, intent: Intent?) {
            when (intent?.action) {
                TtsIntentService.ACTION_ON_LOG -> {
                    mLogAdapter?.let {
                        val log = intent.getParcelableExtra<AppLog>(KeyConst.KEY_DATA) as AppLog
                        val layout = binding.rvLog.layoutManager as LinearLayoutManager
                        val isBottom = layout.findLastVisibleItemPosition() == layout.itemCount - 1
                        it.append(log)
                        /* 判断是否在最底部 */
                        if (isBottom)
                            binding.rvLog.scrollToPosition(it.itemCount - 1)
                    }
                }
                TtsIntentService.ACTION_ON_STARTED -> {
                    mLogAdapter?.removeAll() /* 清空日志 */
                    updateState(true)
                }
                TtsIntentService.ACTION_ON_CLOSED -> {
                    updateState(false) /* 设置运行按钮可点击 */
                }
            }
        }
    }
}