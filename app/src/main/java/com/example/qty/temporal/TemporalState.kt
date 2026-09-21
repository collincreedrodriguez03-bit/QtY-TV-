package com.example.qty.temporal

/**
 * Temporal State in QtY.
 *
 * Separation of Concerns:
 * - Observation Window (W): Historical lookback interval over which equations/measurements operate (e.g. 30s, 60s, 120s, 300s).
 * - Prediction Horizon (H): Target forward interval for validation/evaluation (e.g. 5s, 10s, 30s, 60s, 120s, 300s, 600s, 900s).
 *
 * Observation window and prediction horizon must remain conceptually separate.
 */
data class TemporalState(
    val currentTimestampMs: Long,
    val observationWindowSeconds: Int = DEFAULT_OBSERVATION_WINDOW_SECONDS,
    val targetHorizonSeconds: Int = DEFAULT_TARGET_HORIZON_SECONDS
) {
    companion object {
        val EXPERIMENTAL_HORIZONS_SECONDS = listOf(5, 10, 30, 60, 120, 300, 600, 900)
        val OBSERVATION_WINDOWS_SECONDS = listOf(15, 30, 60, 120, 300)

        const val DEFAULT_OBSERVATION_WINDOW_SECONDS = 60
        const val DEFAULT_TARGET_HORIZON_SECONDS = 30
    }

    val observationWindowMs: Long
        get() = observationWindowSeconds * 1000L

    val targetHorizonMs: Long
        get() = targetHorizonSeconds * 1000L

    val windowStartTimeMs: Long
        get() = currentTimestampMs - observationWindowMs
}
