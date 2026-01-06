package com.xiaozhao45.celestite
class JVMPlatform : Platform {
    // 对应枚举中的 JVM 类型
    override val type: PlatformType = PlatformType.JVM

    // 获取 Java 版本，例如 "Java 17.0.8"
    override val name: String = "Java ${System.getProperty("java.version")}"
}

// 实现 actual 函数
actual fun getPlatform(): Platform = JVMPlatform()