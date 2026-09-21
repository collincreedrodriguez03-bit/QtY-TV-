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
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
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
import com.example.qty.pricedynamics.volatility.VolatilityEngineOutput
import com.example.qty.pricedynamics.volatility.VolatilityRegime
import com.example.qty.temporal.TemporalState
import com.example.ui.theme.TelemetryAmber
import com.example.ui.theme.TelemetryCardBorder
import com.example.ui.theme.TelemetryCyan
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
 * VOLATILITY ENGINE CARD (Engine 2 of 2)
 *
 * Displays:
 * - Independently calculated Volatility evidence and regime
 * - Realized annual volatility, returns std dev, Parkinson volatility, and ATR %
 * - Clear indication that this is the Volatility engine independently observable from Trend
 */
@Composable
fun VolatilityEngineCard(
    output: VolatilityEngineOutput?,
    temporalState: TemporalState,
    modifier: Modifier = Modifier
) {
    val evidence = output?.evidence
    val verdict = output?.verdict

    val regime = verdict?.regime ?: VolatilityRegime.FAIL_CLOSED
    val confidence = verdict?.judgeConfidence ?: 0.0

    val (regimeColor, regimeLabel) = when (regime) {
        VolatilityRegime.LOW_VOLATILITY -> Pair(TelemetryCyan, "LOW VOL")
        VolatilityRegime.NORMAL_VOLATILITY -> Pair(TelemetryGreen, "NORMAL VOL")
        VolatilityRegime.HIGH_VOLATILITY -> Pair(TelemetryRed, "HIGH VOL")
        VolatilityRegime.FAIL_CLOSED -> Pair(TelemetryAmber, "FAIL CLOSED")
    }

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

            // Primary Output Regime & Confidence
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .background(regimeColor.copy(alpha = 0.15f), CircleShape)
                            .border(1.dp, regimeColor.copy(alpha = 0.7f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.ShowChart,
                            contentDescription = "Volatility Regime",
                            tint = regimeColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "REGIME STATE",
                            color = TextMuted,
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = regimeLabel,
                            color = regimeColor,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "CONFIDENCE",
                        color = TextMuted,
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = String.format(Locale.US, "%.1f%%", confidence * 100.0),
                        color = TextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Metrics Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                VolatilityMetricPill(
                    title = "ANNUALIZED VOL",
                    value = String.format(Locale.US, "%.2f%%", (evidence?.realizedVolatilityAnnualized ?: 0.0) * 100.0),
                    subtitle = "Realized Dispersion",
                    modifier = Modifier.weight(1f)
                )
                VolatilityMetricPill(
                    title = "PARKINSON",
                    value = String.format(Locale.US, "%.2f%%", (evidence?.parkinsonVolatility ?: 0.0) * 100.0),
                    subtitle = "Range Volatility",
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                VolatilityMetricPill(
                    title = "RETURNS STDDEV",
                    value = String.format(Locale.US, "%.4f", evidence?.returnsStdDev ?: 0.0),
                    subtitle = "Per-Tick Dispersion",
                    modifier = Modifier.weight(1f)
                )
                VolatilityMetricPill(
                    title = "SAMPLE DEPTH",
                    value = "${evidence?.sampleCount ?: 0} Ticks",
                    subtitle = "Window: ${temporalState.observationWindowSeconds}s",
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Audit justification / metadata footer
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(TelemetryObsidian, RoundedCornerShape(8.dp))
                    .border(0.5.dp, TelemetryCardBorder, RoundedCornerShape(8.dp))
                    .padding(10.dp)
            ) {
                Column {
                    Text(
                        text = "AUDITABLE EVIDENCE // SOURCE: ${evidence?.sourceIdentity ?: "PENDING"}",
                        color = TextMuted,
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = verdict?.justification ?: "Waiting for sufficient chronological observation window...",
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

@Composable
private fun VolatilityMetricPill(
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
                color = TelemetryCyan,
                fontSize = 9.sp,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}
