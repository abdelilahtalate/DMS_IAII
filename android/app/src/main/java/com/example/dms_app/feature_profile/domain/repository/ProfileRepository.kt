package com.example.dms_app.feature_profile.domain.repository

import com.example.dms_app.feature_profile.domain.Device
import com.example.dms_app.feature_profile.domain.UserProfile

interface ProfileRepository {
    suspend fun getProfile(userId: Int): Result<UserProfile>
    suspend fun updateProfile(userId: Int, profile: UserProfile): Result<UserProfile>
    suspend fun getDevices(userId: Int): Result<List<Device>>
    suspend fun addDevice(userId: Int, device: Device): Result<Device>
}
