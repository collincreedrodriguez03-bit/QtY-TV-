package com.example.qty.temporal

import com.example.qty.data.intake.MarketTick
import java.util.Collections
import kotlin.math.max

/**
 * Chronologically ordered authentic time-series buffer.
 *
 * Strict Rules:
 * - Chronological monotonicity is maintained.
 * - No lookahead leakage: all temporal slice queries take an explicit cut-off timestamp T,
 *   guaranteeing that no future information t > T is ever accessed.
 */
class TimeSeriesWindow(
    private val maxCapacity: Int = 2000
) {
    private val ticks = ArrayList<MarketTick>(maxCapacity)

    @Synchronized
    fun addTick(tick: MarketTick) {
        if (ticks.isNotEmpty()) {
            val last = ticks[ticks.size - 1]
            if (tick.timestampMs < last.timestampMs) {
                // Drop or reject out-of-order tick to preserve strict monotonicity
                return
            }
        }
        ticks.add(tick)
        if (ticks.size > maxCapacity) {
            ticks.removeAt(0)
        }
    }

    @Synchronized
    fun addAll(newTicks: List<MarketTick>) {
        for (t in newTicks) {
            addTick(t)
        }
    }

    /**
     * Slices the time-series for an observation window [cutoffTimestampMs - windowDurationMs, cutoffTimestampMs].
     * Guarantees:
     * - No lookahead: all returned ticks satisfy tick.timestampMs <= cutoffTimestampMs.
     * - Chronological order is preserved.
     */
    @Synchronized
    fun getObservationWindow(
        cutoffTimestampMs: Long,
        windowDurationSeconds: Int
    ): List<MarketTick> {
        val windowDurationMs = windowDurationSeconds * 1000L
        val minTimestampMs = max(0L, cutoffTimestampMs - windowDurationMs)

        val result = mutableListOf<MarketTick>()
        for (i in ticks.indices) {
            val t = ticks[i]
            if (t.timestampMs in minTimestampMs..cutoffTimestampMs) {
                result.add(t)
            } else if (t.timestampMs > cutoffTimestampMs) {
                // Because ticks are chronologically sorted, we can stop
                break
            }
        }
        return Collections.unmodifiableList(result)
    }

    @Synchronized
    fun getAllTicks(): List<MarketTick> = ArrayList(ticks)

    @Synchronized
    fun getLatestTick(): MarketTick? = ticks.lastOrNull()

    @Synchronized
    fun size(): Int = ticks.size

    @Synchronized
    fun clear() {
        ticks.clear()
    }
}
