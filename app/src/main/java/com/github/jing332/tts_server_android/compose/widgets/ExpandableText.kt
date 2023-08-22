/*
package com.github.jing332.text_searcher.ui.widgets

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.TextUnit
import com.github.jing332.text_searcher.R


// from https://stackoverflow.com/a/72982110/13197001
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ExpandableText(
    modifier: Modifier = Modifier,
    textModifier: Modifier = Modifier,
    style: TextStyle = LocalTextStyle.current,
    fontFamily: FontFamily = FontFamily.Default,
    fontStyle: FontStyle? = null,
    fontSize: TextUnit = LocalTextStyle.current.fontSize,
    fontWeight: FontWeight = LocalTextStyle.current.fontWeight ?: FontWeight.Normal,
    lineHeight: TextUnit = LocalTextStyle.current.lineHeight,
    text: String,
    collapsedMaxLine: Int = 2,
    showMoreText: String = stringResource(R.string.expandable_text_more),
    showMoreStyle: SpanStyle = SpanStyle(
        fontWeight = FontWeight.ExtraBold,
        color = MaterialTheme.colorScheme.primary
    ),
    showLessText: String = stringResource(R.string.expandable_text_less),
    showLessStyle: SpanStyle = showMoreStyle,
    textAlign: TextAlign? = null,

    onLongClick: (() -> Unit)? = null,
    onLongClickLabel: String? = null
) {
    var isExpanded by remember { mutableStateOf(false) }
    var clickable by remember { mutableStateOf(false) }
    var lastCharIndex by remember { mutableIntStateOf(0) }

    Box(
        modifier = Modifier
            .combinedClickable(
                onClick = {
                    if (clickable)
                        isExpanded = !isExpanded
                },
                onLongClick = onLongClick,
                onLongClickLabel = onLongClickLabel,
                onClickLabel = if (isExpanded) showLessText else showMoreText
            )
            .then(modifier)
    ) {
        Text(
            modifier = textModifier
                .fillMaxWidth()
                .animateContentSize(),
            text = buildAnnotatedString {
                if (clickable) {
                    if (isExpanded) {
                        append(text)
                        withStyle(style = showLessStyle) { append(showLessText) }
                    } else {
                        val adjustText = text.substring(startIndex = 0, endIndex = lastCharIndex)
                            .dropLast(showMoreText.length)
                            .dropLastWhile { Character.isWhitespace(it) || it == '.' }
                        append(adjustText)
                        withStyle(style = showMoreStyle) { append(showMoreText) }
                    }
                } else {
                    append(text)
                }
            },
            maxLines = if (isExpanded) Int.MAX_VALUE else collapsedMaxLine,
            fontStyle = fontStyle,
            onTextLayout = { textLayoutResult ->
                if (!isExpanded && textLayoutResult.hasVisualOverflow) {
                    clickable = true
                    lastCharIndex = textLayoutResult.getLineEnd(collapsedMaxLine - 1)
                }
            },
            style = style,
            textAlign = textAlign,
            fontSize = fontSize,
            fontWeight = fontWeight,
            lineHeight = lineHeight,
            fontFamily = fontFamily
        )
    }

}*/
