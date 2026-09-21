package com.example.qty.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.qty.data.intake.MarketTick
import com.example.qty.data.integrity.IntegrityState
import com.example.qty.ledger.EvaluationTier
import com.example.qty.ledger.TrendLedgerEntity
import com.example.qty.pricedynamics.trend.TrendDirection
import com.example.qty.pricedynamics.trend.TrendEngineOutput
import com.example.qty.temporal.TemporalState
import com.example.ui.theme.TelemetryAmber
import com.example.ui.theme.TelemetryCardBorder
import com.example.ui.theme.TelemetryCyan
import com.example.ui.theme.TelemetryGreen
import com.example.ui.theme.TelemetryObsidian
import com.example.ui.theme.TelemetryRed
import com.example.ui.theme.TelemetrySurface
import com.example.ui.theme.TelemetrySurfaceVariant
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun TelemetryHeaderCard(
    tick: MarketTick?,
    integrityState: IntegrityState,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("telemetry_header_card"),
        shape = RoundedCornerShape(12.dp),
        color = TelemetrySurface,
        border = androidx.compose.foundation.BorderStroke(1.dp, TelemetryCardBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(
                                if (integrityState.isPassing) TelemetryGreen else TelemetryRed,
                                CircleShape
                            )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = tick?.sourceIdentity ?: "AWAITING AUTHENTIC INTAKE",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                val (badgeColor, badgeText) = when (integrityState) {
                    is IntegrityState.Nominal -> Pair(TelemetryGreen, "INTEGRITY: PASSING")
                    is IntegrityState.FailedClosed -> Pair(TelemetryRed, "FAIL_CLOSED")
                    is IntegrityState.ChronologicalViolation -> Pair(TelemetryRed, "TIME_ORDER_FAIL")
                    is IntegrityState.ValueViolation -> Pair(TelemetryRed, "VALUE_FAIL")
                    is IntegrityState.StaleData -> Pair(TelemetryAmber, "STALE_DATA")
                }

                Box(
                    modifier = Modifier
                        .background(badgeColor.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                        .border(1.dp, badgeColor.copy(alpha = 0.6f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = badgeText,
                        color = badgeColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(
                        text = "BTC / USD",
                        color = TextMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = if (tick != null) String.format(Locale.US, "$%,.2f", tick.price) else "---.--",
                        color = TextPrimary,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "EXCHANGE TIME",
                        color = TextMuted,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    val timeStr = if (tick != null) {
                        val sdf = SimpleDateFormat("HH:mm:ss.SSS", Locale.US)
                        sdf.format(Date(tick.timestampMs))
                    } else "--:--:--.---"
                    Text(
                        text = timeStr,
                        color = TelemetryCyan,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = if (tick != null) "Latency: ${tick.latencyMs}ms | Seq #${tick.sequenceId}" else "Syncing...",
                        color = TextMuted,
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}

@Composable
fun TrendEngineMetricsCard(
    output: TrendEngineOutput?,
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
        shape = RoundedCornerShape(12.dp),
        color = TelemetrySurface,
        border = androidx.compose.foundation.BorderStroke(1.dp, TelemetryCardBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "TREND EXPERIMENTAL ENGINE",
                    color = TelemetryCyan,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "DUO WEIGHT: 50%",
                    color = TextMuted,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Verdict Direction & Confidence
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(dirColor.copy(alpha = 0.15f), CircleShape)
                            .border(1.dp, dirColor, CircleShape),
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
                            text = "DIRECTION",
                            color = TextMuted,
                            fontSize = 10.sp,
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
                        fontSize = 10.sp,
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
                    .height(6.dp),
                color = dirColor,
                trackColor = TelemetrySurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 4 Specialized Equations Breakdown Grid
            Text(
                text = "SPECIALIST MEASUREMENTS (EVIDENCE)",
                color = TextMuted,
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = FontFamily.Monospace
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MetricPill(
                    title = "OLS SLOPE (β)",
                    value = if (evidence != null) String.format(Locale.US, "%+.2f $/s", evidence.slopePerSecond) else "--",
                    subtitle = if (evidence != null) String.format(Locale.US, "%+.2f bps/s", evidence.normalizedSlopeBps) else "--",
                    modifier = Modifier.weight(1f)
                )
                MetricPill(
                    title = "LINEARITY (R²)",
                    value = if (evidence != null) String.format(Locale.US, "%.3f", evidence.rSquared) else "--",
                    subtitle = if (evidence != null) String.format(Locale.US, "t = %.2f", evidence.tStatistic) else "--",
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MetricPill(
                    title = "EMA SPREAD",
                    value = if (evidence?.emaSpreadPercent != null) String.format(Locale.US, "%+.3f%%", evidence.emaSpreadPercent) else "--",
                    subtitle = "Fast 10 vs Slow 30",
                    modifier = Modifier.weight(1f)
                )
                MetricPill(
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
                        .background(TelemetryObsidian, RoundedCornerShape(6.dp))
                        .padding(8.dp)
                ) {
                    Text(
                        text = verdict.justification,
                        color = TextSecondary,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}

@Composable
private fun MetricPill(
    title: String,
    value: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(TelemetrySurfaceVariant, RoundedCornerShape(8.dp))
            .border(1.dp, TelemetryCardBorder, RoundedCornerShape(8.dp))
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
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
            Spacer(modifier = Modifier.height(1.dp))
            Text(
                text = subtitle,
                color = TelemetryCyan,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

@Composable
fun TemporalControlsCard(
    temporalState: TemporalState,
    onObservationWindowSelected: (Int) -> Unit,
    onTargetHorizonSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("temporal_controls_card"),
        shape = RoundedCornerShape(12.dp),
        color = TelemetrySurface,
        border = androidx.compose.foundation.BorderStroke(1.dp, TelemetryCardBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "TEMPORAL STATE CONFIGURATION",
                color = TelemetryCyan,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
            Text(
                text = "Observation window and prediction horizon remain conceptually separate.",
                color = TextMuted,
                fontSize = 10.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "OBSERVATION WINDOW (W) — LOOKBACK MEASUREMENT",
                color = TextSecondary,
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = FontFamily.Monospace
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                TemporalState.OBSERVATION_WINDOWS_SECONDS.forEach { windowSec ->
                    val isSelected = temporalState.observationWindowSeconds == windowSec
                    ChipSelector(
                        label = "${windowSec}s",
                        isSelected = isSelected,
                        onClick = { onObservationWindowSelected(windowSec) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "EXPERIMENTAL TARGET HORIZONS (H) — NO-LOOKAHEAD FORWARD",
                color = TextSecondary,
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = FontFamily.Monospace
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                TemporalState.EXPERIMENTAL_HORIZONS_SECONDS.forEach { horizonSec ->
                    val isSelected = temporalState.targetHorizonSeconds == horizonSec
                    ChipSelector(
                        label = "${horizonSec}s",
                        isSelected = isSelected,
                        onClick = { onTargetHorizonSelected(horizonSec) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ChipSelector(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .background(
                if (isSelected) TelemetryCyan.copy(alpha = 0.2f) else TelemetrySurfaceVariant,
                RoundedCornerShape(6.dp)
            )
            .border(
                1.dp,
                if (isSelected) TelemetryCyan else TelemetryCardBorder,
                RoundedCornerShape(6.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = label,
            color = if (isSelected) TelemetryCyan else TextSecondary,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            fontFamily = FontFamily.Monospace
        )
    }
}

@Composable
fun EvaluationLedgerCard(
    totalCount: Int,
    tier: EvaluationTier,
    recentEntries: List<TrendLedgerEntity>,
    onCommitSnapshot: () -> Unit,
    onClearLedger: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("evaluation_ledger_card"),
        shape = RoundedCornerShape(12.dp),
        color = TelemetrySurface,
        border = androidx.compose.foundation.BorderStroke(1.dp, TelemetryCardBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "EVALUATION LEDGER & AUDIT",
                    color = TelemetryCyan,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                val tierColor = when (tier) {
                    EvaluationTier.INSUFFICIENT -> TelemetryAmber
                    EvaluationTier.PRELIMINARY -> TelemetryCyan
                    EvaluationTier.CALIBRATION_READY -> TelemetryGreen
                }
                Box(
                    modifier = Modifier
                        .background(tierColor.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                        .border(1.dp, tierColor.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = tier.name,
                        color = tierColor,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // QtY Rule: Accuracy must remain null below minimum sample threshold
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "SAMPLE DEPTH (N)",
                        color = TextMuted,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "$totalCount entries",
                        color = TextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "ACCURACY METRIC",
                        color = TextMuted,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = if (totalCount < 100) "NULL (N < 100)" else "PHASE 3 ACTIVE",
                        color = if (totalCount < 100) TelemetryAmber else TelemetryGreen,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = tier.description,
                color = TextMuted,
                fontSize = 9.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(TelemetryCyan.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                        .border(1.dp, TelemetryCyan.copy(alpha = 0.6f), RoundedCornerShape(6.dp))
                        .clickable(onClick = onCommitSnapshot)
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "COMMIT SNAPSHOT",
                        color = TelemetryCyan,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Box(
                    modifier = Modifier
                        .background(TelemetrySurfaceVariant, RoundedCornerShape(6.dp))
                        .border(1.dp, TelemetryCardBorder, RoundedCornerShape(6.dp))
                        .clickable(onClick = onClearLedger)
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "CLEAR",
                        color = TextMuted,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Stream Table of Latest Entries
            Text(
                text = "AUDIT STREAM (LATEST RECORDED TELEMETRY)",
                color = TextSecondary,
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = FontFamily.Monospace
            )
            Spacer(modifier = Modifier.height(6.dp))

            if (recentEntries.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(TelemetryObsidian, RoundedCornerShape(6.dp))
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No ledger entries yet. Telemetry writes automatically during stream.",
                        color = TextMuted,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(TelemetryObsidian, RoundedCornerShape(6.dp))
                        .padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    recentEntries.take(5).forEach { entry ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val time = SimpleDateFormat("HH:mm:ss", Locale.US).format(Date(entry.timestampMs))
                            Text(
                                text = time,
                                color = TextMuted,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = String.format(Locale.US, "$%,.1f", entry.latestPrice),
                                color = TextPrimary,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = String.format(Locale.US, "%+.2f $/s", entry.slopePerSecond),
                                color = if (entry.slopePerSecond >= 0) TelemetryGreen else TelemetryRed,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = "R²=${String.format(Locale.US, "%.2f", entry.rSquared)}",
                                color = TelemetryCyan,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace
                            )
                            val dColor = when (entry.trendDirection) {
                                "UP" -> TelemetryGreen
                                "DOWN" -> TelemetryRed
                                else -> TelemetryCyan
                            }
                            Text(
                                text = entry.trendDirection,
                                color = dColor,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }
        }
    }
}
