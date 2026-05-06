package com.example.dms_app.feature_trips.presentation

import android.graphics.BlurMaskFilter
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.dms_app.core.ui.components.DriveMindBackground
import com.example.dms_app.core.ui.components.SurfaceCard
import com.example.dms_app.core.ui.theme.LocalAppColors
import com.example.dms_app.core.ui.theme.AppColors
import com.example.dms_app.feature_trips.domain.EventType
import com.example.dms_app.feature_trips.domain.TripEvent

@Composable
fun TripDetailsScreen(
    tripId: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    vm: TripDetailsViewModel = viewModel()
) {
    val state by vm.state.collectAsState()
    val colors = LocalAppColors.current

    LaunchedEffect(tripId) {
        vm.loadTrip(tripId)
    }

    DriveMindBackground {
        Column(
            modifier = modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // 1. TOP BAR
            Row(
                modifier = Modifier
                    .padding(horizontal = 24.dp)
                    .padding(top = 48.dp, bottom = 24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(colors.surface2)
                        .clickable { onBack() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.ArrowBack,
                        contentDescription = "Back",
                        tint = colors.foreground,
                        modifier = Modifier.size(20.dp)
                    )
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Column {
                    Text(
                        text = state.trip?.title ?: "Morning Commute",
                        style = MaterialTheme.typography.headlineMedium,
                        color = colors.foreground
                    )
                    Text(
                        text = state.trip?.date ?: "April 28, 2026",
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.mutedForeground
                    )
                }
            }

            // 2. ROUTE MAP CARD
            RouteMapCard()

            Spacer(modifier = Modifier.height(24.dp))

            // 3. STATS ROW
            StatsRow(
                distance = state.trip?.distance ?: "18 km",
                duration = state.trip?.duration ?: "27 min",
                scoreImprovement = state.trip?.scoreImprovement ?: 15
            )

            Spacer(modifier = Modifier.height(32.dp))

            // 4. TIMELINE SECTION
            Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                Text(
                    text = "Event Timeline",
                    style = MaterialTheme.typography.titleMedium,
                    color = colors.foreground,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                TimelineSection(events = state.events)
            }
            
            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}

@Composable
fun RouteMapCard() {
    val colors = LocalAppColors.current
    SurfaceCard(
        modifier = Modifier
            .padding(horizontal = 24.dp)
            .height(160.dp)
            .fillMaxWidth()
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val width = constraints.maxWidth.toFloat()
            val height = constraints.maxHeight.toFloat()

            Canvas(modifier = Modifier.fillMaxSize()) {
                val path = Path().apply {
                    moveTo(width * 0.15f, height * 0.7f)
                    cubicTo(
                        width * 0.4f, height * 0.2f,
                        width * 0.6f, height * 0.8f,
                        width * 0.85f, height * 0.3f
                    )
                }

                // Glow effect
                drawIntoCanvas { canvas ->
                    val paint = android.graphics.Paint().apply {
                        color = colors.cyan.toArgb()
                        strokeWidth = 6.dp.toPx()
                        style = android.graphics.Paint.Style.STROKE
                        strokeCap = android.graphics.Paint.Cap.ROUND
                        maskFilter = BlurMaskFilter(6.dp.toPx(), BlurMaskFilter.Blur.NORMAL)
                    }
                    canvas.nativeCanvas.drawPath(path.asAndroidPath(), paint)
                }

                // Bezier route path
                drawPath(
                    path = path,
                    color = colors.cyan,
                    style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                )

                // Start dot
                drawCircle(
                    color = colors.success,
                    radius = 5.dp.toPx(),
                    center = androidx.compose.ui.geometry.Offset(width * 0.15f, height * 0.7f)
                )

                // End dot
                drawCircle(
                    color = colors.cyan,
                    radius = 5.dp.toPx(),
                    center = androidx.compose.ui.geometry.Offset(width * 0.85f, height * 0.3f)
                )

                // Event dots on path (approximated positions)
                drawCircle(
                    color = colors.redAlert,
                    radius = 3.5.dp.toPx(),
                    center = androidx.compose.ui.geometry.Offset(width * 0.45f, height * 0.45f)
                )
                drawCircle(
                    color = colors.amberWarn,
                    radius = 3.5.dp.toPx(),
                    center = androidx.compose.ui.geometry.Offset(width * 0.65f, height * 0.65f)
                )
            }

            // Start/End labels
            Text(
                text = "Home",
                style = MaterialTheme.typography.bodySmall,
                color = colors.mutedForeground,
                modifier = Modifier
                    .padding(start = maxWidth * 0.12f, top = maxHeightToDp(0.75f))
            )
            Text(
                text = "Work",
                style = MaterialTheme.typography.bodySmall,
                color = colors.mutedForeground,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(end = maxWidth * 0.12f, top = maxHeightToDp(0.15f))
            )

            // Legend Row
            Row(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                LegendItem(color = colors.redAlert, label = "Phone")
                LegendItem(color = colors.amberWarn, label = "Warning")
            }
        }
    }
}

@Composable
private fun BoxWithConstraintsScope.maxHeightToDp(fraction: Float): androidx.compose.ui.unit.Dp {
    return maxHeight * fraction
}

@Composable
fun LegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(color))
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = label, style = MaterialTheme.typography.bodySmall, color = LocalAppColors.current.mutedForeground)
    }
}

