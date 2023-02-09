package com.github.jing332.tts_server_android.ui.forwarder.ms

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
import com.github.jing332.tts_server_android.*
import com.github.jing332.tts_server_android.constant.KeyConst
import com.github.jing332.tts_server_android.databinding.MsTtsForwarderLogFragmentBinding
import com.github.jing332.tts_server_android.help.config.ServerConfig
import com.github.jing332.tts_server_android.service.forwarder.ms.TtsIntentService
import com.github.jing332.tts_server_android.ui.AppLog
import com.github.jing332.tts_server_android.ui.LogLevel
import com.github.jing332.tts_server_android.ui.view.adapter.LogListItemAdapter
import tts_server_lib.Tts_server_lib

class MsTtsForwarderLogPage : Fragment() {
    private val binding: MsTtsForwarderLogFragmentBinding by lazy {
        MsTtsForwarderLogFragmentBinding.inflate(layoutInflater)
    }

    private val mReceiver by lazy { MyReceiver() }
    private var mLogAdapter: LogListItemAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mLogAdapter = LogListItemAdapter()
        binding.rvLog.adapter = mLogAdapter
        val layoutManager = LinearLayoutManager(requireContext())
        layoutManager.stackFromEnd = true
        binding.rvLog.layoutManager = layoutManager

        /*启动按钮*/
        binding.btnStart.setOnClickListener {
            ServerConfig.port = binding.etPort.text.toString().toInt()
            val i = Intent(app, TtsIntentService::class.java)
            requireContext().startService(i)
        }
        /* 关闭按钮 */
        binding.btnClose.setOnClickListener {
            if (TtsIntentService.instance?.isRunning == true) { /*服务运行中*/
                TtsIntentService.instance?.closeServer() /*关闭服务 然后将通过广播通知MainActivity*/
            }
        }

        val port = ServerConfig.port
        binding.etPort.setText(port.toString())
        if (TtsIntentService.instance?.isRunning == true) {
            setControlStatus(false)
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
            App.localBroadcast.registerReceiver(mReceiver, this)
        }
        App.isServerLogEnabled = true
    }

    override fun onDestroy() {
        super.onDestroy()
        App.localBroadcast.unregisterReceiver(mReceiver)
        App.isServerLogEnabled = false
    }

    /* 设置底部按钮、端口 是否可点击 */
    fun setControlStatus(enable: Boolean) {
        if (enable) { //可点击{运行}按钮，编辑
            binding.etPort.isEnabled = true
            binding.btnStart.isEnabled = true
            binding.btnClose.isEnabled = false
        } else { //禁用按钮，编辑
            binding.etPort.isEnabled = false
            binding.btnStart.isEnabled = false
            binding.btnClose.isEnabled = true
        }
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
                    setControlStatus(false)
                }
                TtsIntentService.ACTION_ON_CLOSED -> {
                    setControlStatus(true) /* 设置运行按钮可点击 */
                }
            }
        }
    }
}