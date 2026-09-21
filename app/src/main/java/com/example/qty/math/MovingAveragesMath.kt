package com.example.qty.math

/**
 * Mathematical calculations for Exponential Moving Averages (EMA) and Simple Moving Averages (SMA).
 */
object MovingAveragesMath {

    data class EmaSpreadResult(
        val emaFast: Double,
        val emaSlow: Double,
        val spreadDollar: Double,
        val spreadPercent: Double,
        val isFastAboveSlow: Boolean
    )

    fun computeSma(prices: List<Double>, period: Int): Double? {
        if (prices.size < period || period <= 0) return null
        val sub = prices.takeLast(period)
        return sub.average()
    }

    /**
     * Computes EMA series for a sequence of prices.
     */
    fun computeEma(prices: List<Double>, period: Int): Double? {
        if (prices.isEmpty() || period <= 0) return null
        if (prices.size < period) {
            return prices.average()
        }
        val alpha = 2.0 / (period + 1.0)
        // Seed with the SMA of the initial period
        var currentEma = prices.subList(0, period).average()
        for (i in period until prices.size) {
            currentEma = (prices[i] * alpha) + (currentEma * (1.0 - alpha))
        }
        return currentEma
    }

    /**
     * Computes EMA spread between fast and slow periods.
     */
    fun computeEmaSpread(
        prices: List<Double>,
        fastPeriod: Int = 10,
        slowPeriod: Int = 30
    ): EmaSpreadResult? {
        if (prices.size < 5) return null
        val fast = computeEma(prices, fastPeriod) ?: return null
        val slow = computeEma(prices, slowPeriod) ?: return null

        val spreadDollar = fast - slow
        val spreadPercent = if (slow > 1e-9) (spreadDollar / slow) * 100.0 else 0.0

        return EmaSpreadResult(
            emaFast = fast,
            emaSlow = slow,
            spreadDollar = spreadDollar,
            spreadPercent = spreadPercent,
            isFastAboveSlow = fast > slow
        )
    }
}
