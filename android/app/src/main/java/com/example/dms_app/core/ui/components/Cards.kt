package com.example.dms_app.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.dms_app.core.ui.theme.LocalAppColors

@Composable
fun SurfaceCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    val colors = LocalAppColors.current
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .border(1.dp, colors.border, RoundedCornerShape(20.dp)),
        color = colors.cardBg,
        content = content
    )
}

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val colors = LocalAppColors.current
    val bgColor = if (colors.isDark) Color.White.copy(alpha = 0.06f) else Color.Black.copy(alpha = 0.04f)
    val borderColor = if (colors.isDark) Color.White.copy(alpha = 0.1f) else Color.Black.copy(alpha = 0.08f)

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(20.dp))
            .padding(16.dp)
    ) {
        content()
    }
}

@Composable
fun GradientCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val colors = LocalAppColors.current
    val gradient = if (colors.isDark) {
        Brush.linearGradient(listOf(colors.surface, colors.surface2))
    } else {
        Brush.linearGradient(listOf(Color.White, colors.surface2))
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .background(gradient)
            .border(1.dp, colors.border, RoundedCornerShape(24.dp))
            .padding(20.dp)
    ) {
        content()
    }
}
