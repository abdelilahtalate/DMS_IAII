package com.example.dms_app.feature_trips.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.dms_app.core.ui.components.*
import com.example.dms_app.core.ui.theme.AppColors
import com.example.dms_app.core.ui.theme.LocalAppColors
import com.example.dms_app.feature_trips.domain.TripSummary

@Composable
fun TripsListScreen(
    onNavigateToDetails: (Int) -> Unit,
    modifier: Modifier = Modifier,
    vm: TripsViewModel = viewModel()
) {
    val state by vm.state.collectAsState()
    val colors = LocalAppColors.current

    LaunchedEffect(Unit) {
        vm.loadTrips()
    }

    DriveMindBackground {
        Column(modifier = modifier.fillMaxSize()) {
            // Header
            Column(
                modifier = Modifier
                    .padding(horizontal = 24.dp)
                    .padding(top = 48.dp, bottom = 16.dp)
            ) {
                Text(
                    text = "My Trips",
                    style = MaterialTheme.typography.displaySmall,
                    color = colors.foreground
                )
                Text(
                    text = if (state.isLoading) "Loading trips..." else "${state.filteredTrips.size} trips shown",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.mutedForeground
                )
            }

            // Filter Row
            FilterRow(
                activeFilter = state.activeFilter,
                onFilterChange = { vm.setFilter(it) }
            )

            Spacer(modifier = Modifier.height(24.dp))

            if (state.isLoading && state.trips.isEmpty()) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    androidx.compose.material3.CircularProgressIndicator(color = colors.cyan)
                }
            } else if (state.filteredTrips.isEmpty()) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Rounded.History,
                            contentDescription = null,
                            tint = colors.mutedForeground,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No trips found for this period",
                            style = MaterialTheme.typography.bodyLarge,
                            color = colors.mutedForeground
                        )
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(start = 24.dp, end = 24.dp, bottom = 100.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(state.filteredTrips) { trip ->
                        TripCard(trip = trip, onClick = { onNavigateToDetails(trip.id) })
                    }
                }
            }
        }
    }
}

@Composable
fun FilterRow(activeFilter: String, onFilterChange: (String) -> Unit) {
    val filters = listOf("All", "Today", "This Week", "This Month")
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(filters) { filter ->
            FilterChip(
                text = filter,
                selected = filter == activeFilter,
                onClick = { onFilterChange(filter) }
            )
        }
    }
}

@Composable
fun TripCard(trip: TripSummary, onClick: () -> Unit) {
    val colors = LocalAppColors.current
    val sColor = scoreColor(trip.score, colors)

    SurfaceCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // a) SCORE BADGE
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(sColor.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = trip.score.toString(),
                    style = MaterialTheme.typography.displaySmall.copy(fontSize = 18.sp),
                    color = sColor,
                    fontWeight = FontWeight.Bold
                )
            }

            // b) INFO COLUMN
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = trip.name,
                        style = MaterialTheme.typography.titleMedium,
                        color = colors.foreground
                    )
                    
                    BadgePill(
                        text = trip.scoreChange,
                        variant = if (trip.scoreChange.startsWith("+")) StatusVariant.SUCCESS else StatusVariant.DANGER
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.AccessTime,
                        contentDescription = null,
                        tint = colors.mutedForeground,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = trip.timeRange,
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.mutedForeground
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.Place,
                        contentDescription = null,
                        tint = colors.mutedForeground,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${trip.distanceKm} km",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.mutedForeground
                    )
                }

                Text(
                    text = trip.date,
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.mutedForeground,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            Icon(
                imageVector = Icons.Rounded.ChevronRight,
                contentDescription = null,
                tint = colors.mutedForeground,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

fun scoreColor(score: Int, colors: AppColors) = when {
    score >= 80 -> colors.success
    score >= 60 -> colors.amberWarn
    else -> colors.redAlert
}
