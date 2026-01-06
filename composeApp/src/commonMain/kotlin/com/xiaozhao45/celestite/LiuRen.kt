package com.xiaozhao45.celestite

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import xuan.core.daliuren.DaLiuRen
import xuan.core.daliuren.settings.DaLiuRenJiChuSetting
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


// --- 数据类定义 ---
data class GridCellData(
    val index: Int, // 地盘索引
    val tianPan: String,
    val tianGan: String,
    val shen: String,
    val diPan: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LiuRenPage() {
    // 1. 获取当前全局时间 (Compose 会在这里自动订阅更新)
    // 建议用 val，因为我们不修改这个局部变量，而是去修改 ChartState
    val time = ChartState.lrTime

    var showTimeDialog by remember { mutableStateOf(false) }

    // 初始化核心逻辑
    // 当 ChartState.lrTime 变化时，上面的 time 也会变，这里的 remember 就会重新计算
    val daLiuRen = remember(time) {
        val setting = DaLiuRenJiChuSetting().apply {
            date = time.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
            dateType = 0 // 公历
        }
        DaLiuRen(setting)
    }


    // 时间选择弹窗逻辑
    if (showTimeDialog) {
        TimeSelectionDialog(
            initialTime = time,
            onDismiss = { showTimeDialog = false },
            onConfirm = { newTime ->
                // 【关键修复】
                // 必须修改全局单例 ChartState 里的变量，而不是修改本地变量
                ChartState.lrTime = newTime

                showTimeDialog = false
            }
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
    ) { paddingValues ->

        BoxWithConstraints(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            val isLandscape = maxWidth > maxHeight

            if (isLandscape) {
                // --- 横屏布局 ---
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(24.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 左侧：信息面板
                    Column(
                        modifier = Modifier
                            .weight(0.4f)
                            .fillMaxHeight()
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        SiZhuInfoCard(daLiuRen, onClick = { showTimeDialog = true })
                        BottomInfoSection(daLiuRen)
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    // 右侧：核心式盘
                    Box(
                        modifier = Modifier
                            .weight(0.6f)
                            .fillMaxWidth()

                        ,
                        contentAlignment = Alignment.Center
                    ) {
                        ScalableContent(
                            UserPreferences.liurenUIScale,
                            { LiurenDiscLayout(daLiuRen, modifier = Modifier.fillMaxWidth()) }
                        )
                    }
                }
            } else {
                // --- 竖屏布局 ---
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    SiZhuInfoCard(daLiuRen, onClick = { showTimeDialog = true })
                    ScalableContent(
                        UserPreferences.liurenUIScale,
                        { LiurenDiscLayout(daLiuRen, modifier = Modifier.fillMaxWidth()) }
                    )

                    BottomInfoSection(daLiuRen)
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }


}

/**
 * 四柱八字展示区
 */
@Composable
fun SiZhuInfoCard(data: DaLiuRen, onClick: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        modifier = Modifier
            .fillMaxWidth()
            .clip(CardDefaults.shape)
            .clickable(onClick = onClick)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            // 头部时间
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "公历 ${data.solarStr}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "农历 ${data.lunarStr}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }

                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(50),
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "修改时间",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(modifier = Modifier.height(16.dp))

            // 四柱
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                SiZhuColumn("年柱", data.yearGanZhi, data.yearGanZhiKongWang, Modifier.weight(1f))
                SiZhuColumn("月柱", data.monthGanZhi, data.monthGanZhiKongWang, Modifier.weight(1f))
                SiZhuColumn("日柱", data.dayGanZhi, data.dayGanZhiKongWang, Modifier.weight(1f))
                SiZhuColumn("时柱", data.hourGanZhi, data.hourGanZhiKongWang, Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(modifier = Modifier.height(16.dp))

            // 底部标签
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                InfoTag("月将", "${data.yueJiang} ${data.yueJiangShen}")
                InfoTag("占时", "${data.hourZhi}时")
                InfoTag("旬空", data.dayGanZhiKongWang)
            }
        }
    }
}

@Composable
fun SiZhuColumn(label: String, ganZhi: String, kongWang: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.secondary
        )
        Spacer(modifier = Modifier.height(4.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = ganZhi,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (ganZhi.length > 1 && kongWang.contains(ganZhi.substring(1))) {
                Spacer(modifier = Modifier.width(4.dp))
                VoidIcon(
                    modifier = Modifier.size(8.dp),
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
fun InfoTag(label: String, value: String) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        shape = RoundedCornerShape(8.dp),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.tertiary
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

/**
 * 核心式盘布局
 *
 * 修正了索引逻辑以匹配 SHI_ER_GONG_DI_ZHI = [寅, 卯, 辰, 巳, 午, 未, 申, 酉, 戌, 亥, 子, 丑]
 * 0=寅, 1=卯, 2=辰, 3=巳, 4=午, 5=未, 6=申, 7=酉, 8=戌, 9=亥, 10=子, 11=丑
 */
@Composable
fun LiurenDiscLayout(data: DaLiuRen, modifier: Modifier = Modifier) {
    val spacing = 4.dp

    // ==========================================
    // 配置区域
    // ==========================================
    // 中间宫位（卯辰酉戌）的拉伸倍率
    // 1.0f = 正方形 (默认)
    // 1.3f = 高度是宽度的 1.3 倍 (推荐)
    // 1.5f = 更修长
    val middleElongation = UserPreferences.liurenHighScale

    // 计算总权重和容器比例
    // 上下行权重各为 1 (对应高度1)，中间行权重为 2 * 倍率 (对应高度 2*X)
    val topBottomWeight = 1f
    val middleRowWeight = middleElongation * 2

    // 总高度份数 = 1 + (X*2) + 1
    val totalHeightUnits = topBottomWeight + middleRowWeight + topBottomWeight

    // 动态计算宽高比：宽度固定4份 / 高度动态份数
    // 这样能保证上下行恰好是正方形
    val discAspectRatio = 4f / totalHeightUnits

    fun getCell(idx: Int): GridCellData {
        return GridCellData(
            index = idx,
            tianPan = data.tianPan[idx],
            tianGan = data.tianGan[idx],
            shen = data.shenPan[idx],
            diPan = data.diPan[idx]
        )
    }

    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(24.dp),
        tonalElevation = 4.dp,
        // 【关键修改】：不再是固定的 1f，而是根据拉伸倍率计算出的比例
        modifier = modifier.aspectRatio(discAspectRatio)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(6.dp),
            verticalArrangement = Arrangement.spacedBy(spacing)
        ) {
            // --- 第一行 (南) ---
            // 权重设为 1，代表标准正方形高度
            Row(
                modifier = Modifier.weight(topBottomWeight),
                horizontalArrangement = Arrangement.spacedBy(spacing)
            ) {
                listOf(3, 4, 5, 6).forEach { idx ->
                    GongWeiCell(getCell(idx), data.dayGan, Modifier.weight(1f))
                }
            }

            // --- 中间行 ---
            // 权重设为 2 * 倍率，拉伸高度
            Row(
                modifier = Modifier.weight(middleRowWeight),
                horizontalArrangement = Arrangement.spacedBy(spacing)
            ) {
                // 左侧 (东)
                Column(
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                    verticalArrangement = Arrangement.spacedBy(spacing)
                ) {
                    GongWeiCell(getCell(2), data.dayGan, Modifier.weight(1f))
                    GongWeiCell(getCell(1), data.dayGan, Modifier.weight(1f))
                }

                // 中间大区域
                Surface(
                    modifier = Modifier.weight(2f).fillMaxHeight(),
                    color = MaterialTheme.colorScheme.surfaceContainer,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    CenterPanel(data)
                }

                // 右侧 (西)
                Column(
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                    verticalArrangement = Arrangement.spacedBy(spacing)
                ) {
                    GongWeiCell(getCell(7), data.dayGan, Modifier.weight(1f))
                    GongWeiCell(getCell(8), data.dayGan, Modifier.weight(1f))
                }
            }

            // --- 底部行 (北) ---
            // 权重设为 1，保持正方形
            Row(
                modifier = Modifier.weight(topBottomWeight),
                horizontalArrangement = Arrangement.spacedBy(spacing)
            ) {
                listOf(0, 11, 10, 9).forEach { idx ->
                    GongWeiCell(getCell(idx), data.dayGan, Modifier.weight(1f))
                }
            }
        }
    }
}
// ==========================================
// 1. 定义音阶映射表 (放在类外面或 companion object 中)
// ==========================================

// 【中国传统调式】 (五声：宫商角徵羽 - C D E G A)
// 特点：去掉了 F(Fa) 和 B(Ti)，古风，不跑调
private val DI_ZHI_TRADITIONAL_MAP = mapOf(
    "子" to 261.63,  // C4
    "丑" to 293.66,  // D4
    "寅" to 329.63,  // E4
    "卯" to 392.00,  // G4 (跳过了 F)
    "辰" to 440.00,  // A4
    "巳" to 523.25,  // C5
    "午" to 587.33,  // D5
    "未" to 659.25,  // E5
    "申" to 783.99,  // G5
    "酉" to 880.00,  // A5
    "戌" to 1046.50, // C6
    "亥" to 1174.66  // D6
)

// 【C自然大调】 (七声：Do Re Mi Fa Sol La Ti - C D E F G A B)
// 特点：现代，包含半音关系，旋律感更强
private val DI_ZHI_C_MAJOR_MAP = mapOf(
    "子" to 261.63,  // C4 (Do)
    "丑" to 293.66,  // D4 (Re)
    "寅" to 329.63,  // E4 (Mi)
    "卯" to 349.23,  // F4 (Fa) - 以前是 G，这里补上 F
    "辰" to 392.00,  // G4 (Sol)
    "巳" to 440.00,  // A4 (La)
    "午" to 493.88,  // B4 (Ti) - 以前没有，这里补上
    "未" to 523.25,  // C5 (Do)
    "申" to 587.33,  // D5 (Re)
    "酉" to 659.25,  // E5 (Mi)
    "戌" to 698.46,  // F5 (Fa)
    "亥" to 783.99   // G5 (Sol)
)

// ==========================================
// 2. 播放函数实现
// ==========================================

/**
 * 播放六壬地支音效
 * @param branch 需要播放的地支名称 (例如 "子", "丑")
 * 注意：为了知道播哪个音，必须传入 branch 参数，或者函数内部能访问到 currentBranch 变量
 */
private fun liuRenPlayNote(branch: String) {
    // 1. 检查静音设置 (假设 UserPreferences.liurenScale 有 Mute 选项)
    val scaleMode = UserPreferences.liurenScale
    if (scaleMode == LiurenScale.Mute) {
        return
    }

    // 2. 根据设置选择对应的频率表
    val freqMap = when (scaleMode) {
        LiurenScale.ChineseTraditional -> DI_ZHI_TRADITIONAL_MAP
        LiurenScale.CMajor -> DI_ZHI_C_MAJOR_MAP
        else -> DI_ZHI_TRADITIONAL_MAP // 默认回退到传统
    }

    // 3. 查表获取频率
    val frequency = freqMap[branch] ?: 0.0

    // 4. 播放
    if (frequency > 0.0) {
        playTone(frequency)
    }
}

/**
 * 单个宫位单元格
 */
@Composable
fun GongWeiCell(
    data: GridCellData,
    dayGan: String,
    modifier: Modifier = Modifier
) {
    val liuQin = remember<String>(data.tianPan, dayGan) {
        calcLiuQinInternal(dayGan, data.tianPan)
    }

    // 控制地盘显示的状态
    var showDiPan by remember { mutableStateOf(false) }
    //var showDiPan = showDiPanControl && UserPreferences.liurenEarthBranchDisplay
    // 记录最后一次点击的时间戳，用于重置计时器
    var lastClickTime by remember { mutableStateOf(0L) }

    // 监听点击时间变化，实现6秒自动隐藏
    LaunchedEffect(lastClickTime) {
        var Delay = UserPreferences.liurenAnimationDuration * 1000L

        if (lastClickTime > 0) {
            showDiPan = true
            delay(Delay) // 等待6秒
            showDiPan = false
        }
    }

    // 使用 Surface 替代 Card 以避免 ColumnScope 干扰 AnimatedVisibility 的解析
    Surface(
        modifier = modifier
            .fillMaxSize()
            .clickable {
                // 1. 播放音符
                liuRenPlayNote(data.diPan)

                // 2. 更新点击时间，触发 LaunchedEffect 重置计时
                lastClickTime = System.currentTimeMillis()
            },
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        shape = RoundedCornerShape(8.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize().padding(6.dp)) {
            Text(
                text = data.tianGan,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.align(Alignment.TopStart)
            )
            Text(
                text = data.shen,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.align(Alignment.TopEnd)
            )
            Text(
                text = liuQin,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.align(Alignment.BottomStart)
            )
            Text(
                text = data.tianPan,
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.align(Alignment.BottomEnd)
            )

            // 地盘文字，带有淡入淡出动画
            // 由于外层是 Surface (非 ColumnScope)，这里会正确调用顶层的 AnimatedVisibility
            AnimatedVisibility(
                visible = showDiPan && UserPreferences.liurenEarthBranchDisplay,

                // 修复 1: 使用 EnterTransition.None 代表无动画
                enter = if (UserPreferences.liurenFadeEffect) {
                    fadeIn()
                } else {
                    EnterTransition.None
                },

                // 修复 2: 使用 ExitTransition.None 代表无动画
                exit = if (UserPreferences.liurenFadeEffect) {
                    fadeOut()
                } else {
                    ExitTransition.None
                },

                modifier = Modifier.align(Alignment.Center)
            ) {
                Text(
                    text = data.diPan,
                    style = MaterialTheme.typography.displayMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f), // 稍微提高一点不透明度以便看见
                )
            }
        }
    }
}

/**
 * 中间区域：三传与四课
 */
@Composable
fun CenterPanel(data: DaLiuRen) {
    Column(
        modifier = Modifier.fillMaxSize().padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            val labels = listOf("初传", "中传", "末传")
            for (i in 0..2) {
                SanChuanRow(
                    label = labels[i],
                    zhi = data.sanChuan[i],
                    gan = data.sanChuanDunGan[i],
                    shen = data.sanChuanShenJiang[i],
                    liuQin = data.sanChuanLiuQin[i]
                )
            }
        }

        HorizontalDivider(
            color = MaterialTheme.colorScheme.outlineVariant,
            modifier = Modifier.padding(vertical = 4.dp)
        )

        Row(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            for (i in 3 downTo 0) {
                SiKeColumn(
                    shen = data.siKeShenJiang[i][0],
                    tian = data.siKe[i][0],
                    di = data.siKe[i][1]
                )
            }
        }
    }
}

@Composable
fun SanChuanRow(label: String, zhi: String, gan: String, shen: String, liuQin: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(liuQin, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.secondary, modifier = Modifier.width(42.dp))
        Text(gan, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
        Surface(
            color = MaterialTheme.colorScheme.primaryContainer,
            shape = RoundedCornerShape(6.dp),
            modifier = Modifier.padding(horizontal = 8.dp)
        ) {
            Text(
                text = zhi,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp)
            )
        }
        Text(shen, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.tertiary)
    }
}

@Composable
fun SiKeColumn(shen: String, tian: String, di: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(shen, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.tertiary)
        Spacer(modifier = Modifier.height(2.dp))
        Text(tian, style = MaterialTheme.typography.titleLarge,  color = MaterialTheme.colorScheme.onSurface)
        Text(di, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

/**
 * 底部补充信息
 */
@Composable
fun BottomInfoSection(data: DaLiuRen) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
            Text(
                "课体信息",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                BottomInfoItem("格局", data.tianDiPanType)
                BottomInfoItem("节气", "${data.prevJie} / ${data.nextJie}")
                BottomInfoItem("取法", data.sanChuanQuFa ?: "无")
            }
        }
    }
}

