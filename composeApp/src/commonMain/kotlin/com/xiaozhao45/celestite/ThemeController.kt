package com.xiaozhao45.celestite


import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

object ThemeController {
    // 1. 定义三种模式：跟随系统、总是浅色、总是深色
    enum class ThemeMode {
        SYSTEM, LIGHT, DARK
    }

    // 2. 这是一个 State，Compose 会自动观察它的变化
    // 初始化时从你的 UserPreferences 读取保存的值
    var themeMode by mutableStateOf(loadThemeFromPrefs())
        private set

    // 3. 提供一个方法来更新主题
    fun updateThemeMode(newMode: ThemeMode) {
        themeMode = newMode
        // 这里同时保存到你的 UserPreferences 本地存储
        saveThemeToPrefs(newMode)
    }

    // 模拟读取你的 UserPreferences
    private fun loadThemeFromPrefs(): ThemeMode {
        return UserPreferences.themeMode
    }

    // 模拟保存
    private fun saveThemeToPrefs(mode: ThemeMode) {
        // UserPreferences.themeMode = mode
        UserPreferences.themeMode = mode
    }
}