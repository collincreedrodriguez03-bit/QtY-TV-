package com.example.qty.pricedynamics.trend

import com.example.qty.data.intake.MarketTick
import com.example.qty.math.MovingAveragesMath
import com.example.qty.math.RegressionMath
import com.example.qty.math.ReturnsMath

/**
 * QtY Trend Specialists / Mathematical Equations:
 *
 * 1. LinearRegressionTrendSpecialist:
 *    Calculates OLS slope, R-squared, t-stat, and normalized slope in bps/sec.
 *
 * 2. MovingAverageTrendSpecialist:
 *    Calculates Fast (10) vs Slow (30) EMA divergence and spread.
 *
 * 3. LogReturnDriftSpecialist:
 *    Calculates cumulative log return drift across the window.
 *
 * 4. ChannelPositionSpecialist:
 *    Calculates current price percentile relative to window min/max bounds.
 */
class LinearRegressionTrendSpecialist {
    fun evaluate(ticks: List<MarketTick>): RegressionMath.OlsResult? {
        if (ticks.size < 2) return null
        val points = ticks.map { Pair(it.timestampMs, it.price) }
        return RegressionMath.computeOls(points)
    }
}

class MovingAverageTrendSpecialist(
    private val fastPeriod: Int = 10,
    private val slowPeriod: Int = 30
) {
    fun evaluate(ticks: List<MarketTick>): MovingAveragesMath.EmaSpreadResult? {
        if (ticks.size < 3) return null
        val prices = ticks.map { it.price }
        return MovingAveragesMath.computeEmaSpread(prices, fastPeriod, slowPeriod)
    }
}

class LogReturnDriftSpecialist {
    fun evaluate(ticks: List<MarketTick>): ReturnsMath.DriftResult? {
        if (ticks.size < 2) return null
        val prices = ticks.map { it.price }
        return ReturnsMath.computeDrift(prices)
    }
}

class ChannelPositionSpecialist {
    data class ChannelResult(
        val minPrice: Double,
        val maxPrice: Double,
        val channelRange: Double,
        val positionPercentile: Double // 0.0 (at min) to 1.0 (at max)
    )

    fun evaluate(ticks: List<MarketTick>): ChannelResult? {
        if (ticks.isEmpty()) return null
        val prices = ticks.map { it.price }
        val min = prices.minOrNull() ?: return null
        val max = prices.maxOrNull() ?: return null
        val last = prices.last()

        val range = max - min
        val percentile = if (range > 1e-9) {
            ((last - min) / range).coerceIn(0.0, 1.0)
        } else {
            0.5
        }

        return ChannelResult(
            minPrice = min,
            maxPrice = max,
            channelRange = range,
            positionPercentile = percentile
        )
    }
}
