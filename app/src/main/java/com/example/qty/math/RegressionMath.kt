package com.example.qty.math

import kotlin.math.max
import kotlin.math.sqrt

/**
 * Mathematical calculations for Ordinary Least Squares (OLS) Regression.
 *
 * Implements exact closed-form equations:
 * - Slope beta = Cov(t, p) / Var(t)
 * - Intercept alpha = mean(p) - beta * mean(t)
 * - R-squared = Cov(t, p)^2 / (Var(t) * Var(p))
 * - Standard Error of Slope = s_e / sqrt(S_tt)
 * - t-statistic = beta / SE(beta)
 */
object RegressionMath {

    data class OlsResult(
        val slopePerSecond: Double,
        val intercept: Double,
        val rSquared: Double,
        val standardError: Double,
        val tStatistic: Double,
        val meanPrice: Double,
        val normalizedSlopeBps: Double, // Basis points per second
        val sampleSize: Int
    )

    /**
     * Compute OLS regression over chronologically ordered points (timestampMs, price).
     * Time is normalized in seconds relative to the first point to prevent floating point numerical instability.
     */
    fun computeOls(points: List<Pair<Long, Double>>): OlsResult? {
        val n = points.size
        if (n < 2) return null

        val t0 = points.first().first

        var sumT = 0.0
        var sumP = 0.0

        // Pass 1: compute means
        for (i in 0 until n) {
            val tSec = (points[i].first - t0) / 1000.0
            val p = points[i].second
            sumT += tSec
            sumP += p
        }

        val meanT = sumT / n
        val meanP = sumP / n

        // Pass 2: compute variances and covariance
        var sTt = 0.0
        var sPp = 0.0
        var sTp = 0.0

        for (i in 0 until n) {
            val tSec = (points[i].first - t0) / 1000.0
            val p = points[i].second
            val dt = tSec - meanT
            val dp = p - meanP
            sTt += dt * dt
            sPp += dp * dp
            sTp += dt * dp
        }

        // If time variance is 0 (all points identical timestamp) or price variance 0
        if (sTt <= 1e-12) {
            return OlsResult(
                slopePerSecond = 0.0,
                intercept = meanP,
                rSquared = 0.0,
                standardError = 0.0,
                tStatistic = 0.0,
                meanPrice = meanP,
                normalizedSlopeBps = 0.0,
                sampleSize = n
            )
        }

        val slope = sTp / sTt
        val intercept = meanP - slope * meanT

        val rSquared = if (sPp > 1e-12) {
            val r2 = (sTp * sTp) / (sTt * sPp)
            r2.coerceIn(0.0, 1.0)
        } else {
            0.0
        }

        // Residual variance for standard error
        val se = if (n > 2 && sTt > 1e-12) {
            var sumResidualsSq = 0.0
            for (i in 0 until n) {
                val tSec = (points[i].first - t0) / 1000.0
                val p = points[i].second
                val predicted = intercept + slope * tSec
                val res = p - predicted
                sumResidualsSq += res * res
            }
            val residualVariance = max(0.0, sumResidualsSq / (n - 2))
            val seSlope = sqrt(residualVariance / sTt)
            seSlope
        } else {
            0.0
        }

        val tStat = if (se > 1e-9) slope / se else 0.0
        val normalizedSlopeBps = if (meanP > 1e-9) (slope / meanP) * 10_000.0 else 0.0

        return OlsResult(
            slopePerSecond = slope,
            intercept = intercept,
            rSquared = rSquared,
            standardError = se,
            tStatistic = tStat,
            meanPrice = meanP,
            normalizedSlopeBps = normalizedSlopeBps,
            sampleSize = n
        )
    }
}
