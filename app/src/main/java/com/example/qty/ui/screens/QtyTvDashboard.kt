package com.example.qty.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
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
import com.example.qty.data.integrity.IntegrityState
import com.example.qty.pricedynamics.trend.TrendDirection
import com.example.qty.ui.QtyTvUiState
import com.example.qty.ui.components.CombinedPrototypeEvidenceCard
import com.example.qty.ui.components.EvaluationLedgerCard
import com.example.qty.ui.components.InfiniteMarketView
import com.example.qty.ui.components.TelemetryHeaderCard
import com.example.qty.ui.components.TemporalControlsCard
import com.example.qty.ui.components.TrendEngineCard
import com.example.qty.ui.components.VolatilityEngineCard
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

@Composable
fun QtyTvDashboard(
    uiState: QtyTvUiState,
    onToggleStream: () -> Unit,
    onBootstrapRetry: () -> Unit,
    onObservationWindowSelected: (Int) -> Unit,
    onTargetHorizonSelected: (Int) -> Unit,
    onCommitSnapshot: () -> Unit,
    onClearLedger: () -> Unit,
    modifier: Modifier = Modifier,
    enableAnimation: Boolean = true
) {
    Scaffold(
        modifier = modifier.fillMaxSize().testTag("qty_tv_dashboard"),
        containerColor = TelemetryObsidian,
        contentWindowInsets = WindowInsets.safeDrawing
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 1. App Bar / Header: Strict QtY TV Brand (Trend + Volatility)
            item {
                DashboardHeader(
                    isStreamActive = uiState.isStreamActive,
                    isBootstrapping = uiState.isInitialBootstrapping,
                    onToggleStream = onToggleStream,
                    onRefresh = onBootstrapRetry
                )
            }

            // 2. Fail Closed or Bootstrapping Banner if active
            if (uiState.integrityState.isFailClosed) {
                item {
                    FailClosedBanner(
                        state = uiState.integrityState,
                        onRetry = onBootstrapRetry
                    )
                }
            } else if (uiState.isInitialBootstrapping) {
                item {
                    BootstrappingBanner(message = uiState.statusMessage)
                }
            }

            // 3. Telemetry Header Card (BTC State: Authentic price, source, exchange clock, latency)
            item {
                TelemetryHeaderCard(
                    tick = uiState.latestTick,
                    integrityState = uiState.integrityState
                )
            }

            // 4. Infinite Market View / Orbital Mathematical Visualization (Metaphor for continuous state space)
            item {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "CONTINUOUS STATE SPACE // ORBITAL VIEW",
                            color = TextSecondary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "HORIZON: ${uiState.temporalState.observationWindowSeconds}s (N=${uiState.recentPriceSeries.size})",
                            color = TelemetryCyan,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    InfiniteMarketView(
                        points = uiState.recentPriceSeries,
                        slopePerSecond = uiState.trendOutput?.evidence?.slopePerSecond ?: 0.0,
                        rSquared = uiState.trendOutput?.evidence?.rSquared ?: 0.0,
                        direction = uiState.trendOutput?.verdict?.direction ?: TrendDirection.FAIL_CLOSED,
                        enableAnimation = enableAnimation
                    )
                }
            }

            // 5. ENGINE 1: TREND ENGINE CARD (Independent Trend Evidence)
            item {
                TrendEngineCard(
                    output = uiState.trendOutput,
                    temporalState = uiState.temporalState
                )
            }

            // 6. ENGINE 2: VOLATILITY ENGINE CARD (Independent Volatility Evidence - Phase 1 unengaged)
            item {
                VolatilityEngineCard(
                    temporalState = uiState.temporalState
                )
            }

            // 7. Flow Pipeline Card: BTC State → Trend → Volatility → Combined Prototype Evidence → Prediction/Outcome
            item {
                CombinedPrototypeEvidenceCard(
                    output = uiState.trendOutput,
                    temporalState = uiState.temporalState
                )
            }

            // 8. Temporal Horizon Controls (W Lookback and H Horizon)
            item {
                TemporalControlsCard(
                    temporalState = uiState.temporalState,
                    onObservationWindowSelected = onObservationWindowSelected,
                    onTargetHorizonSelected = onTargetHorizonSelected
                )
            }

            // 9. Evaluation Ledger Card (Sample depth, Tier badge, Audit stream)
            item {
                EvaluationLedgerCard(
                    totalCount = uiState.totalLedgerCount,
                    tier = uiState.evaluationTier,
                    recentEntries = uiState.recentLedgerEntries,
                    onCommitSnapshot = onCommitSnapshot,
                    onClearLedger = onClearLedger
                )
            }

            // 10. Minimalist QtY TV Prototype Separation Footer
            item {
                QtYArchitectureFooter()
            }
        }
    }
}

