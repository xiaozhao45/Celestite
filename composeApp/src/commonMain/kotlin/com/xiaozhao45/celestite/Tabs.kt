package com.xiaozhao45.celestite

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabOptions
import cafe.adriel.voyager.transitions.SlideTransition

// 1. 奇门 Tab
object QiMenTab : Tab {
    override val options: TabOptions
        @Composable
        get() {
            val title = "奇门"
            val icon = rememberVectorPainter(Icons.Filled.Home)
            return remember { TabOptions(index = 0u, title = title, icon = icon) }
        }

    @Composable
    override fun Content() {
        // 【关键】：Tab 内部独立的导航栈
        // 起点是 GenericReadyScreen，并告诉它下一步去 QiMenScreen
        Navigator(GenericReadyScreen(targetIndex = 0)) { navigator ->
            SlideTransition(navigator)
        }
    }
}

// 2. 六壬 Tab
object LiuRenTab : Tab {
    override val options: TabOptions
        @Composable
        get() {
            val title = "六壬"
            val icon = rememberVectorPainter(Icons.Filled.Category)
            return remember { TabOptions(index = 1u, title = title, icon = icon) }
        }

    @Composable
    override fun Content() {
        Navigator(GenericReadyScreen(targetIndex = 1)) { navigator ->
            SlideTransition(navigator)
        }
    }
}

// 3. 命理 Tab
object MingLiTab : Tab {
    override val options: TabOptions
        @Composable
        get() {
            val title = "命理"
            // 【修复】先在外面调用 Composable 函数获取 Painter
            val icon = rememberVectorPainter(Icons.Filled.Person)

            // 然后在 remember 内部只使用变量
            return remember { TabOptions(index = 2u, title = title, icon = icon) }
        }

    @Composable
    override fun Content() {
        Navigator(GenericReadyScreen(targetIndex = 2))
    }
}

// 4. 易经 Tab
object YiJingTab : Tab {
    override val options: TabOptions
        @Composable
        get() {
            val title = "易经"
            // 【修复】同上
            val icon = rememberVectorPainter(Icons.Filled.Book)

            return remember { TabOptions(index = 3u, title = title, icon = icon) }
        }

    @Composable
    override fun Content() {
        Navigator(GenericReadyScreen(targetIndex = 3))
    }
}

// 5. 设置 Tab
object SettingsTab : Tab {
    override val options: TabOptions
        @Composable
        get() {
            val title = "设置"
            val icon = rememberVectorPainter(Icons.Filled.Settings)
            return remember { TabOptions(index = 4u, title = title, icon = icon) }
        }

    @Composable
    override fun Content() {
        // 设置页通常不需要选时间，直接显示
        Navigator(SettingsScreen())
    }
}