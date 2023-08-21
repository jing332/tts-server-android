package com.github.jing332.tts_server_android.compose.theme

import android.content.Context
import android.os.Build
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.TweenSpec
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

/**
 * 默认主题
 */
@Composable
fun defaultTheme(
    darkTheme: Boolean
): ColorScheme =
    if (!darkTheme)
        lightColorScheme(
            primary = bean_green_seed,
            onPrimary = bean_green_md_theme_light_onPrimary,
            primaryContainer = bean_green_md_theme_light_primaryContainer,
            onPrimaryContainer = bean_green_md_theme_light_onPrimaryContainer,
            secondary = bean_green_md_theme_light_secondary,
            onSecondary = bean_green_md_theme_light_onSecondary,
            secondaryContainer = bean_green_md_theme_light_secondaryContainer,
            onSecondaryContainer = bean_green_md_theme_light_onSecondaryContainer,
            tertiary = bean_green_md_theme_light_tertiary,
            onTertiary = bean_green_md_theme_light_onTertiary,
            tertiaryContainer = bean_green_md_theme_light_tertiaryContainer,
            onTertiaryContainer = bean_green_md_theme_light_onTertiaryContainer,
            error = bean_green_md_theme_light_error,
            errorContainer = bean_green_md_theme_light_errorContainer,
            onError = bean_green_md_theme_light_onError,
            onErrorContainer = bean_green_md_theme_light_onErrorContainer,
            background = bean_green_md_theme_light_background,
            onBackground = bean_green_md_theme_light_onBackground,
            outline = bean_green_md_theme_light_outline,
            inverseOnSurface = bean_green_md_theme_light_inverseOnSurface,
            inverseSurface = bean_green_md_theme_light_inverseSurface,
            inversePrimary = bean_green_md_theme_light_inversePrimary,
            surfaceTint = bean_green_md_theme_light_surfaceTint,
            outlineVariant = bean_green_md_theme_light_outlineVariant,
            scrim = bean_green_md_theme_light_scrim,
            surface = bean_green_md_theme_light_surface,
            onSurface = bean_green_md_theme_light_onSurface,
            surfaceVariant = bean_green_md_theme_light_surfaceVariant,
            onSurfaceVariant = bean_green_md_theme_light_onSurfaceVariant,
        )
    else
        darkColorScheme(
            primary = bean_green_md_theme_dark_primary,
            onPrimary = bean_green_md_theme_dark_onPrimary,
            primaryContainer = bean_green_md_theme_dark_primaryContainer,
            onPrimaryContainer = bean_green_md_theme_dark_onPrimaryContainer,
            secondary = bean_green_md_theme_dark_secondary,
            onSecondary = bean_green_md_theme_dark_onSecondary,
            secondaryContainer = bean_green_md_theme_dark_secondaryContainer,
            onSecondaryContainer = bean_green_md_theme_dark_onSecondaryContainer,
            tertiary = bean_green_md_theme_dark_tertiary,
            onTertiary = bean_green_md_theme_dark_onTertiary,
            tertiaryContainer = bean_green_md_theme_dark_tertiaryContainer,
            onTertiaryContainer = bean_green_md_theme_dark_onTertiaryContainer,
            error = bean_green_md_theme_dark_error,
            errorContainer = bean_green_md_theme_dark_errorContainer,
            onError = bean_green_md_theme_dark_onError,
            onErrorContainer = bean_green_md_theme_dark_onErrorContainer,
            background = bean_green_md_theme_dark_background,
            onBackground = bean_green_md_theme_dark_onBackground,
            outline = bean_green_md_theme_dark_outline,
            inverseOnSurface = bean_green_md_theme_dark_inverseOnSurface,
            inverseSurface = bean_green_md_theme_dark_inverseSurface,
            inversePrimary = bean_green_md_theme_dark_inversePrimary,
            surfaceTint = bean_green_md_theme_dark_surfaceTint,
            outlineVariant = bean_green_md_theme_dark_outlineVariant,
            scrim = bean_green_md_theme_dark_scrim,
            surface = bean_green_md_theme_dark_surface,
            onSurface = bean_green_md_theme_dark_onSurface,
            surfaceVariant = bean_green_md_theme_dark_surfaceVariant,
            onSurfaceVariant = bean_green_md_theme_dark_onSurfaceVariant,
        )


/**
 * 动态颜色主题
 */
@Composable
fun dynamicColorTheme(
    darkTheme: Boolean,
    context: Context = LocalContext.current
): ColorScheme =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
    else
        defaultTheme(darkTheme)

/**
 * 绿色
 */
