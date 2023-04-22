package com.github.jing332.tts_server_android.ui.forwarder.sys

import android.content.Intent
import android.net.Uri
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import com.github.jing332.tts_server_android.R
import com.github.jing332.tts_server_android.help.config.SysTtsForwarderConfig
import com.github.jing332.tts_server_android.service.forwarder.ForwarderServiceManager
import com.github.jing332.tts_server_android.service.forwarder.ForwarderServiceManager.startSysTtsForwarder
import com.github.jing332.tts_server_android.service.forwarder.system.SysTtsForwarderService
import com.github.jing332.tts_server_android.ui.forwarder.AbsForwarderHomePageFragment
import com.github.jing332.tts_server_android.ui.forwarder.AbsForwarderHostFragment
import com.github.jing332.tts_server_android.ui.forwarder.AbsForwarderWebPageFragment
import com.github.jing332.tts_server_android.utils.MyTools
import com.github.jing332.tts_server_android.utils.toast

class HostFragment() : AbsForwarderHostFragment() {
    override val isServiceRunning: Boolean = SysTtsForwarderService.isRunning
    override fun onSwitchChanged(isChecked: Boolean) {
        if (isChecked) {
            requireContext().startSysTtsForwarder()
        } else {
            ForwarderServiceManager.closeSysTtsForwarder()
        }
    }

    override val homePageFragment: AbsForwarderHomePageFragment
        get() = HomeFragment()

    override val webPageFragment: AbsForwarderWebPageFragment
        get() = WebFragment()

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.forwarder_system, menu)
    }

    override fun onPrepareMenu(menu: Menu) {
        super.onPrepareMenu(menu)
        menu.findItem(R.id.menu_wake_lock)?.isChecked =
            SysTtsForwarderConfig.isWakeLockEnabled
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        return when (menuItem.itemId) {
            R.id.menu_open_web -> {
                if (SysTtsForwarderService.isRunning) {
                    val intent = Intent(Intent.ACTION_VIEW)
                    intent.data =
                        Uri.parse("http://localhost:${SysTtsForwarderConfig.port}")
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intent)
                } else {
                    toast(R.string.server_please_start_service)
                }
                true
            }
            R.id.menu_wake_lock -> {
                menuItem.isChecked = !menuItem.isChecked
                SysTtsForwarderConfig.isWakeLockEnabled = menuItem.isChecked
                toast(R.string.server_restart_service_to_update)
                true
            }
            R.id.menu_shortcut -> {
                MyTools.addShortcut(
                    requireContext(),
                    getString(R.string.forwarder_systts),
                    "forwarder_system",
                    R.mipmap.ic_launcher_round,
                    Intent(requireContext(), ScSwitchActivity::class.java)
                )
                true
            }
            else -> false
        }
    }
}
