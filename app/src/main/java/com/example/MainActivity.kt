package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.audio.MetronomeEngine
import com.example.audio.PitchDetector
import com.example.audio.ToneGenerator
import com.example.data.SettingsRepository
import com.example.model.Instrument
import com.example.model.PitchUtils
import com.example.model.ThemeMode
import com.example.ui.components.BottomTab
import com.example.ui.screens.AppNavigationDrawerSheet
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.MetronomeScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.screens.TunerScreen
import com.example.ui.theme.AccentRed
import com.example.ui.theme.BandTuneTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val pitchDetector = PitchDetector()
    private val toneGenerator = ToneGenerator()
    private val metronomeEngine = MetronomeEngine()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val settingsRepository = remember { SettingsRepository(this@MainActivity) }
            var appSettings by remember { mutableStateOf(settingsRepository.loadSettings()) }
            var activeInstrument by remember { mutableStateOf(Instrument.DEFAULT) }
            var showSplash by remember { mutableStateOf(true) }
            var currentTab by remember { mutableStateOf(BottomTab.HOME) }
            var isSettingsOpen by remember { mutableStateOf(false) }
            var activeReferencePitch by remember { mutableStateOf<String?>(null) }
            var showExitDialog by remember { mutableStateOf(false) }

            val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
            val scope = rememberCoroutineScope()

            var hasMicPermission by remember {
                mutableStateOf(
                    ContextCompat.checkSelfPermission(
                        this@MainActivity,
                        Manifest.permission.RECORD_AUDIO
                    ) == PackageManager.PERMISSION_GRANTED
                )
            }

            // Microphone Permission Launcher
            val micPermissionLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.RequestPermission()
            ) { isGranted ->
                hasMicPermission = isGranted
                if (isGranted) {
                    pitchDetector.startListening()
                }
            }

            // Start pitch listening if permission granted
            LaunchedEffect(Unit) {
                if (hasMicPermission) {
                    pitchDetector.startListening()
                } else {
                    micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                }
            }

            // Cleanup audio engines when activity disposed
            DisposableEffect(Unit) {
                onDispose {
                    pitchDetector.stopListening()
                    toneGenerator.stopTone()
                    metronomeEngine.stop()
                }
            }

            // Android Hardware Back Button Handling
            BackHandler(enabled = true) {
                when {
                    drawerState.isOpen -> scope.launch { drawerState.close() }
                    isSettingsOpen -> isSettingsOpen = false
                    currentTab != BottomTab.HOME -> currentTab = BottomTab.HOME
                    else -> showExitDialog = true
                }
            }

            // Theme mode evaluation:
            // System Default (Green and White) and Light Mode (Violet and White) are light modes.
            // Dark Mode is dark mode.
            val isDark = when (appSettings.themeMode) {
                ThemeMode.SYSTEM -> false
                ThemeMode.LIGHT -> false
                ThemeMode.WARM_ORANGE -> false
            }

            BandTuneTheme(
                themeMode = appSettings.themeMode,
                darkTheme = isDark
            ) {
                if (showSplash) {
                    SplashScreen(
                        onTimeout = { showSplash = false }
                    )
                } else {
                    ModalNavigationDrawer(
                        drawerState = drawerState,
                        drawerContent = {
                            AppNavigationDrawerSheet(
                                activeInstrument = activeInstrument,
                                onInstrumentSelected = { instrument ->
                                    activeInstrument = instrument
                                    scope.launch { drawerState.close() }
                                }
                            )
                        }
                    ) {
                        if (isSettingsOpen) {
                            SettingsScreen(
                                settings = appSettings,
                                onSettingsChange = { newSettings ->
                                    appSettings = newSettings
                                    settingsRepository.saveSettings(newSettings)
                                    metronomeEngine.volume = newSettings.soundVolume
                                },
                                onBack = { isSettingsOpen = false }
                            )
                        } else {
                            Box(modifier = Modifier.fillMaxSize()) {
                                when (currentTab) {
                                    BottomTab.HOME -> {
                                        HomeScreen(
                                            activeInstrument = activeInstrument,
                                            onOpenDrawer = {
                                                scope.launch { drawerState.open() }
                                            },
                                            onNavigateToTuner = { currentTab = BottomTab.TUNER },
                                            onNavigateToMetronome = { currentTab = BottomTab.METRONOME },
                                            onNavigateToSettings = { isSettingsOpen = true }
                                        )
                                    }

                                    BottomTab.TUNER -> {
                                        val detectedFreq by pitchDetector.detectedFrequency.collectAsState()
                                        val isListening by pitchDetector.isListening.collectAsState()

                                        val noteResult = remember(detectedFreq, activeInstrument, appSettings.calibrationA4Hz, appSettings.showTransposedNotes) {
                                            PitchUtils.analyzeFrequency(
                                                frequencyHz = detectedFreq,
                                                instrument = activeInstrument,
                                                a4Calibration = appSettings.calibrationA4Hz,
                                                showTransposedNote = appSettings.showTransposedNotes
                                            )
                                        }

                                        TunerScreen(
                                            activeInstrument = activeInstrument,
                                            noteResult = noteResult,
                                            isListening = isListening,
                                            hasMicPermission = hasMicPermission,
                                            calibrationA4 = appSettings.calibrationA4Hz,
                                            activeReferencePitch = activeReferencePitch,
                                            onPlayReferencePitch = { pitchKey ->
                                                activeReferencePitch = pitchKey
                                                val targetFreq = PitchUtils.getReferencePitchFrequencyForInstrument(
                                                    writtenNoteName = pitchKey,
                                                    instrument = activeInstrument,
                                                    a4Calibration = appSettings.calibrationA4Hz
                                                )
                                                toneGenerator.startTone(targetFreq, appSettings.soundVolume)
                                            },
                                            onStopReferencePitch = {
                                                activeReferencePitch = null
                                                toneGenerator.stopTone()
                                            },
                                            onRequestMicPermission = {
                                                micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                            },
                                            onToggleMic = {
                                                if (isListening) {
                                                    pitchDetector.stopListening()
                                                } else {
                                                    if (hasMicPermission) {
                                                        pitchDetector.startListening()
                                                    } else {
                                                        micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                                    }
                                                }
                                            },
                                            onBack = { currentTab = BottomTab.HOME }
                                        )
                                    }

                                    BottomTab.METRONOME -> {
                                        val isPlaying by metronomeEngine.isPlayingState.collectAsState()
                                        val currentBeat by metronomeEngine.currentBeat.collectAsState()

                                        MetronomeScreen(
                                            bpm = metronomeEngine.bpm,
                                            timeSignature = metronomeEngine.timeSignature,
                                            accentType = metronomeEngine.accentType,
                                            isPlaying = isPlaying,
                                            currentBeat = currentBeat,
                                            onBpmChange = { newBpm ->
                                                metronomeEngine.bpm = newBpm
                                            },
                                            onTimeSignatureChange = { newTs ->
                                                metronomeEngine.timeSignature = newTs
                                            },
                                            onAccentTypeChange = { newAccent ->
                                                metronomeEngine.accentType = newAccent
                                            },
                                            onTogglePlay = {
                                                metronomeEngine.volume = appSettings.soundVolume
                                                metronomeEngine.toggle()
                                            },
                                            onBack = { currentTab = BottomTab.HOME }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Exit BandTune Confirmation Dialog
                    if (showExitDialog) {
                        AlertDialog(
                            onDismissRequest = { showExitDialog = false },
                            title = {
                                Text(
                                    text = "Exit BandTune?",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            },
                            text = {
                                Text(
                                    text = "Do you want to leave the application?",
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            },
                            confirmButton = {
                                Button(
                                    onClick = {
                                        showExitDialog = false
                                        finish()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = AccentRed),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("Exit", color = Color.White, fontWeight = FontWeight.Bold)
                                }
                            },
                            dismissButton = {
                                TextButton(onClick = { showExitDialog = false }) {
                                    Text("Cancel", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                }
                            },
                            shape = RoundedCornerShape(20.dp)
                        )
                    }
                }
            }
        }
    }
}
