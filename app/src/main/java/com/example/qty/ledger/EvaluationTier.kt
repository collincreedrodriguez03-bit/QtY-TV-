package com.example.qty.ledger

/**
 * Established QtY Evaluation Tiers:
 * - N < 100 = INSUFFICIENT
 * - 100–299 = PRELIMINARY
 * - 300+ = CALIBRATION_READY
 *
 * Accuracy must remain null below the established minimum sample threshold.
 */
enum class EvaluationTier(val displayName: String, val description: String) {
    INSUFFICIENT(
        displayName = "INSUFFICIENT (N < 100)",
        description = "Sample count is below statistical validity minimum. Accuracy metric is held strictly null."
    ),
    PRELIMINARY(
        displayName = "PRELIMINARY (100–299)",
        description = "Early directional indication. Subject to high sampling variance."
    ),
    CALIBRATION_READY(
        displayName = "CALIBRATION_READY (300+)",
        description = "Sufficient authentic sample depth for formal calibration and validation."
    );

    companion object {
        fun fromSampleCount(n: Int): EvaluationTier {
            return when {
                n < 100 -> INSUFFICIENT
                n < 300 -> PRELIMINARY
                else -> CALIBRATION_READY
            }
        }
    }
}