@Composable
private fun DashboardHeader(
    isStreamActive: Boolean,
    isBootstrapping: Boolean,
    onToggleStream: () -> Unit,
    onRefresh: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("dashboard_header"),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "QtY TV",
                    color = TextPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .background(TelemetryCyan.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                        .border(1.dp, TelemetryCyan.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "TREND + VOLATILITY",
                        color = TelemetryCyan,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
            Text(
                text = "Two engines exploring an unbounded quantitative market space",
                color = TextMuted,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            // Play/Pause Stream Button
            IconButton(
                onClick = onToggleStream,
                modifier = Modifier
                    .size(36.dp)
                    .background(TelemetrySurfaceVariant, CircleShape)
                    .border(1.dp, TelemetryCardBorder, CircleShape)
                    .testTag("toggle_stream_button")
            ) {
                Icon(
                    imageVector = if (isStreamActive) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                    contentDescription = if (isStreamActive) "Pause Stream" else "Resume Stream",
                    tint = if (isStreamActive) TelemetryGreen else TextMuted,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Refresh / Reconnect Button
            IconButton(
                onClick = onRefresh,
                modifier = Modifier
                    .size(36.dp)
                    .background(TelemetrySurfaceVariant, CircleShape)
                    .border(1.dp, TelemetryCardBorder, CircleShape)
                    .testTag("refresh_button")
            ) {
                if (isBootstrapping) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = TelemetryCyan,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        imageVector = Icons.Filled.Refresh,
                        contentDescription = "Reconnect Authentic Source",
                        tint = TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun FailClosedBanner(
    state: IntegrityState,
    onRetry: () -> Unit
) {
    val message = when (state) {
        is IntegrityState.FailedClosed -> state.reason
        is IntegrityState.ChronologicalViolation -> state.message
        is IntegrityState.ValueViolation -> state.message
        is IntegrityState.StaleData -> "Stale data: age ${state.ageMs}ms > threshold ${state.thresholdMs}ms"
        else -> "Data integrity failure"
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(TelemetryRed.copy(alpha = 0.15f), RoundedCornerShape(10.dp))
            .border(1.dp, TelemetryRed, RoundedCornerShape(10.dp))
            .padding(12.dp)
            .testTag("fail_closed_banner")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.Warning,
                    contentDescription = "Fail Closed Alert",
                    tint = TelemetryRed,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "FAIL CLOSED TRIGGERED",
                        color = TelemetryRed,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = message,
                        color = TextPrimary,
                        fontSize = 10.sp
                    )
                }
            }

            Box(
                modifier = Modifier
                    .background(TelemetryRed, RoundedCornerShape(6.dp))
                    .clickable(onClick = onRetry)
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "RECONNECT",
                    color = Color.White,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}

@Composable
private fun BootstrappingBanner(message: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(TelemetryCyan.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
            .border(1.dp, TelemetryCyan.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
            .padding(10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            CircularProgressIndicator(
                modifier = Modifier.size(16.dp),
                color = TelemetryCyan,
                strokeWidth = 2.dp
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = message,
                color = TelemetryCyan,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

@Composable
private fun QtYArchitectureFooter() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(TelemetrySurface, RoundedCornerShape(8.dp))
            .border(1.dp, TelemetryCardBorder, RoundedCornerShape(8.dp))
            .padding(12.dp)
    ) {
        Column {
            Text(
                text = "QtY TV // PROTOTYPE SEPARATION",
                color = TextMuted,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "QtY TV isolates TWO engines only: Trend + Volatility exploring an unbounded quantitative market space. The deeper Engine Room (transformations, full algebraic pipeline, formal validation funnel) remains architecturally housed in the primary QtY system.",
                color = TextSecondary,
                fontSize = 9.sp,
                fontFamily = FontFamily.Monospace,
                lineHeight = 14.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "BTC State → Trend [Active Phase 1] → Volatility [Pending Phase 2] → Combined Evidence → Outcome",
                color = TelemetryCyanDim,
                fontSize = 8.sp,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}
