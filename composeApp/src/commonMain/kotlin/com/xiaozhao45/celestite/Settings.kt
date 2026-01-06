package com.xiaozhao45.celestite

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.russhwolf.settings.Settings
import com.xiaozhao45.celestite.Components.AnimatedAlertDialog
import com.xiaozhao45.celestite.Components.AnimatedAlertDialogCenter
import com.xiaozhao45.celestite.Components.AnimatedDialog
import com.xiaozhao45.celestite.Components.ColorPalettePreference
import com.xiaozhao45.celestite.UserPreferences.liurenHighScale
import java.time.LocalDateTime
import kotlin.math.roundToInt
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

// ==========================================
// 1. 数据模型与枚举 (Data Models & Enums)
// ==========================================

enum class QimenScale(val label: String) {
    Default("默认"),
    ChineseTraditional("中国传统调式"),
    CMajor("C自然大调"),
    Mute("不演奏音符")
}

enum class LiurenScale(val label: String) {
    ChineseTraditional("中国传统调式"),
    CMajor("C自然大调"),
    Mute("不演奏音符")
}

enum class QimenArrangement(val label: String) {
    Hour("时家"),
    Year("年家"),
    Month("月家"),
    Day("日家")
}


// 1. Boolean 委托
fun Settings.boolean(key: String, default: Boolean): ReadWriteProperty<Any?, Boolean> {
    return object : ReadWriteProperty<Any?, Boolean> {
        override fun getValue(thisRef: Any?, property: KProperty<*>): Boolean =
            getBoolean(key, default)

        override fun setValue(thisRef: Any?, property: KProperty<*>, value: Boolean) =
            putBoolean(key, value)
    }
}

// 2. String 委托
fun Settings.string(key: String, default: String): ReadWriteProperty<Any?, String> {
    return object : ReadWriteProperty<Any?, String> {
        override fun getValue(thisRef: Any?, property: KProperty<*>): String =
            getString(key, default)

        override fun setValue(thisRef: Any?, property: KProperty<*>, value: String) =
            putString(key, value)
    }
}

// 3. Int 委托
fun Settings.int(key: String, default: Int): ReadWriteProperty<Any?, Int> {
    return object : ReadWriteProperty<Any?, Int> {
        override fun getValue(thisRef: Any?, property: KProperty<*>): Int =
            getInt(key, default)

        override fun setValue(thisRef: Any?, property: KProperty<*>, value: Int) =
            putInt(key, value)
    }
}

// 4. Enum (枚举) 委托 - 自动存取枚举的名字 (String)
inline fun <reified T : Enum<T>> Settings.enum(
    key: String,
    default: T
): ReadWriteProperty<Any?, T> {
    return object : ReadWriteProperty<Any?, T> {
        override fun getValue(thisRef: Any?, property: KProperty<*>): T {
            val savedName = getStringOrNull(key) ?: return default
            return try {
                enumValueOf<T>(savedName)
            } catch (e: Exception) {
                default // 如果枚举名字改了导致解析失败，返回默认值
            }
        }

        override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
            putString(key, value.name)
        }
    }
}


// 5. IntList 委托 - 将 List<Int> 存为 "1,0,1,1" 格式的字符串
fun Settings.intList(key: String, default: List<Int>): ReadWriteProperty<Any?, List<Int>> {
    return object : ReadWriteProperty<Any?, List<Int>> {
        override fun getValue(thisRef: Any?, property: KProperty<*>): List<Int> {
            val savedString = getStringOrNull(key)
            return if (savedString == null) {
                default
            } else {
                try {
                    if (savedString.isBlank()) emptyList()
                    else savedString.split(",").map { it.toInt() }
                } catch (e: Exception) {
                    default // 解析失败（比如数据损坏）时回退到默认值
                }
            }
        }

        override fun setValue(thisRef: Any?, property: KProperty<*>, value: List<Int>) {
            // 将 List 转为 "1,1,0" 格式
            val stringValue = value.joinToString(",")
            putString(key, stringValue)
        }
    }
}

