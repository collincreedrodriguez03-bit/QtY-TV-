package com.example.qty.pricedynamics.trend

/**
 * Formal Trend Evidence record in QtY.
 * Preserves all underlying measurements, source identity, timestamps,
 * and integrity state for independent audit and future validation.
 */
data class TrendEvidence(
    val timestampMs: Long,
    val observationWindowSeconds: Int,
    val sampleCount: Int,
    val sourceIdentity: String,
    val latestPrice: Double,
    val slopePerSecond: Double,
    val normalizedSlopeBps: Double,
    val rSquared: Double,
    val tStatistic: Double,
    val emaFast: Double?,
    val emaSlow: Double?,
    val emaSpreadPercent: Double?,
    val cumulativeDriftBps: Double?,
    val channelPosition: Double?,
    val isIntegrityValid: Boolean,
    val failClosedReason: String? = null
)
