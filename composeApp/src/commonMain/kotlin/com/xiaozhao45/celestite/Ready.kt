package com.xiaozhao45.celestite

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Water
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import kotlinx.coroutines.delay
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.GenericShape // 用于绘制三角形
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close // 用于关闭图标
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
// 帮助信息配置类
private data class ModeConfig(
    val title: String,
    val description: String,
    val icon: ImageVector
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReadyPage(
    onNavigateToPage: (Int) -> Unit,
    targetModeIndex: Int? = null // 传入当前 Tab 的索引，用于显示对应信息
) {
    // --- 状态管理 ---
    var selectedTime by remember { mutableStateOf(LocalDateTime.now()) }
    var isTimeLocked by remember { mutableStateOf(false) }
    var showTimeDialog by remember { mutableStateOf(false) }

    val timeFormatter = remember { DateTimeFormatter.ofPattern("HH:mm:ss") }
    val dateFormatter = remember { DateTimeFormatter.ofPattern("yyyy年MM月dd日") }
    var showTimeTip by remember {
        mutableStateOf(UserPreferences.shouldShowTooltip(TooltipIndices.TIME_CARD))
    }
    // 计时器逻辑
    LaunchedEffect(isTimeLocked) {
        if (!isTimeLocked) {
            while (true) {
                selectedTime = LocalDateTime.now()
                delay(1000)
            }
        }
    }

    // 获取当前模式的配置信息
    val modeConfig = getModeConfig(targetModeIndex ?: 0)

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surface,
        floatingActionButton = {
            // 底部的大号 FAB，作为“开始”按钮
            ExtendedFloatingActionButton(
                onClick = {
                    updateGlobalTime(selectedTime)
                    // 如果有目标索引，直接跳转；没有则默认去奇门(0)
                    onNavigateToPage(targetModeIndex ?: 0)
                },
                icon = { Icon(Icons.Default.PlayArrow, contentDescription = null) },
                text = { Text("开始${modeConfig.title}排盘") },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                expanded = true
            )
        },
        floatingActionButtonPosition = FabPosition.Center
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()) // 防止小屏幕内容溢出
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            // 1. 顶部应用标题
            Text(
                text = "Celestite",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.outline
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = modeConfig.title,
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(32.dp))
            OnboardingTooltip(
                text = "点击这里可以手动修改时间，修改后所有预备页都将使用指定时间，但你仍可在点击后选择时间时重置回当前。",
                isVisible = showTimeTip,
                onDismiss = {
                    // 1. UI 上立即消失
                    showTimeTip = false
                    // 2. 持久化保存：第 0 位设为 0
                    UserPreferences.dismissTooltip(TooltipIndices.TIME_CARD)
                }
            ) {
                // 2. 时间卡片 (核心交互)
                TimeDisplayCard(
                    time = selectedTime,
                    dateStr = selectedTime.format(dateFormatter),
                    timeStr = selectedTime.format(timeFormatter),
                    isLocked = isTimeLocked,
                    onClick = { showTimeDialog = true },
                    onReset = {
                        isTimeLocked = false
                        selectedTime = LocalDateTime.now()
                    }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 3. 帮助/介绍信息卡片
            InfoDisplayCard(modeConfig)

            Spacer(modifier = Modifier.height(16.dp))

            // 4. 静态帮助卡片
            ReadyPageHelpCard()
            // 底部留白，防止被 FAB 遮挡
            Spacer(modifier = Modifier.height(80.dp))
        }
    }

    // 时间选择弹窗 (保持原有逻辑)
    if (showTimeDialog) {
        TimeSelectionDialog(
            initialTime = selectedTime,
            onDismiss = { showTimeDialog = false },
            onConfirm = { newTime ->
                selectedTime = newTime
                isTimeLocked = true
                showTimeDialog = false
            }
        )
    }
}

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

private fun TriangleShape() = GenericShape { size, _ ->
    moveTo(size.width / 2f, 0f) // 顶点
    lineTo(size.width, size.height) // 右底角
    lineTo(0f, size.height) // 左底角
    close()
}

@Composable
private fun ReadyPageHelpCard() {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            // 使用 surfaceContainerHigh 或 Low，使其比背景稍微亮一点，但比主卡片暗
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        // 去掉阴影，使其显得更“平”，层级更低
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Info, // 或者用 HelpOutline
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.secondary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "使用说明", // 标题
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.secondary,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "为了防止每次进入都会立即排出当前时间的盘，开幕暴击，故设计此预备页面。",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 22.sp // 增加行高，提升阅读舒适度
            )
        }
    }
}

@Composable
private fun TimeDisplayCard(
    time: LocalDateTime,
    dateStr: String,
    timeStr: String,
    isLocked: Boolean,
    onClick: () -> Unit,
    onReset: () -> Unit
) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 卡片顶部状态栏
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SuggestionChip(
                    onClick = { /* 不响应 */ },
                    label = { Text(if (isLocked) "手动定盘" else "当前时间") },
                    icon = {
                        Icon(
                            if (isLocked) Icons.Default.AccessTime else Icons.Default.Refresh,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    colors = SuggestionChipDefaults.suggestionChipColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                        labelColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ),
                    border = null
                )

                if (isLocked) {
                    IconButton(onClick = onReset) {
                        Icon(Icons.Default.Refresh, contentDescription = "重置")
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 时间显示
            Text(
                text = timeStr,
                style = MaterialTheme.typography.displayLarge.copy(
                    fontSize = 68.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Medium
                ),
                color = MaterialTheme.colorScheme.primary
            )

            Text(
                text = dateStr,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 底部提示
            Text(
                text = "点击卡片修改排盘时间",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
private fun InfoDisplayCard(config: ModeConfig) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = config.icon,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = "关于${config.title}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = config.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 22.sp
                )
            }
        }
    }
}

// 帮助函数：根据索引返回对应的文案配置
private fun getModeConfig(index: Int): ModeConfig {
    return when (index) {
        0 -> ModeConfig(
            title = "奇门遁甲",
            description = "奇门遁甲是中国古代术数著作，也是奇门、六壬、太乙三大秘宝中的第一大秘术。以“天时、地利、人和、神助”为核心，构建时空模型。",
            icon = Icons.Default.AutoAwesome
        )
        1 -> ModeConfig(
            title = "大六壬",
            description = "大六壬以月将加占时，在天地盘上通过干支生克、三传四课、十二贵神等判断事物发展。是“三式”中最擅长占测人事的一门。",
            icon = Icons.Default.Water
        )
        2 -> ModeConfig(
            title = "命理占系",
            description = "命理占系在此排盘程序中同时支持紫微斗数和子平数，统称为命理占系。",
            icon = Icons.Default.Person
        )
        3 -> ModeConfig(
            title = "易经占系",
            description = "易经占系页同时提供六爻和梅花易数的术数排盘。其实还没做好hhh.",
            icon = Icons.Default.Book
        )
        else -> ModeConfig(
            title = "排盘工具",
            description = "Celestite 提供专业的排盘服务。请在上方选择时间，点击按钮开始排盘。",
            icon = Icons.Default.Info
        )
    }
}