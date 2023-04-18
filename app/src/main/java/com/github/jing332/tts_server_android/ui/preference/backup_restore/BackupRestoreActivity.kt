package com.github.jing332.tts_server_android.ui.preference.backup_restore

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.github.jing332.tts_server_android.R
import com.github.jing332.tts_server_android.databinding.BackupRestoreActivityBinding
import com.github.jing332.tts_server_android.ui.base.BackActivity
import com.github.jing332.tts_server_android.utils.FileUtils.readBytes
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class BackupRestoreActivity : BackActivity() {
    private val binding by lazy { BackupRestoreActivityBinding.inflate(layoutInflater) }
    private lateinit var fragment: BackupRestoreFragment

    companion object {
        const val TAG = "BackupRestoreActivity"
        const val ACTION_RESTORE = "$TAG.action_restore"
        const val KEY_URI = "key_uri"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        fragment = BackupRestoreFragment()
        supportFragmentManager.beginTransaction()
            .add(R.id.container, fragment, "BackupRestoreFragment")
            .commit()

//        (intent.getBinder() as? ByteArrayBinder)?.let {
//            fragment.restore(it.data)
//        }
        restoreFromIntent(intent)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        restoreFromIntent(intent)
    }

    private fun restoreFromIntent(intent: Intent?) {
        intent?.data?.let {
            MaterialAlertDialogBuilder(this)
                .setTitle(R.string.restore)
                .setMessage(R.string.restore_confirm)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.restore) { _, _ ->
                    val bytes = it.readBytes(this)
                    fragment.restore(bytes)
                    intent.data = null
                }
                .show()
        }
    }


    inner class MyReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == ACTION_RESTORE) {

            }
        }
    }
}