package com.xiaozhao45.celestite.Components


import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties

/**
 * 新手引导气泡组件
 *
 * @param text 提示文字
 * @param isVisible 是否显示
 * @param onDismiss 请求关闭回调
 * @param modifier 修饰符
 * @param content 需要被提示包裹的目标组件
 */
@Composable
fun OnboardingTooltip(
    text: String,
    isVisible: Boolean,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    // 渲染目标组件
    Box(modifier = modifier) {
        content()

        if (isVisible) {
            // 使用 Popup 浮在上面
            Popup(
                alignment = Alignment.BottomCenter,
                offset = IntOffset(0, 140), // 稍微向下偏移，避免遮住组件本身 (根据需要调整 y 值)
                properties = PopupProperties(
                    focusable = false, // 关键：设为 false，不抢占焦点，允许点击背后
                    dismissOnBackPress = false,
                    dismissOnClickOutside = false
                )
            ) {
                TooltipBubble(text = text, onDismiss = onDismiss)
            }
        }
    }
}

@Composable
private fun TooltipBubble(
    text: String,
    onDismiss: () -> Unit
) {
    // 气泡背景色
    val bubbleColor = MaterialTheme.colorScheme.tertiaryContainer
    val contentColor = MaterialTheme.colorScheme.onTertiaryContainer

    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 上方的小三角箭头
        Box(
            modifier = Modifier
                .size(width = 16.dp, height = 8.dp)
                .background(bubbleColor, TriangleShape())
        )

        // 气泡主体
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = bubbleColor,
            shadowElevation = 4.dp,
            modifier = Modifier
                .widthIn(max = 240.dp) // 限制最大宽度
                .clickable { onDismiss() } // 点击气泡本身也可以关闭
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = text,
                    style = MaterialTheme.typography.bodyMedium,
                    color = contentColor,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                // 关闭按钮
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    tint = contentColor.copy(alpha = 0.6f),
                    modifier = Modifier
                        .size(16.dp)
                        .clickable(onClick = onDismiss)
                )
            }
        }
    }
}

// 绘制三角形的 Shape
private fun TriangleShape() = GenericShape { size, _ ->
    moveTo(size.width / 2f, 0f) // 顶点
    lineTo(size.width, size.height) // 右底角
    lineTo(0f, size.height) // 左底角
    close()
}