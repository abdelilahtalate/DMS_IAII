package com.example.dms_app.feature_trips.presentation

import com.example.dms_app.feature_trips.domain.TripSummary

data class TripsUiState(
    val trips: List<TripSummary> = emptyList(),
    val filteredTrips: List<TripSummary> = emptyList(),
    val activeFilter: String = "All",
    val isLoading: Boolean = false,
    val error: String? = null
)
