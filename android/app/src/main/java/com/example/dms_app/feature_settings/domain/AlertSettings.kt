package com.example.dms_app.feature_settings.domain

data class AlertSettings(
    val fatigueSensitivity: String,
    val phoneSensitivity: String,
    val distractionSensitivity: String,
    val voiceAlerts: Boolean,
    val hapticFeedback: Boolean,
    val criticalOnly: Boolean,
    val preferredVoice: String
) {
    companion object {
        fun defaults(): AlertSettings {
            return AlertSettings(
                fatigueSensitivity = "HIGH",
                phoneSensitivity = "MEDIUM",
                distractionSensitivity = "HIGH",
                voiceAlerts = true,
                hapticFeedback = true,
                criticalOnly = false,
                preferredVoice = "DEFAULT"
            )
        }
    }
}