@Composable
fun greenTheme(
    darkTheme: Boolean
): ColorScheme =
    if (!darkTheme)
        lightColorScheme(
            primary = green_seed,
            onPrimary = green_md_theme_light_onPrimary,
            primaryContainer = green_md_theme_light_primaryContainer,
            onPrimaryContainer = green_md_theme_light_onPrimaryContainer,
            secondary = green_md_theme_light_secondary,
            onSecondary = green_md_theme_light_onSecondary,
            secondaryContainer = green_md_theme_light_secondaryContainer,
            onSecondaryContainer = green_md_theme_light_onSecondaryContainer,
            tertiary = green_md_theme_light_tertiary,
            onTertiary = green_md_theme_light_onTertiary,
            tertiaryContainer = green_md_theme_light_tertiaryContainer,
            onTertiaryContainer = green_md_theme_light_onTertiaryContainer,
            error = green_md_theme_light_error,
            errorContainer = green_md_theme_light_errorContainer,
            onError = green_md_theme_light_onError,
            onErrorContainer = green_md_theme_light_onErrorContainer,
            background = green_md_theme_light_background,
            onBackground = green_md_theme_light_onBackground,
            outline = green_md_theme_light_outline,
            inverseOnSurface = green_md_theme_light_inverseOnSurface,
            inverseSurface = green_md_theme_light_inverseSurface,
            inversePrimary = green_md_theme_light_inversePrimary,
            surfaceTint = green_md_theme_light_surfaceTint,
            outlineVariant = green_md_theme_light_outlineVariant,
            scrim = green_md_theme_light_scrim,
            surface = green_md_theme_light_surface,
            onSurface = green_md_theme_light_onSurface,
            surfaceVariant = green_md_theme_light_surfaceVariant,
            onSurfaceVariant = green_md_theme_light_onSurfaceVariant,
        )
    else
        darkColorScheme(
            primary = green_md_theme_dark_primary,
            onPrimary = green_md_theme_dark_onPrimary,
            primaryContainer = green_md_theme_dark_primaryContainer,
            onPrimaryContainer = green_md_theme_dark_onPrimaryContainer,
            secondary = green_md_theme_dark_secondary,
            onSecondary = green_md_theme_dark_onSecondary,
            secondaryContainer = green_md_theme_dark_secondaryContainer,
            onSecondaryContainer = green_md_theme_dark_onSecondaryContainer,
            tertiary = green_md_theme_dark_tertiary,
            onTertiary = green_md_theme_dark_onTertiary,
            tertiaryContainer = green_md_theme_dark_tertiaryContainer,
            onTertiaryContainer = green_md_theme_dark_onTertiaryContainer,
            error = green_md_theme_dark_error,
            errorContainer = green_md_theme_dark_errorContainer,
            onError = green_md_theme_dark_onError,
            onErrorContainer = green_md_theme_dark_onErrorContainer,
            background = green_md_theme_dark_background,
            onBackground = green_md_theme_dark_onBackground,
            outline = green_md_theme_dark_outline,
            inverseOnSurface = green_md_theme_dark_inverseOnSurface,
            inverseSurface = green_md_theme_dark_inverseSurface,
            inversePrimary = green_md_theme_dark_inversePrimary,
            surfaceTint = green_md_theme_dark_surfaceTint,
            outlineVariant = green_md_theme_dark_outlineVariant,
            scrim = green_md_theme_dark_scrim,
            surface = green_md_theme_dark_surface,
            onSurface = green_md_theme_dark_onSurface,
            surfaceVariant = green_md_theme_dark_surfaceVariant,
            onSurfaceVariant = green_md_theme_dark_onSurfaceVariant,
        )

/**
 * 红色
 */
@Composable
fun redTheme(
    darkTheme: Boolean
): ColorScheme =
    if (!darkTheme)
        lightColorScheme(
            primary = red_seed,
            onPrimary = red_md_theme_light_onPrimary,
            primaryContainer = red_md_theme_light_primaryContainer,
            onPrimaryContainer = red_md_theme_light_onPrimaryContainer,
            secondary = red_md_theme_light_secondary,
            onSecondary = red_md_theme_light_onSecondary,
            secondaryContainer = red_md_theme_light_secondaryContainer,
            onSecondaryContainer = red_md_theme_light_onSecondaryContainer,
            tertiary = red_md_theme_light_tertiary,
            onTertiary = red_md_theme_light_onTertiary,
            tertiaryContainer = red_md_theme_light_tertiaryContainer,
            onTertiaryContainer = red_md_theme_light_onTertiaryContainer,
            error = red_md_theme_light_error,
            errorContainer = red_md_theme_light_errorContainer,
            onError = red_md_theme_light_onError,
            onErrorContainer = red_md_theme_light_onErrorContainer,
            background = red_md_theme_light_background,
            onBackground = red_md_theme_light_onBackground,
            outline = red_md_theme_light_outline,
            inverseOnSurface = red_md_theme_light_inverseOnSurface,
            inverseSurface = red_md_theme_light_inverseSurface,
            inversePrimary = red_md_theme_light_inversePrimary,
            surfaceTint = red_md_theme_light_surfaceTint,
            outlineVariant = red_md_theme_light_outlineVariant,
            scrim = red_md_theme_light_scrim,
            surface = red_md_theme_light_surface,
            onSurface = red_md_theme_light_onSurface,
            surfaceVariant = red_md_theme_light_surfaceVariant,
            onSurfaceVariant = red_md_theme_light_onSurfaceVariant,
        )
    else
        darkColorScheme(
            primary = red_md_theme_dark_primary,
            onPrimary = red_md_theme_dark_onPrimary,
            primaryContainer = red_md_theme_dark_primaryContainer,
            onPrimaryContainer = red_md_theme_dark_onPrimaryContainer,
            secondary = red_md_theme_dark_secondary,
            onSecondary = red_md_theme_dark_onSecondary,
            secondaryContainer = red_md_theme_dark_secondaryContainer,
            onSecondaryContainer = red_md_theme_dark_onSecondaryContainer,
            tertiary = red_md_theme_dark_tertiary,
            onTertiary = red_md_theme_dark_onTertiary,
            tertiaryContainer = red_md_theme_dark_tertiaryContainer,
            onTertiaryContainer = red_md_theme_dark_onTertiaryContainer,
            error = red_md_theme_dark_error,
            errorContainer = red_md_theme_dark_errorContainer,
            onError = red_md_theme_dark_onError,
            onErrorContainer = red_md_theme_dark_onErrorContainer,
            background = red_md_theme_dark_background,
            onBackground = red_md_theme_dark_onBackground,
            outline = red_md_theme_dark_outline,
            inverseOnSurface = red_md_theme_dark_inverseOnSurface,
            inverseSurface = red_md_theme_dark_inverseSurface,
            inversePrimary = red_md_theme_dark_inversePrimary,
            surfaceTint = red_md_theme_dark_surfaceTint,
            outlineVariant = red_md_theme_dark_outlineVariant,
            scrim = red_md_theme_dark_scrim,
            surface = red_md_theme_dark_surface,
            onSurface = red_md_theme_dark_onSurface,
            surfaceVariant = red_md_theme_dark_surfaceVariant,
            onSurfaceVariant = red_md_theme_dark_onSurfaceVariant,
        )

