package com.example.qty.ledger

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Immutable auditable record stored in the QtY Evaluation Ledger.
 * Provides complete provenance, temporal state, measurements, and judge verdicts
 * for backtesting and validation across phases.
 */
@Entity(tableName = "trend_evaluation_ledger")
data class TrendLedgerEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val timestampMs: Long,
    val sourceIdentity: String,
    val prototypeVersion: String = PROTOTYPE_VERSION,
    val latestPrice: Double,
    val observationWindowSeconds: Int,
    val targetHorizonSeconds: Int,
    val sampleCount: Int,
    val slopePerSecond: Double,
    val normalizedSlopeBps: Double,
    val rSquared: Double,
    val tStatistic: Double,
    val emaSpreadPercent: Double?,
    val cumulativeDriftBps: Double?,
    val channelPosition: Double?,
    val trendDirection: String,
    val judgeConfidence: Double,
    val isIntegrityValid: Boolean,
    val evaluationTier: String
) {
    companion object {
        const val PROTOTYPE_VERSION = "QtY-TV-v1.0.0-Phase1"
    }
}
