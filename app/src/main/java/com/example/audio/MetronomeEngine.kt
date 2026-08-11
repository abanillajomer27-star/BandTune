package com.example.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import com.example.model.MetronomeAccentType
import com.example.model.TimeSignature
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.sin

class MetronomeEngine {
    private var isPlaying = false
    private var metronomeJob: Job? = null

    private val _currentBeat = MutableStateFlow(0)
    val currentBeat: StateFlow<Int> = _currentBeat

    private val _isPlayingState = MutableStateFlow(false)
    val isPlayingState: StateFlow<Boolean> = _isPlayingState

    var bpm: Int = 120
    var timeSignature: TimeSignature = TimeSignature.TS_4_4
    var accentType: MetronomeAccentType = MetronomeAccentType.WOODBLOCK
    var volume: Float = 0.85f

    private fun generateClickBuffer(
        frequencyHz: Double,
        durationMs: Int,
        type: MetronomeAccentType,
        sampleRate: Int = 44100
    ): ShortArray {
        val numSamples = (sampleRate * durationMs / 1000.0).toInt()
        val buffer = ShortArray(numSamples)
        val masterVol = volume.coerceIn(0.1f, 1.0f)

        for (i in 0 until numSamples) {
            val t = i.toDouble() / sampleRate
            val envelope = (1.0 - t / (durationMs / 1000.0)).coerceAtLeast(0.0) // exponential decay
            val val1 = when (type) {
                MetronomeAccentType.WOODBLOCK -> {
                    sin(2.0 * PI * frequencyHz * t) * envelope * envelope
                }
                MetronomeAccentType.BEEP -> {
                    sin(2.0 * PI * frequencyHz * t) * envelope
                }
                MetronomeAccentType.CLICK -> {
                    if (i < numSamples / 10) (Math.random() * 2.0 - 1.0) * envelope else sin(2.0 * PI * frequencyHz * t) * envelope
                }
                MetronomeAccentType.DRUM -> {
                    sin(2.0 * PI * (frequencyHz * (1.0 - t * 5.0).coerceAtLeast(0.2)) * t) * envelope
                }
            }
            buffer[i] = (val1 * 32767.0 * masterVol).toInt().coerceIn(-32768, 32767).toShort()
        }
        return buffer
    }

    fun start() {
        if (isPlaying) return
        isPlaying = true
        _isPlayingState.value = true

        metronomeJob = CoroutineScope(Dispatchers.Default).launch {
            val sampleRate = 44100
            val accentFreq = when (accentType) {
                MetronomeAccentType.WOODBLOCK -> 1200.0
                MetronomeAccentType.BEEP -> 1760.0
                MetronomeAccentType.CLICK -> 2000.0
                MetronomeAccentType.DRUM -> 300.0
            }
            val regularFreq = accentFreq * 0.75

            val accentBuffer = generateClickBuffer(accentFreq, 25, accentType, sampleRate)
            val regularBuffer = generateClickBuffer(regularFreq, 20, accentType, sampleRate)

            val minBufferSize = AudioTrack.getMinBufferSize(
                sampleRate,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            )

            val audioTrack = AudioTrack.Builder()
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
                .setBufferSizeInBytes(minBufferSize)
                .setTransferMode(AudioTrack.MODE_STREAM)
                .build()

            audioTrack.play()

            var beatCounter = 0

            while (isPlaying) {
                val totalBeats = timeSignature.beatsPerMeasure
                val isAccentBeat = (beatCounter % totalBeats == 0)

                _currentBeat.value = beatCounter % totalBeats

                val bufferToPlay = if (isAccentBeat) accentBuffer else regularBuffer
                audioTrack.write(bufferToPlay, 0, bufferToPlay.size)

                // Calculate beat duration in milliseconds
                val intervalMs = (60.0 / bpm * 1000.0).toLong()
                val clickDurationMs = 25L
                val sleepTimeMs = (intervalMs - clickDurationMs).coerceAtLeast(10L)

                delay(sleepTimeMs)
                beatCounter++
            }

            try {
                audioTrack.stop()
                audioTrack.release()
            } catch (_: Exception) {}
        }
    }

    fun stop() {
        isPlaying = false
        _isPlayingState.value = false
        metronomeJob?.cancel()
        metronomeJob = null
        _currentBeat.value = 0
    }

    fun toggle() {
        if (isPlaying) stop() else start()
    }
}
