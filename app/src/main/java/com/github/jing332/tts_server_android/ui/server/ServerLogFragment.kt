package com.github.jing332.tts_server_android.ui.server

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
import com.github.jing332.tts_server_android.constant.KeyConst
import com.github.jing332.tts_server_android.databinding.FragmentServerLogBinding
import com.github.jing332.tts_server_android.help.ServerConfig
import com.github.jing332.tts_server_android.service.TtsIntentService
import com.github.jing332.tts_server_android.ui.custom.adapter.LogListItemAdapter
import tts_server_lib.Tts_server_lib

class ServerLogFragment : Fragment() {
    private val vb by lazy { FragmentServerLogBinding.inflate(layoutInflater) }
    private val vm: ServerLogViewModel by viewModels()

    private val mReceiver by lazy { MyReceiver() }
    private val logList: ArrayList<AppLog> by lazy { ArrayList() }
    private val logAdapter: LogListItemAdapter by lazy { LogListItemAdapter(logList) }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return vb.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        vb.rvLog.adapter = logAdapter
        val layoutManager = LinearLayoutManager(requireContext())
        layoutManager.stackFromEnd = true
        vb.rvLog.layoutManager = layoutManager

        /*启动按钮*/
        vb.btnStart.setOnClickListener {
            ServerConfig.port = vb.etPort.text.toString().toInt()
            val i = Intent(App.context, TtsIntentService::class.java)
            requireContext().startService(i)
        }
        /* 关闭按钮 */
        vb.btnClose.setOnClickListener {
            if (TtsIntentService.instance?.isRunning == true) { /*服务运行中*/
                TtsIntentService.instance?.closeServer() /*关闭服务 然后将通过广播通知MainActivity*/
            }
        }
        
        val port = ServerConfig.port
        vb.etPort.setText(port.toString())
        if (TtsIntentService.instance?.isRunning == true) {
            setControlStatus(false)
            val localIp = Tts_server_lib.getOutboundIP()
            val msg = "服务已在运行, 监听地址: ${localIp}:${port}"
            logList.add(AppLog(LogLevel.WARN, msg))
        } else {
            val msg = "请点击启动按钮\n然后右上角菜单打开网页版↗️" +
                    "\n随后生成链接导入阅读APP即可使用" +
                    "\n\n关闭请点关闭按钮, 并等待响应。" +
                    "\n⚠️注意: 本APP需常驻后台运行！⚠️"
            logList.add(AppLog(LogLevel.INFO, msg))
        }

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
            vb.etPort.isEnabled = true
            vb.btnStart.isEnabled = true
            vb.btnClose.isEnabled = false
        } else { //禁用按钮，编辑
            vb.etPort.isEnabled = false
            vb.btnStart.isEnabled = false
            vb.btnClose.isEnabled = true
        }
    }

    /* 监听广播 */
    inner class MyReceiver : BroadcastReceiver() {
        @Suppress("DEPRECATION")
        override fun onReceive(ctx: Context?, intent: Intent?) {
            when (intent?.action) {
                TtsIntentService.ACTION_ON_LOG -> {
                    val log = intent.getParcelableExtra<AppLog>(KeyConst.KEY_DATA) as AppLog
                    val layout = vb.rvLog.layoutManager as LinearLayoutManager
                    val isBottom = layout.findLastVisibleItemPosition() == layout.itemCount - 1
                    logAdapter.append(log)
                    /* 判断是否在最底部 */
                    if (isBottom)
                        vb.rvLog.scrollToPosition(logAdapter.itemCount - 1)
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