package com.xiaozhao45.celestite

import xuan.core.qimen.zhuan.QiMenZhiRun

/**
 * 扩展函数：将 Java 库的 QiMenZhiRun 对象转换为 UI 需要的 QiMenData
 */
fun QiMenZhiRun.toUiData(): QiMenData {
    // 1. 获取基础列表数据 (处理 Java 可能的 null)
    val shenPan = this.shenPan ?: emptyList()
    val tianPan = this.tianPan ?: emptyList()
    val renPan = this.renPan ?: emptyList()
    val diPan = this.diPan ?: emptyList()

    // 天盘干（不含寄宫）
    val heavenStems = this.tianPanQiYiTianQinNo ?: emptyList()
    // 天盘寄干（含寄宫，通常对应天禽星携带的干）
    val hiddenHeavenStems = this.tianPanQiYiTianQinYes ?: emptyList()

    // 旺衰与长生状态 (数据结构复杂，需要层层解包)
    val starWangShuai = this.jiuXingWangShuai ?: emptyList()
    val heavenStages = this.tianPanQiYiLuoGongTianQinNoLink ?: emptyList()
    val earthStages = this.diPanQiYiLuoGongLink ?: emptyList()

    // 神煞位置
    val yiMaLoc = this.yiMaGongWei // 返回的是 1-9 的 Int
    val voidLocs = this.liuJiaXunKongGongWei ?: emptyList() // 返回 1-9 的 List

    val palaceList = mutableListOf<PalaceData>()

    // 2. 遍历 0~8 (对应宫位 1~9)
    for (i in 0 until 9) {
        // 判断是否是中宫 (索引4，即洛书数5)
        val isCenter = (i == 4)

        // --- 安全获取数据的辅助逻辑 ---
        val godStr = shenPan.getOrElse(i) { "" }
        val starStr = tianPan.getOrElse(i) { "" }
        val doorStr = renPan.getOrElse(i) { "" }
        val heavenStemStr = heavenStems.getOrElse(i) { "" }
        val earthStemStr = diPan.getOrElse(i) { "" }

        // 获取天盘寄干 (如果有)
        var hiddenHStem: String? = hiddenHeavenStems.getOrElse(i) { "" }
        if (hiddenHStem.isNullOrEmpty()) hiddenHStem = null

        // --- 解析复杂状态列表 ---
        // 结构通常是: List<宫位> -> List<干> -> List<[干名, 状态]>
        // 例如: [[庚, 衰], [辛, 旺]]

        // 天盘长生
        var heavenStageStr = ""
        val hList = heavenStages.getOrElse(i) { emptyList() }
        if (hList.isNotEmpty() && hList[0].size > 1) {
            heavenStageStr = hList[0][1].toString()
        }

        // 地盘长生
        var earthStageStr = ""
        val eList = earthStages.getOrElse(i) { emptyList() }
        if (eList.isNotEmpty() && eList[0].size > 1) {
            earthStageStr = eList[0][1].toString()
        }

        // 九星旺衰
        var starStatusStr = ""
        val sList = starWangShuai.getOrElse(i) { emptyList() }
        if (sList.size > 1) {
            starStatusStr = sList[1].toString()
        }

        // --- 构建 PalaceData 对象 ---
        val palace = PalaceData(
            id = i + 1,
            isCenter = isCenter,
            god = godStr,
            star = starStr,
            door = doorStr,
            heavenStem = heavenStemStr,
            heavenStage = heavenStageStr,
            hiddenHeavenStem = hiddenHStem,
            earthStem = earthStemStr,
            earthStage = earthStageStr,
            // 暂时不处理地盘寄宫，除非你库里有专门字段，否则设为null
            hiddenEarthStem = null,
            starStatus = starStatusStr,
            isHorse = (yiMaLoc != null && yiMaLoc == (i + 1)),
            isVoid = voidLocs.contains(i + 1)
        )

        palaceList.add(palace)
    }

    // 3. 构建并返回最终的 QiMenData
    return QiMenData(
        yearPillar = this.yearGanZhi ?: "",
        monthPillar = this.monthGanZhi ?: "",
        dayPillar = this.dayGanZhi ?: "",
        hourPillar = this.hourGanZhi ?: "",
        timeString = this.solarStr ?: "",
        lunarDate = this.lunarDate?.toString() ?: "",
        solarTerm = this.jieQi ?: "",
        juShu = (this.yinYangDun ?: "") + (this.juShu?.toString() ?: "") + "局",
        valueStar = this.zhiFu ?: "",
        valueDoor = this.zhiShi ?: "",
        monthGeneral = this.yueJiangShen ?: "",
        monthCommand = this.monthGanZhi ?: "", // 通常用月柱代表月令
        palaces = palaceList
    )
}