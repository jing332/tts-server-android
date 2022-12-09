package com.github.jing332.tts_server_android.ui.server

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.WebStorage
import android.webkit.WebView
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.github.jing332.tts_server_android.App
import com.github.jing332.tts_server_android.R
import com.github.jing332.tts_server_android.databinding.FragmentMainServerBinding
import com.github.jing332.tts_server_android.help.ServerConfig
import com.github.jing332.tts_server_android.service.TtsIntentService
import com.github.jing332.tts_server_android.ui.MainActivity
import com.github.jing332.tts_server_android.ui.ScSwitchActivity
import com.github.jing332.tts_server_android.util.MyTools
import com.github.jing332.tts_server_android.util.reduceDragSensitivity
import com.github.jing332.tts_server_android.util.setFadeAnim
import com.github.jing332.tts_server_android.util.toast


class MainServerFragment : Fragment() {

    private val binding: FragmentMainServerBinding by lazy {
        FragmentMainServerBinding.inflate(layoutInflater)
    }

    private val mReceiver: MyReceiver by lazy { MyReceiver() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        App.localBroadcast.registerReceiver(
            mReceiver,
            IntentFilter(MainActivity.ACTION_OPTION_ITEM_SELECTED_ID).apply {
                addAction(MainActivity.ACTION_BACK_KEY_DOWN)
            }
        )

        binding.viewPager.reduceDragSensitivity(8)
        binding.viewPager.isSaveEnabled = false
        binding.viewPager.adapter = FragmentAdapter(this)
        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                binding.bottomNavigationView.menu.getItem(position).isChecked = true
            }
        })
        binding.bottomNavigationView.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.menu_serverLog -> binding.viewPager.setCurrentItem(0, true)
                R.id.menu_serverWeb -> {
                    binding.viewPager.setCurrentItem(1, true)
                }
            }
            true
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return binding.root
    }

    override fun onDestroy() {
        super.onDestroy()
        App.localBroadcast.unregisterReceiver(mReceiver)
    }

    fun optionsItemSelected(itemId: Int): Boolean {
        when (itemId) {
            R.id.menu_openWeb -> { /* {打开网页版} 按钮 */
                if (TtsIntentService.instance?.isRunning == true) {
                    val intent = Intent(Intent.ACTION_VIEW)
                    intent.data =
                        Uri.parse("http://localhost:${TtsIntentService.instance?.cfg?.port}")
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intent)
                } else {
                    toast(R.string.please_start_service)
                }
            }
            R.id.menu_clearWebData -> {
                WebView(requireContext()).apply {
                    clearCache(true)
                    clearFormData()
                    clearSslPreferences()
                }
                CookieManager.getInstance().apply {
                    removeAllCookies(null)
                    flush()
                }
                WebStorage.getInstance().deleteAllData()
                toast(R.string.cleared)
            }
            R.id.menu_setToken -> {
                val token = ServerConfig.token

                val editText = EditText(requireContext())
                editText.setText(token)
                AlertDialog.Builder(requireContext()).setTitle(getString(R.string.set_token))
                    .setView(editText)
                    .setPositiveButton(
                        android.R.string.ok
                    ) { _, _ ->
                        val text = editText.text.toString()
                        if (text != token) {
                            toast(getString(R.string.token_set_to) + text.ifEmpty { "空" })
                            ServerConfig.token = text
                        }
                    }.setNegativeButton(R.string.reset) { _, _ ->
                        ServerConfig.token = ""
                        toast(getString(R.string.ok_reset))
                    }.setFadeAnim().show()
            }
            R.id.menu_wakeLock -> { /* 唤醒锁 */
                ServerConfig.isWakeLockEnabled = !ServerConfig.isWakeLockEnabled
                toast(R.string.restart_service_to_update)
            }
            R.id.menu_shortcut -> {
                MyTools.addShortcut(
                    requireContext(),
                    getString(R.string.app_switch),
                    "server_switch",
                    R.drawable.ic_switch,
                    Intent(requireContext(), ScSwitchActivity::class.java)
                )
            }
        }
        return true
    }

    val logFragment = ServerLogFragment()
    val webFragment = ServerWebFragment()

    inner class FragmentAdapter(fragment: Fragment) :
        FragmentStateAdapter(fragment) {
        private val fragmentList = arrayListOf(logFragment, webFragment)
        override fun getItemCount(): Int {
            return fragmentList.size
        }

        override fun createFragment(position: Int): Fragment {
            return fragmentList[position]
        }
    }

    inner class MyReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                MainActivity.ACTION_OPTION_ITEM_SELECTED_ID -> {
                    val itemId = intent.getIntExtra(MainActivity.KEY_MENU_ITEM_ID, -1)
                    optionsItemSelected(itemId)
                }
                MainActivity.ACTION_BACK_KEY_DOWN -> {
                    if (!webFragment.onBackKeyDown())
                        binding.viewPager.setCurrentItem(0, true)
                }
            }
        }

    }

}