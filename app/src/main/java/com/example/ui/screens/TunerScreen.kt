package com.example.ui.screens

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Instrument
import com.example.model.NoteResult
import com.example.model.PitchUtils
import com.example.model.TranspositionKey
import com.example.model.TuningStatus
import com.example.ui.theme.AccentGreen
import com.example.ui.theme.AccentOrange
import com.example.ui.theme.AccentRed
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun TunerScreen(
    activeInstrument: Instrument,
    noteResult: NoteResult,
    isListening: Boolean,
    hasMicPermission: Boolean = true,
    calibrationA4: Double,
    activeReferencePitch: String?,
    onPlayReferencePitch: (String) -> Unit,
    onStopReferencePitch: () -> Unit,
    onRequestMicPermission: () -> Unit = {},
    onToggleMic: () -> Unit,
    onBack: () -> Unit = {}
) {
    val scrollState = rememberScrollState()
    val hasSignal = noteResult.frequencyHz > 0.0
    val primaryColor = MaterialTheme.colorScheme.primary
    val primaryContainer = MaterialTheme.colorScheme.primaryContainer

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back to Home",
                            tint = primaryColor
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Column {
                        Text(
                            text = "MARCHING BAND TUNER",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.5.sp,
                            color = primaryColor
                        )
                        Text(
                            text = activeInstrument.name,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }

                IconButton(
                    onClick = onToggleMic,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(if (isListening) primaryContainer else MaterialTheme.colorScheme.surfaceVariant)
                        .testTag("tuner_mic_toggle_button")
                ) {
                    Icon(
                        imageVector = if (isListening) Icons.Default.Mic else Icons.Default.MicOff,
                        contentDescription = "Toggle Mic Listening",
                        tint = if (isListening) primaryColor else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Permission Warning Card
            if (!hasMicPermission) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = AccentOrange.copy(alpha = 0.12f)
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, AccentOrange.copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.MicOff,
                            contentDescription = null,
                            tint = AccentOrange,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Microphone Access Required",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Grant mic permission to detect instrument pitch in real-time.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Bento Card 1: Main Precision Tuning Gauge Box
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("tuner_main_bento_card"),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Status Badge
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.GraphicEq,
                                contentDescription = null,
                                tint = if (isListening) primaryColor else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isListening) "PITCH DETECTOR ACTIVE" else "TUNER OFF",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.2.sp,
                                color = primaryColor.copy(alpha = 0.8f)
                            )
                        }

                        Text(
                            text = "A4 = ${calibrationA4.toInt()} Hz",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Displayed Note Name: Written Transposed Note for active instrument
                    val isTransposed = activeInstrument.transpositionKey != TranspositionKey.CONCERT_C
                    val displayedNoteName = if (hasSignal) {
                        if (isTransposed) noteResult.transposedNoteName else noteResult.concertNoteName
                    } else "--"
                    val displayedOctave = if (hasSignal) {
                        if (isTransposed) noteResult.transposedOctave else noteResult.octave
                    } else 0

                    // Arc & Needle Canvas Gauge
                    TunerNeedleGauge(
                        centsDeviation = if (hasSignal) noteResult.centsDeviation else 0.0,
                        isInTune = noteResult.isInTune,
                        detectedNote = displayedNoteName,
                        octave = displayedOctave,
                        hasSignal = hasSignal,
                        isListening = isListening,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(190.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // In Tune Status Indicator Badge
                    val statusBg = when {
                        !isListening -> MaterialTheme.colorScheme.surface
                        !hasSignal -> primaryContainer.copy(alpha = 0.6f)
                        noteResult.tuningStatus == TuningStatus.PERFECT -> AccentGreen.copy(alpha = 0.18f)
                        noteResult.tuningStatus == TuningStatus.FLAT -> AccentOrange.copy(alpha = 0.18f)
                        else -> AccentRed.copy(alpha = 0.18f)
                    }
                    val statusText = when {
                        !isListening -> "Mic Off • Tap Microphone to Listen"
                        !hasSignal -> "Play a note to begin tuning"
                        noteResult.tuningStatus == TuningStatus.PERFECT -> "PERFECT IN TUNE!"
                        noteResult.tuningStatus == TuningStatus.FLAT -> "FLAT • Tune Up ⬆ (${String.format("%.1f", noteResult.centsDeviation)}¢)"
                        else -> "SHARP • Tune Down ⬇ (+${String.format("%.1f", noteResult.centsDeviation)}¢)"
                    }
                    val statusColor = when {
                        !isListening -> MaterialTheme.colorScheme.onSurfaceVariant
                        !hasSignal -> primaryColor
                        noteResult.tuningStatus == TuningStatus.PERFECT -> AccentGreen
                        noteResult.tuningStatus == TuningStatus.FLAT -> AccentOrange
                        else -> AccentRed
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(statusBg)
                            .padding(horizontal = 18.dp, vertical = 10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(statusColor)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = statusText,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = statusColor
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Bento Grid Metrics Row: Frequency, Cents, Concert Pitch, Written Note
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        BentoMetricItem(
                            label = "FREQUENCY",
                            value = if (hasSignal) String.format("%.1f", noteResult.frequencyHz) else "--",
                            unit = "Hz"
                        )
                        BentoMetricItem(
                            label = "CENT DEVIATION",
                            value = if (hasSignal) String.format("%+.1f", noteResult.centsDeviation) else "0.0",
                            unit = "¢"
                        )
                        BentoMetricItem(
                            label = "CONCERT PITCH",
                            value = if (hasSignal) noteResult.concertNoteName else "--",
                            unit = ""
                        )
                        if (isTransposed) {
                            BentoMetricItem(
                                label = "WRITTEN NOTE",
                                value = if (hasSignal) noteResult.transposedNoteName else "--",
                                unit = ""
                            )
                        }
                    }
                }
            }

            // Bento Card 2: Band Reference Pitch Selector (F, Bb, Eb, Ab, Db, Gb)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("reference_pitch_bento_card"),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.MusicNote,
                                contentDescription = null,
                                tint = primaryColor,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Reference Tone Pitch",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        if (activeReferencePitch != null) {
                            IconButton(
                                onClick = onStopReferencePitch,
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(AccentRed.copy(alpha = 0.15f))
                            ) {
                                Icon(
                                    imageVector = Icons.Default.VolumeOff,
                                    contentDescription = "Stop Pitch",
                                    tint = AccentRed,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Tap a reference key to play a clean drone pitch matched to ${activeInstrument.name}:",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Standard Band Tuning Notes: F, Bb, Eb, Ab, Db, Gb
                    val referenceKeys = listOf("F", "B♭", "E♭", "A♭", "D♭", "G♭")

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        referenceKeys.forEach { pitchKey ->
                            val isSelected = activeReferencePitch == pitchKey

                            // Compute written note name for transposed display on button
                            val offset = activeInstrument.transpositionKey.semitoneOffsetFromConcert
                            val writtenLabel = if (offset != 0) {
                                val concertMidi = when (pitchKey.replace("♭", "B").replace("BB", "BB")) {
                                    "F" -> 5; "B♭" -> 10; "E♭" -> 3; "A♭" -> 8; "D♭" -> 1; "G♭" -> 6; else -> 0
                                }
                                PitchUtils.getTransposedNoteName(concertMidi, offset, useFlats = true)
                            } else null

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(52.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(if (isSelected) primaryColor else MaterialTheme.colorScheme.surfaceVariant)
                                    .clickable {
                                        if (isSelected) onStopReferencePitch() else onPlayReferencePitch(pitchKey)
                                    }
                                    .testTag("ref_pitch_button_$pitchKey"),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = pitchKey,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                    )
                                    if (writtenLabel != null) {
                                        Text(
                                            text = "($writtenLabel)",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = if (isSelected) Color.White.copy(alpha = 0.85f) else primaryColor
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TunerNeedleGauge(
    centsDeviation: Double,
    isInTune: Boolean,
    detectedNote: String,
    octave: Int,
    hasSignal: Boolean,
    isListening: Boolean,
    modifier: Modifier = Modifier
) {
    val animatedCents by animateFloatAsState(
        targetValue = centsDeviation.toFloat(),
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "centsNeedleAnimation"
    )

    val surfaceColor = MaterialTheme.colorScheme.surface
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    val onSurfaceVariantColor = MaterialTheme.colorScheme.onSurfaceVariant
    val primaryColor = MaterialTheme.colorScheme.primary

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            val centerX = width / 2f
            val centerY = height * 0.92f
            val radius = height * 0.78f

            // Background Arc Track
            val arcRect = Size(radius * 2f, radius * 2f)
            val arcTopLeft = Offset(centerX - radius, centerY - radius)

            // Outer Arc
            drawArc(
                color = onSurfaceVariantColor.copy(alpha = 0.15f),
                startAngle = 210f,
                sweepAngle = 120f,
                useCenter = false,
                topLeft = arcTopLeft,
                size = arcRect,
                style = Stroke(width = 16.dp.toPx(), cap = StrokeCap.Round)
            )

            // Center Perfect In-Tune Zone Highlight (±5 cents)
            val perfectAngleStart = 270f - (5f / 50f) * 60f
            val perfectAngleSweep = (10f / 50f) * 60f
            drawArc(
                color = AccentGreen.copy(alpha = 0.35f),
                startAngle = perfectAngleStart,
                sweepAngle = perfectAngleSweep,
                useCenter = false,
                topLeft = arcTopLeft,
                size = arcRect,
                style = Stroke(width = 16.dp.toPx(), cap = StrokeCap.Butt)
            )

            // Tick Marks (-50 to +50 cents)
            val totalTicks = 20
            for (i in 0..totalTicks) {
                val tickCents = -50f + i * 5f
                val angleDeg = 270f + (tickCents / 50f) * 60f
                val angleRad = Math.toRadians(angleDeg.toDouble())

                val isMajor = tickCents % 25f == 0f || tickCents == 0f
                val isCenter = tickCents == 0f

                val tickLength = if (isCenter) 22.dp.toPx() else if (isMajor) 16.dp.toPx() else 10.dp.toPx()
                val tickWidth = if (isCenter) 3.5.dp.toPx() else if (isMajor) 2.dp.toPx() else 1.dp.toPx()

                val tickColor = when {
                    isCenter -> AccentGreen
                    isMajor -> onSurfaceColor.copy(alpha = 0.6f)
                    else -> onSurfaceVariantColor.copy(alpha = 0.35f)
                }

                val innerR = radius - 18.dp.toPx()
                val outerR = innerR + tickLength

                val startX = centerX + innerR * cos(angleRad).toFloat()
                val startY = centerY + innerR * sin(angleRad).toFloat()
                val endX = centerX + outerR * cos(angleRad).toFloat()
                val endY = centerY + outerR * sin(angleRad).toFloat()

                drawLine(
                    color = tickColor,
                    start = Offset(startX, startY),
                    end = Offset(endX, endY),
                    strokeWidth = tickWidth,
                    cap = StrokeCap.Round
                )
            }

            // Needle Angle (-50 cents -> 210 deg, 0 -> 270 deg, +50 cents -> 330 deg)
            val needleAngleDeg = 270f + (animatedCents / 50f) * 60f
            val needleAngleRad = Math.toRadians(needleAngleDeg.toDouble())

            val needleColor = when {
                !hasSignal -> primaryColor.copy(alpha = 0.4f)
                isInTune -> AccentGreen
                animatedCents < 0 -> AccentOrange
                else -> AccentRed
            }

            val needleLength = radius - 8.dp.toPx()
            val needleEndX = centerX + needleLength * cos(needleAngleRad).toFloat()
            val needleEndY = centerY + needleLength * sin(needleAngleRad).toFloat()

            // Draw Needle shadow & main line
            drawLine(
                color = needleColor.copy(alpha = 0.25f),
                start = Offset(centerX, centerY),
                end = Offset(needleEndX + 2.dp.toPx(), needleEndY + 2.dp.toPx()),
                strokeWidth = 6.dp.toPx(),
                cap = StrokeCap.Round
            )

            drawLine(
                color = needleColor,
                start = Offset(centerX, centerY),
                end = Offset(needleEndX, needleEndY),
                strokeWidth = 4.dp.toPx(),
                cap = StrokeCap.Round
            )

            // Pivot Circle
            drawCircle(
                color = needleColor,
                radius = 12.dp.toPx(),
                center = Offset(centerX, centerY)
            )
            drawCircle(
                color = surfaceColor,
                radius = 5.dp.toPx(),
                center = Offset(centerX, centerY)
            )
        }

        // Center Note Display Overlay
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(bottom = 20.dp)
        ) {
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = detectedNote,
                    fontSize = 52.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (isInTune && hasSignal) AccentGreen else MaterialTheme.colorScheme.onSurface
                )
                if (hasSignal && octave > 0) {
                    Text(
                        text = octave.toString(),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = primaryColor,
                        modifier = Modifier.padding(bottom = 8.dp, start = 2.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun BentoMetricItem(
    label: String,
    value: String,
    unit: String
) {
    val primaryColor = MaterialTheme.colorScheme.primary

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.0.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(2.dp))
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = value,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (unit.isNotEmpty()) {
                Spacer(modifier = Modifier.width(2.dp))
                Text(
                    text = unit,
                    fontSize = 11.sp,
                    color = primaryColor,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
