package com.example.dms_app.core.config

object AppConfig {
    // For emulator testing, use 10.0.2.2 to reach localhost
    // Change this to your local IP (e.g., 192.168.1.X) when testing on a real device
    private const val HOST = "192.168.137.1"
    private const val PORT = "8000"

    val BASE_URL = "http://$HOST:$PORT/"
    val WS_BASE_URL = "ws://$HOST:$PORT/"
}
