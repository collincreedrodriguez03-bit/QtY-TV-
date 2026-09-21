package com.example.qty.pricedynamics.trend

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
    val judgeConfidence: Double, // Grounded in R^2, bounded [0.0, 1.0]
    val directionalAgreement: Boolean,
    val isFailClosed: Boolean,
    val justification: String
)

/**
 * Evaluates TrendEvidence according to established QtY principles.
 * Fails closed if evidence has integrity violations or insufficient samples (minimum 2 points required for OLS regression).
 *
 * NOTE ON ASSUMPTIONS:
 * Unverified magic thresholds (such as arbitrary minSampleThreshold = 5, minSlopeBpsThreshold = 0.01,
 * sample weight scaling against 30, and agreement multiplier 1.0 / 0.3) have been removed.
 * Judge confidence is strictly grounded in verifiable R² and directional agreement without unproven scaling parameters.
 */
class TrendJudge(
    // Experimental parameter isolated for validation: minimum required samples for regression
    private val minSampleThreshold: Int = 2
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

        // 2. Directional consensus across specialists (strict sign-based agreement without arbitrary bps thresholds)
        val slopePositive = evidence.normalizedSlopeBps > 0.0
        val slopeNegative = evidence.normalizedSlopeBps < 0.0

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
        // Grounded strictly in R² (linearity of trend) when directional agreement is confirmed.
        val confidence = if (direction == TrendDirection.NO_TREND || !directionalAgreement) {
            0.0
        } else {
            evidence.rSquared.coerceIn(0.0, 1.0)
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
