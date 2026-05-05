package com.example.dms_app.core.ui.components

import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.example.dms_app.core.ui.theme.LocalAppColors
import com.example.dms_app.core.ui.theme.PillShape

@Composable
fun ThemeToggleSwitch(
    isDark: Boolean,
    onToggle: () -> Unit
) {
    val colors = LocalAppColors.current
    val thumbOffset by animateDpAsState(
        targetValue = if (isDark) 2.dp else 28.dp,
        animationSpec = spring(),
        label = "thumb_offset"
    )
    val thumbColor by animateColorAsState(
        targetValue = if (isDark) colors.cyan else colors.amberWarn,
        label = "thumb_color"
    )

    Box(
        modifier = Modifier
            .width(56.dp)
            .height(28.dp)
            .clip(PillShape)
            .background(if (isDark) colors.surface3 else colors.surface2)
            .clickable { onToggle() }
            .padding(2.dp)
    ) {
        Box(
            modifier = Modifier
                .offset(x = thumbOffset)
                .size(24.dp)
                .clip(CircleShape)
                .background(thumbColor)
        )
    }
}

@Composable
fun ThemeToggleIconButton(
    isDark: Boolean,
    onToggle: () -> Unit
) {
    val colors = LocalAppColors.current
    IconButton(
        onClick = onToggle,
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(colors.surface2)
    ) {
        Crossfade(targetState = isDark, label = "icon_anim") { dark ->
            if (dark) {
                Icon(
                    imageVector = Icons.Default.DarkMode,
                    contentDescription = "Dark Mode",
                    tint = colors.cyan
                )
            } else {
                Icon(
                    imageVector = Icons.Default.LightMode,
                    contentDescription = "Light Mode",
                    tint = colors.amberWarn
                )
            }
        }
    }
}
