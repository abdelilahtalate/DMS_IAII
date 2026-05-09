package com.example.dms_app.feature_insights.domain

data class DayScore(val day: String, val score: Int)

data class InsightStats(
    val averageScore: Int,
    val scoreChange: Int,
    val bestDay: String,
    val bestScore: Int,
    val totalTrips: Int,
    val eventCount: Int,
    val totalDistance: Double = 0.0,
    val totalDuration: Int = 0
)
