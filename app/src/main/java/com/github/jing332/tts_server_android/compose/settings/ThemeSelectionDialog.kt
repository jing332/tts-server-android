package com.github.jing332.tts_server_android.compose.settings

import android.os.Build
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.github.jing332.tts_server_android.R
import com.github.jing332.tts_server_android.compose.theme.AppTheme
import kotlinx.coroutines.delay


@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ThemeSelectionDialog(
    onDismissRequest: () -> Unit,
    currentTheme: AppTheme,
    onChangeTheme: (AppTheme) -> Unit
) {
    AlertDialog(onDismissRequest = onDismissRequest,
        title = {
            Text(text = stringResource(id = R.string.theme))
        },
        confirmButton = {
            TextButton(onClick = onDismissRequest) {
                Text(stringResource(id = R.string.close))
            }
        }, text = {
            val view = LocalView.current
            val context = LocalContext.current
            Column {
                var showWarn by remember { androidx.compose.runtime.mutableStateOf(false) }
                AnimatedVisibility(
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    visible = showWarn
                ) {
                    LaunchedEffect(showWarn) {
                        view.announceForAccessibility(context.getString(R.string.dynamic_color_not_support))
                        delay(2000)
                        showWarn = false
                    }

                    Text(
                        text = stringResource(id = R.string.dynamic_color_not_support),
                        modifier = Modifier.padding(bottom = 8.dp),
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Bold
                    )
                }


                FlowRow {
                    AppTheme.values().forEach {
                        val leadingIcon: @Composable () -> Unit =
                            { Icon(Icons.Default.Check, null) }
//                var selected by remember { mutableStateOf(it == AppTheme.DEFAULT) }
                        val selected = currentTheme.id == it.id
                        FilterChip(
                            selected,
                            modifier = Modifier.padding(horizontal = 2.dp),
                            leadingIcon = if (selected) leadingIcon else null,
                            onClick = {
                                if (it == AppTheme.DYNAMIC_COLOR && Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
                                    showWarn = true
                                    return@FilterChip
                                }

                                onChangeTheme(it)
                            },
                            label = { Text(stringResource(id = it.stringResId), color = it.color) }
                        )
                    }
                }
            }
        })
}
