package com.github.jing332.tts_server_android.compose.backup

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Input
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Input
import androidx.compose.material.icons.filled.Output
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.github.jing332.tts_server_android.R
import com.github.jing332.tts_server_android.compose.settings.BasePreferenceWidget
import com.github.jing332.tts_server_android.compose.theme.AppTheme
import com.github.jing332.tts_server_android.utils.FileUtils.readBytes

class BackupRestoreActivity : AppCompatActivity() {
    companion object {
        const val TAG = "BackupRestoreActivity"
    }

    private var showFromFileRestoreDialog = mutableStateOf<ByteArray?>(null)

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppTheme {
                var showBackupDialog by remember { mutableStateOf(false) }
                if (showBackupDialog) {
                    BackupDialog(onDismissRequest = { showBackupDialog = false })
                }

                var showRestoreDialog by remember { mutableStateOf(false) }
                if (showRestoreDialog) {
                    RestoreDialog(onDismissRequest = { showRestoreDialog = false })
                }

                Scaffold(topBar = {
                    TopAppBar(
                        title = { Text(stringResource(id = R.string.backup_restore)) },
                        navigationIcon = {
                            IconButton(onClick = { finish() }) {
                                Icon(
                                    Icons.AutoMirrored.Filled.ArrowBack,
                                    stringResource(id = R.string.nav_back)
                                )
                            }
                        })
                }) {
                    Column(Modifier.padding(it)) {
                        BasePreferenceWidget(
                            onClick = { showBackupDialog = true },
                            title = { Text(stringResource(id = R.string.backup)) },
                            icon = { Icon(Icons.Default.Output, null) }
                        )

                        BasePreferenceWidget(
                            onClick = { showRestoreDialog = true },
                            title = { Text(stringResource(id = R.string.restore)) },
                            icon = { Icon(Icons.AutoMirrored.Filled.Input, null) }
                        )
                    }
                }
            }
        }
        restoreFromIntent(intent)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        restoreFromIntent(intent)
    }

    private fun restoreFromIntent(intent: Intent?) {
        intent?.data?.let {
            showFromFileRestoreDialog.value = it.readBytes(this)
            intent.data = null
//            MaterialAlertDialogBuilder(this)
//                .setTitle(R.string.restore)
//                .setMessage(R.string.restore_confirm)
//                .setNegativeButton(R.string.cancel, null)
//                .setPositiveButton(R.string.restore) { _, _ ->
//                    val bytes = it.readBytes(this)
//                    fragment.restore(bytes)
//                }.setOnDismissListener { intent.data = null }
//                .show()
        }
    }
}