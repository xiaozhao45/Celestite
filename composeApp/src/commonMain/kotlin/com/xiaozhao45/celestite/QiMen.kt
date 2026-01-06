package com.xiaozhao45.celestite

import xuan.core.qimen.zhuan.QiMen
import xuan.core.qimen.zhuan.QiMenZhiRun
import xuan.core.qimen.zhuan.QiMenZhuanPan
import xuan.core.qimen.zhuan.settings.QiMenZhuanPanJiChuSetting
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


// -----------------------------------------------------------------------------------------
// 奇门遁甲数据模型与逻辑
// -----------------------------------------------------------------------------------------

/**
 * 奇门遁甲整体数据
 */
data class QiMenData(
    val yearPillar: String = "",
    val monthPillar: String = "",
    val dayPillar: String = "",
    val hourPillar: String = "",
    val timeString: String = "",
    val lunarDate: String = "",
    val solarTerm: String = "",
    val juShu: String = "",
    val valueStar: String = "",
    val valueDoor: String = "",
    val monthGeneral: String = "",
    val monthCommand: String = "",
    val palaces: List<PalaceData>
)

/**
 * 宫位数据模型
 */
data class PalaceData(
    val id: Int, // 1-9
    val god: String = "",
    val star: String = "",
    val door: String = "",

    // 天盘信息（位于九星右侧）
    val heavenStem: String = "",
    val heavenStage: String = "",

    // 天盘寄宫信息（位于九星左侧）
    val hiddenHeavenStem: String? = null,

    // 地盘信息（位于八门右侧）
    val earthStem: String = "",
    val earthStage: String = "",

    // 地盘寄宫信息（位于八门左侧，特用于坤二宫）
    val hiddenEarthStem: String? = null,

    val starStatus: String = "", // 旺相休囚
    val isHorse: Boolean = false, // 马星
    val isVoid: Boolean = false, // 空亡
    val isCenter: Boolean = false // 是否中宫
)

//class QiMenSettings {
//    var Name: String = "某人"
//    var Occupy: String = "某事"
//    val Sex: Int = 0
//    var Time: LocalDateTime = LocalDateTime.now()
//    val LeapMonth: Int = 0
//    val JieQiType: Int = 1
//    val ZiShi: Int = 0
//}

/**
 * 核心排盘函数
 * 接收 LocalDateTime，返回 UI 需要的 QiMenData
 */
