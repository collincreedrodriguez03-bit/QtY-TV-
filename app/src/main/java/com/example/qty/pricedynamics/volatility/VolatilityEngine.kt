package com.example.qty.pricedynamics.volatility

import com.example.qty.data.integrity.IntegrityState
import com.example.qty.temporal.TemporalState
import com.example.qty.temporal.TimeSeriesWindow

/**
 * Output produced by the Volatility Experimental Engine (Phase 2).
 * Independently observable for telemetry, audit, and tournament combination.
 */
data class VolatilityEngineOutput(
    val temporalState: TemporalState,
    val evidence: VolatilityEvidence,
    val verdict: VolatilityJudgeVerdict,
    val windowTicks: List<Pair<Long, Double>>
)

/**
 * Volatility Experimental Engine in QtY TV (Phase 2).
 *
 * Implements strict QtY principles:
 * - Independently observable Volatility measurements/evidence through time.
 * - Consumes authentic, timestamp-correct, chronologically ordered BTC data.
 * - No lookahead bias.
 * - Fail closed on invalid data.
 */
class VolatilityEngine(
    private val volatilitySpecialist: VolatilitySpecialist = VolatilitySpecialist(),
    private val volatilityJudge: VolatilityJudge = VolatilityJudge()
) {
    fun process(
        timeSeries: TimeSeriesWindow,
        temporalState: TemporalState,
        integrityState: IntegrityState
    ): VolatilityEngineOutput {
        val windowTicks = timeSeries.getObservationWindow(
            cutoffTimestampMs = temporalState.currentTimestampMs,
            windowDurationSeconds = temporalState.observationWindowSeconds
        )

        val latestTick = windowTicks.lastOrNull() ?: timeSeries.getLatestTick()

        val isIntegrityValid = integrityState.isPassing && windowTicks.size >= 2
        val failReason = when {
            integrityState.isFailClosed -> "Integrity check failed: $integrityState"
            windowTicks.isEmpty() -> "Observation window contains 0 authentic ticks"
            windowTicks.size < 2 -> "Window contains insufficient ticks (${windowTicks.size}) for volatility"
            else -> null
        }

        if (!isIntegrityValid || latestTick == null) {
            val emptyEvidence = VolatilityEvidence(
                timestampMs = temporalState.currentTimestampMs,
                observationWindowSeconds = temporalState.observationWindowSeconds,
                sampleCount = windowTicks.size,
                sourceIdentity = latestTick?.sourceIdentity ?: "UNKNOWN",
                returnsStdDev = 0.0,
                realizedVolatilityAnnualized = 0.0,
                parkinsonVolatility = 0.0,
                atrPercent = 0.0,
                isIntegrityValid = false,
                failClosedReason = failReason
            )
            val failVerdict = volatilityJudge.evaluate(emptyEvidence)
            return VolatilityEngineOutput(
                temporalState = temporalState,
                evidence = emptyEvidence,
                verdict = failVerdict,
                windowTicks = windowTicks.map { Pair(it.timestampMs, it.price) }
            )
        }

        val volResult = volatilitySpecialist.evaluate(windowTicks)

        val evidence = VolatilityEvidence(
            timestampMs = temporalState.currentTimestampMs,
            observationWindowSeconds = temporalState.observationWindowSeconds,
            sampleCount = windowTicks.size,
            sourceIdentity = latestTick.sourceIdentity,
            returnsStdDev = volResult?.returnsStdDev ?: 0.0,
            realizedVolatilityAnnualized = volResult?.realizedVolatilityAnnualized ?: 0.0,
            parkinsonVolatility = volResult?.parkinsonVolatility ?: 0.0,
            atrPercent = volResult?.atrPercent ?: 0.0,
            isIntegrityValid = true,
            failClosedReason = null
        )

        val verdict = volatilityJudge.evaluate(evidence)

        return VolatilityEngineOutput(
            temporalState = temporalState,
            evidence = evidence,
            verdict = verdict,
            windowTicks = windowTicks.map { Pair(it.timestampMs, it.price) }
        )
    }
}
