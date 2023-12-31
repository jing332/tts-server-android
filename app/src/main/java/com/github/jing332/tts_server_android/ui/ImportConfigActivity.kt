package com.github.jing332.tts_server_android.ui

import android.content.ContentResolver
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material3.Text
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import com.drake.net.utils.fileName
import com.github.jing332.tts_server_android.R
import com.github.jing332.tts_server_android.compose.systts.LocalImportFilePath
import com.github.jing332.tts_server_android.compose.systts.LocalImportRemoteUrl
import com.github.jing332.tts_server_android.compose.theme.AppTheme
import com.github.jing332.tts_server_android.compose.widgets.AppSelectionDialog
import com.github.jing332.tts_server_android.ui.systts.ImportConfigFactory
import com.github.jing332.tts_server_android.ui.systts.ImportConfigFactory.gotoEditorFromJS
import com.github.jing332.tts_server_android.ui.systts.ImportType
import com.github.jing332.tts_server_android.utils.FileUtils.readAllText
import com.github.jing332.tts_server_android.utils.longToast


class ImportConfigActivity : AppCompatActivity() {
    companion object {
        const val TAG = "ImportConfigActivity"
    }

    private var showFileTypeSelectDialog = mutableStateOf("")

    private var type = mutableStateOf("")
    private var url = mutableStateOf("")
    private var path = mutableStateOf("")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            AppTheme {
                if (showFileTypeSelectDialog.value.isNotEmpty()) {
                    AppSelectionDialog(
                        onDismissRequest = { showFileTypeSelectDialog.value = "" },
                        title = {
                            Text(stringResource(id = R.string.import_file_as))
                        },
                        value = Any(),
                        values = ImportType.values().toList(),
                        entries = ImportType.values().map { stringResource(id = it.strResId) },
                        onClick = { v, _ ->
                            type.value = (v as ImportType).id
                            path.value = showFileTypeSelectDialog.value

                            showFileTypeSelectDialog.value = ""
                        }
                    )
                }

                val sheet = remember(type.value) {
                    ImportConfigFactory.getBottomSheet(
                        type = type.value,
                        onBadFormat = {
                            if (type.value.isNotEmpty()) {
                                longToast(R.string.import_config_type_unknown_msg)
                                finish()
                            }
                        }
                    )
                }

                CompositionLocalProvider(
                    LocalImportRemoteUrl provides url,
                    LocalImportFilePath provides path
                ) {
                    sheet { finish() }
                }


            }
        }

        importConfigFromIntent(intent)
    }


    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        importConfigFromIntent(intent)
    }

    private fun importConfigFromIntent(intent: Intent?) {
        intent?.data?.let {
            when (it.scheme) {
                ContentResolver.SCHEME_CONTENT -> importFileFromIntent(intent)
                ContentResolver.SCHEME_FILE -> importFileFromIntent(intent)
                "ttsrv" -> importUrlFromIntent(intent)
                else -> longToast(getString(R.string.invalid_scheme_msg))
            }
        }
    }

    private fun importUrlFromIntent(intent: Intent?) {
        intent?.data?.let { uri ->
            if (uri.scheme == "ttsrv") {
                val path = uri.host ?: ""
                val url = uri.path?.removePrefix("/") ?: ""
                if (url.isBlank()) {
                    longToast(getString(R.string.invalid_url_msg, url))
                    intent.data = null
                    return
                }

                type.value = path
                this.url.value = url

                intent.data = null
            }
        }
    }

    private fun importFileFromIntent(intent: Intent?) {
        if (intent?.data != null) {
            if (intent.data?.fileName()?.endsWith("js", true) == true) {
                val txt = intent.data?.readAllText(this)
                if (txt.isNullOrBlank()) {
                    longToast(R.string.js_file_type_not_recognized)
                } else
                    if (!gotoEditorFromJS(txt)) longToast(R.string.js_file_type_not_recognized)

            } else
                showFileTypeSelectDialog.value = intent.data.toString()

            intent.data = null
        }
    }


}