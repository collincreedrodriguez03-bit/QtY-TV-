package com.example.qty.pricedynamics.trend

import kotlin.math.abs
import kotlin.math.min

/**
 * Direction evaluated by the QtY Trend Judge.
 */
enum class TrendDirection {
    UP,
    DOWN,
    NO_TREND,
    FAIL_CLOSED
}

/**
 * Result of the Trend Judge evaluation in QtY.
 */
data class TrendJudgeVerdict(
    val direction: TrendDirection,
    val judgeConfidence: Double, // Grounded in R^2 and agreement, bounded [0.0, 1.0]
    val directionalAgreement: Boolean,
    val isFailClosed: Boolean,
    val justification: String
)

/**
 * Evaluates TrendEvidence according to established QtY principles.
 * Fails closed if evidence has integrity violations or insufficient samples.
 */
class TrendJudge(
    private val minSampleThreshold: Int = 5,
    private val minSlopeBpsThreshold: Double = 0.01 // Minimal slope threshold to avoid zero division noise
) {
    fun evaluate(evidence: TrendEvidence): TrendJudgeVerdict {
        // 1. Fail Closed enforcement
        if (!evidence.isIntegrityValid) {
            return TrendJudgeVerdict(
                direction = TrendDirection.FAIL_CLOSED,
                judgeConfidence = 0.0,
                directionalAgreement = false,
                isFailClosed = true,
                justification = "Data integrity invalid: ${evidence.failClosedReason ?: "Integrity check failed"}"
            )
        }

        if (evidence.sampleCount < minSampleThreshold) {
            return TrendJudgeVerdict(
                direction = TrendDirection.FAIL_CLOSED,
                judgeConfidence = 0.0,
                directionalAgreement = false,
                isFailClosed = true,
                justification = "Insufficient samples (${evidence.sampleCount} < $minSampleThreshold required for trend evaluation)"
            )
        }

        // 2. Directional consensus across specialists
        val slopePositive = evidence.normalizedSlopeBps > minSlopeBpsThreshold
        val slopeNegative = evidence.normalizedSlopeBps < -minSlopeBpsThreshold

        val emaPositive = (evidence.emaSpreadPercent ?: 0.0) > 0.0
        val emaNegative = (evidence.emaSpreadPercent ?: 0.0) < 0.0

        val driftPositive = (evidence.cumulativeDriftBps ?: 0.0) > 0.0
        val driftNegative = (evidence.cumulativeDriftBps ?: 0.0) < 0.0

        val upAgreement = slopePositive && (emaPositive || driftPositive)
        val downAgreement = slopeNegative && (emaNegative || driftNegative)

        val direction = when {
            upAgreement -> TrendDirection.UP
            downAgreement -> TrendDirection.DOWN
            else -> TrendDirection.NO_TREND
        }

        val directionalAgreement = upAgreement || downAgreement

        // 3. Mathematical Judge Confidence
        // Grounded in R-squared (linearity of trend) scaled by sample sufficiency (up to 30 samples)
        // and penalizing conflicting specialist signals.
        val sampleWeight = min(1.0, evidence.sampleCount.toDouble() / 30.0)
        val agreementMultiplier = if (directionalAgreement) 1.0 else 0.3

        val confidence = if (direction == TrendDirection.NO_TREND) {
            0.0
        } else {
            (evidence.rSquared * sampleWeight * agreementMultiplier).coerceIn(0.0, 1.0)
        }

        val justification = buildString {
            append("Direction: $direction. ")
            append("R²=${"%.3f".format(evidence.rSquared)}, ")
            append("Slope=${"%.3f".format(evidence.slopePerSecond)}$/s (${"%.2f".format(evidence.normalizedSlopeBps)} bps/s), ")
            if (evidence.emaSpreadPercent != null) {
                append("EMA spread=${"%.3f".format(evidence.emaSpreadPercent)}%, ")
            }
            append("N=${evidence.sampleCount}.")
        }

        return TrendJudgeVerdict(
            direction = direction,
            judgeConfidence = confidence,
            directionalAgreement = directionalAgreement,
            isFailClosed = false,
            justification = justification
        )
    }
}
