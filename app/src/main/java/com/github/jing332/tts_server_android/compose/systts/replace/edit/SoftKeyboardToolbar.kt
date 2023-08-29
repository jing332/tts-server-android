@file:OptIn(ExperimentalMaterial3Api::class)

package com.github.jing332.tts_server_android.compose.systts.replace.edit

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.github.jing332.tts_server_android.R

@Preview
@Composable
fun PreviewEditToolbarDialog() {
    // @formatter:off
    val keys = listOf("(", ")", "[", "]","{", "}", "<", ">", "!", "@","#", "$", "%", "^","&", "*", "-", "+", "=", "|","\\", "/", "?", ",", ":", ";", "\"", "'", " ", "\n"
    ).map { it to it }
    // @formatter:on

    var isVisible by remember { mutableStateOf(true) }
    if (isVisible)
        EditToolbarKeyDialog(keys = keys) {
            isVisible = false
        }
}

@Composable
fun EditToolbarKeyDialog(
    keys: List<Pair<String, String>>,
    onResult: (List<Pair<String, String>>?) -> Unit,
) {

    AlertDialog(onDismissRequest = { onResult.invoke(null) }) {
        Surface(
            color = AlertDialogDefaults.containerColor,
            shape = AlertDialogDefaults.shape,
            modifier = Modifier.padding(8.dp)
        ) {
            Column {
                Text(
                    text = "stringResource(R.string.soft_keyboard_toolbar_settings)",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
                var value by remember {
                    mutableStateOf(keys.joinToString("\n") { "${it.first} = ${it.second}" })
                }
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Text(
                        text = "stringResource(R.string.soft_keyboard_toolbar_msg_tip)",
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )

                    OutlinedTextField(
                        modifier = Modifier.padding(8.dp),
                        value = value, onValueChange = { value = it },
                        maxLines = 15
                    )
                }

                Box(
                    modifier = Modifier
                        .padding(8.dp)
                        .padding(end = 8.dp)
                        .align(Alignment.End)
                ) {
                    TextButton(
                        onClick = {
                            onResult.invoke(value.split("\n").map {
                                val split = it.split(" = ")

                                if (split.size < 2)
                                    it to it
                                else split[0] to split[1]
                            }.filter { it.first.isNotBlank() && it.second.isNotBlank() })
                        }) {
                        Text(text = stringResource(id = R.string.save))
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun PreviewSoftKeyboardToolbar() {
    // @formatter:off
    SoftKeyboardInputToolbar(items =listOf("(", ")", "[", "]", "{", "}", "<", ">", "!", "@", "#", "$", "%", "^", "&", "*", "-", "+", "=", "|", "\\", "/", "?", ",", ":", ";", "\"", "'", " ", "\n").map { it to it }, onClick = {  } )
    // @formatter:on
}

/**
 * @param items: Pair<displayName, value>
 */
@Composable
fun SoftKeyboardInputToolbar(
    items: List<Pair<String, String>>,
    onClick: (key: String) -> Unit,

    onSettings: () -> Unit = {},
) {
    Column {
        Box( // 顶部阴影
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .shadow(1.dp, RoundedCornerShape(topStart = 1.dp, topEnd = 1.dp))
        )
        LazyRow(modifier = Modifier.fillMaxWidth()) {
            itemsIndexed(items.map { it.second }) { index, key ->
                Text(
                    text = items[index].first,
                    modifier = Modifier
                        .clip(MaterialTheme.shapes.medium)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = rememberRipple(bounded = true),
                            onClick = {
                                onClick.invoke(key)
                            }
                        )
                        .size(width = Dp.Unspecified, height = 48.dp)
                        .widthIn(48.dp)
                        .wrapContentHeight(Alignment.CenterVertically),
                    textAlign = TextAlign.Center,
                )
            }
            item {
                IconButton(onClick = onSettings, modifier = Modifier.size(48.dp)) {
                    Icon(Icons.Filled.Settings, stringResource(id = R.string.settings))
                }
            }
        }
    }
}