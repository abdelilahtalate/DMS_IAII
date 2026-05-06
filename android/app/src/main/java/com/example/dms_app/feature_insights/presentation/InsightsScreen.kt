package com.example.dms_app.feature_insights.presentation

import android.graphics.BlurMaskFilter
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.dms_app.core.ui.components.DriveMindBackground
import com.example.dms_app.core.ui.components.GradientCard
import com.example.dms_app.core.ui.components.SurfaceCard
import com.example.dms_app.core.ui.theme.LocalAppColors
import com.example.dms_app.feature_insights.domain.DayScore

@Composable
fun InsightsScreen(
    modifier: Modifier = Modifier,
    vm: InsightsViewModel = viewModel()
) {
    val state by vm.state.collectAsState()
    val colors = LocalAppColors.current

    DriveMindBackground {
        Column(
            modifier = modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 80.dp)
        ) {
            // 1. HEADER
            Column(
                modifier = Modifier
                    .padding(horizontal = 24.dp)
                    .padding(top = 48.dp, bottom = 24.dp)
            ) {
                Text(
                    text = "Insights",
                    style = MaterialTheme.typography.displaySmall,
                    color = colors.foreground
                )
                Text(
                    text = "Your driving analytics",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.mutedForeground
                )
            }

            // 2. WEEKLY CHART CARD
            state.weeklyScores.let { scores ->
                if (scores.isNotEmpty()) {
                    WeeklyChartCard(scores = scores)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 3. STATS GRID
            state.stats?.let { stats ->
                StatsGrid(stats = stats)
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 4. RISK HEATMAP CARD
            state.heatmapData.let { data ->
                if (data.isNotEmpty()) {
                    RiskHeatmapCard(data = data)
                }
            }
        }
    }
}

@Composable
fun WeeklyChartCard(scores: List<DayScore>) {
    val colors = LocalAppColors.current
    GradientCard(
        modifier = Modifier
            .padding(horizontal = 24.dp)
            .fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "Weekly Focus", style = MaterialTheme.typography.titleMedium, color = colors.foreground)
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.Rounded.TrendingUp,
                        contentDescription = null,
                        tint = colors.success,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Text(
                    text = "+8% vs last week",
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.success
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // AREA CHART
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(128.dp)
            ) {
                val width = size.width
                val height = size.height
                val spacing = width / (scores.size - 1)
                
                // Map scores [60-100] to [height-0]
                val points = scores.mapIndexed { index, dayScore ->
                    val x = index * spacing
                    val y = height - ((dayScore.score - 60).coerceAtLeast(0) / 40f) * height
                    androidx.compose.ui.geometry.Offset(x, y)
                }

                // 1. Draw filled area
                val fillPath = Path().apply {
                    moveTo(points.first().x, height)
                    points.forEach { lineTo(it.x, it.y) }
                    lineTo(points.last().x, height)
                    close()
                }
                drawPath(
                    path = fillPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(colors.cyan.copy(alpha = 0.3f), Color.Transparent),
                        startY = 0f,
                        endY = height
                    )
                )

                // 2. Draw polyline
                val strokePath = Path().apply {
                    moveTo(points.first().x, points.first().y)
                    points.drop(1).forEach { lineTo(it.x, it.y) }
                }
                drawPath(
                    path = strokePath,
                    color = colors.cyan,
                    style = Stroke(width = 2.dp.toPx(), join = StrokeJoin.Round)
                )

                // 3. Draw dots with glow
                points.forEach { point ->
                    drawIntoCanvas { canvas ->
                        val paint = android.graphics.Paint().apply {
                            color = colors.cyan.toArgb()
                            setMaskFilter(BlurMaskFilter(4.dp.toPx(), BlurMaskFilter.Blur.NORMAL))
                        }
                        canvas.nativeCanvas.drawCircle(point.x, point.y, 5.dp.toPx(), paint)
                    }
                    drawCircle(
                        color = colors.cyan,
                        radius = 4.dp.toPx(),
                        center = point
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Day labels
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                scores.forEach { 
                    Text(
                        text = it.day,
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.mutedForeground
                    )
                }
            }
        }
    }
}

@Composable
fun StatsGrid(stats: com.example.dms_app.feature_insights.domain.InsightStats) {
    val colors = LocalAppColors.current
    
    Column(modifier = Modifier.padding(horizontal = 24.dp)) {
        Row(modifier = Modifier.fillMaxWidth()) {
            StatCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Rounded.TrackChanges,
                iconColor = colors.cyan,
                label = "Average Score",
                value = stats.averageScore.toString(),
                trend = "+${stats.scoreChange} from last week",
                trendColor = colors.success
            )
            Spacer(modifier = Modifier.width(12.dp))
            StatCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Rounded.CalendarToday,
                iconColor = colors.success,
                label = "Best Day",
                value = stats.bestDay,
                trend = "${stats.bestScore} points",
                trendColor = colors.mutedForeground,
                valueStyle = MaterialTheme.typography.displaySmall
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            StatCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Rounded.TrendingUp,
                iconColor = colors.amberWarn,
                label = "Total Trips",
                value = stats.totalTrips.toString(),
                trend = "This week",
                trendColor = colors.mutedForeground
            )
            Spacer(modifier = Modifier.width(12.dp))
            StatCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Rounded.Warning,
                iconColor = colors.redAlert,
                label = "Events",
                value = stats.eventCount.toString(),
                trend = "Distraction alerts",
                trendColor = colors.mutedForeground
            )
        }
    }
}