/**
 * 粉色
 */
@Composable
fun pinkTheme(
    darkTheme: Boolean
): ColorScheme =
    if (!darkTheme)
        lightColorScheme(
            primary = pink_seed,
            onPrimary = pink_md_theme_light_onPrimary,
            primaryContainer = pink_md_theme_light_primaryContainer,
            onPrimaryContainer = pink_md_theme_light_onPrimaryContainer,
            secondary = pink_md_theme_light_secondary,
            onSecondary = pink_md_theme_light_onSecondary,
            secondaryContainer = pink_md_theme_light_secondaryContainer,
            onSecondaryContainer = pink_md_theme_light_onSecondaryContainer,
            tertiary = pink_md_theme_light_tertiary,
            onTertiary = pink_md_theme_light_onTertiary,
            tertiaryContainer = pink_md_theme_light_tertiaryContainer,
            onTertiaryContainer = pink_md_theme_light_onTertiaryContainer,
            error = pink_md_theme_light_error,
            errorContainer = pink_md_theme_light_errorContainer,
            onError = pink_md_theme_light_onError,
            onErrorContainer = pink_md_theme_light_onErrorContainer,
            background = pink_md_theme_light_background,
            onBackground = pink_md_theme_light_onBackground,
            outline = pink_md_theme_light_outline,
            inverseOnSurface = pink_md_theme_light_inverseOnSurface,
            inverseSurface = pink_md_theme_light_inverseSurface,
            inversePrimary = pink_md_theme_light_inversePrimary,
            surfaceTint = pink_md_theme_light_surfaceTint,
            outlineVariant = pink_md_theme_light_outlineVariant,
            scrim = pink_md_theme_light_scrim,
            surface = pink_md_theme_light_surface,
            onSurface = pink_md_theme_light_onSurface,
            surfaceVariant = pink_md_theme_light_surfaceVariant,
            onSurfaceVariant = pink_md_theme_light_onSurfaceVariant,
        )
    else
        darkColorScheme(
            primary = pink_md_theme_dark_primary,
            onPrimary = pink_md_theme_dark_onPrimary,
            primaryContainer = pink_md_theme_dark_primaryContainer,
            onPrimaryContainer = pink_md_theme_dark_onPrimaryContainer,
            secondary = pink_md_theme_dark_secondary,
            onSecondary = pink_md_theme_dark_onSecondary,
            secondaryContainer = pink_md_theme_dark_secondaryContainer,
            onSecondaryContainer = pink_md_theme_dark_onSecondaryContainer,
            tertiary = pink_md_theme_dark_tertiary,
            onTertiary = pink_md_theme_dark_onTertiary,
            tertiaryContainer = pink_md_theme_dark_tertiaryContainer,
            onTertiaryContainer = pink_md_theme_dark_onTertiaryContainer,
            error = pink_md_theme_dark_error,
            errorContainer = pink_md_theme_dark_errorContainer,
            onError = pink_md_theme_dark_onError,
            onErrorContainer = pink_md_theme_dark_onErrorContainer,
            background = pink_md_theme_dark_background,
            onBackground = pink_md_theme_dark_onBackground,
            outline = pink_md_theme_dark_outline,
            inverseOnSurface = pink_md_theme_dark_inverseOnSurface,
            inverseSurface = pink_md_theme_dark_inverseSurface,
            inversePrimary = pink_md_theme_dark_inversePrimary,
            surfaceTint = pink_md_theme_dark_surfaceTint,
            outlineVariant = pink_md_theme_dark_outlineVariant,
            scrim = pink_md_theme_dark_scrim,
            surface = pink_md_theme_dark_surface,
            onSurface = pink_md_theme_dark_onSurface,
            surfaceVariant = pink_md_theme_dark_surfaceVariant,
            onSurfaceVariant = pink_md_theme_dark_onSurfaceVariant,
        )

/**
 * 蓝色
 */
