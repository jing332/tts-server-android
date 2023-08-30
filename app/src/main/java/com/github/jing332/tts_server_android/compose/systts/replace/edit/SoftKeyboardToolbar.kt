@file:OptIn(ExperimentalMaterial3Api::class)

package com.github.jing332.tts_server_android.compose.systts.replace.edit

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.github.jing332.tts_server_android.R

@Composable
fun SoftKeyboardInputToolbar(
    symbols: LinkedHashMap<String,String>,
    onClick: (chars: String) -> Unit,

    onSettings: () -> Unit = {},
) {
    Column {
        HorizontalDivider(Modifier.fillMaxWidth())
        LazyRow(modifier = Modifier.fillMaxWidth()) {
            itemsIndexed(symbols.toList()) { index, entry ->
                Text(
                    text = entry.second,
                    modifier = Modifier
                        .clip(MaterialTheme.shapes.medium)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = rememberRipple(bounded = true),
                            onClick = {
                                onClick.invoke(entry.first)
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