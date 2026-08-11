package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun AnimatedMarchingMusician(
    modifier: Modifier = Modifier,
    size: Dp = 220.dp
) {
    val infiniteTransition = rememberInfiniteTransition(label = "marching")

    // Walking / leg swing phase
    val legSwing by infiniteTransition.animateFloat(
        initialValue = -1.0f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "legSwing"
    )

    // Trombone slide extension phase
    val slideExtend by infiniteTransition.animateFloat(
        initialValue = 0.0f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "slideExtend"
    )

    // Body/hat vertical bounce
    val bodyBounce by infiniteTransition.animateFloat(
        initialValue = 0.0f,
        targetValue = 8.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(300, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bodyBounce"
    )

    // Floating musical notes offset
    val notesFloat by infiniteTransition.animateFloat(
        initialValue = 0.0f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "notesFloat"
    )

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.toPx()
            val height = size.toPx()
            val centerX = width * 0.45f
            val groundY = height * 0.85f

            // 1. Soft glowing background radial aura
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF7C4DFF).copy(alpha = 0.25f),
                        Color(0xFF651FFF).copy(alpha = 0.08f),
                        Color.Transparent
                    ),
                    center = Offset(centerX, height * 0.5f),
                    radius = width * 0.55f
                ),
                radius = width * 0.55f,
                center = Offset(centerX, height * 0.5f)
            )

            // 2. Floating musical notes rising & drifting
            val noteSymbols = listOf("♪", "♫", "♩", "♬")
            val noteOffsets = listOf(
                Offset(0.72f, 0.45f),
                Offset(0.82f, 0.35f),
                Offset(0.78f, 0.25f),
                Offset(0.88f, 0.18f)
            )

            for (i in noteOffsets.indices) {
                val progress = (notesFloat + i * 0.25f) % 1.0f
                val noteX = width * (noteOffsets[i].x + sin(progress * 3.14f * 2) * 0.05f)
                val noteY = height * (noteOffsets[i].y - progress * 0.35f)
                val alpha = (1.0f - progress).coerceIn(0.0f, 1.0f)

                drawCircle(
                    color = Color(0xFF00E676).copy(alpha = alpha * 0.8f),
                    radius = 4.dp.toPx() * (1.0f - progress * 0.5f),
                    center = Offset(noteX, noteY)
                )

                drawCircle(
                    color = Color(0xFF7C4DFF).copy(alpha = alpha * 0.5f),
                    radius = 8.dp.toPx() * (1.0f - progress * 0.3f),
                    center = Offset(noteX - 10.dp.toPx(), noteY + 5.dp.toPx())
                )
            }

            // 3. Musician Drawing Parameters
            val bounceY = groundY - (height * 0.45f) - (bodyBounce * 0.5f)
            val torsoTopY = bounceY - height * 0.15f
            val headCenterY = torsoTopY - height * 0.08f

            val goldColor = Color(0xFFFFB300)
            val bandGreen = Color(0xFF00E676)
            val bandOrange = Color(0xFFFF6D00)
            val violetAccent = Color(0xFF7C4DFF)
            val darkUniform = Color(0xFF1C1A26)

            // --- LEGS (Marching Motion) ---
            val hipX = centerX
            val hipY = bounceY

            // Left Leg (swings forward/back)
            val leftKneeX = hipX - 15.dp.toPx() * legSwing
            val leftKneeY = hipY + 25.dp.toPx()
            val leftFootX = leftKneeX - 10.dp.toPx() * legSwing
            val leftFootY = groundY

            val legPathLeft = Path().apply {
                moveTo(hipX, hipY)
                lineTo(leftKneeX, leftKneeY)
                lineTo(leftFootX, leftFootY)
            }
            drawPath(
                path = legPathLeft,
                color = darkUniform,
                style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
            )

            // Right Leg
            val rightKneeX = hipX + 15.dp.toPx() * legSwing
            val rightKneeY = hipY + 25.dp.toPx()
            val rightFootX = rightKneeX + 10.dp.toPx() * legSwing
            val rightFootY = groundY

            val legPathRight = Path().apply {
                moveTo(hipX, hipY)
                lineTo(rightKneeX, rightKneeY)
                lineTo(rightFootX, rightFootY)
            }
            drawPath(
                path = legPathRight,
                color = darkUniform,
                style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
            )

            // White marching shoes
            drawCircle(color = Color.White, radius = 5.dp.toPx(), center = Offset(leftFootX, leftFootY))
            drawCircle(color = Color.White, radius = 5.dp.toPx(), center = Offset(rightFootX, rightFootY))

            // --- TORSO / MARCHING BAND UNIFORM ---
            val torsoPath = Path().apply {
                moveTo(centerX - 14.dp.toPx(), bounceY)
                lineTo(centerX + 14.dp.toPx(), bounceY)
                lineTo(centerX + 16.dp.toPx(), torsoTopY)
                lineTo(centerX - 16.dp.toPx(), torsoTopY)
                close()
            }
            drawPath(path = torsoPath, color = darkUniform)

            // Uniform Sash / Trim (Orange & Green)
            val sashPath = Path().apply {
                moveTo(centerX - 16.dp.toPx(), torsoTopY)
                lineTo(centerX + 14.dp.toPx(), bounceY)
                lineTo(centerX + 8.dp.toPx(), bounceY)
                lineTo(centerX - 16.dp.toPx(), torsoTopY + 12.dp.toPx())
                close()
            }
            drawPath(path = sashPath, color = bandOrange)

            // Shoulder Epaulets
            drawRect(
                color = goldColor,
                topLeft = Offset(centerX - 20.dp.toPx(), torsoTopY),
                size = androidx.compose.ui.geometry.Size(8.dp.toPx(), 4.dp.toPx())
            )
            drawRect(
                color = goldColor,
                topLeft = Offset(centerX + 12.dp.toPx(), torsoTopY),
                size = androidx.compose.ui.geometry.Size(8.dp.toPx(), 4.dp.toPx())
            )

            // --- HEAD & MARCHING SHAKO HAT ---
            // Head
            drawCircle(
                color = Color(0xFFFFCC80),
                radius = 12.dp.toPx(),
                center = Offset(centerX, headCenterY)
            )

            // Shako Hat (Tall marching hat)
            val shakoBottomY = headCenterY - 6.dp.toPx()
            val shakoTopY = shakoBottomY - 26.dp.toPx()

            val shakoPath = Path().apply {
                moveTo(centerX - 13.dp.toPx(), shakoBottomY)
                lineTo(centerX + 13.dp.toPx(), shakoBottomY)
                lineTo(centerX + 11.dp.toPx(), shakoTopY)
                lineTo(centerX - 11.dp.toPx(), shakoTopY)
                close()
            }
            drawPath(path = shakoPath, color = darkUniform)

            // Shako Visor / Plume
            drawRect(
                color = bandGreen,
                topLeft = Offset(centerX - 13.dp.toPx(), shakoTopY + 10.dp.toPx()),
                size = androidx.compose.ui.geometry.Size(26.dp.toPx(), 4.dp.toPx())
            )

            // Feather Plume on top
            val plumePath = Path().apply {
                moveTo(centerX, shakoTopY)
                quadraticTo(
                    centerX - 10.dp.toPx(), shakoTopY - 20.dp.toPx(),
                    centerX - 4.dp.toPx(), shakoTopY - 25.dp.toPx()
                )
                quadraticTo(
                    centerX + 8.dp.toPx(), shakoTopY - 18.dp.toPx(),
                    centerX, shakoTopY
                )
            }
            drawPath(path = plumePath, color = bandOrange)

            // --- TROMBONE INSTRUMENT ---
            val mouthX = centerX + 10.dp.toPx()
            val mouthY = headCenterY + 2.dp.toPx()

            // Outer slide extension position
            val slideOffset = 20.dp.toPx() + slideExtend * 35.dp.toPx()

            val bellX = mouthX + 50.dp.toPx()
            val bellY = mouthY - 10.dp.toPx()

            // Trombone Main Outer Tubing (Brass Gold)
            val mainTubePath = Path().apply {
                moveTo(mouthX, mouthY)
                lineTo(bellX, bellY)
            }
            drawPath(
                path = mainTubePath,
                color = goldColor,
                style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
            )

            // Trombone Bell Flare
            val bellFlare = Path().apply {
                moveTo(bellX, bellY - 2.dp.toPx())
                quadraticTo(
                    bellX + 15.dp.toPx(), bellY - 18.dp.toPx(),
                    bellX + 22.dp.toPx(), bellY - 24.dp.toPx()
                )
                lineTo(bellX + 22.dp.toPx(), bellY + 18.dp.toPx())
                quadraticTo(
                    bellX + 15.dp.toPx(), bellY + 12.dp.toPx(),
                    bellX, bellY + 2.dp.toPx()
                )
                close()
            }
            drawPath(path = bellFlare, color = goldColor)

            // Inner Slide & Moving Outer Slide
            val slideInnerPath = Path().apply {
                moveTo(mouthX + 5.dp.toPx(), mouthY + 5.dp.toPx())
                lineTo(mouthX + 80.dp.toPx(), mouthY + 5.dp.toPx())
            }
            drawPath(
                path = slideInnerPath,
                color = Color.LightGray,
                style = Stroke(width = 3.dp.toPx())
            )

            // Moving Outer Slide (Extends out and back)
            val slideOuterPath = Path().apply {
                moveTo(mouthX + 10.dp.toPx() + slideOffset, mouthY + 3.dp.toPx())
                lineTo(mouthX + 35.dp.toPx() + slideOffset, mouthY + 3.dp.toPx())
                lineTo(mouthX + 35.dp.toPx() + slideOffset, mouthY + 7.dp.toPx())
                lineTo(mouthX + 10.dp.toPx() + slideOffset, mouthY + 7.dp.toPx())
            }
            drawPath(
                path = slideOuterPath,
                color = goldColor,
                style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Square)
            )

            // --- ARMS / HANDS HOLDING TROMBONE ---
            // Right Hand on slide
            val rightHandX = mouthX + 20.dp.toPx() + slideOffset
            val rightHandY = mouthY + 5.dp.toPx()

            val rightArmPath = Path().apply {
                moveTo(centerX + 12.dp.toPx(), torsoTopY + 10.dp.toPx())
                lineTo(rightHandX, rightHandY)
            }
            drawPath(
                path = rightArmPath,
                color = darkUniform,
                style = Stroke(width = 6.dp.toPx(), cap = StrokeCap.Round)
            )

            // Left Hand holding inner brace
            val leftHandX = mouthX + 12.dp.toPx()
            val leftHandY = mouthY + 5.dp.toPx()

            val leftArmPath = Path().apply {
                moveTo(centerX - 10.dp.toPx(), torsoTopY + 10.dp.toPx())
                lineTo(leftHandX, leftHandY)
            }
            drawPath(
                path = leftArmPath,
                color = darkUniform,
                style = Stroke(width = 6.dp.toPx(), cap = StrokeCap.Round)
            )

            // Gloves
            drawCircle(color = Color.White, radius = 4.dp.toPx(), center = Offset(rightHandX, rightHandY))
            drawCircle(color = Color.White, radius = 4.dp.toPx(), center = Offset(leftHandX, leftHandY))
        }
    }
}