@Composable
fun blueTheme(
    darkTheme: Boolean
): ColorScheme =
    if (!darkTheme)
        lightColorScheme(
            primary = blue_seed,
            onPrimary = blue_md_theme_light_onPrimary,
            primaryContainer = blue_md_theme_light_primaryContainer,
            onPrimaryContainer = blue_md_theme_light_onPrimaryContainer,
            secondary = blue_md_theme_light_secondary,
            onSecondary = blue_md_theme_light_onSecondary,
            secondaryContainer = blue_md_theme_light_secondaryContainer,
            onSecondaryContainer = blue_md_theme_light_onSecondaryContainer,
            tertiary = blue_md_theme_light_tertiary,
            onTertiary = blue_md_theme_light_onTertiary,
            tertiaryContainer = blue_md_theme_light_tertiaryContainer,
            onTertiaryContainer = blue_md_theme_light_onTertiaryContainer,
            error = blue_md_theme_light_error,
            errorContainer = blue_md_theme_light_errorContainer,
            onError = blue_md_theme_light_onError,
            onErrorContainer = blue_md_theme_light_onErrorContainer,
            background = blue_md_theme_light_background,
            onBackground = blue_md_theme_light_onBackground,
            outline = blue_md_theme_light_outline,
            inverseOnSurface = blue_md_theme_light_inverseOnSurface,
            inverseSurface = blue_md_theme_light_inverseSurface,
            inversePrimary = blue_md_theme_light_inversePrimary,
            surfaceTint = blue_md_theme_light_surfaceTint,
            outlineVariant = blue_md_theme_light_outlineVariant,
            scrim = blue_md_theme_light_scrim,
            surface = blue_md_theme_light_surface,
            onSurface = blue_md_theme_light_onSurface,
            surfaceVariant = blue_md_theme_light_surfaceVariant,
            onSurfaceVariant = blue_md_theme_light_onSurfaceVariant,
        )
    else
        darkColorScheme(
            primary = blue_md_theme_dark_primary,
            onPrimary = blue_md_theme_dark_onPrimary,
            primaryContainer = blue_md_theme_dark_primaryContainer,
            onPrimaryContainer = blue_md_theme_dark_onPrimaryContainer,
            secondary = blue_md_theme_dark_secondary,
            onSecondary = blue_md_theme_dark_onSecondary,
            secondaryContainer = blue_md_theme_dark_secondaryContainer,
            onSecondaryContainer = blue_md_theme_dark_onSecondaryContainer,
            tertiary = blue_md_theme_dark_tertiary,
            onTertiary = blue_md_theme_dark_onTertiary,
            tertiaryContainer = blue_md_theme_dark_tertiaryContainer,
            onTertiaryContainer = blue_md_theme_dark_onTertiaryContainer,
            error = blue_md_theme_dark_error,
            errorContainer = blue_md_theme_dark_errorContainer,
            onError = blue_md_theme_dark_onError,
            onErrorContainer = blue_md_theme_dark_onErrorContainer,
            background = blue_md_theme_dark_background,
            onBackground = blue_md_theme_dark_onBackground,
            outline = blue_md_theme_dark_outline,
            inverseOnSurface = blue_md_theme_dark_inverseOnSurface,
            inverseSurface = blue_md_theme_dark_inverseSurface,
            inversePrimary = blue_md_theme_dark_inversePrimary,
            surfaceTint = blue_md_theme_dark_surfaceTint,
            outlineVariant = blue_md_theme_dark_outlineVariant,
            scrim = blue_md_theme_dark_scrim,
            surface = blue_md_theme_dark_surface,
            onSurface = blue_md_theme_dark_onSurface,
            surfaceVariant = blue_md_theme_dark_surfaceVariant,
            onSurfaceVariant = blue_md_theme_dark_onSurfaceVariant,
        )

/**
 * 青色
 */
@Composable
fun cyanTheme(
    darkTheme: Boolean
): ColorScheme =
    if (!darkTheme)
        lightColorScheme(
            primary = cyan_seed,
            onPrimary = cyan_md_theme_light_onPrimary,
            primaryContainer = cyan_md_theme_light_primaryContainer,
            onPrimaryContainer = cyan_md_theme_light_onPrimaryContainer,
            secondary = cyan_md_theme_light_secondary,
            onSecondary = cyan_md_theme_light_onSecondary,
            secondaryContainer = cyan_md_theme_light_secondaryContainer,
            onSecondaryContainer = cyan_md_theme_light_onSecondaryContainer,
            tertiary = cyan_md_theme_light_tertiary,
            onTertiary = cyan_md_theme_light_onTertiary,
            tertiaryContainer = cyan_md_theme_light_tertiaryContainer,
            onTertiaryContainer = cyan_md_theme_light_onTertiaryContainer,
            error = cyan_md_theme_light_error,
            errorContainer = cyan_md_theme_light_errorContainer,
            onError = cyan_md_theme_light_onError,
            onErrorContainer = cyan_md_theme_light_onErrorContainer,
            background = cyan_md_theme_light_background,
            onBackground = cyan_md_theme_light_onBackground,
            outline = cyan_md_theme_light_outline,
            inverseOnSurface = cyan_md_theme_light_inverseOnSurface,
            inverseSurface = cyan_md_theme_light_inverseSurface,
            inversePrimary = cyan_md_theme_light_inversePrimary,
            surfaceTint = cyan_md_theme_light_surfaceTint,
            outlineVariant = cyan_md_theme_light_outlineVariant,
            scrim = cyan_md_theme_light_scrim,
            surface = cyan_md_theme_light_surface,
            onSurface = cyan_md_theme_light_onSurface,
            surfaceVariant = cyan_md_theme_light_surfaceVariant,
            onSurfaceVariant = cyan_md_theme_light_onSurfaceVariant,
        )
    else
        darkColorScheme(
            primary = cyan_md_theme_dark_primary,
            onPrimary = cyan_md_theme_dark_onPrimary,
            primaryContainer = cyan_md_theme_dark_primaryContainer,
            onPrimaryContainer = cyan_md_theme_dark_onPrimaryContainer,
            secondary = cyan_md_theme_dark_secondary,
            onSecondary = cyan_md_theme_dark_onSecondary,
            secondaryContainer = cyan_md_theme_dark_secondaryContainer,
            onSecondaryContainer = cyan_md_theme_dark_onSecondaryContainer,
            tertiary = cyan_md_theme_dark_tertiary,
            onTertiary = cyan_md_theme_dark_onTertiary,
            tertiaryContainer = cyan_md_theme_dark_tertiaryContainer,
            onTertiaryContainer = cyan_md_theme_dark_onTertiaryContainer,
            error = cyan_md_theme_dark_error,
            errorContainer = cyan_md_theme_dark_errorContainer,
            onError = cyan_md_theme_dark_onError,
            onErrorContainer = cyan_md_theme_dark_onErrorContainer,
            background = cyan_md_theme_dark_background,
            onBackground = cyan_md_theme_dark_onBackground,
            outline = cyan_md_theme_dark_outline,
            inverseOnSurface = cyan_md_theme_dark_inverseOnSurface,
            inverseSurface = cyan_md_theme_dark_inverseSurface,
            inversePrimary = cyan_md_theme_dark_inversePrimary,
            surfaceTint = cyan_md_theme_dark_surfaceTint,
            outlineVariant = cyan_md_theme_dark_outlineVariant,
            scrim = cyan_md_theme_dark_scrim,
            surface = cyan_md_theme_dark_surface,
            onSurface = cyan_md_theme_dark_onSurface,
            surfaceVariant = cyan_md_theme_dark_surfaceVariant,
            onSurfaceVariant = cyan_md_theme_dark_onSurfaceVariant,
        )


