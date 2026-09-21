package com.example.qty.ui

import com.example.qty.data.intake.MarketTick
import com.example.qty.data.integrity.IntegrityState
import com.example.qty.ledger.EvaluationTier
import com.example.qty.ledger.TrendLedgerEntity
import com.example.qty.pricedynamics.trend.TrendEngineOutput
import com.example.qty.temporal.TemporalState

/**
 * Observable UI state for the QtY TV dashboard.
 */
data class QtyTvUiState(
    val latestTick: MarketTick? = null,
    val integrityState: IntegrityState = IntegrityState.Nominal,
    val temporalState: TemporalState = TemporalState(currentTimestampMs = System.currentTimeMillis()),
    val trendOutput: TrendEngineOutput? = null,
    val timeSeriesSize: Int = 0,
    val recentPriceSeries: List<Pair<Long, Double>> = emptyList(),
    val totalLedgerCount: Int = 0,
    val evaluationTier: EvaluationTier = EvaluationTier.INSUFFICIENT,
    val recentLedgerEntries: List<TrendLedgerEntity> = emptyList(),
    val isStreamActive: Boolean = false,
    val statusMessage: String = "Initializing authentic market telemetry...",
    val isInitialBootstrapping: Boolean = true
)
