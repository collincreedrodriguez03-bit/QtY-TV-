package com.example.qty.math

import kotlin.math.ln
import kotlin.math.sqrt

/**
 * Mathematical calculations for Volatility and Returns dispersion.
 */
object VolatilityMath {

    data class VolatilityResult(
        val returnsStdDev: Double,
        val realizedVolatilityAnnualized: Double,
        val parkinsonVolatility: Double,
        val atrPercent: Double,
        val sampleSize: Int
    )

    fun computeVolatility(prices: List<Double>): VolatilityResult? {
        val n = prices.size
        if (n < 2) return null

        val logReturns = mutableListOf<Double>()
        var sumReturns = 0.0
        for (i in 1 until n) {
            val pPrev = prices[i - 1]
            val pCurr = prices[i]
            if (pPrev > 0.0 && pCurr > 0.0) {
                val ret = ln(pCurr / pPrev)
                logReturns.add(ret)
                sumReturns += ret
            }
        }

        if (logReturns.isEmpty()) return null

        val meanReturn = sumReturns / logReturns.size
        var sumSquaredDev = 0.0
        for (r in logReturns) {
            val diff = r - meanReturn
            sumSquaredDev += diff * diff
        }

        val variance = sumSquaredDev / (logReturns.size.coerceAtLeast(1))
        val stdDev = sqrt(variance)

        // Annualized realized volatility assuming 1 sample per second (~31.5M seconds per year)
        val annualizedVol = stdDev * sqrt(31_536_000.0)

        // Parkinson volatility (range-based proxy using consecutive high/low estimation)
        var sumLogRatioSq = 0.0
        for (i in 1 until n) {
            val high = maxOf(prices[i], prices[i-1])
            val low = minOf(prices[i], prices[i-1])
            if (low > 0.0) {
                val ratio = ln(high / low)
                sumLogRatioSq += ratio * ratio
            }
        }
        val parkinson = sqrt((1.0 / (4.0 * n * ln(2.0))) * sumLogRatioSq) * sqrt(31_536_000.0)

        // Average True Range (ATR) percent approximation
        var sumRangePct = 0.0
        for (i in 1 until n) {
            val pPrev = prices[i-1]
            val diff = kotlin.math.abs(prices[i] - pPrev)
            if (pPrev > 0.0) {
                sumRangePct += (diff / pPrev) * 100.0
            }
        }
        val atrPct = if (n > 1) sumRangePct / (n - 1) else 0.0

        return VolatilityResult(
            returnsStdDev = stdDev,
            realizedVolatilityAnnualized = annualizedVol,
            parkinsonVolatility = parkinson,
            atrPercent = atrPct,
            sampleSize = n
        )
    }
}
