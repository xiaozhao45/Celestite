package com.xiaozhao45.celestite
// commonMain/kotlin/com/example/Platform.kt

enum class PlatformType {
    ANDROID,
    IOS,
    JVM,
    JS
}

interface Platform {
    val type: PlatformType
    val name: String
}

expect fun getPlatform(): Platform