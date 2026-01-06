package com.xiaozhao45.celestite

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.isCtrlPressed
import androidx.compose.ui.input.pointer.isShiftPressed
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.xiaozhao45.celestite.Components.InfoTag
import com.xiaozhao45.celestite.Components.PillarItem
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin


/**
 * 奇门遁甲页面
 * 修改说明：
 * 1. 增加了长按宫位或按住Shift/Ctrl点击宫位弹出 Dialog 的逻辑。
 * 2. 引入了 PalaceFunctionDialog 用于显示弹窗。
 */
@Composable
fun QiMenPage() {
    val time = ChartState.qmTime

    // 2. 状态管理：当前排盘数据
    // 【关键修改】：使用 remember(time)
    // 意思是：只要 time 变了，我就重新执行 calculateQiMenData，否则就用缓存的
    var currentData = remember(time) {
        calculateQiMenData(time)
    }

    // 状态管理：是否显示时间选择弹窗
    var showDialog by remember { mutableStateOf(false) }
    // 状态管理：当前选中的宫位索引 (0-8)，-1表示未选中
    var selectedPalaceIndex by remember { mutableStateOf(-1) }

    // 状态管理：宫位功能弹窗
    var showFunctionDialog by remember { mutableStateOf(false) }
    var functionPalaceId by remember { mutableStateOf(0) }
    var showPlcTip by remember {
        mutableStateOf(UserPreferences.shouldShowTooltip(TooltipIndices.GONG_WEI))
    }
    // 时间选择弹窗
    if (showDialog) {
        TimeSelectionDialog(
            initialTime = ChartState.qmTime,
            onDismiss = { showDialog = false },
            onConfirm = { newTime ->
                ChartState.qmTime = newTime

                // 点击确定后，重新计算排盘并更新数据
                currentData = calculateQiMenData(newTime)
                selectedPalaceIndex = -1 // 重置选中状态
                showDialog = false
            }
        )
    }

    // 宫位功能弹窗 (长按或 Shift/Ctrl+Click 触发)
    if (showFunctionDialog) {
        PalaceFunctionDialog(
            palaceId = functionPalaceId,
            data = currentData,
            onDismiss = { showFunctionDialog = false }
        )
    }
    OnboardingTooltip(
        text = "你可以点击宫位来查看其先天、后天宫位，也可以长按（按住Shift或Ctrl点击）来查看宫位内容解释。",
        isVisible = showPlcTip,
        onDismiss = {
            // 1. UI 上立即消失
            showPlcTip = false
            // 2. 持久化保存：第 0 位设为 0
            UserPreferences.dismissTooltip(TooltipIndices.GONG_WEI)
        }

    ){
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val screenWidth = maxWidth
            val isLandscape = screenWidth > 600.dp

            if (isLandscape) {
                // --- 横屏布局 ---
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .weight(0.4f)
                            .fillMaxHeight()
                            .verticalScroll(rememberScrollState())
                    ) {
                        QiMenInfoPanel(
                            data = currentData,
                            onNewClick = { showDialog = true }
                        )
                    }

                    BoxWithConstraints(
                        modifier = Modifier
                            .weight(0.6f)
                            .fillMaxHeight(),
                        contentAlignment = Alignment.Center
                    ) {
                        val gridSize = androidx.compose.ui.unit.min(maxWidth, maxHeight)

                        ScalableContent(scale = UserPreferences.qimenUIScale) {
                            QiMenGrid(
                                palaces = currentData.palaces,
                                selectedIdx = selectedPalaceIndex,
                                onPalaceClick = { clickedIndex ->
                                    // 1. 播放声音
                                    if (UserPreferences.qimenPalaceSound) { playPalaceSound(clickedIndex) }
                                    // 2. 切换选中状态
                                    selectedPalaceIndex = if (selectedPalaceIndex == clickedIndex) -1 else clickedIndex
                                },
                                onPalaceLongClick = { clickedIndex ->
                                    // 触发功能弹窗
                                    functionPalaceId = clickedIndex
                                    showFunctionDialog = true
                                },
                                modifier = Modifier.size(gridSize)
                            )
                        }


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
                    QiMenInfoPanel(
                        data = currentData,
                        onNewClick = { showDialog = true }
                    )
                    ScalableContent(scale = UserPreferences.qimenUIScale) {

                        QiMenGrid(
                            palaces = currentData.palaces,
                            selectedIdx = selectedPalaceIndex,
                            onPalaceClick = { clickedIndex ->
                                // 1. 播放声音
                                if (UserPreferences.qimenPalaceSound) { playPalaceSound(clickedIndex) }
                                // 2. 切换选中状态
                                selectedPalaceIndex = if (selectedPalaceIndex == clickedIndex) -1 else clickedIndex
                            },
                            onPalaceLongClick = { clickedIndex ->
                                // 触发功能弹窗
                                functionPalaceId = clickedIndex
                                showFunctionDialog = true
                            },
                            modifier = Modifier.fillMaxWidth()
                        )

                    }

                }
            }
        }
    }

}

