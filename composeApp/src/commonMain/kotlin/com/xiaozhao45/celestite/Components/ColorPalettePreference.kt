package com.xiaozhao45.celestite.Components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import com.xiaozhao45.celestite.PresetColors

@Composable
fun ColorPalettePreference(
    title: String = "主题颜色",
    description: String? = "选择应用的主色调",
    selectedColorInt: Int,
    onColorSelected: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        // 标题部分
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            Text(text = title, style = MaterialTheme.typography.bodyLarge)
            if (description != null) {
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 颜色选择列表
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(PresetColors) { color ->
                val isSelected = color.toArgb() == selectedColorInt

                ColorSwatch(
                    color = color,
                    isSelected = isSelected,
                    onClick = { onColorSelected(color.toArgb()) }
                )
            }
        }
    }
}

@Composable
fun ColorSwatch(
    color: Color,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(48.dp) // 触摸目标大小
            .clip(CircleShape)
            .background(color)
            .clickable(onClick = onClick)
    ) {
        if (isSelected) {
            // 选中状态：显示对钩，并加一个半透明的黑色遮罩让白色对钩更明显
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.2f))
            )
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Selected",
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}