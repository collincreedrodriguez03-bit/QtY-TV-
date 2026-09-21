package com.example.qty.backtest

import com.example.qty.data.intake.MarketTick
import com.example.qty.pricedynamics.trend.TrendEngine
import com.example.qty.pricedynamics.trend.TrendEngineOutput
import com.example.qty.pricedynamics.volatility.VolatilityEngine
import com.example.qty.pricedynamics.volatility.VolatilityEngineOutput
import com.example.qty.temporal.TemporalState
import com.example.qty.temporal.TimeSeriesWindow
import com.example.qty.data.integrity.DataIntegrityVerifier
import com.example.qty.data.integrity.IntegrityState

/**
 * Outcome of a chronological backtest prediction against actual settlement.
 */
data class BacktestPredictionOutcome(
    val tickIndex: Int,
    val predictionTimestampMs: Long,
    val settlementTimestampMs: Long,
    val targetHorizonSeconds: Int,
    val sourceIdentity: String,
    val startingPrice: Double,
    val targetSettlementPrice: Double,
    val actualSettlementPrice: Double?,
    val predictedDirection: String, // UP, DOWN, NO_TREND
    val volatilityRegime: String,
    val trendConfidence: Double,
    val volatilityConfidence: Double,
    val isCorrect: Boolean?,
    val absoluteError: Double?,
    val outcomeState: OutcomeState,
    val prototypeVersion: String = "QtY-TV-v1.0.0-Phase3"
)

enum class OutcomeState {
    SUCCESS_EVALUATED,
    PENDING_SETTLEMENT, // Future horizon not yet reached in historical sequence
    FAIL_CLOSED
}

/**
 * Backtest Engine for Phase 3:
 * Evaluates chronological historical tick sequences with zero lookahead bias,
 * produces independent Trend and Volatility evidence, synthesizes prototype predictions against actual forward settlement,
 * and records all prediction outcomes.
 */
class ChronologicalBacktestEngine(
    private val observationWindowSeconds: Int = 60,
    private val targetHorizonSeconds: Int = 30
) {
    private val trendEngine = TrendEngine()
    private val volatilityEngine = VolatilityEngine()
    private val integrityVerifier = DataIntegrityVerifier()

    fun runBacktest(historicalTicks: List<MarketTick>): List<BacktestPredictionOutcome> {
        if (historicalTicks.size < 3) return emptyList()

        // Ensure chronological sorting by exchange event timestamp
        val sortedTicks = historicalTicks.sortedBy { it.timestampMs }
        val timeSeries = TimeSeriesWindow()
        val outcomes = mutableListOf<BacktestPredictionOutcome>()

        for (i in sortedTicks.indices) {
            val tick = sortedTicks[i]
            val integrityState = integrityVerifier.verify(tick, currentLocalWallClockMs = tick.timestampMs + 100L)
            timeSeries.addTick(tick)

            val temporalState = TemporalState(
                currentTimestampMs = tick.timestampMs,
                observationWindowSeconds = observationWindowSeconds,
                targetHorizonSeconds = targetHorizonSeconds
            )

            // Run independent engines
            val trendOutput = trendEngine.process(timeSeries, temporalState, integrityState)
            val volatilityOutput = volatilityEngine.process(timeSeries, temporalState, integrityState)

            // Look for settlement target at roughly currentTimestampMs + (targetHorizonSeconds * 1000L)
            val targetSettlementMs = tick.timestampMs + (targetHorizonSeconds * 1000L)
            val startingPrice = tick.price

            // Find actual future tick at or closest after targetSettlementMs without lookahead during evaluation
            val settlementTick = sortedTicks.drop(i + 1).firstOrNull { it.timestampMs >= targetSettlementMs }

            val outcomeState: OutcomeState
            val actualPrice: Double?
            val isCorrect: Boolean?
            val absError: Double?

            if (settlementTick != null) {
                outcomeState = OutcomeState.SUCCESS_EVALUATED
                actualPrice = settlementTick.price
                val priceChange = actualPrice - startingPrice
                val predictedUp = trendOutput.verdict.direction == com.example.qty.pricedynamics.trend.TrendDirection.UP
                val predictedDown = trendOutput.verdict.direction == com.example.qty.pricedynamics.trend.TrendDirection.DOWN

                isCorrect = when {
                    predictedUp && priceChange > 0.0 -> true
                    predictedDown && priceChange < 0.0 -> true
                    !predictedUp && !predictedDown && kotlin.math.abs(priceChange) <= startingPrice * 0.0005 -> true
                    else -> false
                }
                absError = kotlin.math.abs(actualPrice - (startingPrice + (trendOutput.evidence.slopePerSecond * targetHorizonSeconds)))
            } else {
                outcomeState = OutcomeState.PENDING_SETTLEMENT
                actualPrice = null
                isCorrect = null
                absError = null
            }

            val targetPriceEst = startingPrice + (trendOutput.evidence.slopePerSecond * targetHorizonSeconds)

            outcomes.add(
                BacktestPredictionOutcome(
                    tickIndex = i,
                    predictionTimestampMs = tick.timestampMs,
                    settlementTimestampMs = targetSettlementMs,
                    targetHorizonSeconds = targetHorizonSeconds,
                    sourceIdentity = tick.sourceIdentity,
                    startingPrice = startingPrice,
                    targetSettlementPrice = targetPriceEst,
                    actualSettlementPrice = actualPrice,
                    predictedDirection = trendOutput.verdict.direction.name,
                    volatilityRegime = volatilityOutput.verdict.regime.name,
                    trendConfidence = trendOutput.verdict.judgeConfidence,
                    volatilityConfidence = volatilityOutput.verdict.judgeConfidence,
                    isCorrect = isCorrect,
                    absoluteError = absError,
                    outcomeState = outcomeState
                )
            )
        }

        return outcomes
    }
}
