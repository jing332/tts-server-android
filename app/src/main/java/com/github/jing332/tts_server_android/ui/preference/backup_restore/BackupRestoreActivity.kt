package com.github.jing332.tts_server_android.ui.preference.backup_restore

import android.os.Bundle
import com.github.jing332.tts_server_android.R
import com.github.jing332.tts_server_android.databinding.BackupRestoreActivityBinding
import com.github.jing332.tts_server_android.ui.base.AppBackActivity

class BackupRestoreActivity : AppBackActivity<BackupRestoreActivityBinding>() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val fragment = BackupRestoreFragment()

        supportFragmentManager.beginTransaction()
            .add(R.id.container, fragment, "BackupRestoreFragment")
            .commit()
    }
}