/**
 * 橙色
 */
@Composable
fun orangeTheme(
    darkTheme: Boolean
): ColorScheme =
    if (!darkTheme)
        lightColorScheme(
            primary = orange_seed,
            onPrimary = orange_md_theme_light_onPrimary,
            primaryContainer = orange_md_theme_light_primaryContainer,
            onPrimaryContainer = orange_md_theme_light_onPrimaryContainer,
            secondary = orange_md_theme_light_secondary,
            onSecondary = orange_md_theme_light_onSecondary,
            secondaryContainer = orange_md_theme_light_secondaryContainer,
            onSecondaryContainer = orange_md_theme_light_onSecondaryContainer,
            tertiary = orange_md_theme_light_tertiary,
            onTertiary = orange_md_theme_light_onTertiary,
            tertiaryContainer = orange_md_theme_light_tertiaryContainer,
            onTertiaryContainer = orange_md_theme_light_onTertiaryContainer,
            error = orange_md_theme_light_error,
            errorContainer = orange_md_theme_light_errorContainer,
            onError = orange_md_theme_light_onError,
            onErrorContainer = orange_md_theme_light_onErrorContainer,
            background = orange_md_theme_light_background,
            onBackground = orange_md_theme_light_onBackground,
            outline = orange_md_theme_light_outline,
            inverseOnSurface = orange_md_theme_light_inverseOnSurface,
            inverseSurface = orange_md_theme_light_inverseSurface,
            inversePrimary = orange_md_theme_light_inversePrimary,
            surfaceTint = orange_md_theme_light_surfaceTint,
            outlineVariant = orange_md_theme_light_outlineVariant,
            scrim = orange_md_theme_light_scrim,
            surface = orange_md_theme_light_surface,
            onSurface = orange_md_theme_light_onSurface,
            surfaceVariant = orange_md_theme_light_surfaceVariant,
            onSurfaceVariant = orange_md_theme_light_onSurfaceVariant,
        )
    else
        darkColorScheme(
            primary = orange_md_theme_dark_primary,
            onPrimary = orange_md_theme_dark_onPrimary,
            primaryContainer = orange_md_theme_dark_primaryContainer,
            onPrimaryContainer = orange_md_theme_dark_onPrimaryContainer,
            secondary = orange_md_theme_dark_secondary,
            onSecondary = orange_md_theme_dark_onSecondary,
            secondaryContainer = orange_md_theme_dark_secondaryContainer,
            onSecondaryContainer = orange_md_theme_dark_onSecondaryContainer,
            tertiary = orange_md_theme_dark_tertiary,
            onTertiary = orange_md_theme_dark_onTertiary,
            tertiaryContainer = orange_md_theme_dark_tertiaryContainer,
            onTertiaryContainer = orange_md_theme_dark_onTertiaryContainer,
            error = orange_md_theme_dark_error,
            errorContainer = orange_md_theme_dark_errorContainer,
            onError = orange_md_theme_dark_onError,
            onErrorContainer = orange_md_theme_dark_onErrorContainer,
            background = orange_md_theme_dark_background,
            onBackground = orange_md_theme_dark_onBackground,
            outline = orange_md_theme_dark_outline,
            inverseOnSurface = orange_md_theme_dark_inverseOnSurface,
            inverseSurface = orange_md_theme_dark_inverseSurface,
            inversePrimary = orange_md_theme_dark_inversePrimary,
            surfaceTint = orange_md_theme_dark_surfaceTint,
            outlineVariant = orange_md_theme_dark_outlineVariant,
            scrim = orange_md_theme_dark_scrim,
            surface = orange_md_theme_dark_surface,
            onSurface = orange_md_theme_dark_onSurface,
            surfaceVariant = orange_md_theme_dark_surfaceVariant,
            onSurfaceVariant = orange_md_theme_dark_onSurfaceVariant,
        )

/**
 * 紫色
 */
