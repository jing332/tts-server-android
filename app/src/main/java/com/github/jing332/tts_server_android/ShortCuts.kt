package com.github.jing332.tts_server_android

import android.content.Context
import android.content.Intent
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import com.github.jing332.tts_server_android.ui.forwarder.ms.ScSwitchActivity
import com.github.jing332.tts_server_android.ui.systts.plugin.PluginManagerActivity
import com.github.jing332.tts_server_android.ui.systts.replace.ReplaceManagerActivity

object ShortCuts {
    private inline fun <reified T> buildIntent(context: Context): Intent {
        val intent = Intent(context, T::class.java)
        intent.action = Intent.ACTION_VIEW
        return intent
    }

    private fun buildSysSwitchShortCutInfo(context: Context): ShortcutInfoCompat {
        val msSwitchIntent =
            buildIntent<com.github.jing332.tts_server_android.ui.forwarder.system.ScSwitchActivity>(
                context
            )
        return ShortcutInfoCompat.Builder(context, "forwarder_sys_switch")
            .setShortLabel(context.getString(R.string.forwarder_systts))
            .setLongLabel(context.getString(R.string.forwarder_systts))
            .setIcon(IconCompat.createWithResource(context, R.drawable.ic_switch))
            .setIntent(msSwitchIntent)
            .build()
    }


    private fun buildMsSwitchShortCutInfo(context: Context): ShortcutInfoCompat {
        val msSwitchIntent = buildIntent<ScSwitchActivity>(context)
        return ShortcutInfoCompat.Builder(context, "forwarder_ms_switch")
            .setShortLabel(context.getString(R.string.forwarder_ms))
            .setLongLabel(context.getString(R.string.forwarder_ms))
            .setIcon(IconCompat.createWithResource(context, R.drawable.ic_switch))
            .setIntent(msSwitchIntent)
            .build()
    }

    private fun buildReplaceManagerShortCutInfo(context: Context): ShortcutInfoCompat {
        return ShortcutInfoCompat.Builder(context, "replace_manager")
            .setShortLabel(context.getString(R.string.systts_replace_rule_manager))
            .setLongLabel(context.getString(R.string.systts_replace_rule_manager))
            .setIcon(IconCompat.createWithResource(context, R.drawable.ic_shortcut_replace))
            .setIntent(buildIntent<ReplaceManagerActivity>(context))
            .build()
    }

    private fun buildPluginManagerShortCutInfo(context: Context): ShortcutInfoCompat {
        return ShortcutInfoCompat.Builder(context, "plugin_manager")
            .setShortLabel(context.getString(R.string.plugin_manager))
            .setLongLabel(context.getString(R.string.plugin_manager))
            .setIcon(IconCompat.createWithResource(context, R.drawable.ic_shortcut_plugin))
            .setIntent(buildIntent<PluginManagerActivity>(context))
            .build()
    }

    fun buildShortCuts(context: Context) {
        ShortcutManagerCompat.setDynamicShortcuts(
            context, listOf(
                buildMsSwitchShortCutInfo(context),
                buildSysSwitchShortCutInfo(context),
                buildReplaceManagerShortCutInfo(context),
                buildPluginManagerShortCutInfo(context)
            )
        )
    }


}