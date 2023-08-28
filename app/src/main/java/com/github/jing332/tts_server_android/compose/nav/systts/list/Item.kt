package com.github.jing332.tts_server_android.compose.nav.systts.list

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CopyAll
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import com.github.jing332.tts_server_android.R
import com.github.jing332.tts_server_android.compose.widgets.HtmlText
import com.github.jing332.tts_server_android.conf.AppConfig
import com.github.jing332.tts_server_android.utils.performLongPress
import org.burnoutcrew.reorderable.ReorderableLazyListState
import org.burnoutcrew.reorderable.detectReorder


@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun Item(
    modifier: Modifier,
    name: String,
    speechTarget: String,
    type: String,
    desc: String,
    params: String,
    reorderState: ReorderableLazyListState,

    enabled: Boolean,
    onEnabledChange: (Boolean) -> Unit,
    onClick: () -> Unit,
    onLongClick: () -> Unit,

    onCopy: () -> Unit,
    onDelete: () -> Unit,
    onEdit: () -> Unit,
    onAudition: () -> Unit,
) {
    val targetLen = 6

    @Composable
    fun TargetScreen(modifier: Modifier = Modifier) {
        OutlinedCard(shape = MaterialTheme.shapes.extraSmall, modifier = modifier) {
            Text(
                text = speechTarget,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                textAlign = TextAlign.Center,
                maxLines = 1,
            )
        }
    }

    val view = LocalView.current
    ElevatedCard(modifier) {
        ConstraintLayout(
            Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .combinedClickable(
                    onClick = onClick,
                    onLongClick = {
                        view.performLongPress()
                        onLongClick()
                    }
                )
                .padding(vertical = 4.dp)
        ) {
            val (checkRef,
                nameRef,
                contentRef,
                targetRef,
                typeRef,
                buttonsRef) = createRefs()
            Row(
                Modifier
                    .constrainAs(checkRef) {
                        start.linkTo(parent.start)
                        top.linkTo(nameRef.top)
                        bottom.linkTo(contentRef.bottom)
                    }
                    .detectReorder(reorderState)) {
                Checkbox(
                    checked = enabled,
                    onCheckedChange = onEnabledChange,
                )
            }

            Column(
                Modifier
                    .constrainAs(nameRef) {
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                        top.linkTo(parent.top)
                    }
                    .padding(bottom = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    name,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold,
                    overflow = TextOverflow.Clip,
                )

                if (speechTarget.isNotEmpty() && speechTarget.length > targetLen)
                    TargetScreen()
            }

            Column(
                Modifier
                    .constrainAs(contentRef) {
                        start.linkTo(checkRef.end)
                        top.linkTo(nameRef.bottom)
                        bottom.linkTo(parent.bottom)
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

            if (speechTarget.isNotEmpty() && speechTarget.length <= targetLen)
                TargetScreen(Modifier
                    .constrainAs(targetRef) {
                        top.linkTo(nameRef.top)
                        end.linkTo(parent.end)
                    }
                    .padding(end = 4.dp)
                )

            Row(modifier = Modifier.constrainAs(buttonsRef) {
                top.linkTo(parent.top)
                bottom.linkTo(parent.bottom)
                end.linkTo(parent.end)
            }) {
                val swapButton = AppConfig.isSwapListenAndEditButton.value
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
                        HorizontalDivider()
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

            Text(
                text = type,
                modifier = Modifier
                    .constrainAs(typeRef) {
                        end.linkTo(parent.end)
//                        top.linkTo(buttonsRef.bottom)
                        bottom.linkTo(parent.bottom)
                    }
                    .padding(end = 4.dp),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.tertiary,
            )

        }

    }
}