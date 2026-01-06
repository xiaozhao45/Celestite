package com.xiaozhao45.celestite

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.tab.CurrentTab
import cafe.adriel.voyager.navigator.tab.LocalTabNavigator
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabNavigator
import java.time.LocalDateTime
import kotlin.math.roundToInt

val LocalThemeConfig = compositionLocalOf { ThemeConfigActions() }
data class ThemeConfigActions(
    val refresh: () -> Unit = {}
)



@Composable
fun App() {
    var currentSeedColorInt by remember { mutableStateOf(UserPreferences.customThemeColor) }
    var useDynamicColor by remember { mutableStateOf(UserPreferences.useDynamicColor) }

    // 定义刷新函数：当设置页修改了偏好后，调用这个函数，重新读取 UserPreferences 到 State 中
    val refreshTheme = {
        currentSeedColorInt = UserPreferences.customThemeColor
        useDynamicColor = UserPreferences.useDynamicColor
    }

    val currentMode = ThemeController.themeMode

    // 2. 计算最终是否显示深色
    // isSystemInDarkTheme() 是 Compose 自带的方法，能自动响应系统设置变化
    val isDark = when (currentMode) {
        ThemeController.ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeController.ThemeMode.DARK -> true
        ThemeController.ThemeMode.LIGHT -> false
    }
    AppTheme(
        darkTheme = isDark,
        seedColor = Color(currentSeedColorInt),
        useDynamicColor = useDynamicColor
    )  {
        CompositionLocalProvider(
            LocalThemeConfig provides ThemeConfigActions(refresh = refreshTheme)
        ) {
            TabNavigator(QiMenTab) {
                // 1. 【关键修复】将 content 的定义移到 BoxWithConstraints 外面
                // 这样无论窗口怎么变，这个 content 实例永远是同一个，状态永远不会丢
                val tabContent = remember {
                    movableContentOf { paddingValues: PaddingValues ->
                        Box(modifier = Modifier.padding(paddingValues)) {
                            CurrentTab()
                        }
                    }
                }

                BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                    val isWideScreen = maxWidth >= 600.dp

                    // 2. 【架构优化】采用统一的布局结构
                    // 根布局永远是 Row，内容永远包裹在 Scaffold 里
                    // 这样切换大小时，组件树不会发生剧烈的“拆卸-重建”，只是布局参数变化
                    Row(modifier = Modifier.fillMaxSize()) {

                        // A. 左侧：如果在大屏，显示 Rail；小屏则隐藏 (不占用空间)
                        if (isWideScreen) {
                            NavigationRail {
                                Spacer(Modifier.weight(1f))
                                TabNavigationRailItem(QiMenTab)
                                TabNavigationRailItem(LiuRenTab)
                                TabNavigationRailItem(MingLiTab)
                                TabNavigationRailItem(YiJingTab)
                                TabNavigationRailItem(SettingsTab)
                                Spacer(Modifier.weight(1f))
                            }
                        }

                        // B. 主体：永远是一个 Scaffold
                        Scaffold(
                            containerColor = MaterialTheme.colorScheme.background,
                            // C. 底部：如果在小屏，显示 BottomBar；大屏则为 null
                            bottomBar = {
                                if (!isWideScreen) {
                                    NavigationBar {
                                        TabNavigationItem(QiMenTab)
                                        TabNavigationItem(LiuRenTab)
                                        TabNavigationItem(MingLiTab)
                                        TabNavigationItem(YiJingTab)
                                        TabNavigationItem(SettingsTab)
                                    }
                                }
                            }
                        ) { padding ->
                            // 3. 调用上面定义的可移动内容
                            tabContent(padding)
                        }
                    }
                }
            }
        }

    }
}

// =================================================================
// 导航辅助组件
// =================================================================

/**
 * 底部导航栏单项 (Bottom Navigation Item)
 */
@Composable
private fun RowScope.TabNavigationItem(tab: Tab) {
    val tabNavigator = LocalTabNavigator.current

    NavigationBarItem(
        selected = tabNavigator.current == tab,
        onClick = { tabNavigator.current = tab },
        icon = { Icon(painter = tab.options.icon!!, contentDescription = tab.options.title) },
        label = { Text(tab.options.title) }
    )
}

/**
 * 侧边导航栏单项 (Navigation Rail Item)
 */
@Composable
private fun TabNavigationRailItem(tab: Tab) {
    val tabNavigator = LocalTabNavigator.current

    NavigationRailItem(
        selected = tabNavigator.current == tab,
        onClick = { tabNavigator.current = tab },
        icon = { Icon(painter = tab.options.icon!!, contentDescription = tab.options.title) },
        label = { Text(tab.options.title) }
    )
}

// =================================================================
// 工具组件 (Utilities) - 保持原样
// =================================================================

/**
 * 时间选择弹窗
 */
