package com.example.dms_app.core.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.dp
import com.example.dms_app.core.ui.theme.LocalAppColors

@Composable
fun DriveMindBackground(
    content: @Composable BoxScope.() -> Unit
) {
    val colors = LocalAppColors.current
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        // Dot Grid
        Canvas(modifier = Modifier.fillMaxSize()) {
            val spacing = 50.dp.toPx()
            val radius = 1.dp.toPx()
            val dotColor = colors.cyan.copy(alpha = 0.05f)
            
            var x = 0f
            while (x < size.width) {
                var y = 0f
                while (y < size.height) {
                    drawCircle(dotColor, radius, Offset(x, y))
                    y += spacing
                }
                x += spacing
            }
        }
        
        content()
    }
}
