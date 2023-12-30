package com.github.jing332.tts_server_android.compose.widgets

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonElevation
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.focused
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun ColumnToggleButtonGroup(
    modifier: Modifier = Modifier,
    buttonCount: Int,
    primarySelection: Int = -1,
    selectedColor: Color = Color.White,
    unselectedColor: Color = Color.Unspecified,
    selectedContentColor: Color = Color.Black,
    unselectedContentColor: Color = Color.Gray,
    buttonIconTint: Color = selectedContentColor,
    unselectedButtonIconTint: Color = unselectedContentColor,
    borderColor: Color = selectedColor,
    buttonTexts: Array<String> = Array(buttonCount) { "" },
    buttonIcons: Array<Painter> = Array(buttonCount) { emptyPainter },
    shape: CornerBasedShape = MaterialTheme.shapes.small,
    borderSize: Dp = 1.dp,
    border: BorderStroke = BorderStroke(borderSize, borderColor),
    elevation: ButtonElevation = ButtonDefaults.buttonElevation(),
    enabled: Boolean = true,
    buttonHeight: Dp = 60.dp,
    iconPosition: IconPosition = IconPosition.Start,
    onButtonClick: (index: Int) -> Unit,
) {
    Column(modifier = modifier) {
        val squareCorner = CornerSize(0.dp)
        var selectionIndex by rememberSaveable { mutableStateOf(primarySelection) }

        repeat(buttonCount) { index ->
            val buttonShape = when (index) {
                0 -> shape.copy(bottomStart = squareCorner, bottomEnd = squareCorner)
                buttonCount - 1 -> shape.copy(topStart = squareCorner, topEnd = squareCorner)
                else -> shape.copy(all = squareCorner)
            }
            val isButtonSelected = selectionIndex == index
            val backgroundColor = if (isButtonSelected) selectedColor else unselectedColor
            val contentColor =
                if (isButtonSelected) selectedContentColor else unselectedContentColor
            val iconTintColor = if (isButtonSelected) buttonIconTint else unselectedButtonIconTint
            val offset = borderSize * -index

            ToggleButton(
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = buttonHeight)
                    .offset(y = offset),
                buttonShape = buttonShape,
                border = border,
                backgroundColor = backgroundColor,
                elevation = elevation,
                enabled = enabled,
                buttonTexts = buttonTexts,
                buttonIcons = buttonIcons,
                index = index,
                contentColor = contentColor,
                iconTintColor = iconTintColor,
                iconPosition = iconPosition,
                onClick = {
                    selectionIndex = index
                    onButtonClick.invoke(index)
                },
            )
        }
    }
}

@Composable
fun RowToggleButtonGroup(
    modifier: Modifier = Modifier,
    buttonCount: Int,
    selectionIndex: Int,
    selectedColor: Color = MaterialTheme.colorScheme.secondaryContainer,
    unselectedColor: Color = Color.Unspecified,
    selectedContentColor: Color = MaterialTheme.colorScheme.onBackground,
    unselectedContentColor: Color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
    buttonIconTint: Color = selectedContentColor,
    unselectedButtonIconTint: Color = unselectedContentColor,
    borderColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    buttonTexts: Array<String> = Array(buttonCount) { "" },
    buttonIcons: Array<Painter> = Array(buttonCount) { ColorPainter(Color.Transparent) },
    shape: CornerBasedShape = MaterialTheme.shapes.extraLarge,
    borderSize: Dp = 1.dp,
    border: BorderStroke = BorderStroke(borderSize, borderColor),
    elevation: ButtonElevation = ButtonDefaults.buttonElevation(),
    enabled: Boolean = true,
    buttonHeight: Dp = 40.dp,
    iconPosition: IconPosition = IconPosition.Start,
    onButtonClick: (index: Int) -> Unit,
) {
    Row(modifier = modifier.focusGroup()) {
        val squareCorner = CornerSize(0.dp)

        repeat(buttonCount) { index ->
            val buttonShape = when (index) {
                0 -> shape.copy(bottomEnd = squareCorner, topEnd = squareCorner)
                buttonCount - 1 -> shape.copy(topStart = squareCorner, bottomStart = squareCorner)
                else -> shape.copy(all = squareCorner)
            }
            val isButtonSelected = selectionIndex == index
            val backgroundColor = if (isButtonSelected) selectedColor else unselectedColor
            val contentColor =
                if (isButtonSelected) selectedContentColor else unselectedContentColor
            val iconTintColor = if (isButtonSelected) buttonIconTint else unselectedButtonIconTint
            val offset = borderSize * -index

            ToggleButton(
                modifier = Modifier
                    .semantics {
//                        role = Role.Button
                        focused = isButtonSelected
                        selected = isButtonSelected
                        contentDescription = buttonTexts[index]
                        stateDescription = buttonTexts[index]
                    }
//                    .weight(weight = 1f)
                    .defaultMinSize(minHeight = buttonHeight)
                    .offset(x = offset),
                buttonShape = buttonShape,
                border = border,
                backgroundColor = backgroundColor,
                elevation = elevation,
                enabled = enabled,
                buttonTexts = buttonTexts,
                buttonIcons = buttonIcons,
                index = index,
                contentColor = contentColor,
                iconTintColor = iconTintColor,
                iconPosition = iconPosition,
                onClick = {
                    onButtonClick.invoke(index)
                },
            )
        }
    }
}