@Composable
fun purpleTheme(
    darkTheme: Boolean
): ColorScheme =
    if (!darkTheme)
        lightColorScheme(
            primary = purple_seed,
            onPrimary = purple_md_theme_light_onPrimary,
            primaryContainer = purple_md_theme_light_primaryContainer,
            onPrimaryContainer = purple_md_theme_light_onPrimaryContainer,
            secondary = purple_md_theme_light_secondary,
            onSecondary = purple_md_theme_light_onSecondary,
            secondaryContainer = purple_md_theme_light_secondaryContainer,
            onSecondaryContainer = purple_md_theme_light_onSecondaryContainer,
            tertiary = purple_md_theme_light_tertiary,
            onTertiary = purple_md_theme_light_onTertiary,
            tertiaryContainer = purple_md_theme_light_tertiaryContainer,
            onTertiaryContainer = purple_md_theme_light_onTertiaryContainer,
            error = purple_md_theme_light_error,
            errorContainer = purple_md_theme_light_errorContainer,
            onError = purple_md_theme_light_onError,
            onErrorContainer = purple_md_theme_light_onErrorContainer,
            background = purple_md_theme_light_background,
            onBackground = purple_md_theme_light_onBackground,
            outline = purple_md_theme_light_outline,
            inverseOnSurface = purple_md_theme_light_inverseOnSurface,
            inverseSurface = purple_md_theme_light_inverseSurface,
            inversePrimary = purple_md_theme_light_inversePrimary,
            surfaceTint = purple_md_theme_light_surfaceTint,
            outlineVariant = purple_md_theme_light_outlineVariant,
            scrim = purple_md_theme_light_scrim,
            surface = purple_md_theme_light_surface,
            onSurface = purple_md_theme_light_onSurface,
            surfaceVariant = purple_md_theme_light_surfaceVariant,
            onSurfaceVariant = purple_md_theme_light_onSurfaceVariant,
        )
    else
        darkColorScheme(
            primary = purple_md_theme_dark_primary,
            onPrimary = purple_md_theme_dark_onPrimary,
            primaryContainer = purple_md_theme_dark_primaryContainer,
            onPrimaryContainer = purple_md_theme_dark_onPrimaryContainer,
            secondary = purple_md_theme_dark_secondary,
            onSecondary = purple_md_theme_dark_onSecondary,
            secondaryContainer = purple_md_theme_dark_secondaryContainer,
            onSecondaryContainer = purple_md_theme_dark_onSecondaryContainer,
            tertiary = purple_md_theme_dark_tertiary,
            onTertiary = purple_md_theme_dark_onTertiary,
            tertiaryContainer = purple_md_theme_dark_tertiaryContainer,
            onTertiaryContainer = purple_md_theme_dark_onTertiaryContainer,
            error = purple_md_theme_dark_error,
            errorContainer = purple_md_theme_dark_errorContainer,
            onError = purple_md_theme_dark_onError,
            onErrorContainer = purple_md_theme_dark_onErrorContainer,
            background = purple_md_theme_dark_background,
            onBackground = purple_md_theme_dark_onBackground,
            outline = purple_md_theme_dark_outline,
            inverseOnSurface = purple_md_theme_dark_inverseOnSurface,
            inverseSurface = purple_md_theme_dark_inverseSurface,
            inversePrimary = purple_md_theme_dark_inversePrimary,
            surfaceTint = purple_md_theme_dark_surfaceTint,
            outlineVariant = purple_md_theme_dark_outlineVariant,
            scrim = purple_md_theme_dark_scrim,
            surface = purple_md_theme_dark_surface,
            onSurface = purple_md_theme_dark_onSurface,
            surfaceVariant = purple_md_theme_dark_surfaceVariant,
            onSurfaceVariant = purple_md_theme_dark_onSurfaceVariant,
        )

/**
 * 棕色
 */
@Composable
fun brownTheme(
    darkTheme: Boolean
): ColorScheme =
    if (!darkTheme)
        lightColorScheme(
            primary = brown_seed,
            onPrimary = brown_md_theme_light_onPrimary,
            primaryContainer = brown_md_theme_light_primaryContainer,
            onPrimaryContainer = brown_md_theme_light_onPrimaryContainer,
            secondary = brown_md_theme_light_secondary,
            onSecondary = brown_md_theme_light_onSecondary,
            secondaryContainer = brown_md_theme_light_secondaryContainer,
            onSecondaryContainer = brown_md_theme_light_onSecondaryContainer,
            tertiary = brown_md_theme_light_tertiary,
            onTertiary = brown_md_theme_light_onTertiary,
            tertiaryContainer = brown_md_theme_light_tertiaryContainer,
            onTertiaryContainer = brown_md_theme_light_onTertiaryContainer,
            error = brown_md_theme_light_error,
            errorContainer = brown_md_theme_light_errorContainer,
            onError = brown_md_theme_light_onError,
            onErrorContainer = brown_md_theme_light_onErrorContainer,
            background = brown_md_theme_light_background,
            onBackground = brown_md_theme_light_onBackground,
            outline = brown_md_theme_light_outline,
            inverseOnSurface = brown_md_theme_light_inverseOnSurface,
            inverseSurface = brown_md_theme_light_inverseSurface,
            inversePrimary = brown_md_theme_light_inversePrimary,
            surfaceTint = brown_md_theme_light_surfaceTint,
            outlineVariant = brown_md_theme_light_outlineVariant,
            scrim = brown_md_theme_light_scrim,
            surface = brown_md_theme_light_surface,
            onSurface = brown_md_theme_light_onSurface,
            surfaceVariant = brown_md_theme_light_surfaceVariant,
            onSurfaceVariant = brown_md_theme_light_onSurfaceVariant,
        )
    else
        darkColorScheme(
            primary = brown_md_theme_dark_primary,
            onPrimary = brown_md_theme_dark_onPrimary,
            primaryContainer = brown_md_theme_dark_primaryContainer,
            onPrimaryContainer = brown_md_theme_dark_onPrimaryContainer,
            secondary = brown_md_theme_dark_secondary,
            onSecondary = brown_md_theme_dark_onSecondary,
            secondaryContainer = brown_md_theme_dark_secondaryContainer,
            onSecondaryContainer = brown_md_theme_dark_onSecondaryContainer,
            tertiary = brown_md_theme_dark_tertiary,
            onTertiary = brown_md_theme_dark_onTertiary,
            tertiaryContainer = brown_md_theme_dark_tertiaryContainer,
            onTertiaryContainer = brown_md_theme_dark_onTertiaryContainer,
            error = brown_md_theme_dark_error,
            errorContainer = brown_md_theme_dark_errorContainer,
            onError = brown_md_theme_dark_onError,
            onErrorContainer = brown_md_theme_dark_onErrorContainer,
            background = brown_md_theme_dark_background,
            onBackground = brown_md_theme_dark_onBackground,
            outline = brown_md_theme_dark_outline,
            inverseOnSurface = brown_md_theme_dark_inverseOnSurface,
            inverseSurface = brown_md_theme_dark_inverseSurface,
            inversePrimary = brown_md_theme_dark_inversePrimary,
            surfaceTint = brown_md_theme_dark_surfaceTint,
            outlineVariant = brown_md_theme_dark_outlineVariant,
            scrim = brown_md_theme_dark_scrim,
            surface = brown_md_theme_dark_surface,
            onSurface = brown_md_theme_dark_onSurface,
            surfaceVariant = brown_md_theme_dark_surfaceVariant,
            onSurfaceVariant = brown_md_theme_dark_onSurfaceVariant,
        )

