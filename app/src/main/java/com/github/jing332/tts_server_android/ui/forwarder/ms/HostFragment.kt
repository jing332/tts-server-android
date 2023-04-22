package com.github.jing332.tts_server_android.ui.forwarder.ms

import android.content.Intent
import android.net.Uri
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.webkit.CookieManager
import android.webkit.WebStorage
import android.webkit.WebView
import com.github.jing332.tts_server_android.R
import com.github.jing332.tts_server_android.help.config.ServerConfig
import com.github.jing332.tts_server_android.service.forwarder.ForwarderServiceManager.startMsTtsForwarder
import com.github.jing332.tts_server_android.service.forwarder.ms.MsTtsForwarderService
import com.github.jing332.tts_server_android.ui.forwarder.AbsForwarderHomePageFragment
import com.github.jing332.tts_server_android.ui.forwarder.AbsForwarderHostFragment
import com.github.jing332.tts_server_android.ui.forwarder.AbsForwarderWebPageFragment
import com.github.jing332.tts_server_android.ui.view.MaterialTextInput
import com.github.jing332.tts_server_android.utils.MyTools
import com.github.jing332.tts_server_android.utils.toast
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class HostFragment(
) : AbsForwarderHostFragment() {
    override val isServiceRunning: Boolean get() = MsTtsForwarderService.isRunning
    override val homePageFragment: AbsForwarderHomePageFragment
        get() = HomeFragment()
    override val webPageFragment: AbsForwarderWebPageFragment
        get() = WebFragment()

    override fun onSwitchChanged(isChecked: Boolean) {
        if (isChecked) {
            requireContext().startMsTtsForwarder()
        } else {
            MsTtsForwarderService.instance?.close()
        }
    }

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.forwarder_ms, menu)
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        return when (menuItem.itemId) {
            R.id.menu_open_web -> { /* {打开网页版} 按钮 */
                if (MsTtsForwarderService.isRunning) {
                    val intent = Intent(Intent.ACTION_VIEW)
                    intent.data =
                        Uri.parse("http://localhost:${ServerConfig.port}")
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
                et.setHint(R.string.server_set_token)
                et.editText!!.setText(token)
                MaterialAlertDialogBuilder(requireContext()).setTitle(getString(R.string.server_set_token))
                    .setView(et)
                    .setPositiveButton(
                        android.R.string.ok
                    ) { _, _ ->
                        val text = et.editText!!.text.toString()
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