/**
 * 宫位功能弹窗
 * 传入当前宫位的ID，无独立关闭按钮，可按Esc或点击外部关闭。
 */
@Composable
fun PalaceFunctionDialog(
    palaceId: Int,
    data: QiMenData,
    onDismiss: () -> Unit
) {
    val id = palaceId + 1

    Dialog(onDismissRequest = onDismiss) {

        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp,
            modifier = Modifier.padding(16.dp)
        ) {
            Box(
                modifier = Modifier.padding(24.dp),
                contentAlignment = Alignment.Center,

            ) {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    GongIntro(id)
                    if (data.palaces[palaceId].isHorse) {
                        HorseStarText()
                    }
                    if (data.palaces[palaceId].isVoid) {
                        VoidText()
                    }
                }
            }
        }
    }
}

/**
 * 九宫格盘面
 * 增加了 Canvas 绘制连接线和点击逻辑，以及长按/特殊点击逻辑
 */
@Suppress("UnusedBoxWithConstraintsScope")
@Composable
fun QiMenGrid(
    palaces: List<PalaceData>,
    selectedIdx: Int,
    onPalaceClick: (Int) -> Unit,
    onPalaceLongClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    // 确保数据不为空
    if (palaces.size < 9) return

    // MD3 样式变量
    val outerCornerRadius = 16.dp
    val containerColor = MaterialTheme.colorScheme.surfaceContainer
    val dividerColor = MaterialTheme.colorScheme.outlineVariant
    val highlightColor = MaterialTheme.colorScheme.primary

    // 动画：虚线流动相位
    val infiniteTransition = rememberInfiniteTransition(label = "DashAnimation")
    val dashPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 40f, // 2 * (dashOn + dashOff)
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "DashPhase"
    )

    // 计算关联宫位 (先天 -> 当前 -> 后天)
    val relatedIndices = remember(selectedIdx) {
        if (selectedIdx == -1) Triple(-1, -1, -1)
        else getRelatedPalaceIndices(selectedIdx)
    }

    BoxWithConstraints(
        modifier = modifier
            .aspectRatio(1f)
            .background(dividerColor, RoundedCornerShape(outerCornerRadius))
            .padding(2.dp) // 模拟分割线宽
    ) {
        // 绘制底层宫位
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            // 第一行: 巽4(idx 3), 离9(idx 8), 坤2(idx 1)
            Row(modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                GridCellWrapper(palaces[3], 3, selectedIdx, relatedIndices, onPalaceClick, onPalaceLongClick, RoundedCornerShape(topStart = outerCornerRadius))
                GridCellWrapper(palaces[8], 8, selectedIdx, relatedIndices, onPalaceClick, onPalaceLongClick, RectangleShape)
                GridCellWrapper(palaces[1], 1, selectedIdx, relatedIndices, onPalaceClick, onPalaceLongClick, RoundedCornerShape(topEnd = outerCornerRadius))
            }
            // 第二行: 震3(idx 2), 中5(idx 4), 兑7(idx 6)
            Row(modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                GridCellWrapper(palaces[2], 2, selectedIdx, relatedIndices, onPalaceClick, onPalaceLongClick, RectangleShape)
                GridCellWrapper(palaces[4], 4, selectedIdx, relatedIndices, onPalaceClick, onPalaceLongClick, RectangleShape)
                GridCellWrapper(palaces[6], 6, selectedIdx, relatedIndices, onPalaceClick, onPalaceLongClick, RectangleShape)
            }
            // 第三行: 艮8(idx 7), 坎1(idx 0), 乾6(idx 5)
            Row(modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                GridCellWrapper(palaces[7], 7, selectedIdx, relatedIndices, onPalaceClick, onPalaceLongClick, RoundedCornerShape(bottomStart = outerCornerRadius))
                GridCellWrapper(palaces[0], 0, selectedIdx, relatedIndices, onPalaceClick, onPalaceLongClick, RectangleShape)
                GridCellWrapper(palaces[5], 5, selectedIdx, relatedIndices, onPalaceClick, onPalaceLongClick, RoundedCornerShape(bottomEnd = outerCornerRadius))
            }
        }

        // 顶层：绘制连接线
        // 只有当选中时才绘制
        if (selectedIdx != -1 && UserPreferences.qimenHighlight) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val cellW = size.width / 3
                val cellH = size.height / 3

                // 辅助函数：获取指定 Index 的中心坐标
                fun getCenterOfIndex(idx: Int): Offset {
                    val (row, col) = when (idx) {
                        3 -> 0 to 0
                        8 -> 0 to 1
                        1 -> 0 to 2
                        2 -> 1 to 0
                        4 -> 1 to 1
                        6 -> 1 to 2
                        7 -> 2 to 0
                        0 -> 2 to 1
                        5 -> 2 to 2
                        else -> 1 to 1 // 默认中心
                    }
                    return Offset(
                        x = col * cellW + cellW / 2,
                        y = row * cellH + cellH / 2
                    )
                }

                val (pre, curr, post) = relatedIndices

                // 绘制带箭头的虚线
                fun drawArrowLine(start: Offset, end: Offset) {
                    if (start == end) return // 相同点不绘制

                    // 计算中点
                    val midPoint = Offset(
                        x = (start.x + end.x) / 2,
                        y = (start.y + end.y) / 2
                    )

                    // 1. 绘制第一段虚线（起点到中点）
                    drawPath(
                        path = Path().apply {
                            moveTo(start.x, start.y)
                            lineTo(midPoint.x, midPoint.y)
                        },
                        color = highlightColor,
                        style = Stroke(
                            width = 3.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(20f, 20f), dashPhase)
                        )
                    )

                    // 2. 绘制箭头（在中点位置）
                    val angle = atan2(end.y - start.y, end.x - start.x)
                    val arrowSize = 15.dp.toPx()

                    val arrowPath = Path().apply {
                        moveTo(midPoint.x, midPoint.y)
                        lineTo(
                            midPoint.x - arrowSize * cos(angle - Math.PI / 6).toFloat(),
                            midPoint.y - arrowSize * sin(angle - Math.PI / 6).toFloat()
                        )
                        lineTo(
                            midPoint.x - arrowSize * cos(angle + Math.PI / 6).toFloat(),
                            midPoint.y - arrowSize * sin(angle + Math.PI / 6).toFloat()
                        )
                        close()
                    }
                    drawPath(arrowPath, highlightColor)

                    // 3. 绘制第二段虚线（中点到终点）
                    drawPath(
                        path = Path().apply {
                            moveTo(midPoint.x, midPoint.y)
                            lineTo(end.x, end.y)
                        },
                        color = highlightColor,
                        style = Stroke(
                            width = 3.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(20f, 20f), dashPhase)
                        )
                    )
                }

                val p1 = getCenterOfIndex(pre)
                val p2 = getCenterOfIndex(curr)
                val p3 = getCenterOfIndex(post)

                // 路径：先天 -> 当前
                drawArrowLine(p2, p1)
                // 路径：当前 -> 后天
                drawArrowLine(p3, p2)
            }
        }
    }
}