fun Settings.float(key: String, default: Float): ReadWriteProperty<Any?, Float> {
    return object : ReadWriteProperty<Any?, Float> {
        override fun getValue(thisRef: Any?, property: KProperty<*>): Float =
            getFloat(key, default)

        override fun setValue(thisRef: Any?, property: KProperty<*>, value: Float) =
            putFloat(key, value)
    }
}

// 确保这个对象存在于你的项目中
object ChartState {
    // 奇门时间
    var qmTime: LocalDateTime by mutableStateOf(LocalDateTime.now())

    // 六壬时间 (之前你用了 lrTime)
    var lrTime: LocalDateTime by mutableStateOf(LocalDateTime.now())

    // 命理时间
    var baziTime: LocalDateTime by mutableStateOf(LocalDateTime.now())

    // 易经时间
    var yijingTime: LocalDateTime by mutableStateOf(LocalDateTime.now())
}

/**
 * 更新全局时间
 * 这里我们采取简单的策略：把所有排盘模块的时间都设为用户当前选定的这个时间。
 * 这样用户切到奇门是这个时间，切到六壬也是这个时间，体验比较连贯。
 */
fun updateGlobalTime(time: LocalDateTime) {
    ChartState.qmTime = time
    ChartState.lrTime = time
    ChartState.baziTime = time
    ChartState.yijingTime = time
}

object UserPreferences {
    private val settings: Settings = Settings() // 这里的 Settings() 是工厂函数

    // === 奇门设置 ===

    // 自动绑定到 key "qimen_middle_text"，默认值 "无门无路"
    var qimenMiddlePalaceText: String by settings.string("qimen_middle_text", "无门无路")

    var qimenPalaceSound: Boolean by settings.boolean("qimen_sound", true)

    // 枚举处理：直接存取，底层自动转 String
    var qimenScale: QimenScale by settings.enum("qimen_scale", QimenScale.Default)

    var qimenHighlight: Boolean by settings.boolean("qimen_highlight", true)
    var qimenDashedAnim: Boolean by settings.boolean("qimen_dashed", true)
    var qimenArrangement: QimenArrangement by settings.enum("qimen_arrange", QimenArrangement.Hour)
    var qimenZhiRun: Boolean by settings.boolean("qimen_zhirun", true)

    var qimenUIScale: Float by settings.float("qimen_ui_scale", 1.0f)
    // === 六壬设置 ===

    var liurenEarthBranchDisplay: Boolean by settings.boolean("liuren_branch", true)
    var liurenAnimationDuration: Int by settings.int("liuren_anim_dur", 6)
    var liurenFadeEffect: Boolean by settings.boolean("liuren_fade", true)
    var liurenScale: LiurenScale by settings.enum("liuren_scale", LiurenScale.ChineseTraditional)
    var liurenUIScale: Float by settings.float("liuren_ui_scale", 1.0f)
    var liurenHighScale: Float by settings.float("liuren_High_scale", 1.4f)

    // === 主题设置 ===
    // 默认开启动态取色 (Android 12+ Monet)
    var useDynamicColor: Boolean by settings.boolean("app_dynamic_color", true)

    // 自定义主题色 (当动态取色关闭，或在非 Android 平台时使用)
    // 默认存为 Int，这里默认用紫色
    var customThemeColor: Int by settings.int("app_theme_color", 0xFF6750A4.toInt())

    // 深色模式
    var themeMode: ThemeController.ThemeMode by settings.enum("theme_mode", ThemeController.ThemeMode.SYSTEM)

    private val defaultTooltips = List(TooltipIndices.TOTAL_COUNT) { 1 }

    // 实际存储的 List
    private var tooltipFlags: List<Int> by settings.intList("tooltip_flags_v1", defaultTooltips)
    /**
     * 对外暴露的读取方法：判断某个气泡是否应该显示
     * 逻辑：
     * 1. 检查 Index 是否越界 (兼容旧版本数据)
     * 2. 检查值是否为 1
     */
    fun shouldShowTooltip(index: Int): Boolean {
        // 如果存储的列表比当前的 index 短（说明 App 升级了有了新功能，但用户数据是旧的），
        // 默认视为 1 (显示)
        if (index >= tooltipFlags.size) return true

        return tooltipFlags[index] == 1
    }

