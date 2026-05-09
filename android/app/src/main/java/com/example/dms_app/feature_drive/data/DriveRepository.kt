package com.example.dms_app.feature_drive.data

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.example.dms_app.core.network.ApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.isActive
import java.io.BufferedInputStream

class DriveRepository(
    private val apiService: ApiService
) {

    // Reuse options and bitmap to reduce GC pressure
    private val options = BitmapFactory.Options().apply {
        inMutable = true
    }
    private var reusableBitmap: Bitmap? = null

    fun getCameraStream(url: String): Flow<Bitmap> = flow {
        val response = apiService.getStream(url)
        if (!response.isSuccessful) throw Exception("Connect failed: ${response.code()}")

        val inputStream = response.body()?.byteStream() ?: throw Exception("Empty body")
        val bufferedInput = BufferedInputStream(inputStream)
        
        // Reusable buffer (1MB) to avoid frequent allocations
        val frameBuffer = ByteArray(1024 * 1024)
        var frameSize = 0

        try {
            var prev = -1
            while (currentCoroutineContext().isActive) {
                val current = bufferedInput.read()
                if (current == -1) break

                if (prev == 0xFF && current == 0xD8) {
                    frameBuffer[0] = 0xFF.toByte()
                    frameBuffer[1] = 0xD8.toByte()
                    frameSize = 2

                    var innerPrev = 0
                    while (currentCoroutineContext().isActive) {
                        val innerCurrent = bufferedInput.read()
                        if (innerCurrent == -1) break
                        
                        if (frameSize < frameBuffer.size) {
                            frameBuffer[frameSize++] = innerCurrent.toByte()
                        }

                        if (innerPrev == 0xFF && innerCurrent == 0xD9) {
                            // Pool bitmap memory
                            if (reusableBitmap != null) {
                                options.inBitmap = reusableBitmap
                            }

                            try {
                                val bitmap = BitmapFactory.decodeByteArray(frameBuffer, 0, frameSize, options)
                                if (bitmap != null) {
                                    reusableBitmap = bitmap
                                    emit(bitmap)
                                }
                            } catch (e: Exception) {
                                // If inBitmap fails (e.g. dimensions changed), clear it and retry once
                                options.inBitmap = null
                                val bitmap = BitmapFactory.decodeByteArray(frameBuffer, 0, frameSize, options)
                                if (bitmap != null) {
                                    reusableBitmap = bitmap
                                    emit(bitmap)
                                }
                            }
                            break
                        }
                        innerPrev = innerCurrent
                    }
                }
                prev = current
            }
        } finally {
            try {
                bufferedInput.close()
                inputStream.close()
            } catch (e: Exception) {}
        }
    }.flowOn(Dispatchers.IO)

    suspend fun stopPreview(url: String): Boolean {
        return try {
            val stopUrl = url.replace("/preview", "/stop-preview")
            apiService.stopPreview(stopUrl)
            true
        } catch (e: Exception) {
            false
        }
    }
}