/**
 * 封装单元格，处理点击、长按、组合键和高亮逻辑
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun RowScope.GridCellWrapper(
    data: PalaceData,
    index: Int,
    selectedIdx: Int,
    relatedIndices: Triple<Int, Int, Int>,
    onClick: (Int) -> Unit,
    onLongClick: (Int) -> Unit,
    shape: androidx.compose.ui.graphics.Shape
) {
    // 判断是否高亮或变暗
    val isHighlighted = selectedIdx != -1 &&
            (index == selectedIdx || index == relatedIndices.first || index == relatedIndices.third) && UserPreferences.qimenHighlight
    val isDimmed = selectedIdx != -1 && !isHighlighted && UserPreferences.qimenHighlight

    // 用于追踪 Shift/Ctrl 键状态的变量
    var isModifierPressed by remember { mutableStateOf(false) }

    QiMenCell(
        data = data,
        modifier = Modifier
            .weight(1f)
            // 使用 pointerInput 监听键盘修饰符 (Shift/Ctrl) 的状态
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent(PointerEventPass.Initial)
                        val modifiers = event.keyboardModifiers
                        // 更新状态：如果按下了 Shift 或 Ctrl
                        isModifierPressed = modifiers.isShiftPressed || modifiers.isCtrlPressed
                    }
                }
            }
            // 使用 combinedClickable 处理长按和点击
            .combinedClickable(
                onLongClick = { onLongClick(index) }, // 长按直接触发 Dialog
                onClick = {
                    if (isModifierPressed) {
                        // 如果按住了修饰键点击，视为特殊点击，触发 Dialog
                        onLongClick(index)
                    } else {
                        // 普通点击
                        onClick(index)
                    }
                }
            )
            .alpha(if (isDimmed) 0.38f else 1f), // MD3 推荐禁用/变暗态 Alpha 为 0.38
        shape = shape,
        isHighlighted = isHighlighted
    )
}


/**
 * 单个宫位单元格
 */