    /**
     * 对外暴露的写入方法：关闭某个气泡
     */
    fun dismissTooltip(index: Int) {
        // 1. 获取当前列表（如果是不可变列表，转为可变）
        // 如果数据长度不够（旧版本数据），需要先补齐到当前版本长度
        val currentList = tooltipFlags.toMutableList()

        // 补齐逻辑：如果当前列表比总数少，用 1 填充
        while (currentList.size < TooltipIndices.TOTAL_COUNT) {
            currentList.add(1)
        }

        // 2. 修改对应位置为 0
        if (index in currentList.indices) {
            currentList[index] = 0

            // 3. 赋值回去，触发 Settings 保存
            tooltipFlags = currentList
        }
    }

    /**
     * 重置所有气泡（用于调试或设置页）
     */
    fun resetAllTooltips() {
        tooltipFlags = List(TooltipIndices.TOTAL_COUNT) { 1 }
    }

    fun resetToDefaults() {
        // 奇门
        qimenMiddlePalaceText = "无门无路"
        qimenPalaceSound = true
        qimenScale = QimenScale.Default
        qimenHighlight = true
        qimenDashedAnim = true
        qimenArrangement = QimenArrangement.Hour
        qimenZhiRun = true
        qimenUIScale = 1.0f

        // 六壬
        liurenEarthBranchDisplay = true
        liurenAnimationDuration = 6
        liurenFadeEffect = true
        liurenScale = LiurenScale.ChineseTraditional
        liurenUIScale = 1.0f
        liurenHighScale = 1.4f

        // 重置气泡提示
        resetAllTooltips()
    }
}


object TooltipIndices {
    const val TIME_CARD = 0      // 时间卡片提示
    const val START_BTN = 1      // 开始排盘按钮提示
    const val GONG_WEI = 2       // 宫位点击提示
    // 未来要加新提示，就在这里加 const val NEW_FEATURE = 3

    // 当前总共有多少个提示气泡 (用于生成默认列表)
    const val TOTAL_COUNT = 3
}

