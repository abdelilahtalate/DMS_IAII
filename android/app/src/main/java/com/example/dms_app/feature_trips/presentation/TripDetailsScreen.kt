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
import com.example.dms_app.feature_trips.domain.TripLocation
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
import com.google.maps.android.compose.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.JointType
import com.google.android.gms.maps.model.RoundCap

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
        if (state.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                androidx.compose.material3.CircularProgressIndicator(color = colors.cyan)
            }
        } else if (state.trip == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Trip details not found", color = colors.mutedForeground)
            }
        } else {
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
                            text = state.trip?.title ?: "",
                            style = MaterialTheme.typography.headlineMedium,
                            color = colors.foreground
                        )
                        Text(
                            text = state.trip?.date ?: "",
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.mutedForeground
                        )
                    }
                }

                // 2. ROUTE MAP CARD
                RouteMapCard(locations = state.locations, alerts = state.alertMarkers)

                Spacer(modifier = Modifier.height(24.dp))

                // 3. STATS ROW
                StatsRow(
                    distance = state.trip?.distance ?: "0 km",
                    duration = state.trip?.duration ?: "0 min",
                    scoreImprovement = state.trip?.scoreImprovement ?: 0
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
}

@Composable
fun RouteMapCard(locations: List<TripLocation>, alerts: List<TripLocation>) {
    val colors = LocalAppColors.current
    SurfaceCard(
        modifier = Modifier
            .padding(horizontal = 24.dp)
            .height(260.dp)
            .fillMaxWidth()
    ) {
        if (locations.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No map data available", color = colors.mutedForeground)
            }
        } else {
            val path = remember(locations) {
                locations.map { LatLng(it.latitude, it.longitude) }
            }
            
            val cameraPositionState = rememberCameraPositionState {
                position = CameraPosition.fromLatLngZoom(path.first(), 14f)
            }

            Box(modifier = Modifier.fillMaxSize()) {
                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = cameraPositionState,
                    uiSettings = MapUiSettings(
                        zoomControlsEnabled = false,
                        myLocationButtonEnabled = false,
                        mapToolbarEnabled = false
                    ),
                    properties = MapProperties(
                        isMyLocationEnabled = false,
                        mapType = MapType.NORMAL
                    )
                ) {
                    // 1. Draw the Path
                    Polyline(
                        points = path,
                        color = colors.cyan,
                        width = 10f,
                        startCap = RoundCap(),
                        endCap = RoundCap(),
                        jointType = JointType.ROUND
                    )

                    // 2. Start Marker
                    Marker(
                        state = MarkerState(position = path.first()),
                        title = "Start",
                        snippet = "Departure point"
                    )

                    // 3. End Marker
                    Marker(
                        state = MarkerState(position = path.last()),
                        title = "End",
                        snippet = "Destination"
                    )

                    // 4. Alert Markers
                    alerts.forEach { alert ->
                        Marker(
                            state = MarkerState(position = LatLng(alert.latitude, alert.longitude)),
                            title = "Risk Alert",
                            alpha = 0.8f
                        )
                    }
                }

                // Legend Overlay
                Box(
                    modifier = Modifier
                        .padding(12.dp)
                        .align(Alignment.BottomStart)
                        .clip(RoundedCornerShape(12.dp))
                        .background(colors.surface.copy(alpha = 0.8f))
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        LegendItem(color = colors.success, label = "Start")
                        LegendItem(color = colors.cyan, label = "Path")
                        if (alerts.isNotEmpty()) {
                            LegendItem(color = colors.redAlert, label = "Risk")
                        }
                    }
                }
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