fun calculateQiMenData(dateTime: LocalDateTime): QiMenData {
    val setting = QiMenZhuanPanJiChuSetting()
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

    setting.date = dateTime.format(formatter)
    setting.dateType = 0
    setting.jieQiType = 1
    setting.paiPanType = when(UserPreferences.qimenArrangement){
        QimenArrangement.Day -> 2
        QimenArrangement.Hour -> 3
        QimenArrangement.Year -> 0
        QimenArrangement.Month -> 1
    }
    setting.zhiShiType = 0
    setting.yearGanZhiType = 2
    setting.monthGanZhiType = 1
    setting.dayGanZhiType = 0

    // 调用 Java 库
    val qimen: QiMen = if (UserPreferences.qimenZhiRun) {
        QiMenZhiRun(setting)
    } else {
        QiMenZhuanPan(setting)
    }

    // --- 1. 获取原始列表 ---
    val shenPan = qimen.shenPan ?: emptyList() // 神盘
    val tianPan = qimen.tianPan ?: emptyList() // 天盘(九星)
    val renPan = qimen.renPan ?: emptyList()   // 人盘(八门)
    val diPan = qimen.diPan ?: emptyList()     // 地盘(奇仪)

    // 天盘干（不含寄干）
    val heavenStems = qimen.tianPanQiYiTianQinNo ?: emptyList()
    // 天盘寄干（含寄干，即天禽星携带的干）
    val hiddenHeavenStems = qimen.tianPanQiYiTianQinYes ?: emptyList()

    // 状态列表
    val starWangShuai = qimen.jiuXingWangShuai ?: emptyList()
    val heavenStages = qimen.tianPanQiYiLuoGongTianQinNoLink ?: emptyList()
    val earthStages = qimen.diPanQiYiLuoGongLink ?: emptyList()

    // 神煞
    val yiMaLoc = qimen.yiMaGongWei
    val voidLocs = qimen.liuJiaXunKongGongWei ?: emptyList()

    val palaces = mutableListOf<PalaceData>()

    // 【特殊处理】获取中五宫(索引4)的地盘奇仪，用于坤二宫寄宫
    // Java 库返回的 diPan 是 9 位列表，索引 4 即为中宫
    val centerEarthStem = if (diPan.size > 4) diPan[4] else ""

    // --- 2. 遍历 0-8 (严格对应: 坎1, 坤2, 震3, 巽4, 中5, 乾6, 兑7, 艮8, 离9) ---
    for (i in 0 until 9) {
        val isCenter = (i == 4)

        // 辅助取值函数
        fun <T> List<T>.safeGet(idx: Int, default: T): T = if (idx in indices) this[idx] else default

        // 解析长生状态 (List<List<String>> -> e.g. [["丙", "帝旺"], ...])
        var heavenStageStr = ""
        val hList = heavenStages.safeGet(i, emptyList())
        if (hList.isNotEmpty() && hList[0].size > 1) heavenStageStr = hList[0][1].toString()

        var earthStageStr = ""
        val eList = earthStages.safeGet(i, emptyList())
        if (eList.isNotEmpty() && eList[0].size > 1) earthStageStr = eList[0][1].toString()

        // 解析旺衰
        var starStatusStr = ""
        val sList = starWangShuai.safeGet(i, emptyList())
        if (sList.size > 1) starStatusStr = sList[1].toString()

        // 解析天盘寄干 (通常随天禽星)
        val hiddenHeavenStem = hiddenHeavenStems.safeGet(i, "")
        val finalHiddenHeavenStem = if (hiddenHeavenStem.isNotEmpty()) hiddenHeavenStem else null

        // 【特殊处理】只有坤二宫 (索引1) 需要显示地盘寄宫 (即中五宫的地盘干)
        val finalHiddenEarthStem = if (i == 1) centerEarthStem else null

        palaces.add(
            PalaceData(
                id = i + 1,
                // 直接使用 index i，不做任何位移
                god = shenPan.safeGet(i, ""),
                star = tianPan.safeGet(i, ""),
                door = renPan.safeGet(i, ""),
                heavenStem = heavenStems.safeGet(i, ""),
                heavenStage = heavenStageStr,
                hiddenHeavenStem = finalHiddenHeavenStem,
                earthStem = diPan.safeGet(i, ""),
                earthStage = earthStageStr,
                hiddenEarthStem = finalHiddenEarthStem, // 仅在此处应用寄宫逻辑
                starStatus = starStatusStr,
                isHorse = (yiMaLoc != null && yiMaLoc == (i + 1)),
                isVoid = voidLocs.contains(i + 1),
                isCenter = isCenter
            )
        )
    }

    return QiMenData(
        yearPillar = qimen.yearGanZhi ?: "",
        monthPillar = qimen.monthGanZhi ?: "",
        dayPillar = qimen.dayGanZhi ?: "",
        hourPillar = qimen.hourGanZhi ?: "",
        timeString = qimen.solarStr ?: "",
        lunarDate = qimen.lunarDate?.toString() ?: "",
        solarTerm = qimen.jieQi ?: "",
        juShu = (qimen.yinYangDun ?: "") + (qimen.juShu?.toString() ?: "") + "局",
        valueStar = qimen.zhiFu ?: "",
        valueDoor = qimen.zhiShi ?: "",
        monthGeneral = qimen.yueJiangShen ?: "",
        monthCommand = qimen.monthGanZhi ?: "",
        palaces = palaces
    )
}