// ==========================================
// 3. UI 组件 (UI Components)
// ==========================================


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsPage(
    onBackClick: () -> Unit = {}
) {
    var showPlatformErrorDialog by remember { mutableStateOf(false) }

    val uriHandler = LocalUriHandler.current

    val themeConfig = LocalThemeConfig.current
    var themeMode by remember { mutableStateOf(UserPreferences.themeMode) }

    // --- State 定义保持不变 ---
    var qimenMiddleText by remember { mutableStateOf(UserPreferences.qimenMiddlePalaceText) }
    var qimenSound by remember { mutableStateOf(UserPreferences.qimenPalaceSound) }
    var qimenScale by remember { mutableStateOf(UserPreferences.qimenScale) }
    var qimenHighlight by remember { mutableStateOf(UserPreferences.qimenHighlight) }
    var qimenDashed by remember { mutableStateOf(UserPreferences.qimenDashedAnim) }
    var qimenArrange by remember { mutableStateOf(UserPreferences.qimenArrangement) }
    var qimenZhiRun by remember { mutableStateOf(UserPreferences.qimenZhiRun) }
    var qimenUiScale by remember { mutableFloatStateOf(UserPreferences.qimenUIScale) }

    var liurenBranch by remember { mutableStateOf(UserPreferences.liurenEarthBranchDisplay) }
    var liurenAnimDur by remember { mutableIntStateOf(UserPreferences.liurenAnimationDuration) }
    var liurenFade by remember { mutableStateOf(UserPreferences.liurenFadeEffect) }
    var liurenScale by remember { mutableStateOf(UserPreferences.liurenScale) }
    var liurenUiScale by remember { mutableFloatStateOf(UserPreferences.liurenUIScale) }
    var liurenhighScale by remember { mutableFloatStateOf(liurenHighScale) }
    var showResetDialog by remember { mutableStateOf(false) }
    var useDynamicColor by remember { mutableStateOf(UserPreferences.useDynamicColor) }
    var themeColorInt by remember { mutableStateOf(UserPreferences.customThemeColor) }

    val isAnyDialogOpen = showResetDialog || showPlatformErrorDialog


    // 当弹窗打开时，半径变为 10.dp (模糊)，关闭时变回 0.dp (清晰)
    val contentBlurRadius by animateDpAsState(
        targetValue = if (isAnyDialogOpen) 10.dp else 0.dp,
        animationSpec = tween(durationMillis = 300)
    )
    Box(modifier = Modifier.fillMaxSize()) {


        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .blur(contentBlurRadius),
            topBar = {
                TopAppBar(
                    title = { Text("设置") },
                    navigationIcon = {
                        // 如需返回图标可取消注释
                        IconButton(onClick = onBackClick) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") }
                    },
                    actions = {
                        TextButton(onClick = { showResetDialog = true }) {
                            Text("重置默认", color = MaterialTheme.colorScheme.primary)
                        }
                        // 或者使用图标按钮：
                        // IconButton(onClick = { showResetDialog = true }) {
                        //     Icon(Icons.Default.Refresh, contentDescription = "重置设置")
                        // }
                    }
                )
            }
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            )
            {
                item { SettingsHeader("外观与主题") }
                item {
                    ListPreference(
                        title = "主题模式",
                        description = "选择应用显示的明暗风格。",
                        // 使用 ThemeController 中定义的枚举
                        options = ThemeController.ThemeMode.entries,
                        selected = themeMode,
                        // 将枚举转换为中文显示
                        labelMapper = { mode ->
                            when (mode) {
                                ThemeController.ThemeMode.SYSTEM -> "跟随系统"
                                ThemeController.ThemeMode.LIGHT -> "总是浅色"
                                ThemeController.ThemeMode.DARK -> "总是深色"
                            }
                        },
                        onSelectedChange = { newMode ->
                            // 1. 更新当前页面 UI 状态
                            themeMode = newMode

                            // 2. 更新 UserPreferences 持久化存储
                            UserPreferences.themeMode = newMode

                            // 3. 【关键】调用控制器立即刷新主题（无需重启）
                            // 确保你已经按照之前的建议创建了 ThemeController.updateThemeMode
                            ThemeController.updateThemeMode(newMode)
                        }
                    )
                }
                item {
                    SwitchPreference(
                        title = "跟随壁纸颜色 (Monet)",
                        description = "从设备壁纸中提取颜色作为应用主题（仅 Android 12+ 有效）。",
                        checked = useDynamicColor,
                        onCheckedChange = { newValue ->
                            if (currentPlatform == PlatformType.ANDROID) {
                                // Android 平台：正常设置值
                                useDynamicColor = newValue
                                UserPreferences.useDynamicColor = newValue
                            } else {
                                // 非 Android 平台：阻止值改变，并触发弹窗显示
                                // 注意：这里不要设置 useDynamicColor = newValue，否则开关会看起来被打开了
                                showPlatformErrorDialog = true
                            }
                        }
                    )
                }

                // 只有当不使用动态颜色时，才显示调色板
                // 或者你想让用户即使开了动态色也能选备用色，可以去掉 if
                if (!useDynamicColor) {
                    item {
                        ColorPalettePreference(
                            selectedColorInt = themeColorInt,
                            onColorSelected = { newColorInt ->
                                themeColorInt = newColorInt
                                UserPreferences.customThemeColor = newColorInt

                                // 通知 App.kt 重新读取颜色，界面瞬间变色！
                                themeConfig.refresh()
                            }
                        )
                    }
                }
                // --- 奇门设置 ---
                item { SettingsHeader("奇门设置") }

                item {
                    TextPreference(
                        title = "中宫文字内容",
                        description = "自定义奇门盘中宫位置显示的文字。",
                        summary = qimenMiddleText,
                        onValueChange = { newValue ->
                            qimenMiddleText = newValue
                            UserPreferences.qimenMiddlePalaceText = newValue
                        }
                    )
                }
                item {
                    SwitchPreference(
                        title = "宫位音效",
                        description = "点击九宫格时是否播放提示音。",
                        checked = qimenSound,
                        onCheckedChange = { newValue ->
                            qimenSound = newValue
                            UserPreferences.qimenPalaceSound = newValue
                        }
                    )
                }
                item {
                    if (qimenSound) {
                        ListPreference(
                            title = "音阶",
                            description = "选择点击宫位时播放的音乐调式。",
                            options = QimenScale.entries,
                            selected = qimenScale,
                            labelMapper = { it.label },
                            onSelectedChange = { newValue ->
                                qimenScale = newValue
                                UserPreferences.qimenScale = newValue
                            }
                        )
                    }
                }
                item {
                    SwitchPreference(
                        title = "选中高亮",
                        description = "点击宫位后，是否保持高亮选中状态。",
                        checked = qimenHighlight,
                        onCheckedChange = { newValue ->
                            qimenHighlight = newValue
                            UserPreferences.qimenHighlight = newValue
                        }
                    )
                }
                item {
                    if (qimenHighlight) {
                        SwitchPreference(
                            title = "虚线动画",
                            description = "选中宫位时，显示旋转的虚线框动画效果。",
                            checked = qimenDashed,
                            onCheckedChange = { newValue ->
                                qimenDashed = newValue
                                UserPreferences.qimenDashedAnim = newValue
                            }
                        )
                    }
                }
                item {
                    ListPreference(
                        title = "排盘法",
                        description = "选择奇门遁甲的起局排盘方式（如时家、日家）。",
                        options = QimenArrangement.entries,
                        selected = qimenArrange,
                        labelMapper = { it.label },
                        onSelectedChange = { newValue ->
                            qimenArrange = newValue
                            UserPreferences.qimenArrangement = newValue
                        }
                    )
                }
                item {
                    if (qimenArrange == QimenArrangement.Hour) {
                        SwitchPreference(
                            title = "时家置润",
                            description = "开启使用置润法（超神接气），关闭则使用拆补法。",
                            checked = qimenZhiRun,
                            onCheckedChange = { newValue ->
                                qimenZhiRun = newValue
                                UserPreferences.qimenZhiRun = newValue
                            }
                        )
                    }
                }
                item {
                    FloatSliderPreference(
                        title = "式盘界面缩放",
                        description = "调整奇门界面的整体显示大小。",
                        value = qimenUiScale,
                        range = 0.5f..1.5f,
                        steps = 19,
                        onValueChange = { newValue ->
                            qimenUiScale = newValue
                            UserPreferences.qimenUIScale = newValue
                        }
                    )
                }

                // --- 六壬设置 ---
                item { Spacer(modifier = Modifier.height(16.dp)) }
                item { SettingsHeader("六壬设置") }

                item {
                    SwitchPreference(
                        title = "地盘地支展示",
                        description = "在天地盘背景中显示地支文字（如子、丑等）。",
                        checked = liurenBranch,
                        onCheckedChange = { newValue ->
                            liurenBranch = newValue
                            UserPreferences.liurenEarthBranchDisplay = newValue
                        }
                    )
                }
                item {
                    SliderPreference(
                        title = "地盘地支动画时长",
                        description = "调整点击后地支文字显示和消失的持续时间。",
                        value = liurenAnimDur,
                        range = 1f..20f,
                        unit = "s",
                        onValueChange = { newValue ->
                            liurenAnimDur = newValue
                            UserPreferences.liurenAnimationDuration = newValue
                        }
                    )
                }
                item {
                    SwitchPreference(
                        title = "淡出淡入效果",
                        description = "地支文字显示时是否启用渐变动画。",
                        checked = liurenFade,
                        onCheckedChange = { newValue ->
                            liurenFade = newValue
                            UserPreferences.liurenFadeEffect = newValue
                        }
                    )
                }
                item {
                    ListPreference(
                        title = "音阶",
                        description = "选择点击六壬宫位时的声音效果。",
                        options = LiurenScale.entries,
                        selected = liurenScale,
                        labelMapper = { it.label },
                        onSelectedChange = { newValue ->
                            liurenScale = newValue
                            UserPreferences.liurenScale = newValue
                        }
                    )
                }
                item {
                    FloatSliderPreference(
                        title = "式盘界面缩放",
                        description = "调整六壬界面的整体文字和图标大小。",
                        value = liurenUiScale,
                        range = 0.5f..1.5f,
                        steps = 19,
                        onValueChange = { newValue ->
                            liurenUiScale = newValue
                            UserPreferences.liurenUIScale = newValue
                        }
                    )
                }
                item {
                    FloatSliderPreference(
                        title = "式盘长度缩放",
                        description = "调整六壬盘中间区域（三传四课）的高度，以容纳更多内容。",
                        value = liurenhighScale,
                        range = 0.5f..1.5f,
                        steps = 19,
                        onValueChange = { newValue ->
                            liurenhighScale = newValue
                            liurenHighScale = newValue
                        }
                    )
                }

                // --- 关于 ---
                item { Spacer(modifier = Modifier.height(16.dp)) }
                item { SettingsHeader("关于") }
                // ... 关于部分保持不变 ...
                item {
                    AboutCard(
                        description = "Celestite 是一个基于 Compose Multiplatform 开发的排盘软件。",
                        githubUrl = "https://github.com/xiaozhao45/celestite",
                        onGithubClick = { url -> uriHandler.openUri(url) }
                    )
                }
                item { Spacer(modifier = Modifier.height(32.dp)) }
            }
        }
    }

    AnimatedAlertDialogCenter(
        visible = showResetDialog, // 传入状态
        onDismissRequest = { showResetDialog = false },
        icon = { Icon(Icons.Default.Info, contentDescription = null) },
        title = { Text("重置设置") },
        text = { Text("确定要将所有设置恢复为默认状态吗？此操作不可撤销。\n这不会立即让你已经起出的式盘销毁，但可能会改变类型，你明白吗？") },
        confirmButton = {
            TextButton(
                onClick = {
                    // 1. 执行硬盘数据重置
                    UserPreferences.resetToDefaults()

                    // 2. 【关键】手动更新当前页面的所有 State
                    // 因为 State 不会自动监听 UserPreferences，必须手动赋值一遍
                    // 奇门
                    qimenMiddleText = UserPreferences.qimenMiddlePalaceText
                    qimenSound = UserPreferences.qimenPalaceSound
                    qimenScale = UserPreferences.qimenScale
                    qimenHighlight = UserPreferences.qimenHighlight
                    qimenDashed = UserPreferences.qimenDashedAnim
                    qimenArrange = UserPreferences.qimenArrangement
                    qimenZhiRun = UserPreferences.qimenZhiRun
                    qimenUiScale = UserPreferences.qimenUIScale
                    // 六壬
                    liurenBranch = UserPreferences.liurenEarthBranchDisplay
                    liurenAnimDur = UserPreferences.liurenAnimationDuration
                    liurenFade = UserPreferences.liurenFadeEffect
                    liurenScale = UserPreferences.liurenScale
                    liurenUiScale = UserPreferences.liurenUIScale
                    liurenhighScale = liurenHighScale
                    if (currentPlatform.equals(PlatformType.ANDROID)) {
                        useDynamicColor = UserPreferences.useDynamicColor
                    } else { UserPreferences.useDynamicColor = false ; useDynamicColor = UserPreferences.useDynamicColor }
                    themeColorInt = UserPreferences.customThemeColor

                    themeConfig.refresh()
                    // 3. 关闭弹窗
                    showResetDialog = false


                }
            ) {
                Text("确定重置", color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = {
            TextButton(onClick = { showResetDialog = false }) {
                Text("取消")
            }
        }
    )

    AnimatedAlertDialogCenter(
        visible = showPlatformErrorDialog,
        onDismissRequest = { showPlatformErrorDialog = false },
        icon = {
            Icon(
                imageVector = Icons.Filled.Error,
                contentDescription = "Error"
            )
        },
        // 注意：标准组件通常需要 Text{} 包裹字符串，如果是自定义组件支持 String 则保持原样
        title = { Text("你正在使用JVM/Desktop！") },
        text = { Text("在JVM/Desktop上不支持此功能，请在Android 12以上系统中访问。") },
        confirmButton = {
            Button(
                onClick = { showPlatformErrorDialog = false }
            ) {
                Text("确认")
            }
        },
        // dismissButton 通常是可选的，如果不需要“取消”按钮，传入 null
        dismissButton = null
    )

}

// ==========================================
// 4. 辅助组件 (Helper Composables) - 已修改
// ==========================================




@Composable
fun SettingsHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(start = 16.dp, bottom = 8.dp, top = 8.dp)
    )
}

