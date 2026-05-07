package com.example.dms_app.feature_trips.domain.usecase

import com.example.dms_app.feature_trips.data.TripsRepository
import com.example.dms_app.feature_trips.domain.TripSummary
import java.text.SimpleDateFormat
import java.util.*

class GetTripsUseCase(private val repository: TripsRepository) {
    suspend operator fun invoke(userId: Int): Result<List<TripSummary>> {
        return try {
            val sessions = repository.listSessions(userId)
            val summaries = sessions.map { session ->
                TripSummary(
                    id = session.id,
                    name = "Trip #${session.id}",
                    date = formatDate(session.start_time),
                    timeRange = formatTimeRange(session.start_time, session.end_time),
                    distanceKm = session.total_distance / 1000,
                    score = session.average_focus,
                    scoreChange = if (session.alert_count == 0) "+10" else "-${session.alert_count * 2}",
                    rawTimestamp = parseToTimestamp(session.start_time)
                )
            }
            Result.success(summaries)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun formatDate(dateStr: String): String {
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            val outputFormat = SimpleDateFormat("MMM d", Locale.getDefault())
            val date = inputFormat.parse(dateStr)
            outputFormat.format(date!!)
        } catch (e: Exception) {
            dateStr
        }
    }

    private fun formatTimeRange(start: String, end: String?): String {
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            val outputFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
            val startDate = inputFormat.parse(start)
            val startTime = outputFormat.format(startDate!!)
            val endTime = if (end != null) {
                val endDate = inputFormat.parse(end)
                outputFormat.format(endDate!!)
            } else {
                "Ongoing"
            }
            "$startTime - $endTime"
        } catch (e: Exception) {
            "Unknown"
        }
    }

    private fun parseToTimestamp(dateStr: String): Long {
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            inputFormat.parse(dateStr)?.time ?: 0L
        } catch (e: Exception) {
            0L
        }
    }
}
