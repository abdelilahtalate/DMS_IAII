package com.example.dms_app.feature_trips.presentation

import com.example.dms_app.feature_trips.domain.TripDetail
import com.example.dms_app.feature_trips.domain.TripEvent
import com.example.dms_app.feature_trips.domain.TripLocation

data class TripDetailsUiState(
    val trip: TripDetail? = null,
    val events: List<TripEvent> = emptyList(),
    val locations: List<TripLocation> = emptyList(),
    val alertMarkers: List<TripLocation> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)
