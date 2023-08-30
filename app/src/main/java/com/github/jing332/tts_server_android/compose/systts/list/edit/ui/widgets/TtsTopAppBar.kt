package com.github.jing332.tts_server_android.compose.systts.list.edit.ui.widgets

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import com.github.jing332.tts_server_android.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TtsTopAppBar(
    title: @Composable () -> Unit,
    onBackAction: () -> Unit,
    onSaveAction: () -> Unit,

    moreOptions: (@Composable (dismiss: () -> Unit) -> Unit)? = null
) {
    TopAppBar(
        title = title,
        navigationIcon = {
            IconButton(onClick = onBackAction) {
                Icon(Icons.Default.ArrowBack, stringResource(id = R.string.nav_back))
            }
        },
        actions = {
            IconButton(onClick = onSaveAction) {
                Icon(Icons.Default.Save, stringResource(id = R.string.save))
            }

            var showOptions by remember { mutableStateOf(false) }
            if (moreOptions != null)
                IconButton(onClick = {

                }) {
                    Icon(Icons.Default.MoreVert, stringResource(id = R.string.more_options))
                    moreOptions { showOptions = false }
                }
        }
    )
}