@Composable
private fun ToggleButton(
    modifier: Modifier,
    buttonShape: CornerBasedShape,
    border: BorderStroke,
    backgroundColor: Color,
    elevation: ButtonElevation,
    enabled: Boolean,
    buttonTexts: Array<String>,
    buttonIcons: Array<Painter>,
    index: Int,
    contentColor: Color,
    iconTintColor: Color,
    iconPosition: IconPosition,
    onClick: () -> Unit,
) {
    OutlinedButton(
        modifier = modifier,
        contentPadding = PaddingValues(),
        shape = buttonShape,
        border = border,
        onClick = onClick,
        colors = ButtonDefaults.outlinedButtonColors(containerColor = backgroundColor),
        elevation = elevation,
        enabled = enabled,
    ) {
        ButtonContent(
            buttonTexts = buttonTexts,
            buttonIcons = buttonIcons,
            index = index,
            contentColor = contentColor,
            iconTintColor = iconTintColor,
            iconPosition = iconPosition,
        )
    }
}

@Composable
private fun RowScope.ButtonContent(
    buttonTexts: Array<String>,
    buttonIcons: Array<Painter>,
    index: Int,
    contentColor: Color,
    iconTintColor: Color,
    iconPosition: IconPosition = IconPosition.Start,
) {
    when {
        buttonTexts.all { it != "" } && buttonIcons.all { it != emptyPainter } -> ButtonWithIconAndText(
            iconTintColor = iconTintColor,
            buttonIcons = buttonIcons,
            buttonTexts = buttonTexts,
            index = index,
            contentColor = contentColor,
            iconPosition = iconPosition,
        )

        buttonTexts.all { it != "" } && buttonIcons.all { it == emptyPainter } -> TextContent(
            modifier = Modifier.align(Alignment.CenterVertically),
            buttonTexts = buttonTexts,
            index = index,
            contentColor = contentColor,
        )

        buttonTexts.all { it == "" } && buttonIcons.all { it != emptyPainter } -> IconContent(
            modifier = Modifier
                .align(Alignment.CenterVertically)
                .padding(start = 4.dp),
            iconTintColor = iconTintColor,
            buttonIcons = buttonIcons,
            index = index,
        )
    }
}

@Composable
private fun RowScope.ButtonWithIconAndText(
    iconTintColor: Color,
    buttonIcons: Array<Painter>,
    buttonTexts: Array<String>,
    index: Int,
    contentColor: Color,
    iconPosition: IconPosition
) {
    when (iconPosition) {
        IconPosition.Start -> {
            IconContent(
                Modifier.align(Alignment.CenterVertically),
                iconTintColor,
                buttonIcons,
                index
            )
            TextContent(
                Modifier.align(Alignment.CenterVertically),
                buttonTexts,
                index,
                contentColor
            )
        }

        IconPosition.Top -> Column {
            IconContent(
                Modifier.align(Alignment.CenterHorizontally),
                iconTintColor,
                buttonIcons,
                index
            )
            TextContent(
                Modifier.align(Alignment.CenterHorizontally),
                buttonTexts,
                index,
                contentColor
            )
        }

        IconPosition.End -> {
            TextContent(
                Modifier.align(Alignment.CenterVertically),
                buttonTexts,
                index,
                contentColor
            )
            IconContent(
                Modifier.align(Alignment.CenterVertically),
                iconTintColor,
                buttonIcons,
                index
            )
        }

        IconPosition.Bottom -> Column {
            TextContent(
                Modifier.align(Alignment.CenterHorizontally),
                buttonTexts,
                index,
                contentColor
            )
            IconContent(
                Modifier.align(Alignment.CenterHorizontally),
                iconTintColor,
                buttonIcons,
                index
            )
        }
    }
}

@Composable
private fun IconContent(
    modifier: Modifier,
    iconTintColor: Color,
    buttonIcons: Array<Painter>,
    index: Int
) {
    if (iconTintColor == Color.Transparent || iconTintColor == Color.Unspecified) {
        Image(
            modifier = modifier
                .size(24.dp)
                .padding(start = 4.dp),
            painter = buttonIcons[index],
            contentDescription = null,
        )
    } else {
        Image(
            modifier = modifier
                .size(24.dp)
                .padding(start = 4.dp),
            painter = buttonIcons[index],
            contentDescription = null,
            colorFilter = ColorFilter.tint(iconTintColor),
        )
    }
}

@Composable
private fun TextContent(
    modifier: Modifier,
    buttonTexts: Array<String>,
    index: Int,
    contentColor: Color
) {
    Text(
        modifier = modifier.padding(horizontal = 8.dp),
        text = buttonTexts[index],
        color = contentColor,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )
}

private val emptyPainter = ColorPainter(Color.Transparent)

enum class IconPosition {
    Start, Top, End, Bottom
}
