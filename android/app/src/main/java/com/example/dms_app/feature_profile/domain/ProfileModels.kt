package com.example.dms_app.feature_profile.domain

data class UserProfile(
    val name: String,
    val email: String,
    val isPro: Boolean = false,
    val totalTrips: Int = 0,
    val focusScore: Int = 0,
    val daysActive: Int = 0
)

data class Device(
    val id: String,
    val name: String,
    val type: DeviceType,
    val isConnected: Boolean
)

enum class DeviceType {
    CAMERA, OBD
}