@Composable
fun BottomInfoItem(label: String, value: String) {
    Column {
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.outline)
        Text(value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
    }
}

// ----------------------------------------------------------------
// 私有辅助逻辑
// ----------------------------------------------------------------

private val TIAN_GAN_WX_INTERNAL = mapOf(
    "甲" to "木", "乙" to "木", "丙" to "火", "丁" to "火",
    "戊" to "土", "己" to "土", "庚" to "金", "辛" to "金",
    "壬" to "水", "癸" to "水"
)
private val DI_ZHI_WX_INTERNAL = mapOf(
    "子" to "水", "丑" to "土", "寅" to "木", "卯" to "木",
    "辰" to "土", "巳" to "火", "午" to "火", "未" to "土",
    "申" to "金", "酉" to "金", "戌" to "土", "亥" to "水"
)

private fun calcLiuQinInternal(dayGan: String, diZhi: String): String {
    val my = TIAN_GAN_WX_INTERNAL[dayGan] ?: return ""
    val other = DI_ZHI_WX_INTERNAL[diZhi] ?: return ""

    if (my == other) return "兄弟"

    return when (my) {
        "木" -> when (other) { "火" -> "子孙"; "土" -> "妻财"; "金" -> "官鬼"; "水" -> "父母"; else -> "" }
        "火" -> when (other) { "土" -> "子孙"; "金" -> "妻财"; "水" -> "官鬼"; "木" -> "父母"; else -> "" }
        "土" -> when (other) { "金" -> "子孙"; "水" -> "妻财"; "木" -> "官鬼"; "火" -> "父母"; else -> "" }
        "金" -> when (other) { "水" -> "子孙"; "木" -> "妻财"; "火" -> "官鬼"; "土" -> "父母"; else -> "" }
        "水" -> when (other) { "木" -> "子孙"; "火" -> "妻财"; "土" -> "官鬼"; "金" -> "父母"; else -> "" }
        else -> ""
    }
}