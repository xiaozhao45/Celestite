package com.xiaozhao45.celestite.Components

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import kotlinx.coroutines.delay
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
@Composable
fun AnimatedDialog(
    visible: Boolean,
    onDismissRequest: () -> Unit,
    content: @Composable () -> Unit
) {
    var showPopup by remember { mutableStateOf(visible) }

    LaunchedEffect(visible) {
        if (visible) {
            showPopup = true
        } else {
            // 动画结束后销毁
            delay(200)
            showPopup = false
        }
    }

    if (showPopup) {
        Popup(
            alignment = Alignment.Center,
            onDismissRequest = onDismissRequest,
            properties = PopupProperties(focusable = true)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.32f)) // M3 标准遮罩透明度
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onDismissRequest
                    ),
                contentAlignment = Alignment.Center
            ) {
                AnimatedVisibility(
                    visible = visible,
                    // M3 风味：淡入 + 从下方一小段距离升起
                    enter = fadeIn(animationSpec = tween(durationMillis = 300)) +
                            slideInVertically(
                                initialOffsetY = { it / 12 }, // 仅从下方 1/12 高度处升起
                                animationSpec = tween(durationMillis = 300)
                            ),
                    // M3 风味：淡出，不带位移（或极轻微位移）
                    exit = fadeOut(animationSpec = tween(durationMillis = 200))
                ) {
                    Box(
                        modifier = Modifier.clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = {}
                        )
                    ) {
                        content()
                    }
                }
            }
        }
    }
}

/**
 * 修复版：增加了 Border 和 Shadow，适配 Desktop 显示
 */
@Composable
fun AnimatedAlertDialogCenter(
    visible: Boolean,
    onDismissRequest: () -> Unit,
    icon: @Composable (() -> Unit)? = null,
    title: @Composable (() -> Unit)? = null,
    text: @Composable (() -> Unit)? = null,
    confirmButton: @Composable () -> Unit,
    dismissButton: @Composable (() -> Unit)? = null,
) {
    AnimatedDialog(
        visible = visible,
        onDismissRequest = onDismissRequest
    ) {
        // 使用 Box 强制居中，防止某些情况下位置偏移
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                shape = RoundedCornerShape(28.dp),
                color = MaterialTheme.colorScheme.surface, // 使用对比度更好的颜色
                // 【关键修复1】添加边框，让弹窗在 Desktop 上从背景中浮现出来
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                // 【关键修复2】同时使用 Tonal 和 Shadow Elevation
                tonalElevation = 6.dp,
                shadowElevation = 8.dp,
                modifier = Modifier
                    .widthIn(min = 280.dp, max = 560.dp)
                    // 防止内容太长溢出
                    .heightIn(max = 560.dp)
                    // 拦截点击，防止点到弹窗穿透到背景
                    .clickable(enabled = false) {}
                    .padding(24.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .verticalScroll(rememberScrollState()), // 允许内容滚动
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Icon
                    if (icon != null) {
                        CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.secondary) {
                            icon()
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    // Title
                    if (title != null) {
                        CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onSurface) {
                            ProvideTextStyle(MaterialTheme.typography.headlineSmall) {
                                title()
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    // Text
                    if (text != null) {
                        CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onSurfaceVariant) {
                            ProvideTextStyle(MaterialTheme.typography.bodyMedium) {
                                text()
                            }
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                    }

                    // Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        if (dismissButton != null) {
                            dismissButton()
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        confirmButton()
                    }
                }
            }
        }
    }
}

/**
 * 仿照 AlertDialog API 的动画版本
 */
@Composable
fun AnimatedAlertDialog(
    visible: Boolean,
    onDismissRequest: () -> Unit,
    icon: @Composable (() -> Unit)? = null,
    title: @Composable (() -> Unit)? = null,
    text: @Composable (() -> Unit)? = null,
    confirmButton: @Composable () -> Unit,
    dismissButton: @Composable (() -> Unit)? = null,
) {
    AnimatedDialog(
        visible = visible,
        onDismissRequest = onDismissRequest
    ) {
        // 使用 Surface 模拟 AlertDialog 的外观
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            tonalElevation = 6.dp,
            modifier = Modifier
                .widthIn(min = 280.dp, max = 560.dp)
                .padding(24.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Icon
                if (icon != null) {
                    CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.secondary) {
                        icon()
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Title
                if (title != null) {
                    CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onSurface) {
                        ProvideTextStyle(MaterialTheme.typography.headlineSmall) {
                            title()
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Text
                if (text != null) {
                    CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onSurfaceVariant) {
                        ProvideTextStyle(MaterialTheme.typography.bodyMedium) {
                            text()
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }

                // Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    if (dismissButton != null) {
                        dismissButton()
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    confirmButton()
                }
            }
        }
    }
}