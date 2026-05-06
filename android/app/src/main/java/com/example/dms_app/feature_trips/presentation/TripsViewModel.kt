package com.example.dms_app.feature_trips.presentation

import androidx.lifecycle.ViewModel
import com.example.dms_app.feature_trips.domain.TripSummary
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class TripsViewModel : ViewModel() {

    private val _trips = MutableStateFlow(
        listOf(
            TripSummary(1, "Morning Commute", "Today",     "08:15 - 08:42", 18, 85, "+15"),
            TripSummary(2, "Lunch Run",       "Today",     "12:30 - 13:05", 12, 72, "+8"),
            TripSummary(3, "Evening Drive",   "Yesterday", "18:00 - 18:45", 22, 91, "+18"),
            TripSummary(4, "Weekend Trip",    "Apr 26",    "09:00 - 14:30", 156, 78, "+5")
        )
    )
    val trips: StateFlow<List<TripSummary>> = _trips.asStateFlow()

    private val _activeFilter = MutableStateFlow("All")
    val activeFilter: StateFlow<String> = _activeFilter.asStateFlow()

    fun setFilter(f: String) {
        _activeFilter.value = f
        // In a real app, we would filter the trips list here
    }
}
