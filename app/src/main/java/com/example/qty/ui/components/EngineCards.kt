package com.example.qty.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingFlat
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.qty.pricedynamics.trend.TrendDirection
import com.example.qty.pricedynamics.trend.TrendEngineOutput
import com.example.qty.temporal.TemporalState
import com.example.ui.theme.TelemetryAmber
import com.example.ui.theme.TelemetryCardBorder
import com.example.ui.theme.TelemetryCyan
import com.example.ui.theme.TelemetryCyanDim
import com.example.ui.theme.TelemetryGreen
import com.example.ui.theme.TelemetryObsidian
import com.example.ui.theme.TelemetryPurple
import com.example.ui.theme.TelemetryRed
import com.example.ui.theme.TelemetrySurface
import com.example.ui.theme.TelemetrySurfaceVariant
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.util.Locale

/**
 * TREND ENGINE CARD (Primary Engine 1 of 2)
 *
 * Displays:
 * - Current independently calculated Trend evidence
 * - Relevant state/status
 * - Appropriate temporal/horizon context
 * - Clear indication that this is the Trend engine only
 */
@Composable
fun TrendEngineCard(
    output: TrendEngineOutput?,
    temporalState: TemporalState,
    modifier: Modifier = Modifier
) {
    val evidence = output?.evidence
    val verdict = output?.verdict

    val direction = verdict?.direction ?: TrendDirection.FAIL_CLOSED
    val confidence = verdict?.judgeConfidence ?: 0.0

    val (dirColor, dirIcon) = when (direction) {
        TrendDirection.UP -> Pair(TelemetryGreen, Icons.Filled.TrendingUp)
        TrendDirection.DOWN -> Pair(TelemetryRed, Icons.Filled.TrendingDown)
        TrendDirection.NO_TREND -> Pair(TelemetryCyan, Icons.Filled.TrendingFlat)
        TrendDirection.FAIL_CLOSED -> Pair(TelemetryAmber, Icons.Filled.Warning)
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("trend_engine_metrics_card"),
        shape = RoundedCornerShape(14.dp),
        color = TelemetrySurface.copy(alpha = 0.85f),
        border = androidx.compose.foundation.BorderStroke(1.dp, TelemetryCardBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: Strict Engine Identification
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(TelemetryCyan, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "ENGINE 1 // TREND ENGINE",
                        color = TelemetryCyan,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 0.5.sp
                    )
                }

                Box(
                    modifier = Modifier
                        .background(TelemetryCyan.copy(alpha = 0.12f), RoundedCornerShape(4.dp))
                        .border(0.5.dp, TelemetryCyan.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "TREND ENGINE ONLY",
                        color = TelemetryCyan,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Primary Output Verdict & Consensus Confidence
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .background(dirColor.copy(alpha = 0.15f), CircleShape)
                            .border(1.dp, dirColor.copy(alpha = 0.7f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = dirIcon,
                            contentDescription = "Trend Direction",
                            tint = dirColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "DIRECTION STATE",
                            color = TextMuted,
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = direction.name,
                            color = dirColor,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "JUDGE CONFIDENCE",
                        color = TextMuted,
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = String.format(Locale.US, "%.1f%%", confidence * 100.0),
                        color = TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { confidence.toFloat() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp),
                color = dirColor,
                trackColor = TelemetrySurfaceVariant
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Horizon & Temporal Context
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "TEMPORAL HORIZON: W=${temporalState.observationWindowSeconds}s | H=${temporalState.targetHorizonSeconds}s",
                    color = TextSecondary,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "SAMPLES N=${evidence?.sampleCount ?: 0}",
                    color = TelemetryCyan,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Observable Deterministic Evidence Metrics
            Text(
                text = "OBSERVABLE MATHEMATICAL EVIDENCE",
                color = TextMuted,
                fontSize = 9.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = FontFamily.Monospace
            )
            Spacer(modifier = Modifier.height(6.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                EnginePill(
                    title = "OLS SLOPE (β)",
                    value = if (evidence != null) String.format(Locale.US, "%+.2f $/s", evidence.slopePerSecond) else "--",
                    subtitle = if (evidence != null) String.format(Locale.US, "%+.2f bps/s", evidence.normalizedSlopeBps) else "--",
                    modifier = Modifier.weight(1f)
                )
                EnginePill(
                    title = "LINEARITY (R²)",
                    value = if (evidence != null) String.format(Locale.US, "%.3f", evidence.rSquared) else "--",
                    subtitle = if (evidence != null) String.format(Locale.US, "t = %.2f", evidence.tStatistic) else "--",
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                EnginePill(
                    title = "EMA SPREAD",
                    value = if (evidence?.emaSpreadPercent != null) String.format(Locale.US, "%+.3f%%", evidence.emaSpreadPercent) else "--",
                    subtitle = "Fast 10 vs Slow 30",
                    modifier = Modifier.weight(1f)
                )
                EnginePill(
                    title = "CUMULATIVE DRIFT",
                    value = if (evidence?.cumulativeDriftBps != null) String.format(Locale.US, "%+.1f bps", evidence.cumulativeDriftBps) else "--",
                    subtitle = if (evidence?.channelPosition != null) "Pos: ${String.format(Locale.US, "%.0f%%", evidence.channelPosition * 100)}" else "--",
                    modifier = Modifier.weight(1f)
                )
            }

            if (verdict?.justification != null) {
                Spacer(modifier = Modifier.height(10.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(TelemetryObsidian.copy(alpha = 0.8f), RoundedCornerShape(6.dp))
                        .border(0.5.dp, TelemetryCardBorder, RoundedCornerShape(6.dp))
                        .padding(8.dp)
                ) {
                    Text(
                        text = verdict.justification,
                        color = TextSecondary,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        lineHeight = 14.sp
                    )
                }
            }
        }
    }
}

/**
 * VOLATILITY ENGINE CARD (Primary Engine 2 of 2)
 *
 * Displays:
 * - Current status of Volatility engine (Phase 1 intentionally unengaged)
 * - Clear indication that this is the Volatility engine only
 * - Honest unavailable/insufficient state without fabricated numbers
 * - Temporal context expectation (Phase 2 isolation)
 */
@Composable
fun VolatilityEngineCard(
    temporalState: TemporalState,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("volatility_engine_metrics_card"),
        shape = RoundedCornerShape(14.dp),
        color = TelemetrySurface.copy(alpha = 0.85f),
        border = androidx.compose.foundation.BorderStroke(1.dp, TelemetryCardBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: Strict Engine Identification
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(TelemetryPurple, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "ENGINE 2 // VOLATILITY ENGINE",
                        color = TelemetryPurple,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 0.5.sp
                    )
                }

                Box(
                    modifier = Modifier
                        .background(TelemetryPurple.copy(alpha = 0.12f), RoundedCornerShape(4.dp))
                        .border(0.5.dp, TelemetryPurple.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "VOLATILITY ENGINE ONLY",
                        color = TelemetryPurple,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Honest Unengaged / Locked State (Phase 1 Isolation Mandate)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(TelemetryObsidian.copy(alpha = 0.9f), RoundedCornerShape(10.dp))
                    .border(1.dp, TelemetryPurple.copy(alpha = 0.25f), RoundedCornerShape(10.dp))
                    .padding(14.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(TelemetryPurple.copy(alpha = 0.15f), CircleShape)
                            .border(1.dp, TelemetryPurple.copy(alpha = 0.5f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Lock,
                            contentDescription = "Volatility Engine Unengaged",
                            tint = TelemetryPurple,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "STATE: PHASE 1 ISOLATION (UNENGAGED)",
                            color = TelemetryPurple,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Zero fabricated metrics. Volatility quantitative engine will be independently engaged in Phase 2.",
                            color = TextMuted,
                            fontSize = 10.sp,
                            lineHeight = 14.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Context and Non-fabricated placeholders
            Text(
                text = "INDEPENDENT VOLATILITY EVIDENCE",
                color = TextMuted,
                fontSize = 9.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = FontFamily.Monospace
            )
            Spacer(modifier = Modifier.height(6.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                EnginePill(
                    title = "REALIZED VOL (σ)",
                    value = "UNAVAILABLE",
                    subtitle = "Phase 2 Pipeline",
                    modifier = Modifier.weight(1f)
                )
                EnginePill(
                    title = "PARKINSON / GK",
                    value = "UNAVAILABLE",
                    subtitle = "Phase 2 Pipeline",
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                EnginePill(
                    title = "VOL REGIME",
                    value = "AWAITING P2",
                    subtitle = "W=${temporalState.observationWindowSeconds}s context",
                    modifier = Modifier.weight(1f)
                )
                EnginePill(
                    title = "EXPANSION INDEX",
                    value = "AWAITING P2",
                    subtitle = "Zero synthetic fill",
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

/**
 * COMBINED PROTOTYPE EVIDENCE & OUTCOME (Flow pipeline)
 *
 * Implements the explicit flow:
 * BTC State → Trend → Volatility → Combined Prototype Evidence → Prediction/Outcome
 */
@Composable
fun CombinedPrototypeEvidenceCard(
    output: TrendEngineOutput?,
    temporalState: TemporalState,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("combined_prototype_evidence_card"),
        shape = RoundedCornerShape(14.dp),
        color = TelemetrySurface.copy(alpha = 0.85f),
        border = androidx.compose.foundation.BorderStroke(1.dp, TelemetryCardBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Science,
                        contentDescription = "Combined Evidence",
                        tint = TelemetryCyan,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "COMBINED PROTOTYPE EVIDENCE → PREDICTION/OUTCOME",
                        color = TextPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 0.5.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "PIPELINE: BTC State → Trend [Active] → Volatility [Pending P2] → Duo Synthesis",
                color = TextSecondary,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Pipeline flow visualizer
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(TelemetryObsidian, RoundedCornerShape(8.dp))
                    .border(0.5.dp, TelemetryCardBorder, RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                FlowStepItem(label = "BTC STATE", status = "AUTHENTIC", color = TelemetryGreen)
                Text("→", color = TextMuted, fontSize = 12.sp)
                FlowStepItem(label = "TREND", status = output?.verdict?.direction?.name ?: "EVAL", color = TelemetryCyan)
                Text("→", color = TextMuted, fontSize = 12.sp)
                FlowStepItem(label = "VOLATILITY", status = "PHASE 2", color = TelemetryPurple)
                Text("→", color = TextMuted, fontSize = 12.sp)
                FlowStepItem(label = "COMBINED", status = "PARTIAL P1", color = TelemetryAmber)
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Phase 1 Status: Trend engine operates with mathematical inspectability. Volatility engine is scheduled for Phase 2 implementation. Combined prediction synthesis will engage once both engines produce dual observable evidence.",
                color = TextMuted,
                fontSize = 9.sp,
                lineHeight = 13.sp
            )
        }
    }
}

@Composable
private fun FlowStepItem(label: String, status: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            color = TextMuted,
            fontSize = 8.sp,
            fontFamily = FontFamily.Monospace
        )
        Text(
            text = status,
            color = color,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
    }
}

@Composable
private fun EnginePill(
    title: String,
    value: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(TelemetrySurfaceVariant.copy(alpha = 0.7f), RoundedCornerShape(8.dp))
            .border(0.5.dp, TelemetryCardBorder, RoundedCornerShape(8.dp))
            .padding(10.dp)
    ) {
        Column {
            Text(
                text = title,
                color = TextMuted,
                fontSize = 9.sp,
                fontWeight = FontWeight.Medium,
                fontFamily = FontFamily.Monospace
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                color = TextPrimary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
            Spacer(modifier = Modifier.height(1.dp))
            Text(
                text = subtitle,
                color = TelemetryCyanDim,
                fontSize = 9.sp,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}
