package com.example.dms_app.core.utils

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.os.Build
import android.media.AudioManager
import android.media.ToneGenerator

class SoundManager(private val context: Context) {

    private val soundPool: SoundPool = SoundPool.Builder()
        .setMaxStreams(3)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
        )
        .build()

    private var warningSoundId: Int = -1
    private var criticalSoundId: Int = -1
    
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    
    // Fallback tone generator - use notification stream to respect silent mode better
    private val toneGenerator = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 100)

    init {
        // Future: load real sounds from R.raw
    }

    private fun shouldPlaySound(): Boolean {
        return audioManager.ringerMode == AudioManager.RINGER_MODE_NORMAL
    }

    fun playWarning() {
        if (!shouldPlaySound()) return
        
        if (warningSoundId != -1) {
            soundPool.play(warningSoundId, 0.7f, 0.7f, 1, 0, 1f)
        } else {
            // Light warning tone
            toneGenerator.startTone(ToneGenerator.TONE_PROP_BEEP, 150)
        }
    }

    fun playCritical() {
        if (!shouldPlaySound()) return

        if (criticalSoundId != -1) {
            soundPool.play(criticalSoundId, 1f, 1f, 2, 0, 1f)
        } else {
            // Stronger alert tone
            toneGenerator.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 1000)
        }
    }
    
    fun stopAll() {
        toneGenerator.stopTone()
        // SoundPool sounds are usually short and don't need explicit stopping for simple beeps
    }

    fun vibrate(duration: Long = 500) {
        try {
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator
            } else {
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }

            vibrator?.let { v ->
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    v.vibrate(VibrationEffect.createOneShot(duration, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    v.vibrate(duration)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    fun stopVibration() {
        try {
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator
            } else {
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }
            vibrator?.cancel()
        } catch (e: Exception) { }
    }
    
    fun release() {
        stopAll()
        stopVibration()
        soundPool.release()
        toneGenerator.release()
    }
}
