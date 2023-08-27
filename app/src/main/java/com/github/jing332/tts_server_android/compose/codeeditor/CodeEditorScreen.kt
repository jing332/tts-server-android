package com.github.jing332.tts_server_android.compose.codeeditor

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.SettingsRemote
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.github.jing332.tts_server_android.R
import com.github.jing332.tts_server_android.compose.widgets.CheckedMenuItem
import com.github.jing332.tts_server_android.conf.CodeEditorConfig
import io.github.rosemoe.sora.widget.CodeEditor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CodeEditorScreen(
    title: @Composable () -> Unit,
    onBack: () -> Unit,
    onDebug: () -> Unit,
    onSave: () -> Unit,
    onUpdate: (CodeEditor) -> Unit,

    actions: @Composable ColumnScope.(dismiss: () -> Unit) -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(title = title, navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(
                        Icons.Filled.ArrowBack,
                        contentDescription = stringResource(id = R.string.nav_back)
                    )
                }
            },
                actions = {
                    IconButton(onClick = onDebug) {
                        Icon(
                            Icons.Filled.BugReport,
                            contentDescription = stringResource(id = R.string.nav_back)
                        )
                    }
                    IconButton(onClick = onSave) {
                        Icon(
                            Icons.Filled.Save,
                            contentDescription = stringResource(id = R.string.nav_back)
                        )
                    }

                    var showOptions by remember { mutableStateOf(false) }

                    IconButton(onClick = { showOptions = true }) {
                        Icon(Icons.Default.MoreVert, stringResource(id = R.string.more_options))

                        DropdownMenu(
                            expanded = showOptions,
                            onDismissRequest = { showOptions = false }) {
                            DropdownMenuItem(
                                text = { Text(stringResource(id = R.string.save_as_file)) },
                                onClick = { /*TODO*/ },
                                leadingIcon = { Icon(Icons.Default.InsertDriveFile, null) }
                            )

                            var syncEnabled by remember { CodeEditorConfig.isRemoteSyncEnabled }
                            CheckedMenuItem(
                                text = { /*TODO*/ },
                                checked = syncEnabled,
                                onClick = {},
                                onClickCheckBox = { syncEnabled = it },
                                leadingIcon = {
                                    Icon(Icons.Default.SettingsRemote, null)
                                }
                            )

                            HorizontalDivider()

                            DropdownMenuItem(
                                text = { Text(stringResource(id = R.string.theme)) },
                                onClick = {

                                },
                                leadingIcon = { Icon(Icons.Default.ColorLens, null) }
                            )

                            var wordWrap by remember { CodeEditorConfig.isWordWrapEnabled }
                            CheckedMenuItem(
                                text = { Text(stringResource(id = R.string.word_wrap)) },
                                checked = wordWrap,
                                onClick = { wordWrap = it },
                                leadingIcon = {
                                    Icon(Icons.Default.ColorLens, null)
                                }
                            )

                            actions { showOptions = false }
                        }

                    }
                }
            )
        }
    ) { paddingValues ->
        CodeEditor(
            modifier = Modifier
                .fillMaxWidth()
                .padding(paddingValues), onUpdate = onUpdate
        )
    }
}