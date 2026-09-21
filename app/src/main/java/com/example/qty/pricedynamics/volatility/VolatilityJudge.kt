package com.example.qty.pricedynamics.volatility

/**
 * Regime evaluated by the QtY Volatility Judge.
 */
enum class VolatilityRegime {
    LOW_VOLATILITY,
    NORMAL_VOLATILITY,
    HIGH_VOLATILITY,
    FAIL_CLOSED
}

/**
 * Result of the Volatility Judge evaluation in QtY.
 */
data class VolatilityJudgeVerdict(
    val regime: VolatilityRegime,
    val judgeConfidence: Double, // Grounded in sample depth and variance stability
    val isFailClosed: Boolean,
    val justification: String
)

/**
 * Evaluates VolatilityEvidence according to established QtY principles.
 * Fails closed if evidence has integrity violations or insufficient samples.
 */
class VolatilityJudge(
    private val minSampleThreshold: Int = 2
) {
    fun evaluate(evidence: VolatilityEvidence): VolatilityJudgeVerdict {
        if (!evidence.isIntegrityValid) {
            return VolatilityJudgeVerdict(
                regime = VolatilityRegime.FAIL_CLOSED,
                judgeConfidence = 0.0,
                isFailClosed = true,
                justification = "Data integrity invalid: ${evidence.failClosedReason ?: "Integrity check failed"}"
            )
        }

        if (evidence.sampleCount < minSampleThreshold) {
            return VolatilityJudgeVerdict(
                regime = VolatilityRegime.FAIL_CLOSED,
                judgeConfidence = 0.0,
                isFailClosed = true,
                justification = "Insufficient samples (${evidence.sampleCount} < $minSampleThreshold required for volatility evaluation)"
            )
        }

        // Determine regime based on realized volatility annualized relative to baseline (e.g. 50% threshold for high vol)
        val vol = evidence.realizedVolatilityAnnualized
        val regime = when {
            vol > 0.80 -> VolatilityRegime.HIGH_VOLATILITY
            vol < 0.20 -> VolatilityRegime.LOW_VOLATILITY
            else -> VolatilityRegime.NORMAL_VOLATILITY
        }

        val confidence = kotlin.math.min(1.0, evidence.sampleCount.toDouble() / 20.0).coerceIn(0.0, 1.0)

        val justification = buildString {
            append("Regime: $regime. ")
            append("Realized Vol (Annualized)=${"%.2f".format(vol * 100.0)}%, ")
            append("Returns StdDev=${"%.4f".format(evidence.returnsStdDev)}, ")
            append("N=${evidence.sampleCount}.")
        }

        return VolatilityJudgeVerdict(
            regime = regime,
            judgeConfidence = confidence,
            isFailClosed = false,
            justification = justification
        )
    }
}
