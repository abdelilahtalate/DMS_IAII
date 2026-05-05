package com.example.dms_app.core.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.example.dms_app.core.ui.theme.LocalAppColors
import com.example.dms_app.core.ui.theme.PillShape

enum class StatusVariant { SUCCESS, WARNING, DANGER, INFO }

@Composable
fun FilterChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalAppColors.current
    val bgColor by animateColorAsState(if (selected) colors.cyan else colors.surface2, label = "chip_bg")
    val textColor = if (selected) (if (colors.isDark) Color.Black else Color.White) else colors.mutedForeground

    Box(
        modifier = modifier
            .height(36.dp)
            .clip(PillShape)
            .background(bgColor)
            .clickable { onClick() }
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = text, color = textColor, style = MaterialTheme.typography.labelMedium)
    }
}

@Composable
fun StatusChip(
    variant: StatusVariant,
    label: String,
    icon: ImageVector? = null,
    modifier: Modifier = Modifier
) {
    val colors = LocalAppColors.current
    val variantColor = when (variant) {
        StatusVariant.SUCCESS -> colors.success
        StatusVariant.WARNING -> colors.amberWarn
        StatusVariant.DANGER -> colors.redAlert
        StatusVariant.INFO -> colors.cyan
    }

    Box(
        modifier = modifier
            .clip(PillShape)
            .background(variantColor.copy(alpha = 0.2f))
            .border(1.dp, variantColor.copy(alpha = 0.3f), PillShape)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            if (icon != null) {
                Icon(imageVector = icon, contentDescription = null, tint = variantColor, modifier = Modifier.size(14.dp))
            }
            Text(text = label, color = variantColor, style = MaterialTheme.typography.labelMedium)
        }
    }
}

@Composable
fun BadgePill(
    text: String,
    variant: StatusVariant = StatusVariant.INFO,
    modifier: Modifier = Modifier
) {
    StatusChip(variant = variant, label = text, modifier = modifier)
}
