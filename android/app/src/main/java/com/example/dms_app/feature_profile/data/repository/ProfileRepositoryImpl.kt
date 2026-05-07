package com.example.dms_app.feature_profile.data.repository

import com.example.dms_app.core.database.TokenManager
import com.example.dms_app.core.network.*
import com.example.dms_app.feature_profile.domain.Device
import com.example.dms_app.feature_profile.domain.DeviceType
import com.example.dms_app.feature_profile.domain.UserProfile
import com.example.dms_app.feature_profile.domain.repository.ProfileRepository

class ProfileRepositoryImpl(
    private val api: ApiService = ApiClient.apiService,
    private val tokenManager: TokenManager? = null
) : ProfileRepository {

    override suspend fun clearToken() {
        tokenManager?.clearToken()
    }

    override suspend fun getProfile(userId: Int): Result<UserProfile> {
        return try {
            val response = api.getProfile(userId)
            Result.success(mapToDomain(response))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateProfile(userId: Int, profile: UserProfile): Result<UserProfile> {
        return try {
            val request = UserProfileUpdate(
                name = profile.name,
                email = profile.email,
                isPro = profile.isPro
            )
            val response = api.updateProfile(userId, request)
            Result.success(mapToDomain(response))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getDevices(userId: Int): Result<List<Device>> {
        return try {
            val response = api.getProfileDevices(userId)
            Result.success(response.map { mapDeviceToDomain(it) })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun addDevice(userId: Int, device: Device): Result<Device> {
        return try {
            val request = ProfileDeviceCreate(
                id = device.id,
                name = device.name,
                type = device.type.name,
                isConnected = device.isConnected
            )
            val response = api.addProfileDevice(userId, request)
            Result.success(mapDeviceToDomain(response))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun mapToDomain(response: UserProfileResponse): UserProfile {
        return UserProfile(
            name = response.name,
            email = response.email,
            isPro = response.isPro,
            totalTrips = response.totalTrips,
            focusScore = response.focusScore,
            daysActive = response.daysActive
        )
    }

    private fun mapDeviceToDomain(response: ProfileDeviceResponse): Device {
        return Device(
            id = response.id.toString(),
            name = response.name,
            type = try { DeviceType.valueOf(response.type) } catch (e: Exception) { DeviceType.CAMERA },
            isConnected = response.isConnected
        )
    }
}
