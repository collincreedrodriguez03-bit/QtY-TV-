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
    val judgeConfidence: Double, // Experimental evidence quality indicator (sample depth ratio), NOT a formal probability of correctness
    val isFailClosed: Boolean,
    val justification: String
)

/**
 * Evaluates VolatilityEvidence according to established QtY principles.
 *
 * AUDIT & EXPERIMENTAL PARAMETERS:
 * - High volatility threshold (> 0.80 annualized) and low volatility threshold (< 0.20 annualized) are explicitly isolated
 *   as experimental classification parameters rather than established physical constants. They must be validated out-of-sample.
 * - Judge confidence is defined strictly as sample-depth evidence quality (sampleCount / 20.0 capped at 1.0) to represent
 *   observation density, rather than an arbitrary probability of prediction correctness.
 * - Fails closed if data integrity is invalid or sample count is below the minimum threshold.
 */
class VolatilityJudge(
    private val minSampleThreshold: Int = 2,
    private val highVolatilityThreshold: Double = 0.80,
    private val lowVolatilityThreshold: Double = 0.20
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

        // Experimental Regime Classification based on annualized realized volatility
        val vol = evidence.realizedVolatilityAnnualized
        val regime = when {
            vol > highVolatilityThreshold -> VolatilityRegime.HIGH_VOLATILITY
            vol < lowVolatilityThreshold -> VolatilityRegime.LOW_VOLATILITY
            else -> VolatilityRegime.NORMAL_VOLATILITY
        }

        // Evidence Quality Index (Sample depth ratio relative to 20-sample reference baseline)
        val evidenceQualityConfidence = kotlin.math.min(1.0, evidence.sampleCount.toDouble() / 20.0).coerceIn(0.0, 1.0)

        val justification = buildString {
            append("Regime: $regime. ")
            append("Realized Vol (Annualized)=${"%.2f".format(vol * 100.0)}%, ")
            append("Returns StdDev=${"%.4f".format(evidence.returnsStdDev)}, ")
            append("Sample Depth N=${evidence.sampleCount} (Quality Index=${"%.2f".format(evidenceQualityConfidence)}).")
        }

        return VolatilityJudgeVerdict(
            regime = regime,
            judgeConfidence = evidenceQualityConfidence,
            isFailClosed = false,
            justification = justification
        )
    }
}
