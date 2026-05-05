package com.example.dms_app.feature_device.domain

import com.example.dms_app.core.network.PairResponse
import com.example.dms_app.core.network.PairRequest
import com.example.dms_app.feature_device.data.DeviceRepository

class PairDeviceUseCase(
    private val repository: DeviceRepository
) {
    suspend operator fun invoke(
     request: PairRequest
    ): PairResponse {
        return repository.pairDevice(request)
    }
}
