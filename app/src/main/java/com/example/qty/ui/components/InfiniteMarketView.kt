package com.example.qty.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.qty.pricedynamics.trend.TrendDirection
import com.example.ui.theme.TelemetryAmber
import com.example.ui.theme.TelemetryCardBorder
import com.example.ui.theme.TelemetryCyan
import com.example.ui.theme.TelemetryCyanDim
import com.example.ui.theme.TelemetryGreen
import com.example.ui.theme.TelemetryObsidian
import com.example.ui.theme.TelemetryPurple
import com.example.ui.theme.TelemetryRed
import com.example.ui.theme.TelemetrySurfaceVariant
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.util.Locale
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Infinite Market View / Orbital Mathematical Visualization.
 *
 * Primary visual metaphor for QtY TV:
 * - Cosmic / continuous quantitative state space (deep space, orbital guide rings, particle manifold).
 * - Trend orientation: directional flow vector and orbital alignment reflecting verified Trend evidence.
 * - Volatility field: expanding orbital radii and ambient wave amplitude.
 *
 * NOTE: Pure presentation layer. Does NOT generate, alter, or interpolate quantitative data.
 */
@Composable
fun InfiniteMarketView(
    points: List<Pair<Long, Double>>,
    slopePerSecond: Double,
    rSquared: Double,
    direction: TrendDirection,
    modifier: Modifier = Modifier,
    enableAnimation: Boolean = true
) {
    val rotationPhase: Float
    val pulseScale: Float

    if (enableAnimation) {
        val infiniteTransition = rememberInfiniteTransition(label = "orbital_drift")
        val animatedRotation by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 32000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "orbital_rotation"
        )
        val animatedPulse by infiniteTransition.animateFloat(
            initialValue = 0.94f,
            targetValue = 1.06f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 3800, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "core_pulse"
        )
        rotationPhase = animatedRotation
        pulseScale = animatedPulse
    } else {
        rotationPhase = 45f
        pulseScale = 1.0f
    }

    // Pre-seed pseudo-random deterministic stars so background stays crisp and persistent
    val stars = remember {
        List(42) { index ->
            val angle = (index * 47.3f) % (2f * PI.toFloat())
            val distanceRatio = 0.15f + ((index * 29) % 85) / 100f
            val starSize = if (index % 5 == 0) 2.2f else 1.2f
            val alpha = 0.25f + ((index * 17) % 65) / 100f
            Triple(angle, distanceRatio, Pair(starSize, alpha))
        }
    }

    val (accentColor, dirLabel) = when (direction) {
        TrendDirection.UP -> Pair(TelemetryGreen, "DRIFT: POSITIVE VECTOR (+)")
        TrendDirection.DOWN -> Pair(TelemetryRed, "DRIFT: NEGATIVE VECTOR (-)")
        TrendDirection.NO_TREND -> Pair(TelemetryCyan, "DRIFT: NEUTRAL HARMONIC (0)")
        TrendDirection.FAIL_CLOSED -> Pair(TelemetryAmber, "STATE: FAIL_CLOSED (MUTED)")
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(260.dp)
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF0F172A),
                        TelemetryObsidian,
                        Color(0xFF030712)
                    )
                ),
                shape = RoundedCornerShape(16.dp)
            )
            .border(1.dp, TelemetryCardBorder.copy(alpha = 0.8f), RoundedCornerShape(16.dp))
            .padding(12.dp)
            .testTag("trend_canvas_box")
    ) {
        // Core Mathematical Canvas
        Canvas(modifier = Modifier.fillMaxSize().testTag("infinite_market_canvas")) {
            val centerX = size.width / 2f
            val centerY = size.height / 2f
            val maxRadius = minOf(centerX, centerY) * 0.88f

            // 1. Draw cosmic star particles (Static deterministic celestial background)
            for (star in stars) {
                val r = maxRadius * star.second
                val sx = centerX + r * cos(star.first)
                val sy = centerY + r * sin(star.first)
                drawCircle(
                    color = Color(0xFF93C5FD).copy(alpha = star.third.second),
                    radius = star.third.first,
                    center = Offset(sx, sy)
                )
            }

            // 2. Mathematical Orbital Rings (Concentric Iso-potential State Rings)
            val ringCount = 4
            val ringRadii = listOf(0.30f, 0.52f, 0.74f, 0.95f)
            for ((idx, ratio) in ringRadii.withIndex()) {
                val ringRadius = maxRadius * ratio * (if (idx == 1) pulseScale else 1f)
                val ringAlpha = if (idx == 1) 0.35f else 0.16f
                drawCircle(
                    color = TelemetryCyanDim.copy(alpha = ringAlpha),
                    radius = ringRadius,
                    center = Offset(centerX, centerY),
                    style = Stroke(
                        width = 1f,
                        pathEffect = if (idx % 2 == 1) PathEffect.dashPathEffect(floatArrayOf(8f, 10f), 0f) else null
                    )
                )
            }

            // 3. Axis Crosshair Coordinates (Mathematical alignment)
            drawLine(
                color = TelemetryCardBorder.copy(alpha = 0.4f),
                start = Offset(centerX - maxRadius, centerY),
                end = Offset(centerX + maxRadius, centerY),
                strokeWidth = 1f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 8f), 0f)
            )
            drawLine(
                color = TelemetryCardBorder.copy(alpha = 0.4f),
                start = Offset(centerX, centerY - maxRadius),
                end = Offset(centerX, centerY + maxRadius),
                strokeWidth = 1f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 8f), 0f)
            )

            // 4. Authentic Telemetry Data Trajectory (Mapped into Continuous Polar Space)
            if (points.size >= 2) {
                val prices = points.map { it.second }
                val minP = prices.minOrNull() ?: 0.0
                val maxP = prices.maxOrNull() ?: 1.0
                val pRange = (maxP - minP).coerceAtLeast(0.01)

                val pointOffsets = mutableListOf<Offset>()
                val count = points.size
                for (i in points.indices) {
                    val progress = i.toFloat() / (count - 1).coerceAtLeast(1)
                    // Angle ranges around circular arc with rotation
                    val angleDeg = 180f + progress * 240f + (rotationPhase * 0.25f)
                    val angleRad = Math.toRadians(angleDeg.toDouble())
                    val normPrice = ((points[i].second - minP) / pRange).toFloat()
                    val r = maxRadius * (0.35f + normPrice * 0.48f)
                    val px = centerX + (r * cos(angleRad)).toFloat()
                    val py = centerY + (r * sin(angleRad)).toFloat()
                    pointOffsets.add(Offset(px, py))
                }

                // Connect authentic trajectory points
                for (i in 0 until pointOffsets.size - 1) {
                    val p1 = pointOffsets[i]
                    val p2 = pointOffsets[i + 1]
                    drawLine(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                TelemetryCyan.copy(alpha = 0.25f + 0.5f * (i.toFloat() / count)),
                                accentColor.copy(alpha = 0.9f)
                            ),
                            start = p1,
                            end = p2
                        ),
                        start = p1,
                        end = p2,
                        strokeWidth = 2.4f,
                        cap = androidx.compose.ui.graphics.StrokeCap.Round
                    )
                }

                // Head Particle (Latest observed authentic state)
                val head = pointOffsets.last()
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(accentColor, accentColor.copy(alpha = 0f)),
                        center = head,
                        radius = 18f
                    ),
                    radius = 18f,
                    center = head
                )
                drawCircle(
                    color = Color.White,
                    radius = 4.2f,
                    center = head
                )
            }

            // 5. Central Quantum Core (State center)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        TelemetryPurple.copy(alpha = 0.35f),
                        TelemetryCyanDim.copy(alpha = 0.12f),
                        Color.Transparent
                    ),
                    center = Offset(centerX, centerY),
                    radius = maxRadius * 0.28f * pulseScale
                ),
                radius = maxRadius * 0.28f * pulseScale,
                center = Offset(centerX, centerY)
            )
            drawCircle(
                color = TelemetryCyan.copy(alpha = 0.85f),
                radius = 3.5f,
                center = Offset(centerX, centerY)
            )

            // 6. Directional Vector Flow Indicator (Trend Orientation)
            val vectorAngle = when (direction) {
                TrendDirection.UP -> -45.0 // Pointing ascending quadrant
                TrendDirection.DOWN -> 45.0 // Pointing descending quadrant
                TrendDirection.NO_TREND -> 0.0
                TrendDirection.FAIL_CLOSED -> 90.0
            }
            val rad = Math.toRadians(vectorAngle)
            val vecLength = maxRadius * 0.65f
            val vecEnd = Offset(
                x = centerX + (vecLength * cos(rad)).toFloat(),
                y = centerY + (vecLength * sin(rad)).toFloat()
            )
            drawLine(
                color = accentColor.copy(alpha = 0.7f),
                start = Offset(centerX, centerY),
                end = vecEnd,
                strokeWidth = 2f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
            )
            drawCircle(
                color = accentColor,
                radius = 3f,
                center = vecEnd
            )
        }

        // Top Metadata Overlay
        Row(
            modifier = Modifier.fillMaxWidth().align(Alignment.TopStart),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .background(accentColor, CircleShape)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "INFINITE MARKET VIEW // ORBITAL TELEMETRY",
                color = TextSecondary,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 0.5.sp
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = "N=${points.size} SAMPLES",
                color = TelemetryCyan,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace
            )
        }

        // Bottom Mathematical Grounding Overlay
        Row(
            modifier = Modifier.fillMaxWidth().align(Alignment.BottomStart),
            verticalAlignment = Alignment.Bottom
        ) {
            Column {
                Text(
                    text = dirLabel,
                    color = accentColor,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = String.format(Locale.US, "SLOPE: %+.4f $/s | R²: %.3f", slopePerSecond, rSquared),
                    color = TextMuted,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            Box(
                modifier = Modifier
                    .background(Color(0xFF030712).copy(alpha = 0.75f), RoundedCornerShape(4.dp))
                    .border(0.5.dp, TelemetryCardBorder, RoundedCornerShape(4.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "CONTINUOUS STATE SPACE",
                    color = TextMuted,
                    fontSize = 8.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}
