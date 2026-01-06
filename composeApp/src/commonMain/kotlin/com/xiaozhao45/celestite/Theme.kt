package com.xiaozhao45.celestite

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import com.materialkolor.DynamicMaterialTheme
import com.materialkolor.rememberDynamicMaterialThemeState
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import com.materialkolor.PaletteStyle
import com.materialkolor.rememberDynamicColorScheme

@Composable
fun animateColorSchemeAsState(
    targetColorScheme: ColorScheme,
    animationSpec: AnimationSpec<Color> = tween(durationMillis = 600) // 默认 600ms 缓动动画
): State<ColorScheme> {
    val primary by animateColorAsState(targetColorScheme.primary, animationSpec)
    val onPrimary by animateColorAsState(targetColorScheme.onPrimary, animationSpec)
    val primaryContainer by animateColorAsState(targetColorScheme.primaryContainer, animationSpec)
    val onPrimaryContainer by animateColorAsState(targetColorScheme.onPrimaryContainer, animationSpec)
    val inversePrimary by animateColorAsState(targetColorScheme.inversePrimary, animationSpec)
    val secondary by animateColorAsState(targetColorScheme.secondary, animationSpec)
    val onSecondary by animateColorAsState(targetColorScheme.onSecondary, animationSpec)
    val secondaryContainer by animateColorAsState(targetColorScheme.secondaryContainer, animationSpec)
    val onSecondaryContainer by animateColorAsState(targetColorScheme.onSecondaryContainer, animationSpec)
    val tertiary by animateColorAsState(targetColorScheme.tertiary, animationSpec)
    val onTertiary by animateColorAsState(targetColorScheme.onTertiary, animationSpec)
    val tertiaryContainer by animateColorAsState(targetColorScheme.tertiaryContainer, animationSpec)
    val onTertiaryContainer by animateColorAsState(targetColorScheme.onTertiaryContainer, animationSpec)
    val background by animateColorAsState(targetColorScheme.background, animationSpec)
    val onBackground by animateColorAsState(targetColorScheme.onBackground, animationSpec)
    val surface by animateColorAsState(targetColorScheme.surface, animationSpec)
    val onSurface by animateColorAsState(targetColorScheme.onSurface, animationSpec)
    val surfaceVariant by animateColorAsState(targetColorScheme.surfaceVariant, animationSpec)
    val onSurfaceVariant by animateColorAsState(targetColorScheme.onSurfaceVariant, animationSpec)
    val surfaceTint by animateColorAsState(targetColorScheme.surfaceTint, animationSpec)
    val inverseSurface by animateColorAsState(targetColorScheme.inverseSurface, animationSpec)
    val inverseOnSurface by animateColorAsState(targetColorScheme.inverseOnSurface, animationSpec)
    val error by animateColorAsState(targetColorScheme.error, animationSpec)
    val onError by animateColorAsState(targetColorScheme.onError, animationSpec)
    val errorContainer by animateColorAsState(targetColorScheme.errorContainer, animationSpec)
    val onErrorContainer by animateColorAsState(targetColorScheme.onErrorContainer, animationSpec)
    val outline by animateColorAsState(targetColorScheme.outline, animationSpec)
    val outlineVariant by animateColorAsState(targetColorScheme.outlineVariant, animationSpec)
    val scrim by animateColorAsState(targetColorScheme.scrim, animationSpec)

    val surfaceBright by animateColorAsState(targetColorScheme.surfaceBright, animationSpec)
    val surfaceDim by animateColorAsState(targetColorScheme.surfaceDim, animationSpec)
    val surfaceContainer by animateColorAsState(targetColorScheme.surfaceContainer, animationSpec)
    val surfaceContainerHigh by animateColorAsState(targetColorScheme.surfaceContainerHigh, animationSpec)
    val surfaceContainerHighest by animateColorAsState(targetColorScheme.surfaceContainerHighest, animationSpec)
    val surfaceContainerLow by animateColorAsState(targetColorScheme.surfaceContainerLow, animationSpec)
    val surfaceContainerLowest by animateColorAsState(targetColorScheme.surfaceContainerLowest, animationSpec)
    // 使用 derivedStateOf 确保只有颜色值真正变化时才重新生成 ColorScheme 对象
    return remember {
        derivedStateOf {
            ColorScheme(
                primary = primary,
                onPrimary = onPrimary,
                primaryContainer = primaryContainer,
                onPrimaryContainer = onPrimaryContainer,
                inversePrimary = inversePrimary,
                secondary = secondary,
                onSecondary = onSecondary,
                secondaryContainer = secondaryContainer,
                onSecondaryContainer = onSecondaryContainer,
                tertiary = tertiary,
                onTertiary = onTertiary,
                tertiaryContainer = tertiaryContainer,
                onTertiaryContainer = onTertiaryContainer,
                background = background,
                onBackground = onBackground,
                surface = surface,
                onSurface = onSurface,
                surfaceVariant = surfaceVariant,
                onSurfaceVariant = onSurfaceVariant,
                surfaceTint = surfaceTint,
                inverseSurface = inverseSurface,
                inverseOnSurface = inverseOnSurface,
                error = error,
                onError = onError,
                errorContainer = errorContainer,
                onErrorContainer = onErrorContainer,
                outline = outline,
                outlineVariant = outlineVariant,
                scrim = scrim,
                surfaceBright = surfaceBright,
                surfaceDim = surfaceDim,
                surfaceContainer = surfaceContainer,
                surfaceContainerHigh = surfaceContainerHigh,
                surfaceContainerHighest = surfaceContainerHighest,
                surfaceContainerLow = surfaceContainerLow,
                surfaceContainerLowest = surfaceContainerLowest,
            )
        }
    }
}

@Composable
expect fun getPlatformDynamicColorScheme(isDark: Boolean): ColorScheme?
@Composable
fun AppTheme(
    seedColor: Color,
    useDynamicColor: Boolean,
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    // 1. 尝试获取原生系统配色 (Android 12+)
    // 如果 useDynamicColor 为 false，这里直接得到 null
    val systemScheme = if (useDynamicColor) {
        getPlatformDynamicColorScheme(darkTheme)
    } else {
        null
    }

    // 2. 获取 MaterialKolor 生成的配色 (作为备选/自定义方案)
    // 使用 rememberDynamicColorScheme 直接算出 ColorScheme 对象，而不是由它来渲染 UI
    val generatedScheme = rememberDynamicColorScheme(
        seedColor = seedColor,
        isDark = darkTheme,
        style = PaletteStyle.TonalSpot // 默认的标准 Material 3 风格
    )

    // 3. 确定最终的目标配色
    // 如果有系统配色就用系统的，否则用生成的
    val targetScheme = systemScheme ?: generatedScheme

    // 4. 【核心】对整个 ColorScheme 进行平滑过渡动画
    // 这里设置 durationMillis = 800，让颜色变化明显且优雅
    val animatedSchemeState by animateColorSchemeAsState(
        targetColorScheme = targetScheme,
        animationSpec = tween(durationMillis = 800)
    )

    // 5. 渲染唯一的 MaterialTheme
    MaterialTheme(
        colorScheme = animatedSchemeState, // 传入正在做动画的配色方案
        content = content
    )
}