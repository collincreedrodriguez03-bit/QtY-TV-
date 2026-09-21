package com.example.qty.pricedynamics.volatility

import com.example.qty.data.intake.MarketTick
import com.example.qty.math.VolatilityMath

/**
 * Volatility Specialist: Computes realized dispersion and range volatility without arbitrary thresholds.
 */
class VolatilitySpecialist {
    fun evaluate(ticks: List<MarketTick>): VolatilityMath.VolatilityResult? {
        if (ticks.size < 2) return null
        val prices = ticks.map { it.price }
        return VolatilityMath.computeVolatility(prices)
    }
}
