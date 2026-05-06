package com.example.dms_app.feature_home.presentation

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.rounded.AccessTime
import androidx.compose.material.icons.rounded.TrendingUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.PaintingStyle
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.dms_app.core.ui.components.DriveMindBackground
import com.example.dms_app.core.ui.components.GradientCard
import com.example.dms_app.core.ui.components.SurfaceCard
import com.example.dms_app.core.ui.components.ThemeToggleIconButton
import com.example.dms_app.core.ui.theme.LocalAppColors

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    vm: HomeViewModel = viewModel(),
    isDarkTheme: Boolean = true,
    onToggleTheme: () -> Unit = {},
    onStartDrive: () -> Unit,
    onTripClick: (String) -> Unit
) {
    val colors = LocalAppColors.current
    val greeting by vm.greeting.collectAsState()
    val driverName by vm.driverName.collectAsState()
    val focusScore by vm.focusScore.collectAsState()
    val todayStats by vm.todayStats.collectAsState()
    val recentTrips by vm.recentTrips.collectAsState()

    DriveMindBackground {
        Column(
            modifier = modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
        // TOP BAR
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(top = 48.dp, bottom = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = greeting, style = MaterialTheme.typography.bodyMedium, color = colors.mutedForeground)
                Text(text = driverName, style = MaterialTheme.typography.displaySmall, color = colors.foreground)
            }
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                ThemeToggleIconButton(isDark = isDarkTheme, onToggle = onToggleTheme)
                Spacer(modifier = Modifier.width(16.dp))
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(colors.surface2),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = driverName.split(" ").mapNotNull { it.firstOrNull()?.uppercase() }.joinToString("").take(2),
                        style = MaterialTheme.typography.titleMedium,
                        color = colors.foreground
                    )
                }
            }
        }

        // FOCUS SCORE CARD
        GradientCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Circular Score Ring
                Box(modifier = Modifier.size(100.dp), contentAlignment = Alignment.Center) {
                    val arcColor = Brush.sweepGradient(listOf(colors.cyan, colors.success))
                    val bgArcColor = colors.surface3
                    val blurColor = colors.cyan.toArgb()
                    
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawCircle(color = bgArcColor, style = Stroke(width = 8.dp.toPx()))
                        
                        val startAngle = -90f
                        val sweepAngle = (focusScore / 100f) * 360f
                        
                        // Glow
                        drawIntoCanvas { canvas ->
                            val paint = Paint().apply {
                                style = PaintingStyle.Stroke
                                strokeWidth = 8.dp.toPx()
                                strokeCap = StrokeCap.Round
                            }
                            val frameworkPaint = paint.asFrameworkPaint()
                            frameworkPaint.color = blurColor
                            frameworkPaint.maskFilter = android.graphics.BlurMaskFilter(20f, android.graphics.BlurMaskFilter.Blur.NORMAL)
                            
                            canvas.drawArc(
                                rect = androidx.compose.ui.geometry.Rect(Offset.Zero, size),
                                startAngle = startAngle,
                                sweepAngle = sweepAngle,
                                useCenter = false,
                                paint = paint
                            )
                        }

                        // Progress Arc
                        drawArc(
                            brush = arcColor,
                            startAngle = startAngle,
                            sweepAngle = sweepAngle,
                            useCenter = false,
                            style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
                        )
                    }
                    
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = focusScore.toString(), style = MaterialTheme.typography.displaySmall, color = colors.foreground)
                        Text(text = "Score", style = MaterialTheme.typography.labelSmall, color = colors.mutedForeground)
                    }
                }
                
                Spacer(modifier = Modifier.width(24.dp))
                
                // Stats Grid
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "Today's Summary", style = MaterialTheme.typography.titleMedium, color = colors.foreground)
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text(text = "Trips", style = MaterialTheme.typography.labelSmall, color = colors.mutedForeground)
                            Text(text = todayStats.trips.toString(), style = MaterialTheme.typography.displaySmall, color = colors.foreground)
                        }
                        Column {
                            Text(text = "Distance", style = MaterialTheme.typography.labelSmall, color = colors.mutedForeground)
                            Text(text = todayStats.distance, style = MaterialTheme.typography.displaySmall, color = colors.foreground)
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text(text = "Duration", style = MaterialTheme.typography.labelSmall, color = colors.mutedForeground)
                            Text(text = todayStats.duration, style = MaterialTheme.typography.displaySmall, color = colors.foreground)
                        }
                        Column {
                            Text(text = "Score", style = MaterialTheme.typography.labelSmall, color = colors.mutedForeground)
                            Text(text = todayStats.scoreChange, style = MaterialTheme.typography.displaySmall, color = colors.success)
                        }
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // START DRIVE BUTTON
        StartDriveButton(onStartDrive = onStartDrive)
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // RECENT TRIPS SECTION
        Text(
            text = "Recent Trips",
            style = MaterialTheme.typography.titleMedium,
            color = colors.foreground,
            modifier = Modifier.padding(horizontal = 24.dp)
        )
        Spacer(modifier = Modifier.height(12.dp))
        
        Column(modifier = Modifier.padding(horizontal = 24.dp)) {
            recentTrips.forEach { trip ->
                SurfaceCard(
                    modifier = Modifier
                        .padding(bottom = 12.dp)
                        .fillMaxWidth()
                        .clickable { onTripClick(trip.id) }
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val iconBg = if (trip.isPositive) colors.success.copy(alpha = 0.2f) else colors.amberWarn.copy(alpha = 0.2f)
                        val iconColor = if (trip.isPositive) colors.success else colors.amberWarn
                        val icon = if (trip.isPositive) Icons.Rounded.TrendingUp else Icons.Rounded.AccessTime
                        
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(iconBg),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(imageVector = icon, contentDescription = null, tint = iconColor)
                        }
                        
                        Spacer(modifier = Modifier.width(16.dp))
                        
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = trip.name, style = MaterialTheme.typography.titleMedium, color = colors.foreground)
                            Text(text = trip.timeRange, style = MaterialTheme.typography.bodySmall, color = colors.mutedForeground)
                        }
                        
                        Column(horizontalAlignment = Alignment.End) {
                            Text(text = trip.scoreChange, style = MaterialTheme.typography.titleMedium, color = iconColor)
                            Text(text = "Score", style = MaterialTheme.typography.labelSmall, color = colors.mutedForeground)
                        }
                    }
                }
            }
        }
    }
}
}

