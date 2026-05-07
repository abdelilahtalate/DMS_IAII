package com.example.dms_app.core.network

import retrofit2.Response
import retrofit2.http.*
import okhttp3.ResponseBody
import com.google.gson.annotations.SerializedName

data class SignupRequest(
    val full_name: String,
    val email: String,
    val password: String,
    val gender: String,
    val age: Int? = null
)

data class LoginRequest(
    val email: String,
    val password: String
)

data class AuthResponse(
    val message: String?,
    val access_token: String?,
    val error: String? = null
)

data class PairRequest(
    val device_id: String,
    val secret: String,
    val user_email: String
)

data class DeviceInfo(
    val device_id: String,
    val preview_stream: String
)

data class PairResponse(
    val message: String,
    val device: DeviceInfo
)

data class StatusResponse(
    val status: String,
    val message: String? = null
)

// Profile Models
data class UserProfileResponse(
    val id: Int,
    val name: String,
    val email: String,
    val avatarUrl: String? = null,
    val isPro: Boolean = false,
    val totalTrips: Int = 0,
    val focusScore: Int = 0,
    val daysActive: Int = 0
)

data class UserProfileUpdate(
    val name: String,
    val email: String,
    val isPro: Boolean
)


data class ProfileDeviceResponse(
    val id: Int,
    val device_id: String,
    val secret: String,
    val name: String = "ESP Device",
    val type: String = "CAMERA",
    val isConnected: Boolean = true,
    val paired: Boolean = true
)

data class ProfileDeviceCreate(
    val id: String = "",
    val name: String = "",
    val type: String = "CAMERA",
    val isConnected: Boolean = false,
    val device_id: String = "",
    val secret: String = ""
)

// Insight Models
data class InsightStats(
    val averageScore: Int,
    val scoreChange: Int,
    val bestDay: String,
    val bestScore: Int,
    val totalTrips: Int,
    val eventCount: Int,
    val totalDistance: Double = 0.0,
    val totalDuration: Int = 0
)

data class DayScore(
    val day: String,
    val score: Int
)

data class InsightOverview(
    val stats: InsightStats,
    val dayScores: List<DayScore>,
    val heatmapData: List<List<Int>>? = null
)

data class SessionStart(
    val user_id: Int,
    val device_id: String
)

data class SessionResponse(
    @SerializedName("id")
    val session_id: Int,
    val status: String,
    val start_time: String
)

data class LocationCreate(
    val session_id: Int,
    val latitude: Double,
    val longitude: Double,
    val speed: Double
)

data class LocationBatch(
    val session_id: Int,
    val locations: List<LocationCreate>
)

data class AlertCreate(
    val session_id: Int,
    val alert_type: String,
    val latitude: Double,
    val longitude: Double
)

data class DistanceUpdate(
    val session_id: Int,
    val distance_meters: Int
)

data class ScoreUpdate(
    val session_id: Int,
    val average_focus: Int
)

data class SessionSummaryResponse(
    val id: Int,
    val user_id: Int,
    val device_id: Int,
    val status: String,
    val start_time: String,
    val end_time: String? = null,
    val total_distance: Int = 0,
    val average_focus: Int = 100,
    val alert_count: Int,
    val location_count: Int
)

data class SessionDetailResponse(
    val id: Int,
    val user_id: Int,
    val device_id: Int,
    val status: String,
    val start_time: String,
    val end_time: String? = null,
    val total_distance: Int = 0,
    val average_focus: Int = 100,
    val locations: List<LocationResponse>,
    val alerts: List<AlertResponse>
)

data class LocationResponse(
    val id: Int,
    val latitude: Double,
    val longitude: Double,
    val speed: Double,
    val timestamp: String
)

data class AlertResponse(
    val id: Int,
    val alert_type: String,
    val latitude: Double,
    val longitude: Double,
    val timestamp: String
)

interface ApiService {
    @POST("api/v1/auth/signup")
    suspend fun signup(@Body request: SignupRequest): AuthResponse

    @POST("api/v1/auth/login")
    suspend fun login(
        @Body request: LoginRequest
    ): AuthResponse

    @POST("api/v1/pairing/pair")
    suspend fun pairDevice(
        @Body request: PairRequest
    ): PairResponse

    @Streaming
    @GET
    suspend fun getStream(@Url url: String): Response<ResponseBody>

    @POST("api/v1/ai/start")
    suspend fun startAnalysis(): StatusResponse

    @POST("api/v1/ai/stop")
    suspend fun stopAnalysis(): StatusResponse

    @GET
    suspend fun stopPreview(@Url url: String): StatusResponse

    // Profile Endpoints
    @GET("api/v1/profile/{user_id}")
    suspend fun getProfile(@Path("user_id") userId: Int): UserProfileResponse

    @PUT("api/v1/profile/{user_id}")
    suspend fun updateProfile(
        @Path("user_id") userId: Int,
        @Body profile: UserProfileUpdate
    ): UserProfileResponse

    @GET("api/v1/profile/{user_id}/devices")
    suspend fun getProfileDevices(@Path("user_id") userId: Int): List<ProfileDeviceResponse>

    @POST("api/v1/profile/{user_id}/devices")
    suspend fun addProfileDevice(
        @Path("user_id") userId: Int,
        @Body data: ProfileDeviceCreate
    ): ProfileDeviceResponse

    // Session Endpoints
    @POST("api/v1/sessions/start")
    suspend fun startSession(@Body data: SessionStart): SessionResponse

    @POST("api/v1/sessions/stop/{session_id}")
    suspend fun stopSession(@Path("session_id") sessionId: Int): StatusResponse

    @POST("api/v1/sessions/distance")
    suspend fun updateDistance(@Body data: DistanceUpdate): StatusResponse

    @POST("api/v1/sessions/score")
    suspend fun updateScore(@Body data: ScoreUpdate): StatusResponse

    @POST("api/v1/sessions/location")
    suspend fun logLocation(@Body data: LocationCreate): StatusResponse

    @POST("api/v1/sessions/locations")
    suspend fun logLocations(@Body data: LocationBatch): StatusResponse

    @POST("api/v1/sessions/alert")
    suspend fun logAlert(@Body data: AlertCreate): StatusResponse

    @GET("api/v1/sessions/")
    suspend fun listSessions(@Query("user_id") userId: Int? = null): List<SessionSummaryResponse>

    // Insight Endpoints
    @GET("api/v1/insights/{user_id}/overview")
    suspend fun getInsightOverview(@Path("user_id") userId: Int): InsightOverview
}