/**
 * 灰色
 */
@Composable
fun grayTheme(
    darkTheme: Boolean
): ColorScheme =
    if (!darkTheme)
        lightColorScheme(
            primary = gray_seed,
            onPrimary = gray_md_theme_light_onPrimary,
            primaryContainer = gray_md_theme_light_primaryContainer,
            onPrimaryContainer = gray_md_theme_light_onPrimaryContainer,
            secondary = gray_md_theme_light_secondary,
            onSecondary = gray_md_theme_light_onSecondary,
            secondaryContainer = gray_md_theme_light_secondaryContainer,
            onSecondaryContainer = gray_md_theme_light_onSecondaryContainer,
            tertiary = gray_md_theme_light_tertiary,
            onTertiary = gray_md_theme_light_onTertiary,
            tertiaryContainer = gray_md_theme_light_tertiaryContainer,
            onTertiaryContainer = gray_md_theme_light_onTertiaryContainer,
            error = gray_md_theme_light_error,
            errorContainer = gray_md_theme_light_errorContainer,
            onError = gray_md_theme_light_onError,
            onErrorContainer = gray_md_theme_light_onErrorContainer,
            background = gray_md_theme_light_background,
            onBackground = gray_md_theme_light_onBackground,
            outline = gray_md_theme_light_outline,
            inverseOnSurface = gray_md_theme_light_inverseOnSurface,
            inverseSurface = gray_md_theme_light_inverseSurface,
            inversePrimary = gray_md_theme_light_inversePrimary,
            surfaceTint = gray_md_theme_light_surfaceTint,
            outlineVariant = gray_md_theme_light_outlineVariant,
            scrim = gray_md_theme_light_scrim,
            surface = gray_md_theme_light_surface,
            onSurface = gray_md_theme_light_onSurface,
            surfaceVariant = gray_md_theme_light_surfaceVariant,
            onSurfaceVariant = gray_md_theme_light_onSurfaceVariant,
        )
    else
        darkColorScheme(
            primary = gray_md_theme_dark_primary,
            onPrimary = gray_md_theme_dark_onPrimary,
            primaryContainer = gray_md_theme_dark_primaryContainer,
            onPrimaryContainer = gray_md_theme_dark_onPrimaryContainer,
            secondary = gray_md_theme_dark_secondary,
            onSecondary = gray_md_theme_dark_onSecondary,
            secondaryContainer = gray_md_theme_dark_secondaryContainer,
            onSecondaryContainer = gray_md_theme_dark_onSecondaryContainer,
            tertiary = gray_md_theme_dark_tertiary,
            onTertiary = gray_md_theme_dark_onTertiary,
            tertiaryContainer = gray_md_theme_dark_tertiaryContainer,
            onTertiaryContainer = gray_md_theme_dark_onTertiaryContainer,
            error = gray_md_theme_dark_error,
            errorContainer = gray_md_theme_dark_errorContainer,
            onError = gray_md_theme_dark_onError,
            onErrorContainer = gray_md_theme_dark_onErrorContainer,
            background = gray_md_theme_dark_background,
            onBackground = gray_md_theme_dark_onBackground,
            outline = gray_md_theme_dark_outline,
            inverseOnSurface = gray_md_theme_dark_inverseOnSurface,
            inverseSurface = gray_md_theme_dark_inverseSurface,
            inversePrimary = gray_md_theme_dark_inversePrimary,
            surfaceTint = gray_md_theme_dark_surfaceTint,
            outlineVariant = gray_md_theme_dark_outlineVariant,
            scrim = gray_md_theme_dark_scrim,
            surface = gray_md_theme_dark_surface,
            onSurface = gray_md_theme_dark_onSurface,
            surfaceVariant = gray_md_theme_dark_surfaceVariant,
            onSurfaceVariant = gray_md_theme_dark_onSurfaceVariant,
        )


