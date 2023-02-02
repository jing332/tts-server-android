package com.github.jing332.tts_server_android.ui.forwarder.ms

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.webkit.CookieManager
import android.webkit.WebStorage
import android.webkit.WebView
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.github.jing332.tts_server_android.App
import com.github.jing332.tts_server_android.R
import com.github.jing332.tts_server_android.databinding.MsTtsForwarderFragmentBinding
import com.github.jing332.tts_server_android.help.ServerConfig
import com.github.jing332.tts_server_android.service.forwarder.ms.TtsIntentService
import com.github.jing332.tts_server_android.ui.MainActivity
import com.github.jing332.tts_server_android.ui.custom.MaterialTextInput
import com.github.jing332.tts_server_android.util.MyTools
import com.github.jing332.tts_server_android.util.reduceDragSensitivity

import com.github.jing332.tts_server_android.util.toast
import com.google.android.material.dialog.MaterialAlertDialogBuilder


class MsTtsForwarderFragment : Fragment(), MenuProvider {
    private val binding: MsTtsForwarderFragmentBinding by lazy {
        MsTtsForwarderFragmentBinding.inflate(layoutInflater)
    }

    private val mReceiver: MyReceiver by lazy { MyReceiver() }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(this, viewLifecycleOwner, Lifecycle.State.RESUMED)

        App.localBroadcast.registerReceiver(
            mReceiver,
            IntentFilter(MainActivity.ACTION_BACK_KEY_DOWN).apply {
            }
        )

        binding.viewPager.reduceDragSensitivity(8)
        binding.viewPager.isSaveEnabled = false
        binding.viewPager.adapter = FragmentAdapter(this)
        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                binding.bnv.menu.getItem(position).isChecked = true
            }
        })
        binding.bnv.setOnItemSelectedListener {
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

    val logFragment = MsTtsForwarderLogPage()
    val webFragment = MsTtsForwarderWebPage()

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
                MainActivity.ACTION_BACK_KEY_DOWN -> {
                    if (!webFragment.onBackKeyDown())
                        binding.viewPager.setCurrentItem(0, true)
                }
            }
        }

    }

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.forwarder_ms, menu)
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        return when (menuItem.itemId) {
            R.id.menu_open_web -> { /* {打开网页版} 按钮 */
                if (TtsIntentService.instance?.isRunning == true) {
                    val intent = Intent(Intent.ACTION_VIEW)
                    intent.data =
                        Uri.parse("http://localhost:${TtsIntentService.instance?.cfg?.port}")
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intent)
                } else {
                    toast(R.string.server_please_start_service)
                }
                true
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
                true
            }
            R.id.menu_setToken -> {
                val token = ServerConfig.token

                val et = MaterialTextInput(requireContext())
                et.inputLayout.setHint(R.string.server_set_token)
                et.inputEdit.setText(token)
                MaterialAlertDialogBuilder(requireContext()).setTitle(getString(R.string.server_set_token))
                    .setView(et)
                    .setPositiveButton(
                        android.R.string.ok
                    ) { _, _ ->
                        val text = et.inputEdit.text.toString()
                        if (text != token) {
                            toast(getString(R.string.server_token_set_to) + text.ifEmpty {
                                getString(
                                    R.string.none
                                )
                            })
                            ServerConfig.token = text
                        }
                    }.setNegativeButton(R.string.reset) { _, _ ->
                        ServerConfig.token = ""
                        toast(getString(R.string.ok_reset))
                    }.show()
                true
            }
            R.id.menu_wake_lock -> { /* 唤醒锁 */
                ServerConfig.isWakeLockEnabled = !ServerConfig.isWakeLockEnabled
                toast(R.string.server_restart_service_to_update)
                true
            }
            R.id.menu_shortcut -> {
                MyTools.addShortcut(
                    requireContext(),
                    getString(R.string.app_switch),
                    "server_switch",
                    R.drawable.ic_switch,
                    Intent(requireContext(), ScSwitchActivity::class.java)
                )
                true
            }
            else -> false
        }
    }

    override fun onPrepareMenu(menu: Menu) {
        super.onPrepareMenu(menu)
        menu.findItem(R.id.menu_wake_lock)?.isChecked = ServerConfig.isWakeLockEnabled
    }
}