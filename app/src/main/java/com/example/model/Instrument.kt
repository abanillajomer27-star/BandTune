package com.example.model

enum class TranspositionKey(
    val displayName: String,
    val semitoneOffsetFromConcert: Int,
    val description: String
) {
    CONCERT_C("Concert Key (C)", 0, "Concert Pitch (No Transposition)"),
    BB("B♭ Transposed", +2, "Written note is 2 semitones higher than concert pitch"),
    EB("E♭ Transposed", +9, "Written note is 9 semitones higher than concert pitch"),
    F("F Transposed", +7, "Written note is 7 semitones higher than concert pitch")
}

data class Instrument(
    val id: String,
    val name: String,
    val transpositionKey: TranspositionKey,
    val pitchKey: String,
    val defaultClef: String,
    val family: String,
    val description: String
) {
    companion object {
        val WOODWIND_G_CLEF = listOf(
            Instrument(
                id = "clarinet_bb",
                name = "Clarinet in B♭",
                transpositionKey = TranspositionKey.BB,
                pitchKey = "B♭ Transposed",
                defaultClef = "G Clef",
                family = "Woodwind",
                description = "Soprano B♭ Woodwind Clarinet"
            ),
            Instrument(
                id = "saxophone_eb",
                name = "Alto Saxophone in E♭",
                transpositionKey = TranspositionKey.EB,
                pitchKey = "E♭ Transposed",
                defaultClef = "G Clef",
                family = "Woodwind",
                description = "E♭ Alto Saxophone"
            ),
            Instrument(
                id = "flute_c",
                name = "Flute (Concert Key)",
                transpositionKey = TranspositionKey.CONCERT_C,
                pitchKey = "Concert Pitch",
                defaultClef = "G Clef",
                family = "Woodwind",
                description = "Concert Pitch Woodwind Flute"
            )
        )

        val BRASS_G_CLEF = listOf(
            Instrument(
                id = "trumpet_bb",
                name = "Trumpet in B♭",
                transpositionKey = TranspositionKey.BB,
                pitchKey = "B♭ Transposed",
                defaultClef = "G Clef",
                family = "Brass",
                description = "Standard B♭ Marching & Concert Trumpet"
            ),
            Instrument(
                id = "french_horn_f",
                name = "French Horn in F",
                transpositionKey = TranspositionKey.F,
                pitchKey = "F Transposed",
                defaultClef = "G Clef",
                family = "Brass",
                description = "Horn in F"
            )
        )

        val BRASS_BASS_CLEF = listOf(
            Instrument(
                id = "trombone_bb",
                name = "Trombone in B♭",
                transpositionKey = TranspositionKey.BB,
                pitchKey = "B♭ Transposed (Bass Clef)",
                defaultClef = "Bass Clef",
                family = "Brass",
                description = "Tenor / Bass B♭ Slide Trombone"
            ),
            Instrument(
                id = "baritone_bb",
                name = "Baritone / Euphonium in B♭",
                transpositionKey = TranspositionKey.BB,
                pitchKey = "B♭ Transposed (Bass Clef)",
                defaultClef = "Bass Clef",
                family = "Brass",
                description = "B♭ Baritone Horn & Euphonium"
            ),
            Instrument(
                id = "tuba_bb",
                name = "Tuba in B♭",
                transpositionKey = TranspositionKey.BB,
                pitchKey = "B♭ Transposed (Bass Clef)",
                defaultClef = "Bass Clef",
                family = "Brass",
                description = "B♭ Concert / Marching Tuba"
            )
        )

        val ALL_INSTRUMENTS = WOODWIND_G_CLEF + BRASS_G_CLEF + BRASS_BASS_CLEF

        val DEFAULT = ALL_INSTRUMENTS[3] // Trumpet in Bb default
    }
}
