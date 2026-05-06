package com.example.dms_app.core.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dms_app.core.ui.theme.LocalAppColors

enum class NavDestination { HOME, TRIPS, DRIVE, INSIGHTS, PROFILE }

private data class NavItem(val dest: NavDestination, val icon: ImageVector, val label: String)

@Composable
fun BottomNavBar(
    current: NavDestination,
    onNavigate: (NavDestination) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalAppColors.current

    val leftItems = listOf(
        NavItem(NavDestination.HOME, Icons.Rounded.Home, "Home"),
        NavItem(NavDestination.TRIPS, Icons.Rounded.DirectionsCar, "Trips")
    )
    val rightItems = listOf(
        NavItem(NavDestination.INSIGHTS, Icons.Rounded.BarChart, "Insights"),
        NavItem(NavDestination.PROFILE, Icons.Rounded.Person, "Profile")
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(90.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        // ── Aurora glow behind drive button ───────────────────────────────
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .size(width = 140.dp, height = 70.dp)
                .blur(28.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            colors.cyan.copy(alpha = 0.40f),
                            Color.Transparent
                        )
                    )
                )
        )

        // ── Pill bar ──────────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .height(64.dp)
                .clip(RoundedCornerShape(50))
                .background(colors.surface.copy(alpha = 0.92f))
                .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(50)),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            // Left items
            leftItems.forEach { item ->
                NavItemButton(
                    item = item,
                    isActive = current == item.dest,
                    onClick = { onNavigate(item.dest) }
                )
            }

            // Empty space for the center button
            Spacer(modifier = Modifier.width(52.dp))

            // Right items
            rightItems.forEach { item ->
                NavItemButton(
                    item = item,
                    isActive = current == item.dest,
                    onClick = { onNavigate(item.dest) }
                )
            }
        }

        // Center Drive button (pops above bar via offset, unclipped)
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 14.dp) // Lift it up instead of offset to avoid bounding box issues
                .size(52.dp)
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(
                        colors = listOf(Color(0xFF5EEAE0), Color(0xFF1BBDB5)),
                        start = Offset(0f, 0f),
                        end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                    )
                )
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { onNavigate(NavDestination.DRIVE) },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.PlayArrow,
                contentDescription = "Start Drive",
                tint = Color.Black,
                modifier = Modifier.size(26.dp)
            )
        }
    }
}

@Composable
private fun NavItemButton(
    item: NavItem,
    isActive: Boolean,
    onClick: () -> Unit
) {
    val colors = LocalAppColors.current
    
    val iconColor by animateColorAsState(
        targetValue = if (isActive) colors.cyan else colors.mutedForeground,
        animationSpec = tween(300),
        label = "nav_color"
    )
    
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.88f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "nav_scale"
    )

    Column(
        modifier = Modifier
            .graphicsLayer(scaleX = scale, scaleY = scale)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = item.icon,
            contentDescription = item.label,
            tint = iconColor,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = item.label,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
            color = iconColor
        )
    }
}
