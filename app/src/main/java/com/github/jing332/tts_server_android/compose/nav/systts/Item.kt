package com.github.jing332.tts_server_android.compose.nav.systts

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CopyAll
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import com.github.jing332.tts_server_android.R
import com.ireward.htmlcompose.HtmlText
import org.burnoutcrew.reorderable.ReorderableLazyListState
import org.burnoutcrew.reorderable.detectReorder


@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun Item(
    modifier: Modifier,
    name: String,
    desc: String,
    params: String,
    reorderState: ReorderableLazyListState,

    enabled: Boolean,
    onEnabledChange: (Boolean) -> Unit,
    onClick: () -> Unit,

    onCopy: () -> Unit,
    onDelete: () -> Unit,
    onEdit: () -> Unit,
    onAudition: () -> Unit,
) {
    ElevatedCard(
        modifier = modifier,
        onClick = onClick,
    ) {
        ConstraintLayout(
            Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
        ) {
            val (checkRef,
                nameRef,
                contentRef,
                buttonsRef) = createRefs()
            Row(
                Modifier
                    .constrainAs(checkRef) {
                        start.linkTo(parent.start)
                        top.linkTo(parent.top)
                        bottom.linkTo(parent.bottom)
                    }
                    .detectReorder(reorderState)) {
                Checkbox(
                    checked = enabled,
                    onCheckedChange = onEnabledChange,
                )
            }
            Text(
                name,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier
                    .constrainAs(nameRef) {
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                        top.linkTo(parent.top)
                    }
                    .padding(bottom = 4.dp),
                maxLines = 1,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold
            )

            Column(
                Modifier
                    .constrainAs(contentRef) {
                        start.linkTo(checkRef.end)
                        top.linkTo(nameRef.bottom)
                        bottom.linkTo(parent.bottom)
//                        end.linkTo(buttonsRef.start)
                    }
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.Start
            ) {
                HtmlText(
                    text = desc,
                    style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onBackground),
                )

                HtmlText(
                    text = params,
                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onBackground),
                )
            }

            Row(modifier = Modifier.constrainAs(buttonsRef) {
                top.linkTo(parent.top)
                bottom.linkTo(parent.bottom)
                end.linkTo(parent.end)
            }) {
                val swapButton =
                    com.github.jing332.tts_server_android.conf.AppConfig.isSwapListenAndEditButton.value
                IconButton(
                    modifier = Modifier,
                    onClick = {
                        if (swapButton)
                            onAudition()
                        else
                            onEdit()
                    }) {
                    if (swapButton)
                        Icon(Icons.Default.Headphones, stringResource(id = R.string.audition))
                    else
                        Icon(Icons.Default.Edit, stringResource(id = R.string.edit))
                }

                var showOptions by remember { mutableStateOf(false) }
                IconButton(
                    modifier = Modifier,
                    onClick = { showOptions = true }) {
                    Icon(Icons.Default.MoreVert, stringResource(id = R.string.more_options))

                    DropdownMenu(
                        expanded = showOptions,
                        onDismissRequest = { showOptions = false }) {

                        DropdownMenuItem(
                            text = { Text(stringResource(id = if (swapButton) R.string.edit else R.string.audition)) },
                            onClick = {
                               if (swapButton)
                                   onEdit()
                                else
                                   onAudition()
                            },
                            leadingIcon = {
                                Icon(
                                    if (swapButton) Icons.Default.Edit else Icons.Default.Headphones,
                                    null
                                )
                            }
                        )

                        DropdownMenuItem(
                            text = { Text(stringResource(id = R.string.copy)) },
                            onClick = onCopy,
                            leadingIcon = {
                                Icon(Icons.Default.CopyAll, null)
                            }
                        )
                        Divider()
                        DropdownMenuItem(
                            text = { Text(stringResource(id = R.string.delete)) },
                            onClick = onDelete,
                            leadingIcon = {
                                Icon(
                                    Icons.Default.DeleteForever,
                                    null,
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        )
                    }
                }
            }
        }

    }
}