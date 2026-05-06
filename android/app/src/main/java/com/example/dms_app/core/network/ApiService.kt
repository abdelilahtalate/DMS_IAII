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
    val age: Int?
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

data class PairResponse(
    val message: String,
    val device: DeviceInfo
)

data class DeviceInfo(
    val device_id: String,
    val preview_stream: String
)

data class StatusResponse(
    val status: String
)

data class UserProfileResponse(
    val name: String,
    val email: String,
    val isPro: Boolean,
    val totalTrips: Int,
    val focusScore: Int,
    val daysActive: Int
)

data class UserProfileUpdate(
    val name: String? = null,
    val email: String? = null,
    val isPro: Boolean = false
)

data class ProfileDeviceResponse(
    val id: String,
    val name: String,
    val type: String,
    val isConnected: Boolean
)

data class ProfileDeviceCreate(
    val id: String,
    val name: String? = null,
    val type: String = "CAMERA",
    val secret: String = "profile-device",
    val isConnected: Boolean = false
)

data class SessionSummaryResponse(
    val id: Int,
    val user_id: Int,
    val device_id: Int,
    val status: String,
    val start_time: String,
    val end_time: String? = null,
    val alert_count: Int,
    val location_count: Int
)

data class DayScore(
    val day: String,
    val score: Int
)

data class InsightStats(
    val averageScore: Int,
    val scoreChange: Int,
    val bestDay: String,
    val bestScore: Int,
    val totalTrips: Int,
    val eventCount: Int
)

data class InsightOverview(
    val stats: InsightStats,
    val dayScores: List<DayScore>
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

data class AlertCreate(
    val session_id: Int,
    val alert_type: String,
    val latitude: Double,
    val longitude: Double
)

interface ApiService {

    @POST("api/v1/auth/signup")
    suspend fun signup(
        @Body request: SignupRequest
    ): AuthResponse

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

    // Profile Endpoints
    @GET("api/v1/profile/{user_id}")
    suspend fun getProfile(@Path("user_id") userId: Int): UserProfileResponse

    @POST("api/v1/profile/{user_id}")
    suspend fun updateProfile(
        @Path("user_id") userId: Int,
        @Body data: UserProfileUpdate
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

    @POST("api/v1/sessions/location")
    suspend fun logLocation(@Body data: LocationCreate): StatusResponse

    @POST("api/v1/sessions/alert")
    suspend fun logAlert(@Body data: AlertCreate): StatusResponse

    @GET("api/v1/sessions/")
    suspend fun listSessions(@Query("user_id") userId: Int? = null): List<SessionSummaryResponse>

    // Insight Endpoints
    @GET("api/v1/insights/{user_id}/overview")
    suspend fun getInsightOverview(@Path("user_id") userId: Int): InsightOverview
}