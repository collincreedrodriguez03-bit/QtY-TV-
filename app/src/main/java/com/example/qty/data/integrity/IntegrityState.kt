package com.example.qty.data.integrity

/**
 * Data Integrity state of the QtY telemetry intake pipeline.
 */
sealed class IntegrityState {
    data object Nominal : IntegrityState()
    data class ChronologicalViolation(val message: String, val previousTimestampMs: Long, val currentTimestampMs: Long) : IntegrityState()
    data class ValueViolation(val message: String) : IntegrityState()
    data class StaleData(val ageMs: Long, val thresholdMs: Long) : IntegrityState()
    data class FailedClosed(val reason: String, val timestampMs: Long = System.currentTimeMillis()) : IntegrityState()

    val isPassing: Boolean
        get() = this is Nominal

    val isFailClosed: Boolean
        get() = this is FailedClosed || this is ChronologicalViolation || this is ValueViolation
}
