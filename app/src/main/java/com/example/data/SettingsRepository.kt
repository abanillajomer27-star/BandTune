package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.example.model.AppSettings
import com.example.model.MetronomeAccentType
import com.example.model.ThemeMode

class SettingsRepository(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("bandtune_settings_prefs", Context.MODE_PRIVATE)

    fun loadSettings(): AppSettings {
        val themeOrdinal = prefs.getInt("theme_mode", ThemeMode.SYSTEM.ordinal)
        val themeMode = ThemeMode.values().getOrElse(themeOrdinal) { ThemeMode.SYSTEM }

        val calibrationA4 = prefs.getFloat("calibration_a4", 440.0f).toDouble()
        val soundVolume = prefs.getFloat("sound_volume", 0.85f)

        val accentOrdinal = prefs.getInt("metronome_accent", MetronomeAccentType.WOODBLOCK.ordinal)
        val metronomeAccent = MetronomeAccentType.values().getOrElse(accentOrdinal) { MetronomeAccentType.WOODBLOCK }

        val showTransposed = prefs.getBoolean("show_transposed", true)

        return AppSettings(
            themeMode = themeMode,
            calibrationA4Hz = calibrationA4,
            soundVolume = soundVolume,
            metronomeAccent = metronomeAccent,
            showTransposedNotes = showTransposed
        )
    }

    fun saveSettings(settings: AppSettings) {
        prefs.edit()
            .putInt("theme_mode", settings.themeMode.ordinal)
            .putFloat("calibration_a4", settings.calibrationA4Hz.toFloat())
            .putFloat("sound_volume", settings.soundVolume)
            .putInt("metronome_accent", settings.metronomeAccent.ordinal)
            .putBoolean("show_transposed", settings.showTransposedNotes)
            .apply()
    }
}
