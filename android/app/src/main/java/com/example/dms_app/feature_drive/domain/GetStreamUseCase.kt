package com.example.dms_app.feature_drive.domain

import android.graphics.Bitmap
import com.example.dms_app.feature_drive.data.DriveRepository
import kotlinx.coroutines.flow.Flow

class GetStreamUseCase(private val repository: DriveRepository) {
    operator fun invoke(url: String): Flow<Bitmap> {
        return repository.getCameraStream(url)
    }
}
