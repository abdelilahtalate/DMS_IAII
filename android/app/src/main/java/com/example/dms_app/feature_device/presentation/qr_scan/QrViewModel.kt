package com.example.dms_app.feature_device.presentation.qr_scan

import androidx.lifecycle.ViewModel

class QrViewModel : ViewModel() {

    fun parseQr(text: String): Pair<String, String>? {
        return try {
            val parts = text.split("&")
            val deviceId = parts[0].substringAfter("=")
            val secret = parts[1].substringAfter("=")
            deviceId to secret
        } catch (e: Exception) {
            null
        }
    }
}