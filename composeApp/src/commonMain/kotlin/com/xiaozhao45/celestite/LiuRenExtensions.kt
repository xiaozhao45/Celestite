package com.xiaozhao45.celestite

// LiuRenExtensions.kt

import xuan.core.daliuren.DaLiuRen

// 简易五行映射，用于前端补全显示逻辑
val TIAN_GAN_WUXING = mapOf(
    "甲" to "木", "乙" to "木", "丙" to "火", "丁" to "火",
    "戊" to "土", "己" to "土", "庚" to "金", "辛" to "金",
    "壬" to "水", "癸" to "水"
)
val DI_ZHI_WUXING = mapOf(
    "子" to "水", "丑" to "土", "寅" to "木", "卯" to "木",
    "辰" to "土", "巳" to "火", "午" to "火", "未" to "土",
    "申" to "金", "酉" to "金", "戌" to "土", "亥" to "水"
)

// 计算六亲 (根据日干五行 vs 天盘地支五行)
fun calculateLiuQin(dayGan: String, diZhi: String): String {
    val myWuXing = TIAN_GAN_WUXING[dayGan] ?: return ""
    val otherWuXing = DI_ZHI_WUXING[diZhi] ?: return ""

    return when {
        myWuXing == otherWuXing -> "兄弟"
        isSheng(myWuXing, otherWuXing) -> "子孙" // 我生者
        isSheng(otherWuXing, myWuXing) -> "父母" // 生我者
        isKe(myWuXing, otherWuXing) -> "妻财"   // 我克者
        isKe(otherWuXing, myWuXing) -> "官鬼"   // 克我者
        else -> ""
    }
}

// 五行生克判断
fun isSheng(source: String, target: String): Boolean {
    return (source == "木" && target == "火") ||
            (source == "火" && target == "土") ||
            (source == "土" && target == "金") ||
            (source == "金" && target == "水") ||
            (source == "水" && target == "木")
}

fun isKe(source: String, target: String): Boolean {
    return (source == "木" && target == "土") ||
            (source == "土" && target == "水") ||
            (source == "水" && target == "火") ||
            (source == "火" && target == "金") ||
            (source == "金" && target == "木")
}