package com.example.qty.data.integrity

import com.example.qty.data.intake.MarketTick

/**
 * Data Integrity Verifier for authentic market telemetry in QtY.
 * Enforces strict chronological order, no future leakage, provenance verification,
 * and fail-closed security.
 */
class DataIntegrityVerifier(
    private val maxAllowedClockSkewMs: Long = 15_000L,
    private val maxAllowedStalenessMs: Long = 60_000L
) {
    private var lastVerifiedTick: MarketTick? = null
    private var verifiedTickCount: Long = 0L

    @Synchronized
    fun verify(tick: MarketTick, currentLocalWallClockMs: Long = System.currentTimeMillis()): IntegrityState {
        // 1. Basic Value Checks
        if (tick.price <= 0.0 || tick.price.isNaN() || tick.price.isInfinite()) {
            return IntegrityState.ValueViolation("Illegal non-positive or NaN price: ${tick.price}")
        }
        val vol = tick.volume
        if (vol != null && (vol < 0.0 || vol.isNaN() || vol.isInfinite())) {
            return IntegrityState.ValueViolation("Illegal negative or NaN volume: $vol")
        }
        if (tick.sourceIdentity.isBlank()) {
            return IntegrityState.ValueViolation("Missing source identity")
        }

        // 2. Future Leakage / Excessive Clock Skew
        if (tick.timestampMs > currentLocalWallClockMs + maxAllowedClockSkewMs) {
            return IntegrityState.ChronologicalViolation(
                message = "Tick timestamp is in the future (skew: ${tick.timestampMs - currentLocalWallClockMs}ms)",
                previousTimestampMs = currentLocalWallClockMs,
                currentTimestampMs = tick.timestampMs
            )
        }

        // 3. Monotonic Chronological Ordering
        val previous = lastVerifiedTick
        if (previous != null) {
            if (tick.timestampMs < previous.timestampMs) {
                return IntegrityState.ChronologicalViolation(
                    message = "Time-travel violation: tick (${tick.timestampMs}) is older than previous verified (${previous.timestampMs})",
                    previousTimestampMs = previous.timestampMs,
                    currentTimestampMs = tick.timestampMs
                )
            }
        }

        // 4. Staleness verification against local wall clock
        val ageMs = currentLocalWallClockMs - tick.timestampMs
        if (ageMs > maxAllowedStalenessMs) {
            return IntegrityState.StaleData(ageMs = ageMs, thresholdMs = maxAllowedStalenessMs)
        }

        lastVerifiedTick = tick
        verifiedTickCount++
        return IntegrityState.Nominal
    }

    @Synchronized
    fun reset() {
        lastVerifiedTick = null
        verifiedTickCount = 0L
    }

    val totalVerified: Long
        get() = verifiedTickCount
}
