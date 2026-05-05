package com.example.dms_app.feature_device.presentation.pairing

data class PairingUiState(
    val isLoading: Boolean = false,
    val success: Boolean = false,
    val previewUrl: String? = null,
    val error: String? = null
)