@Composable
fun StatsRow(distance: String, duration: String, scoreImprovement: Int) {
    val colors = LocalAppColors.current
    Row(
        modifier = Modifier
            .padding(horizontal = 24.dp)
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SurfaceCard(modifier = Modifier.weight(1f)) {
            Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = distance, style = MaterialTheme.typography.titleMedium, color = colors.foreground)
                Text(text = "Distance", style = MaterialTheme.typography.labelSmall, color = colors.mutedForeground)
            }
        }
        SurfaceCard(modifier = Modifier.weight(1f)) {
            Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = duration, style = MaterialTheme.typography.titleMedium, color = colors.foreground)
                Text(text = "Duration", style = MaterialTheme.typography.labelSmall, color = colors.mutedForeground)
            }
        }
        Box(
            modifier = Modifier
                .weight(1f)
                .height(60.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(colors.success.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "+$scoreImprovement", style = MaterialTheme.typography.titleMedium, color = colors.success)
                Text(text = "Score", style = MaterialTheme.typography.labelSmall, color = colors.success)
            }
        }
    }
}

@Composable
fun TimelineSection(events: List<TripEvent>) {
    val colors = LocalAppColors.current
    Box(modifier = Modifier.fillMaxWidth()) {
        // Vertical line runs from top to bottom
        Box(
            modifier = Modifier
                .padding(start = 15.5.dp)
                .width(1.dp)
                .fillMaxHeight()
                .background(colors.surface3)
        )
        
        Column {
            events.forEach { event ->
                TimelineEventRow(event = event)
            }
        }
    }
}

@Composable
fun TimelineEventRow(event: TripEvent) {
    val colors = LocalAppColors.current
    val eColor = eventColor(event.type, colors)
    val eIcon = eventIcon(event.type)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 24.dp),
        verticalAlignment = Alignment.Top
    ) {
        // Timeline dot with zIndex above line
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(eColor.copy(alpha = 0.2f))
                .zIndex(1f),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = eIcon,
                contentDescription = null,
                tint = eColor,
                modifier = Modifier.size(14.dp)
            )
        }
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = event.label,
                    style = MaterialTheme.typography.titleMedium,
                    color = colors.foreground
                )
                Text(
                    text = event.time,
                    style = MaterialTheme.typography.labelSmall, // Uses JetBrains Mono via theme
                    color = colors.mutedForeground
                )
            }
            if (event.durationSec != null) {
                Text(
                    text = "Duration: ${event.durationSec}s",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.mutedForeground,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

// Helper functions as requested
fun eventColor(type: EventType, colors: AppColors) = when(type) {
    EventType.START, EventType.END -> colors.cyan
    EventType.PHONE -> colors.redAlert
    EventType.WARNING -> colors.amberWarn
}

fun eventIcon(type: EventType) = when(type) {
    EventType.START, EventType.END -> Icons.Rounded.Place
    EventType.PHONE -> Icons.Rounded.Phone
    EventType.WARNING -> Icons.Rounded.Warning
}
