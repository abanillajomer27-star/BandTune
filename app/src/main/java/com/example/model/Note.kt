package com.example.model

import kotlin.math.abs
import kotlin.math.log2
import kotlin.math.pow
import kotlin.math.roundToInt

enum class TuningStatus(val label: String) {
    PERFECT("In Tune"),
    FLAT("Flat"),
    SHARP("Sharp"),
    NONE("No Signal")
}

data class NoteResult(
    val frequencyHz: Double,
    val concertNoteName: String,
    val transposedNoteName: String,
    val octave: Int,
    val transposedOctave: Int,
    val midiNote: Int,
    val targetFrequencyHz: Double,
    val centsDeviation: Double, // -50.0 to +50.0
    val isInTune: Boolean, // within +/- 4 cents
    val tuningStatus: TuningStatus = TuningStatus.NONE
)

object PitchUtils {
    private val NOTE_NAMES_FLAT = arrayOf("C", "D♭", "D", "E♭", "E", "F", "G♭", "G", "A♭", "A", "B♭", "B")
    private val NOTE_NAMES_SHARP = arrayOf("C", "C♯", "D", "D♯", "E", "F", "F♯", "G", "G♯", "A", "A♯", "B")

    fun getConcertNoteName(midiNote: Int, useFlats: Boolean = true): String {
        val noteIndex = (midiNote % 12 + 12) % 12
        return if (useFlats) NOTE_NAMES_FLAT[noteIndex] else NOTE_NAMES_SHARP[noteIndex]
    }

    fun getTransposedNoteName(midiNote: Int, offsetSemitones: Int, useFlats: Boolean = true): String {
        val transposedMidi = midiNote + offsetSemitones
        val noteIndex = (transposedMidi % 12 + 12) % 12
        return if (useFlats) NOTE_NAMES_FLAT[noteIndex] else NOTE_NAMES_SHARP[noteIndex]
    }

    fun midiToFrequency(midiNote: Int, a4Calibration: Double = 440.0): Double {
        return a4Calibration * 2.0.pow((midiNote - 69).toDouble() / 12.0)
    }

    fun analyzeFrequency(
        frequencyHz: Double,
        instrument: Instrument,
        a4Calibration: Double = 440.0,
        showTransposedNote: Boolean = true
    ): NoteResult {
        if (frequencyHz <= 15.0 || frequencyHz.isNaN() || frequencyHz.isInfinite()) {
            return NoteResult(
                frequencyHz = 0.0,
                concertNoteName = "--",
                transposedNoteName = "--",
                octave = 0,
                transposedOctave = 0,
                midiNote = 0,
                targetFrequencyHz = 0.0,
                centsDeviation = 0.0,
                isInTune = false,
                tuningStatus = TuningStatus.NONE
            )
        }

        // Concert MIDI Note calculation
        val exactMidi = 69.0 + 12.0 * log2(frequencyHz / a4Calibration)
        val midiNote = exactMidi.roundToInt()
        val octave = (midiNote / 12) - 1
        val targetFreq = midiToFrequency(midiNote, a4Calibration)

        // Cents deviation calculation
        val cents = 1200.0 * log2(frequencyHz / targetFreq)
        val clampedCents = cents.coerceIn(-50.0, 50.0)

        val concertNote = getConcertNoteName(midiNote, useFlats = true)
        val offset = if (showTransposedNote) instrument.transpositionKey.semitoneOffsetFromConcert else 0
        val transposedNote = getTransposedNoteName(midiNote, offset, useFlats = true)
        val transposedMidi = midiNote + offset
        val transposedOctave = (transposedMidi / 12) - 1

        val inTune = abs(cents) <= 4.0
        val status = when {
            inTune -> TuningStatus.PERFECT
            cents < -4.0 -> TuningStatus.FLAT
            else -> TuningStatus.SHARP
        }

        return NoteResult(
            frequencyHz = frequencyHz,
            concertNoteName = concertNote,
            transposedNoteName = transposedNote,
            octave = octave,
            transposedOctave = transposedOctave,
            midiNote = midiNote,
            targetFrequencyHz = targetFreq,
            centsDeviation = clampedCents,
            isInTune = inTune,
            tuningStatus = status
        )
    }

    // Reference Pitch calculation for band reference notes matching selected instrument
    fun getReferencePitchFrequencyForInstrument(
        writtenNoteName: String,
        instrument: Instrument,
        a4Calibration: Double = 440.0
    ): Double {
        val baseMidi = when (writtenNoteName.uppercase().replace("♭", "B").replace("BFLAT", "BB")) {
            "C" -> 0
            "DB", "C#", "C♯", "D♭" -> 1
            "D" -> 2
            "EB", "D#", "D♯", "E♭" -> 3
            "E" -> 4
            "F" -> 5
            "GB", "F#", "F♯", "G♭" -> 6
            "G" -> 7
            "AB", "G#", "G♯", "A♭" -> 8
            "A" -> 9
            "BB", "A#", "A♯", "B♭" -> 10
            "B" -> 11
            else -> 0
        }

        // Transposed Written Note -> Concert Note MIDI
        val transpositionOffset = instrument.transpositionKey.semitoneOffsetFromConcert
        val concertBaseMidi = (baseMidi - transpositionOffset + 12) % 12

        // Choose appropriate octave for band tuning (Octave 3 or 4)
        val octave = when (instrument.id) {
            "tuba_bb" -> 2
            "trombone_bb", "baritone_bb" -> 3
            "french_horn_f", "saxophone_eb", "trumpet_bb", "clarinet_bb" -> 4
            "flute_c" -> 5
            else -> 4
        }

        val concertMidiNote = concertBaseMidi + (octave + 1) * 12
        return midiToFrequency(concertMidiNote, a4Calibration)
    }
}