@Composable
fun TimeSelectionDialog(
    initialTime: LocalDateTime,
    onDismiss: () -> Unit,
    onConfirm: (LocalDateTime) -> Unit
) {
    var year by remember { mutableStateOf(initialTime.year.toString()) }
    var month by remember { mutableStateOf(initialTime.monthValue.toString()) }
    var day by remember { mutableStateOf(initialTime.dayOfMonth.toString()) }
    var hour by remember { mutableStateOf(initialTime.hour.toString()) }
    var minute by remember { mutableStateOf(initialTime.minute.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("新建排盘 - 输入时间") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                // 年月日
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = year,
                        onValueChange = { if (it.all { c -> c.isDigit() }) year = it },
                        label = { Text("年") },
                        modifier = Modifier.weight(1.5f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = month,
                        onValueChange = { if (it.all { c -> c.isDigit() }) month = it },
                        label = { Text("月") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = day,
                        onValueChange = { if (it.all { c -> c.isDigit() }) day = it },
                        label = { Text("日") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                }
                // 时分
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = hour,
                        onValueChange = { if (it.all { c -> c.isDigit() }) hour = it },
                        label = { Text("时") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = minute,
                        onValueChange = { if (it.all { c -> c.isDigit() }) minute = it },
                        label = { Text("分") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    try {
                        val newTime = LocalDateTime.of(
                            year.toInt(), month.toInt(), day.toInt(),
                            hour.toInt(), minute.toInt()
                        )
                        onConfirm(newTime)
                    } catch (e: Exception) {
                        // 简单处理错误
                    }
                }
            ) {
                Text("确定排盘")
            }
        },
        dismissButton = {
            Row {
                TextButton(
                    onClick = {
                        val now = LocalDateTime.now()
                        year = now.year.toString()
                        month = now.monthValue.toString()
                        day = now.dayOfMonth.toString()
                        hour = now.hour.toString()
                        minute = now.minute.toString()
                    }
                ) {
                    Text("此刻")
                }

                TextButton(onClick = onDismiss) {
                    Text("取消")
                }
            }
        }
    )
}

/**
 * 全局缩放包装器
 */
@Composable
fun ScalableContent(
    scale: Float,
    content: @Composable () -> Unit
) {
    val currentDensity = LocalDensity.current
    val customDensity = remember(currentDensity, scale) {
        Density(
            density = currentDensity.density * scale,
            fontScale = currentDensity.fontScale * scale
        )
    }

    CompositionLocalProvider(
        LocalDensity provides customDensity
    ) {
        content()
    }
}


// =================================================================
// 声音与频率工具
// =================================================================

fun playPalaceSound(index: Int) {
    if (!UserPreferences.qimenPalaceSound) return
    val scale = UserPreferences.qimenScale
    if (scale == QimenScale.Mute) return

    val frequency = when (scale) {
        QimenScale.ChineseTraditional -> getChinesePentatonicFrequency(index)
        QimenScale.CMajor, QimenScale.Default -> getCMajorFrequency(index)
        else -> 0.0
    }

    if (frequency > 0.0) {
        playTone(frequency)
    }
}

private fun getChinesePentatonicFrequency(index: Int): Double {
    return when (index) {
        4 -> 261.63 // 中宫 (C4)
        0 -> 220.00 // 坎宫 (A3)
        7 -> 293.66 // 艮宫 (D4)
        2 -> 329.63 // 震宫 (E4)
        3 -> 392.00 // 巽宫 (G4)
        8 -> 659.25 // 离宫 (E5)
        1 -> 440.00 // 坤宫 (A4)
        6 -> 523.25 // 兑宫 (C5)
        5 -> 587.33 // 乾宫 (D5)
        else -> 0.0
    }
}

private fun getCMajorFrequency(index: Int): Double {
    return when (index) {
        0 -> 261.63 // 坎 (C4)
        7 -> 293.66 // 艮 (D4)
        2 -> 329.63 // 震 (E4)
        3 -> 349.23 // 巽 (F4)
        8 -> 392.00 // 离 (G4)
        1 -> 440.00 // 坤 (A4)
        6 -> 493.88 // 兑 (B4)
        5 -> 523.25 // 乾 (C5)
        4 -> 220.00 // 中 (A3)
        else -> 0.0
    }
}

expect fun playTone(frequency: Double, durationSeconds: Double = 0.8)

// =================================================================
// UI 装饰组件
// =================================================================

@Composable
fun PillarItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
        Spacer(modifier = Modifier.height(6.dp))
        Text(text = value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun BigInfoItem(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.outline)
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
fun HorseStarIcon(modifier: Modifier = Modifier, color: Color = MaterialTheme.colorScheme.onSurface) {
    Canvas(modifier = modifier) {
        val path = Path().apply {
            moveTo(size.width / 2, 0f)
            lineTo(size.width, size.height / 2)
            lineTo(size.width / 2, size.height)
            lineTo(0f, size.height / 2)
            close()
        }
        drawPath(path, color)
    }
}

@Composable
fun VoidIcon(modifier: Modifier = Modifier, color: Color = MaterialTheme.colorScheme.onSurface) {
    Canvas(modifier = modifier) {
        drawCircle(color = color, style = Stroke(width = 1.5.dp.toPx()))
    }
}

// =================================================================
// 其他页面占位
// =================================================================

@Composable
fun MingLiPage() {
    Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Icon(Icons.Filled.Person, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(16.dp))
        Text("命理占系 页面", style = MaterialTheme.typography.headlineMedium)
    }
}

@Composable
fun YiJingPage() {
    Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Icon(Icons.Filled.Book, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(16.dp))
        Text("易经占系 页面", style = MaterialTheme.typography.headlineMedium)
    }
}