package com.example.qty.pricedynamics.volatility

/**
 * Formal Volatility Evidence record in QtY.
 * Preserves independent volatility metrics (returns standard deviation, realized volatility annualized, Parkinson range volatility, garman-klass)
 * and source provenance for independent observation alongside Trend.
 */
data class VolatilityEvidence(
    val timestampMs: Long,
    val observationWindowSeconds: Int,
    val sampleCount: Int,
    val sourceIdentity: String,
    val returnsStdDev: Double,
    val realizedVolatilityAnnualized: Double,
    val parkinsonVolatility: Double,
    val atrPercent: Double,
    val isIntegrityValid: Boolean,
    val failClosedReason: String? = null
)
