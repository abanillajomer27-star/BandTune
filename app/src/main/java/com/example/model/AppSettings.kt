package com.example.model

enum class TimeSignature(
    val displayName: String,
    val beatsPerMeasure: Int,
    val beatUnit: Int
) {
    TS_2_4("2/4", 2, 4),
    TS_3_4("3/4", 3, 4),
    TS_4_4("4/4", 4, 4),
    TS_6_8("6/8", 6, 8)
}

enum class MetronomeAccentType(val displayName: String) {
    WOODBLOCK("Woodblock"),
    BEEP("Digital Beep"),
    CLICK("Mechanical Click"),
    DRUM("Percussion Drum")
}

enum class ThemeMode(val displayName: String) {
    SYSTEM("System Default (Green & White)"),
    LIGHT("Bright Violet & White"),
    WARM_ORANGE("Soft Warm Orange & White")
}

data class AppSettings(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val calibrationA4Hz: Double = 440.0,
    val soundVolume: Float = 0.85f,
    val metronomeAccent: MetronomeAccentType = MetronomeAccentType.WOODBLOCK,
    val showTransposedNotes: Boolean = true
)
