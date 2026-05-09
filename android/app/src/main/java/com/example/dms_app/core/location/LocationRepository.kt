package com.example.dms_app.core.location

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlin.math.roundToInt

class LocationRepository(
    private val locationClient: LocationClient
) {
    fun getSpeedUpdates(): Flow<Int> {
        return locationClient.getLocationUpdates(2000L).map { location ->
            (location.speed * 3.6).roundToInt() // Convert m/s to km/h
        }
    }

    fun getLocationUpdates(interval: Long): Flow<android.location.Location> {
        return locationClient.getLocationUpdates(interval)
    }
}
