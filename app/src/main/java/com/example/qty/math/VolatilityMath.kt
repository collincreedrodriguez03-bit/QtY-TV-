package com.example.qty.math

import kotlin.math.ln
import kotlin.math.sqrt

/**
 * Mathematical calculations for Volatility and Returns dispersion.
 *
 * AUDIT & ASSUMPTIONS NOTE:
 * - Realized volatility annualization scaling factor (sqrt(31,536,000)) assumes 1-second interval observations as an
 *   experimental normalization baseline. Irregularly sampled exchange trades are mapped to this baseline for comparative scaling;
 *   this is an experimental parameterization rather than a closed-form statistical absolute.
 * - Parkinson Volatility and ATR Percent calculations operate on consecutive trade observations as defined experimental proxies
 *   rather than formal OHLC candlestick bars (which would require artificial bar aggregation). No OHLC values are fabricated.
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

        // Experimental normalized annualization baseline (1-second frequency assumption: 31,536,000 seconds/year)
        val annualizedVol = stdDev * sqrt(31_536_000.0)

        // Experimental range proxy (Parkinson volatility proxy on consecutive trade observation pairs without fabricating OHLC bars)
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

        // Experimental ATR percent proxy on consecutive trade price deltas
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
