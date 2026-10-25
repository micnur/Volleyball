package com.example.util

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.media.ToneGenerator
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class RefereeSoundFeedback(private val context: Context) {

    private val vibrator: Vibrator? by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }

    private var toneGen: ToneGenerator? = null

    init {
        try {
            toneGen = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 95)
        } catch (_: Exception) {
            // Ignore if ToneGenerator not permitted
        }
    }

    /**
     * Play single official referee whistle sound (2.8 kHz modulated tone)
     */
    fun playWhistle(enabled: Boolean = true, haptic: Boolean = true) {
        if (haptic) {
            vibratePattern(longArrayOf(0, 120), intArrayOf(0, 200))
        }
        if (!enabled) return

        CoroutineScope(Dispatchers.Default).launch {
            try {
                playPcmWhistle(durationMs = 200, freq = 2800)
            } catch (_: Exception) {
                toneGen?.startTone(ToneGenerator.TONE_PROP_BEEP2, 180)
            }
        }
    }

    /**
     * Play double whistle (Timeout or Substitution)
     */
    fun playDoubleWhistle(enabled: Boolean = true, haptic: Boolean = true) {
        if (haptic) {
            vibratePattern(longArrayOf(0, 80, 60, 140), intArrayOf(0, 180, 0, 220))
        }
        if (!enabled) return

        CoroutineScope(Dispatchers.Default).launch {
            try {
                playPcmWhistle(durationMs = 100, freq = 2800)
                kotlinx.coroutines.delay(80)
                playPcmWhistle(durationMs = 150, freq = 2950)
            } catch (_: Exception) {
                toneGen?.startTone(ToneGenerator.TONE_PROP_ACK, 250)
            }
        }
    }

    /**
     * Play long whistle or buzzer (End of timeout, Set Point, End of Set)
     */
    fun playLongWhistle(enabled: Boolean = true, haptic: Boolean = true) {
        if (haptic) {
            vibratePattern(longArrayOf(0, 450), intArrayOf(0, 255))
        }
        if (!enabled) return

        CoroutineScope(Dispatchers.Default).launch {
            try {
                playPcmWhistle(durationMs = 500, freq = 2750)
            } catch (_: Exception) {
                toneGen?.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 500)
            }
        }
    }

    /**
     * Sanction card sound (strong alert)
     */
    fun playSanctionAlert(enabled: Boolean = true, haptic: Boolean = true) {
        if (haptic) {
            vibratePattern(longArrayOf(0, 200, 100, 300), intArrayOf(0, 255, 0, 255))
        }
        if (!enabled) return

        CoroutineScope(Dispatchers.Default).launch {
            toneGen?.startTone(ToneGenerator.TONE_CDMA_EMERGENCY_RINGBACK, 400)
        }
    }

    private fun vibratePattern(timings: LongArray, amplitudes: IntArray) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val effect = VibrationEffect.createWaveform(timings, amplitudes, -1)
                vibrator?.vibrate(effect)
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(timings, -1)
            }
        } catch (_: Exception) {
            // Ignore if vibration fails
        }
    }

    /**
     * Synthesize clean audio whistle sine wave
     */
    private fun playPcmWhistle(durationMs: Int, freq: Int) {
        val sampleRate = 44100
        val numSamples = (sampleRate * (durationMs / 1000.0)).toInt()
        val generatedSnd = ByteArray(2 * numSamples)

        for (i in 0 until numSamples) {
            // Modulate with small harmonic for realistic whistle timbre
            val t = i.toDouble() / sampleRate
            val sample = 0.8 * Math.sin(2.0 * Math.PI * freq * t) + 0.2 * Math.sin(2.0 * Math.PI * (freq * 1.5) * t)
            val envelope = when {
                i < 300 -> (i / 300.0) // Attack
                i > numSamples - 600 -> ((numSamples - i) / 600.0) // Decay
                else -> 1.0
            }
            val finalSample = (sample * envelope * 32767).toInt().toShort()
            generatedSnd[2 * i] = (finalSample.toInt() and 0x00ff).toByte()
            generatedSnd[2 * i + 1] = ((finalSample.toInt() and 0xff00) shr 8).toByte()
        }

        val audioTrack = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setSampleRate(sampleRate)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build()
            )
            .setBufferSizeInBytes(generatedSnd.size)
            .setTransferMode(AudioTrack.MODE_STATIC)
            .build()

        audioTrack.write(generatedSnd, 0, generatedSnd.size)
        audioTrack.play()
        Thread.sleep(durationMs.toLong() + 50)
        audioTrack.stop()
        audioTrack.release()
    }
}
