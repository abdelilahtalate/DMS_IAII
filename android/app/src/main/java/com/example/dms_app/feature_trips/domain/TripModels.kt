package com.example.dms_app.feature_trips.domain

enum class EventType { START, END, PHONE, WARNING }

data class TripEvent(
    val time: String,
    val type: EventType,
    val label: String,
    val durationSec: Int? = null
)

data class TripDetail(
    val id: String,
    val title: String,
    val date: String,
    val distance: String,
    val duration: String,
    val scoreImprovement: Int
)

data class TripSummary(
    val id: Int,
    val name: String,
    val date: String,
    val timeRange: String,
    val distanceKm: Int,
    val score: Int,
    val scoreChange: String,
    val rawTimestamp: Long = 0L
)

data class TripLocation(
    val latitude: Double,
    val longitude: Double,
    val timestamp: String
)
