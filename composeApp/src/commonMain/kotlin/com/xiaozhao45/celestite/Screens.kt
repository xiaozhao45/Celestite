package com.xiaozhao45.celestite

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow

// 通用的预备页 Screen
data class GenericReadyScreen(val targetIndex: Int) : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow

        // 这里的 UI 需要你稍微调整 ReadyPage 的代码
        // 或者我们在这里直接复用 ReadyPage，但忽略它的 Grid 点击跳转，
        // 而是提供一个单一的“开始排盘”按钮。

        // 为了复用你现有的 ReadyPage，我们可以只用它的时间选择功能
        // 这里我假设你修改 ReadyPage 增加了一个 "SingleMode" (单功能模式)

        ReadyPage(
            // 告诉 ReadyPage 只显示时间，底部显示一个大按钮“开始[奇门]排盘”
            // 而不是显示 4 个 Grid 选项
            targetModeIndex = targetIndex,

            onNavigateToPage = { _ ->
                // 不管点击了什么，根据 targetIndex 决定去哪里
                val nextScreen = when(targetIndex) {
                    0 -> QiMenScreen()
                    1 -> LiuRenScreen()
                    2 -> MingLiScreen()
                    3 -> YiJingScreen()
                    else -> null
                }
                if (nextScreen != null) {
                    navigator.push(nextScreen)
                }
            }
        )
    }
}

// 1. 预备页 (首页) - 这里需要 Navigator 来跳转到别的页面
class ReadyScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow

        ReadyPage(
            onNavigateToPage = { index ->
                val screen = when (index) {
                    0 -> QiMenScreen()
                    1 -> LiuRenScreen()
                    2 -> MingLiScreen()
                    3 -> YiJingScreen()
                    4 -> SettingsScreen()
                    else -> null
                }
                if (screen != null) {
                    navigator.push(screen)
                }
            }
        )
    }
}

// 2. 奇门页
class QiMenScreen : Screen {
    @Composable
    override fun Content() {
        // 不需要 navigator，也不需要 onBackClick
        // Voyager 会自动处理系统返回键
        QiMenPage()
    }
}

// 3. 六壬页
class LiuRenScreen : Screen {
    @Composable
    override fun Content() {
        LiuRenPage()
    }
}

// 4. 设置页
class SettingsScreen : Screen {
    @Composable
    override fun Content() {
        SettingsPage()
    }
}

// 5. 其他页面
class MingLiScreen : Screen {
    @Composable
    override fun Content() {
        // MingLiPage()
    }
}

class YiJingScreen : Screen {
    @Composable
    override fun Content() {
        // YiJingPage()
    }
}