/**
 * 主题颜色状态管理
 */
@Composable
fun themeAnimation(targetTheme: ColorScheme): ColorScheme {
    //动画时长
    val durationMillis = 600
    //插值器
    val animationSpec = TweenSpec<Color>(durationMillis = durationMillis, easing = FastOutLinearInEasing)

    val primary by animateColorAsState(
        targetValue = targetTheme.primary, animationSpec, label = "primary"
    )
    val onPrimary by animateColorAsState(
        targetValue = targetTheme.onPrimary, animationSpec, label = "onPrimary"
    )
    val primaryContainer by animateColorAsState(
        targetValue = targetTheme.primaryContainer, animationSpec, label = "primaryContainer"
    )
    val onPrimaryContainer by animateColorAsState(
        targetValue = targetTheme.onPrimaryContainer, animationSpec, label = "onPrimaryContainer"
    )
    val secondary by animateColorAsState(
        targetValue = targetTheme.secondary, animationSpec, label = "secondary"
    )
    val onSecondary by animateColorAsState(
        targetValue = targetTheme.onSecondary, animationSpec, label = "onSecondary"
    )
    val secondaryContainer by animateColorAsState(
        targetValue = targetTheme.secondaryContainer, animationSpec, label = "secondaryContainer"
    )
    val onSecondaryContainer by animateColorAsState(
        targetValue = targetTheme.onSecondaryContainer, animationSpec, label = "onSecondaryContainer"
    )
    val tertiary by animateColorAsState(
        targetValue = targetTheme.tertiary, animationSpec, label = "tertiary"
    )
    val onTertiary by animateColorAsState(
        targetValue = targetTheme.onTertiary, animationSpec, label = "onTertiary"
    )
    val tertiaryContainer by animateColorAsState(
        targetValue = targetTheme.tertiaryContainer, animationSpec, label = "tertiaryContainer"
    )
    val onTertiaryContainer by animateColorAsState(
        targetValue = targetTheme.onTertiaryContainer, animationSpec, label = "onTertiaryContainer"
    )
    val error by animateColorAsState(
        targetValue = targetTheme.error, animationSpec, label = "error"
    )
    val errorContainer by animateColorAsState(
        targetValue = targetTheme.errorContainer, animationSpec, label = "errorContainer"
    )
    val onError by animateColorAsState(
        targetValue = targetTheme.onError, animationSpec, label = "onError"
    )
    val onErrorContainer by animateColorAsState(
        targetValue = targetTheme.onErrorContainer, animationSpec, label = "onErrorContainer"
    )
    val background by animateColorAsState(
        targetValue = targetTheme.background, animationSpec, label = "background"
    )
    val onBackground by animateColorAsState(
        targetValue = targetTheme.onBackground, animationSpec, label = "onBackground"
    )
    val outline by animateColorAsState(
        targetValue = targetTheme.outline, animationSpec, label = "outline"
    )
    val inverseOnSurface by animateColorAsState(
        targetValue = targetTheme.inverseOnSurface, animationSpec, label = "inverseOnSurface"
    )
    val inverseSurface by animateColorAsState(
        targetValue = targetTheme.inverseSurface, animationSpec, label = "inverseSurface"
    )
    val inversePrimary by animateColorAsState(
        targetValue = targetTheme.inversePrimary, animationSpec, label = "inversePrimary"
    )
    val surfaceTint by animateColorAsState(
        targetValue = targetTheme.surfaceTint, animationSpec, label = "surfaceTint"
    )
    val outlineVariant by animateColorAsState(
        targetValue = targetTheme.outlineVariant, animationSpec, label = "outlineVariant"
    )
    val scrim by animateColorAsState(
        targetValue = targetTheme.scrim, animationSpec, label = "scrim"
    )
    val surface by animateColorAsState(
        targetValue = targetTheme.surface, animationSpec, label = "surface"
    )
    val onSurface by animateColorAsState(
        targetValue = targetTheme.onSurface, animationSpec, label = "onSurface"
    )
    val surfaceVariant by animateColorAsState(
        targetValue = targetTheme.surfaceVariant, animationSpec, label = "surfaceVariant"
    )
    val onSurfaceVariant by animateColorAsState(
        targetValue = targetTheme.onSurfaceVariant, animationSpec, label = "onSurfaceVariant"
    )

    return targetTheme.copy(
        primary = primary,
        onPrimary = onPrimary,
        primaryContainer = primaryContainer,
        onPrimaryContainer = onPrimaryContainer,
        secondary = secondary,
        onSecondary = onSecondary,
        secondaryContainer = secondaryContainer,
        onSecondaryContainer = onSecondaryContainer,
        tertiary = tertiary,
        onTertiary = onTertiary,
        tertiaryContainer = tertiaryContainer,
        onTertiaryContainer = onTertiaryContainer,
        error = error,
        errorContainer = errorContainer,
        onError = onError,
        onErrorContainer = onErrorContainer,
        background = background,
        onBackground = onBackground,
        outline = outline,
        inverseOnSurface = inverseOnSurface,
        inverseSurface = inverseSurface,
        inversePrimary = inversePrimary,
        surfaceTint = surfaceTint,
        outlineVariant = outlineVariant,
        scrim = scrim,
        surface = surface,
        onSurface = onSurface,
        surfaceVariant = surfaceVariant,
        onSurfaceVariant = onSurfaceVariant,
    )
}