@Composable
fun SwitchPreference(
    title: String,
    description: String? = null,
    checked: Boolean,
    // 修改点：去掉了 @Composable，变成普通函数类型
    onCheckedChange: (Boolean) -> Unit
) {
    ListItem(
        headlineContent = { Text(title) },
        supportingContent = if (description != null) {
            { Text(description, style = MaterialTheme.typography.bodySmall) }
        } else null,
        trailingContent = {
            // 现在类型匹配了，Switch 接受 (Boolean) -> Unit
            Switch(checked = checked, onCheckedChange = onCheckedChange)
        },
        // Modifier.clickable 也可以正常调用普通函数
        modifier = Modifier.clickable { onCheckedChange(!checked) }
    )
}

// 【修改】增加了 description 参数，并优化了显示逻辑
@Composable
fun <T> ListPreference(
    title: String,
    description: String? = null,
    options: List<T>,
    selected: T,
    labelMapper: (T) -> String,
    onSelectedChange: (T) -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }

    ListItem(
        headlineContent = { Text(title) },
        supportingContent = {
            Column {
                // 如果有描述，显示描述
                if (description != null) {
                    Text(description, style = MaterialTheme.typography.bodySmall)
                }
                // 显示当前选中的值 (稍微高亮一下)
                Text(
                    text = "当前: ${labelMapper(selected)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        },
        modifier = Modifier.clickable { showDialog = true }
    )

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(title) },
            text = {
                Column(Modifier.selectableGroup()) {
                    options.forEach { option ->
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .selectable(
                                    selected = (option == selected),
                                    onClick = {
                                        onSelectedChange(option)
                                        showDialog = false
                                    },
                                    role = Role.RadioButton
                                )
                                .padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = (option == selected),
                                onClick = null
                            )
                            Text(
                                text = labelMapper(option),
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.padding(start = 16.dp)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("取消")
                }
            }
        )
    }
}