@Composable
fun StatCard(
    modifier: Modifier = Modifier,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color,
    label: String,
    value: String,
    trend: String,
    trendColor: Color,
    valueStyle: androidx.compose.ui.text.TextStyle = MaterialTheme.typography.displayMedium
) {
    val colors = LocalAppColors.current
    SurfaceCard(modifier = modifier) {
        Column(modifier = Modifier.padding(16.dp)) {
            Icon(imageVector = icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = label, style = MaterialTheme.typography.labelSmall, color = colors.mutedForeground)
            Text(text = value, style = valueStyle, color = colors.foreground)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = trend, style = MaterialTheme.typography.labelSmall, color = trendColor)
        }
    }
}

@Composable
fun RiskHeatmapCard(data: List<List<Int>>) {
    val colors = LocalAppColors.current
    SurfaceCard(
        modifier = Modifier
            .padding(horizontal = 24.dp)
            .fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(text = "Risk Heatmap", style = MaterialTheme.typography.titleMedium, color = colors.foreground)
            Text(
                text = "Your risk patterns by time of day",
                style = MaterialTheme.typography.bodySmall,
                color = colors.mutedForeground,
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            val rows = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
            val cols = listOf("12a", "3a", "6a", "9a", "12p", "3p", "6p", "9p")
            
            Column {
                // Column Labels
                Row(modifier = Modifier.padding(start = 32.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    cols.forEach {
                        Text(text = it, style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp), color = colors.mutedForeground)
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))

                data.forEachIndexed { rowIndex, rowData ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = rows[rowIndex],
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            color = colors.mutedForeground,
                            modifier = Modifier.width(32.dp)
                        )
                        
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            rowData.forEach { value ->
                                val cellColor = when(value) {
                                    0 -> colors.surface2
                                    1 -> colors.cyan.copy(alpha = 0.2f)
                                    2 -> colors.cyan.copy(alpha = 0.4f)
                                    3 -> colors.amberWarn.copy(alpha = 0.5f)
                                    4 -> colors.redAlert.copy(alpha = 0.6f)
                                    5 -> colors.redAlert.copy(alpha = 0.8f)
                                    else -> colors.surface2
                                }
                                
                                Box(
                                    modifier = Modifier
                                        .size(18.dp)
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(cellColor)
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Legend
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Low", style = MaterialTheme.typography.labelSmall, color = colors.mutedForeground)
                Spacer(modifier = Modifier.width(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    listOf(0, 1, 2, 3, 4, 5).forEach { value ->
                        val color = when(value) {
                            0 -> colors.surface2
                            1 -> colors.cyan.copy(alpha = 0.2f)
                            2 -> colors.cyan.copy(alpha = 0.4f)
                            3 -> colors.amberWarn.copy(alpha = 0.5f)
                            4 -> colors.redAlert.copy(alpha = 0.6f)
                            5 -> colors.redAlert.copy(alpha = 0.8f)
                            else -> colors.surface2
                        }
                        Box(modifier = Modifier.size(12.dp).background(color))
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "High", style = MaterialTheme.typography.labelSmall, color = colors.mutedForeground)
            }
        }
    }
}