@Composable
fun QiMenCell(
    data: PalaceData,
    modifier: Modifier = Modifier,
    shape: androidx.compose.ui.graphics.Shape,
    isHighlighted: Boolean = false
) {
    // 根据高亮状态决定颜色
    val backgroundColor = if (isHighlighted) {
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
    } else {
        MaterialTheme.colorScheme.surface
    }

    val contentColor = MaterialTheme.colorScheme.onSurface

    // 边框逻辑
    val borderStroke = if (isHighlighted) {
        BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
    } else {
        null
    }

    Surface(
        modifier = modifier
            .fillMaxSize()
            .clip(shape),
        color = backgroundColor,
        contentColor = contentColor,
        border = borderStroke
    ) {
        if (data.isCenter) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = UserPreferences.qimenMiddlePalaceText,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(4.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // 1. 顶部行：马星 - 八神 - 空亡
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.size(12.dp)) {
                        if (data.isHorse) HorseStarIcon(Modifier.fillMaxSize())
                    }
                    Text(
                        text = data.god,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Box(modifier = Modifier.size(12.dp)) {
                        if (data.isVoid) VoidIcon(Modifier.fillMaxSize())
                    }
                }

                // 2. 中间行：天盘层
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 左侧：天盘寄干
                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        if (data.hiddenHeavenStem != null) {
                            Text(
                                text = data.hiddenHeavenStem,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }
                    // 中间：九星 + 旺衰
                    Column(
                        modifier = Modifier.weight(1.5f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = data.star,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = if (isHighlighted) FontWeight.ExtraBold else FontWeight.Medium,
                            color = if (isHighlighted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = data.starStatus,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                    // 右侧：天盘干 + 长生
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = data.heavenStem,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = data.heavenStage,
                            style = MaterialTheme.typography.labelSmall,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                // 3. 底部行：地盘层
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 左侧：地盘寄干
                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        if (data.hiddenEarthStem != null) {
                            Text(
                                text = data.hiddenEarthStem,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }
                    // 中间：八门
                    Box(
                        modifier = Modifier.weight(1.5f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = data.door,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    // 右侧：地盘干 + 长生
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = data.earthStem,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = data.earthStage,
                            style = MaterialTheme.typography.labelSmall,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}


/**
 * 计算宫位关联逻辑 (先天 -> 当前 -> 后天)
 * 逻辑：
 * 1. 找到当前宫位 (Post-Heaven Position)
 * 2. Pre-Heaven: 当前宫位的八卦在先天八卦图中的位置。
 * 3. Post-Heaven (Next): 当前宫位(作为先天位置)对应的卦移动到的后天位置。
 *    (即：Source -> Current -> Displaced)
 */
fun getRelatedPalaceIndices(currentIndex: Int): Triple<Int, Int, Int> {
    // 索引映射:
    // 0=坎, 1=坤, 2=震, 3=巽, 4=中, 5=乾, 6=兑, 7=艮, 8=离
    return when (currentIndex) {
        0 -> Triple(6, 0, 1) // 坎(0)来自西(6), 坎(先天)变为坤(1)
        1 -> Triple(0, 1, 3) // 坤(1)来自北(0), 坤(先天)变为巽(3)
        2 -> Triple(7, 2, 8) // 震(2)来自东北(7), 震(先天)变为离(8)
        3 -> Triple(1, 3, 6) // 巽(3)来自西南(1), 巽(先天)变为兑(6)
        4 -> Triple(4, 4, 4) // 中宫
        5 -> Triple(8, 5, 7) // 乾(5)来自南(8), 乾(先天)变为艮(7)
        6 -> Triple(3, 6, 0) // 兑(6)来自东南(3), 兑(先天)变为坎(0)
        7 -> Triple(5, 7, 2) // 艮(7)来自西北(5), 艮(先天)变为震(2)
        8 -> Triple(2, 8, 5) // 离(8)来自东(2), 离(先天)变为乾(5)
        else -> Triple(currentIndex, currentIndex, currentIndex)
    }
}

/**
 * 奇门遁甲信息面板
 * 风格统一化：参照 SiZhuInfoCard 的紧凑 M3 风格
 */
@Composable
fun QiMenInfoPanel(data: QiMenData, onNewClick: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clip(CardDefaults.shape)
            .clickable(onClick = onNewClick) // 保持原有点击逻辑
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp) // 调小间距更紧凑
        ) {
            // --- 头部：时间与局数 ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = data.timeString,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${data.lunarDate} • ${data.solarTerm}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }

                // 仿照 Edit 按钮样式的局数徽章
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(50)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 使用 Icon 增加视觉丰富度 (可选，这里用 AutoMode 或类似的图标)
                        Icon(
                            imageVector = Icons.Default.Info, // 或者是您项目中的 YinYang 图标
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = data.juShu,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            // --- 中部：四柱干支 ---
            // 移除原本的 Surface 包装，直接展示以保持一致性
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // 使用统一的 PillarItem 组件 (下方定义)
                PillarItem(label = "年柱", value = data.yearPillar, modifier = Modifier.weight(1f))
                PillarItem(label = "月柱", value = data.monthPillar, modifier = Modifier.weight(1f))
                PillarItem(label = "日柱", value = data.dayPillar, modifier = Modifier.weight(1f))
                PillarItem(label = "时柱", value = data.hourPillar, modifier = Modifier.weight(1f))
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            // --- 底部：核心参数 (值符、值使等) ---
            // 改为紧凑的两行布局，类似 SiZhuInfoCard 的底部 InfoTag
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    InfoTag(label = "值符", value = data.valueStar, modifier = Modifier.weight(1f))
                    InfoTag(label = "值使", value = data.valueDoor, modifier = Modifier.weight(1f))
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    InfoTag(label = "月将", value = data.monthGeneral, modifier = Modifier.weight(1f))
                    InfoTag(label = "月令", value = data.monthCommand, modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

