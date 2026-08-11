package com.example.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.sin

class ToneGenerator {
    private var audioTrack: AudioTrack? = null
    private var playJob: Job? = null
    @Volatile private var isPlaying = false

    fun startTone(frequencyHz: Double, volume: Float = 0.85f) {
        stopTone()
        if (frequencyHz <= 20.0) return

        isPlaying = true
        val sampleRate = 44100
        val bufferSize = AudioTrack.getMinBufferSize(
            sampleRate,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        ).coerceAtLeast(2048)

        audioTrack = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setSampleRate(sampleRate)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build()
            )
            .setBufferSizeInBytes(bufferSize)
            .setTransferMode(AudioTrack.MODE_STREAM)
            .build()

        audioTrack?.play()

        playJob = CoroutineScope(Dispatchers.IO).launch {
            val samples = ShortArray(bufferSize)
            var phase = 0.0
            val phaseIncrement = 2.0 * PI * frequencyHz / sampleRate
            val masterVol = volume.coerceIn(0.0f, 1.0f)

            // Attack envelope parameters to prevent clicks
            var sampleCount = 0L
            val attackSamples = (sampleRate * 0.03).toLong() // 30ms smooth attack

            while (isPlaying) {
                for (i in samples.indices) {
                    // Envelope gain calculation
                    val envelope = if (sampleCount < attackSamples) {
                        sampleCount.toFloat() / attackSamples.toFloat()
                    } else {
                        1.0f
                    }

                    // Rich organ/brass acoustic tone: Fundamental + 2nd harmonic (octave) + 3rd harmonic (fifth)
                    val fundamental = sin(phase)
                    val h2 = 0.20 * sin(2.0 * phase)
                    val h3 = 0.08 * sin(3.0 * phase)
                    
                    val combinedWave = (fundamental + h2 + h3) / 1.28
                    val sampleValue = (combinedWave * 32767.0 * masterVol * envelope).toInt()
                        .coerceIn(-32768, 32767)

                    samples[i] = sampleValue.toShort()
                    phase += phaseIncrement
                    if (phase >= 2.0 * PI) {
                        phase -= 2.0 * PI
                    }
                    sampleCount++
                }
                audioTrack?.write(samples, 0, samples.size)
            }
        }
    }

    fun stopTone() {
        isPlaying = false
        playJob?.cancel()
        playJob = null
        try {
            audioTrack?.stop()
            audioTrack?.release()
        } catch (_: Exception) {}
        audioTrack = null
    }
}
