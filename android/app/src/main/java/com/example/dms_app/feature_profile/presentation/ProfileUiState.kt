package com.example.dms_app.feature_profile.presentation

import com.example.dms_app.feature_profile.domain.Device
import com.example.dms_app.feature_profile.domain.UserProfile

data class ProfileUiState(
    val userProfile: UserProfile? = null,
    val connectedDevices: List<Device> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)
