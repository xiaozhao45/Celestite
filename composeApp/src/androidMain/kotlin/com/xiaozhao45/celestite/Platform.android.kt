package com.xiaozhao45.celestite

class AndroidPlatform : Platform {
    override val type: PlatformType = PlatformType.ANDROID
    override val name: String = "Android ${android.os.Build.VERSION.SDK_INT}"
}

actual fun getPlatform(): Platform = AndroidPlatform()