// 【修改】增加了 description 参数
@Composable
fun TextPreference(
    title: String,
    description: String? = null,
    summary: String,
    onValueChange: (String) -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }
    var tempText by remember(summary) { mutableStateOf(summary) }

    ListItem(
        headlineContent = { Text(title) },
        supportingContent = {
            Column {
                if (description != null) {
                    Text(description, style = MaterialTheme.typography.bodySmall)
                }
                Text(
                    text = summary,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        modifier = Modifier.clickable {
            tempText = summary
            showDialog = true
        }
    )

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(title) },
            text = {
                OutlinedTextField(
                    value = tempText,
                    onValueChange = { tempText = it },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    onValueChange(tempText)
                    showDialog = false
                }) {
                    Text("确定")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("取消")
                }
            }
        )
    }
}

// 【修改】增加了 description 参数，放在 Slider 上方
@Composable
fun SliderPreference(
    title: String,
    description: String? = null,
    value: Int,
    range: ClosedFloatingPointRange<Float>,
    unit: String = "",
    onValueChange: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.bodyLarge)
                if (description != null) {
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Text(
                text = "$value$unit",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(start = 16.dp)
            )
        }
        Slider(
            value = value.toFloat(),
            onValueChange = { onValueChange(it.toInt()) },
            valueRange = range,
            steps = (range.endInclusive - range.start).toInt() - 1
        )
    }
}

// 【修改】增加了 description 参数，放在 Slider 上方
@Composable
fun FloatSliderPreference(
    title: String,
    description: String? = null,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    steps: Int = 0,
    onValueChange: (Float) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.bodyLarge)
                if (description != null) {
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Text(
                text = "${(value * 100).roundToInt()}%",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(start = 16.dp)
            )
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = range,
            steps = steps
        )
    }
}

@Composable
fun AboutCard(
    description: String,
    githubUrl: String,
    onGithubClick: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "关于应用",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedButton(
                onClick = { onGithubClick(githubUrl) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Code, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("访问 Github 仓库")
            }
        }
    }
}