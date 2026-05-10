package com.example.dms_app.core.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.example.dms_app.core.ui.theme.LocalAppColors
import com.example.dms_app.core.ui.theme.PillShape

@Composable
fun ToggleSwitch(
    isOn: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalAppColors.current
    val thumbOffset by animateDpAsState(
        targetValue = if (isOn) 24.dp else 2.dp,
        animationSpec = spring(),
        label = "thumb_offset"
    )
    val thumbColor by animateColorAsState(
        targetValue = if (isOn) colors.cyan else colors.mutedForeground.copy(alpha = 0.5f),
        label = "thumb_color"
    )
    val bgColor by animateColorAsState(
        targetValue = if (isOn) colors.cyan.copy(alpha = 0.2f) else colors.surface3,
        label = "bg_color"
    )

    Box(
        modifier = modifier
            .width(50.dp)
            .height(28.dp)
            .clip(PillShape)
            .background(bgColor)
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
