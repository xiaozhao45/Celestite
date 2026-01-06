package com.xiaozhao45.celestite


import android.os.Build
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

@Composable
actual fun getPlatformDynamicColorScheme(isDark: Boolean): ColorScheme? {
    val context = LocalContext.current
    // 只有 Android 12 (S) 及以上才支持 Monet 动态取色
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        if (isDark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
    } else {
        null
    }
}