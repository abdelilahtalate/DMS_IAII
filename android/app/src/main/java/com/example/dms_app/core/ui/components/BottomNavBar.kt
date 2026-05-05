package com.example.dms_app.core.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dms_app.core.ui.theme.LocalAppColors
import androidx.compose.animation.animateColorAsState

@Composable
fun BottomNavBar(
    selectedRoute: String,
    onRouteSelected: (String) -> Unit
) {
    val colors = LocalAppColors.current
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        Surface(
            modifier = Modifier
                .wrapContentWidth()
                .height(72.dp)
                .clip(RoundedCornerShape(50))
                .border(
                    width = 1.dp,
                    color = if (colors.isDark) Color.White.copy(alpha = 0.15f) else Color.Black.copy(alpha = 0.08f),
                    shape = RoundedCornerShape(50)
                ),
            color = if (colors.isDark) colors.surface.copy(alpha = 0.85f) else colors.surface.copy(alpha = 0.95f),
            tonalElevation = 8.dp
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                NavItem(Icons.Default.Home, "Home", selectedRoute == "home") { onRouteSelected("home") }
                NavItem(Icons.Default.Route, "Trips", selectedRoute == "trips") { onRouteSelected("trips") }
                
                DriveButton { onRouteSelected("drive") }
                
                NavItem(Icons.Default.BarChart, "Insights", selectedRoute == "insights") { onRouteSelected("insights") }
                NavItem(Icons.Default.Person, "Profile", selectedRoute == "profile") { onRouteSelected("profile") }
            }
        }
    }
}

@Composable
fun NavItem(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val colors = LocalAppColors.current
    val tint by animateColorAsState(if (isSelected) colors.cyan else colors.mutedForeground, label = "tint")
    val scale by animateFloatAsState(if (isSelected) 1.0f else 0.9f, label = "scale")

    Column(
        modifier = Modifier
            .width(60.dp)
            .fillMaxHeight()
            .clickable(onClick = onClick)
            .scale(scale),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = tint,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            color = tint,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp)
        )
    }
}

@Composable
fun DriveButton(onClick: () -> Unit) {
    val colors = LocalAppColors.current
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "scale"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "alpha"
    )

    Box(
        modifier = Modifier
            .size(72.dp)
            .offset(y = (-12).dp),
        contentAlignment = Alignment.Center
    ) {
        // Pulse Effect
        Canvas(modifier = Modifier.size(56.dp)) {
            drawCircle(
                color = colors.cyan.copy(alpha = 0.3f * pulseAlpha),
                radius = (size.minDimension / 2) * pulseScale
            )
        }

        Surface(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .clickable(onClick = onClick),
            color = Color.Transparent
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.linearGradient(
                            colors = listOf(colors.cyan, colors.cyan.copy(alpha = 0.7f))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Start Drive",
                    tint = Color.Black,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}
