package com.example.qty.math

import kotlin.math.ln

/**
 * Mathematical calculations for price returns and log drift.
 */
object ReturnsMath {

    data class DriftResult(
        val totalLogReturn: Double,
        val totalBps: Double,
        val meanLogReturn: Double,
        val returnCount: Int
    )

    fun computeLogReturns(prices: List<Double>): List<Double> {
        if (prices.size < 2) return emptyList()
        val returns = ArrayList<Double>(prices.size - 1)
        for (i in 1 until prices.size) {
            val prev = prices[i - 1]
            val curr = prices[i]
            if (prev > 0.0 && curr > 0.0) {
                returns.add(ln(curr / prev))
            } else {
                returns.add(0.0)
            }
        }
        return returns
    }

    fun computeDrift(prices: List<Double>): DriftResult? {
        if (prices.size < 2) return null
        val first = prices.first()
        val last = prices.last()
        if (first <= 0.0 || last <= 0.0) return null

        val totalLog = ln(last / first)
        val totalBps = totalLog * 10_000.0
        val logReturns = computeLogReturns(prices)
        val meanLog = if (logReturns.isNotEmpty()) logReturns.average() else 0.0

        return DriftResult(
            totalLogReturn = totalLog,
            totalBps = totalBps,
            meanLogReturn = meanLog,
            returnCount = logReturns.size
        )
    }
}
