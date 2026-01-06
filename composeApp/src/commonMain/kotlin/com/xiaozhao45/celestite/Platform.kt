package com.xiaozhao45.celestite

// 1. 定义支持的平台类型枚举
enum class PlatformType {
    ANDROID,
    IOS,
    DESKTOP, // JVM
    WEB
}
// 2. 声明一个 expect 变量，要求各平台必须实现它
expect val currentPlatform: PlatformType

// 3. (可选) 辅助属性，方便在代码里写 if (isMobile) 等
val isAndroid: Boolean get() = currentPlatform == PlatformType.ANDROID
val isIos: Boolean get() = currentPlatform == PlatformType.IOS
val isDesktop: Boolean get() = currentPlatform == PlatformType.DESKTOP
val isMobile: Boolean get() = isAndroid || isIos
