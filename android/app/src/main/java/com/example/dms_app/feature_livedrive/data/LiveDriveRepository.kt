package com.example.dms_app.feature_livedrive.data

import com.example.dms_app.core.network.ApiClient
import com.example.dms_app.core.network.StatusResponse
import com.example.dms_app.core.config.AppConfig
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import okhttp3.*
import java.util.concurrent.TimeUnit

class LiveDriveRepository {

    private val client = OkHttpClient.Builder()
        .readTimeout(0, TimeUnit.MILLISECONDS)
        .build()

    suspend fun startAnalysis(): StatusResponse {
        return ApiClient.apiService.startAnalysis()
    }

    suspend fun stopAnalysis(): StatusResponse {
        return ApiClient.apiService.stopAnalysis()
    }

    suspend fun startSession(userId: Int, deviceId: String): com.example.dms_app.core.network.SessionResponse {
        return ApiClient.apiService.startSession(com.example.dms_app.core.network.SessionStart(userId, deviceId))
    }

    suspend fun stopSession(sessionId: Int): StatusResponse {
        return ApiClient.apiService.stopSession(sessionId)
    }

    suspend fun logLocation(sessionId: Int, lat: Double, lon: Double, speed: Double): StatusResponse {
        return ApiClient.apiService.logLocation(com.example.dms_app.core.network.LocationCreate(sessionId, lat, lon, speed))
    }

    suspend fun logAlert(sessionId: Int, alertType: String, lat: Double, lon: Double): StatusResponse {
        return ApiClient.apiService.logAlert(com.example.dms_app.core.network.AlertCreate(sessionId, alertType, lat, lon))
    }

    fun observeAiResults(): Flow<String> = callbackFlow {
        val request = Request.Builder()
            .url("${AppConfig.WS_BASE_URL}api/v1/ai/stream_analytics")
            .build()

        val listener = object : WebSocketListener() {
            override fun onMessage(webSocket: WebSocket, text: String) {
                trySend(text)
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                close()
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                close(t)
            }
        }

        val ws = client.newWebSocket(request, listener)

        awaitClose {
            ws.close(1000, "Closed by app")
        }
    }
}
