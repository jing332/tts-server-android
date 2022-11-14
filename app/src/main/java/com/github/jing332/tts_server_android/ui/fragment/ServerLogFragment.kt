package com.github.jing332.tts_server_android.ui.fragment

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.jing332.tts_server_android.*
import com.github.jing332.tts_server_android.databinding.FragmentServerLogBinding
import com.github.jing332.tts_server_android.service.TtsIntentService
import com.github.jing332.tts_server_android.ui.custom.adapter.LogListItemAdapter
import com.github.jing332.tts_server_android.util.SharedPrefsUtils
import tts_server_lib.Tts_server_lib

class ServerLogFragment : Fragment() {
    private val binding by lazy { FragmentServerLogBinding.inflate(layoutInflater) }
    private val viewModel: ServerLogFragmentViewModel by viewModels()

    private val mReceiver by lazy { MyReceiver() }
    private val logList: ArrayList<MyLog> by lazy { ArrayList() }
    private val logAdapter: LogListItemAdapter by lazy { LogListItemAdapter(logList) }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.rvLog.adapter = logAdapter
        val layoutManager = LinearLayoutManager(requireContext())
        layoutManager.stackFromEnd = true
        binding.rvLog.layoutManager = layoutManager

        /*启动按钮*/
        binding.btnStart.setOnClickListener {
            SharedPrefsUtils.setPort(requireContext(), binding.etPort.text.toString().toInt())
            val i = Intent(App.context, TtsIntentService::class.java)
            requireContext().startService(i)
        }
        /* 关闭按钮 */
        binding.btnClose.setOnClickListener {
            if (TtsIntentService.instance?.isRunning == true) { /*服务运行中*/
                TtsIntentService.instance?.closeServer() /*关闭服务 然后将通过广播通知MainActivity*/
            }
        }


        val port = SharedPrefsUtils.getPort(requireContext())
        binding.etPort.setText(port.toString())
        if (TtsIntentService.instance?.isRunning == true) {
            setControlStatus(false)
            val localIp = Tts_server_lib.getOutboundIP()
            val msg = "服务已在运行, 监听地址: ${localIp}:${port}"
            logList.add(MyLog(LogLevel.WARN, msg))
        } else {
            val msg = "请点击启动按钮\n然后右上角菜单打开网页版↗️" +
                    "\n随后生成链接导入阅读APP即可使用" +
                    "\n\n关闭请点关闭按钮, 并等待响应。" +
                    "\n⚠️注意: 本APP需常驻后台运行！⚠️"
            logList.add(MyLog(LogLevel.INFO, msg))
        }

        /*注册广播*/
        IntentFilter(TtsIntentService.ACTION_ON_LOG).apply {
            addAction(TtsIntentService.ACTION_ON_STARTED)
            addAction(TtsIntentService.ACTION_ON_CLOSED)
            App.localBroadcast.registerReceiver(mReceiver, this)
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        App.localBroadcast.unregisterReceiver(mReceiver)
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
                    val data = intent.getSerializableExtra("data") as MyLog
                    val layout = binding.rvLog.layoutManager as LinearLayoutManager
                    val isBottom = layout.findLastVisibleItemPosition() == layout.itemCount - 1
                    logAdapter.append(data)
                    /* 判断是否在最底部 */
                    if (isBottom)
                        binding.rvLog.scrollToPosition(logAdapter.itemCount - 1)
                }
                TtsIntentService.ACTION_ON_STARTED -> {
                    logAdapter.removeAll() /* 清空日志 */
                    setControlStatus(false)
                }
                TtsIntentService.ACTION_ON_CLOSED -> {
                    setControlStatus(true) /* 设置运行按钮可点击 */
                }
            }
        }
    }
}