@Composable
fun StartDriveButton(onStartDrive: () -> Unit) {
    val colors = LocalAppColors.current
    val isPressed = remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed.value) 0.97f else 1f,
        animationSpec = spring(),
        label = "button_scale"
    )
    
    val infiniteTransition = rememberInfiniteTransition(label = "shimmer")
    val shimmerOffset by infiniteTransition.animateFloat(
        initialValue = -500f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_anim"
    )

    Box(
        modifier = Modifier
            .padding(horizontal = 24.dp)
            .fillMaxWidth()
            .height(72.dp)
            .scale(scale)
            .clickable { onStartDrive() }
    ) {
        // Glow Shadow
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawIntoCanvas { canvas ->
                val paint = Paint().apply {
                    val frameworkPaint = this.asFrameworkPaint()
                    frameworkPaint.color = colors.cyan.toArgb()
                    frameworkPaint.maskFilter = android.graphics.BlurMaskFilter(20.dp.toPx(), android.graphics.BlurMaskFilter.Blur.NORMAL)
                }
                canvas.drawRoundRect(
                    left = 0f, top = 0f, right = size.width, bottom = size.height,
                    radiusX = 24.dp.toPx(), radiusY = 24.dp.toPx(),
                    paint = paint
                )
            }
        }
        
        // Button Surface
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(24.dp))
                .background(Brush.linearGradient(listOf(colors.cyan, colors.cyan.copy(alpha = 0.8f))))
        ) {
            // Shimmer
            Canvas(modifier = Modifier.fillMaxSize()) {
                val shimmerBrush = Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0f),
                        Color.White.copy(alpha = 0.2f),
                        Color.White.copy(alpha = 0f)
                    ),
                    start = Offset(shimmerOffset, 0f),
                    end = Offset(shimmerOffset + 200f, size.height)
                )
                drawRect(shimmerBrush)
            }
            
            // Content
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Start Drive",
                    tint = Color.Black,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Start Drive",
                    style = MaterialTheme.typography.displaySmall,
                    color = Color.Black,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
