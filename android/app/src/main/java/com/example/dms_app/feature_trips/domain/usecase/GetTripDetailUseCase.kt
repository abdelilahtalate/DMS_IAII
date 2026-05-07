package com.example.dms_app.feature_trips.domain.usecase

import com.example.dms_app.feature_trips.data.TripsRepository
import com.example.dms_app.feature_trips.domain.EventType
import com.example.dms_app.feature_trips.domain.TripDetail
import com.example.dms_app.feature_trips.domain.TripEvent
import com.example.dms_app.feature_trips.domain.TripLocation
import java.text.SimpleDateFormat
import java.util.*

class GetTripDetailUseCase(private val repository: TripsRepository) {
    suspend operator fun invoke(sessionId: Int): Result<Triple<TripDetail, List<TripEvent>, Pair<List<TripLocation>, List<TripLocation>>>> {
        return try {
            val session = repository.getSessionDetail(sessionId)
            
            val detail = TripDetail(
                id = session.id.toString(),
                title = "Trip #${session.id}",
                date = formatDateFull(session.start_time),
                distance = "${session.total_distance / 1000} km",
                duration = formatDuration(session.start_time, session.end_time),
                scoreImprovement = session.average_focus
            )
            
            val events = mutableListOf<TripEvent>()
            events.add(TripEvent(formatTime(session.start_time), EventType.START, "Started Trip"))
            
            session.alerts.forEach { alert ->
                val type = when (alert.alert_type) {
                    "fatigue" -> EventType.WARNING
                    "distraction" -> EventType.PHONE
                    "phone_usage" -> EventType.PHONE
                    else -> EventType.WARNING
                }
                events.add(TripEvent(formatTime(alert.timestamp), type, alert.alert_type.replace("_", " ").capitalize()))
            }
            
            if (session.end_time != null) {
                events.add(TripEvent(formatTime(session.end_time), EventType.END, "Arrived at Destination"))
            }
            
            events.sortBy { it.time }
            
            val locations = session.locations.map { 
                TripLocation(it.latitude, it.longitude, it.timestamp) 
            }
            val alertMarkers = session.alerts.map { 
                TripLocation(it.latitude, it.longitude, it.timestamp) 
            }
            
            Result.success(Triple(detail, events, Pair(locations, alertMarkers)))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun formatDateFull(dateStr: String): String {
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            val outputFormat = SimpleDateFormat("MMMM d, yyyy", Locale.getDefault())
            val date = inputFormat.parse(dateStr)
            outputFormat.format(date!!)
        } catch (e: Exception) {
            dateStr
        }
    }

    private fun formatTime(dateStr: String): String {
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            val outputFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
            val date = inputFormat.parse(dateStr)
            outputFormat.format(date!!)
        } catch (e: Exception) {
            dateStr
        }
    }

    private fun formatDuration(start: String, end: String?): String {
        if (end == null) return "Ongoing"
        return try {
            val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            val startDate = format.parse(start)
            val endDate = format.parse(end)
            val diffMs = endDate!!.time - startDate!!.time
            val diffMin = diffMs / (1000 * 60)
            "${diffMin} min"
        } catch (e: Exception) {
            "Unknown"
        }
    }
}
