package com.example.dms_app.feature_home.domain

import com.example.dms_app.core.network.StatusResponse
import com.example.dms_app.feature_home.data.HomeRepository

class StopAnalysisUseCase(private val repository: HomeRepository) {
    suspend operator fun invoke(): StatusResponse {
        return repository.stopAnalysis()
    }
}
