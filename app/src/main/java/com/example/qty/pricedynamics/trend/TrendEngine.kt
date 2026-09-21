package com.example.qty.pricedynamics.trend

import com.example.qty.data.integrity.IntegrityState
import com.example.qty.temporal.TemporalState
import com.example.qty.temporal.TimeSeriesWindow

/**
 * Output produced by the Trend Experimental Engine.
 * Independently observable for telemetry, audit, and tournament combination.
 */
data class TrendEngineOutput(
    val temporalState: TemporalState,
    val evidence: TrendEvidence,
    val verdict: TrendJudgeVerdict,
    val windowTicks: List<Pair<Long, Double>>
)

/**
 * Trend Experimental Engine in QtY TV (Phase 1).
 *
 * Implements strict QtY principles:
 * - Independently observable Trend measurements/evidence through time.
 * - Consumes authentic, timestamp-correct, chronologically ordered BTC data.
 * - No lookahead bias.
 * - Fail closed on invalid data.
 */
class TrendEngine(
    private val regressionSpecialist: LinearRegressionTrendSpecialist = LinearRegressionTrendSpecialist(),
    private val maSpecialist: MovingAverageTrendSpecialist = MovingAverageTrendSpecialist(),
    private val driftSpecialist: LogReturnDriftSpecialist = LogReturnDriftSpecialist(),
    private val channelSpecialist: ChannelPositionSpecialist = ChannelPositionSpecialist(),
    private val trendJudge: TrendJudge = TrendJudge()
) {
    fun process(
        timeSeries: TimeSeriesWindow,
        temporalState: TemporalState,
        integrityState: IntegrityState
    ): TrendEngineOutput {
        val windowTicks = timeSeries.getObservationWindow(
            cutoffTimestampMs = temporalState.currentTimestampMs,
            windowDurationSeconds = temporalState.observationWindowSeconds
        )

        val latestTick = windowTicks.lastOrNull() ?: timeSeries.getLatestTick()

        // Check fail-closed condition
        val isIntegrityValid = integrityState.isPassing && windowTicks.size >= 2
        val failReason = when {
            integrityState.isFailClosed -> "Integrity check failed: $integrityState"
            windowTicks.isEmpty() -> "Observation window contains 0 authentic ticks"
            windowTicks.size < 2 -> "Window contains insufficient ticks (${windowTicks.size}) for regression"
            else -> null
        }

        if (!isIntegrityValid || latestTick == null) {
            val emptyEvidence = TrendEvidence(
                timestampMs = temporalState.currentTimestampMs,
                observationWindowSeconds = temporalState.observationWindowSeconds,
                sampleCount = windowTicks.size,
                sourceIdentity = latestTick?.sourceIdentity ?: "UNKNOWN",
                latestPrice = latestTick?.price ?: 0.0,
                slopePerSecond = 0.0,
                normalizedSlopeBps = 0.0,
                rSquared = 0.0,
                tStatistic = 0.0,
                emaFast = null,
                emaSlow = null,
                emaSpreadPercent = null,
                cumulativeDriftBps = null,
                channelPosition = null,
                isIntegrityValid = false,
                failClosedReason = failReason
            )
            val failVerdict = trendJudge.evaluate(emptyEvidence)
            return TrendEngineOutput(
                temporalState = temporalState,
                evidence = emptyEvidence,
                verdict = failVerdict,
                windowTicks = windowTicks.map { Pair(it.timestampMs, it.price) }
            )
        }

        // Run specialists
        val ols = regressionSpecialist.evaluate(windowTicks)
        val ema = maSpecialist.evaluate(windowTicks)
        val drift = driftSpecialist.evaluate(windowTicks)
        val channel = channelSpecialist.evaluate(windowTicks)

        val evidence = TrendEvidence(
            timestampMs = temporalState.currentTimestampMs,
            observationWindowSeconds = temporalState.observationWindowSeconds,
            sampleCount = windowTicks.size,
            sourceIdentity = latestTick.sourceIdentity,
            latestPrice = latestTick.price,
            slopePerSecond = ols?.slopePerSecond ?: 0.0,
            normalizedSlopeBps = ols?.normalizedSlopeBps ?: 0.0,
            rSquared = ols?.rSquared ?: 0.0,
            tStatistic = ols?.tStatistic ?: 0.0,
            emaFast = ema?.emaFast,
            emaSlow = ema?.emaSlow,
            emaSpreadPercent = ema?.spreadPercent,
            cumulativeDriftBps = drift?.totalBps,
            channelPosition = channel?.positionPercentile,
            isIntegrityValid = true,
            failClosedReason = null
        )

        val verdict = trendJudge.evaluate(evidence)

        return TrendEngineOutput(
            temporalState = temporalState,
            evidence = evidence,
            verdict = verdict,
            windowTicks = windowTicks.map { Pair(it.timestampMs, it.price) }
        )
    }
}
