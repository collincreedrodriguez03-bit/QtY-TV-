package com.example.qty.ledger

import com.example.qty.pricedynamics.trend.TrendEngineOutput
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

/**
 * Repository interface for QtY Evaluation Ledger operations.
 */
class EvaluationLedgerRepository(private val dao: TrendLedgerDao) {

    val recentEntries: Flow<List<TrendLedgerEntity>> = dao.getRecentEntries(100)
    val totalCount: Flow<Int> = dao.getTotalCount()

    suspend fun recordSnapshot(output: TrendEngineOutput): Long = withContext(Dispatchers.IO) {
        val currentCount = dao.getCountSync()
        val tier = EvaluationTier.fromSampleCount(currentCount + 1)

        val entity = TrendLedgerEntity(
            timestampMs = output.evidence.timestampMs,
            sourceIdentity = output.evidence.sourceIdentity,
            prototypeVersion = TrendLedgerEntity.PROTOTYPE_VERSION,
            latestPrice = output.evidence.latestPrice,
            observationWindowSeconds = output.temporalState.observationWindowSeconds,
            targetHorizonSeconds = output.temporalState.targetHorizonSeconds,
            sampleCount = output.evidence.sampleCount,
            slopePerSecond = output.evidence.slopePerSecond,
            normalizedSlopeBps = output.evidence.normalizedSlopeBps,
            rSquared = output.evidence.rSquared,
            tStatistic = output.evidence.tStatistic,
            emaSpreadPercent = output.evidence.emaSpreadPercent,
            cumulativeDriftBps = output.evidence.cumulativeDriftBps,
            channelPosition = output.evidence.channelPosition,
            trendDirection = output.verdict.direction.name,
            judgeConfidence = output.verdict.judgeConfidence,
            isIntegrityValid = output.evidence.isIntegrityValid,
            evaluationTier = tier.name
        )

        dao.insertEntry(entity)
    }

    suspend fun clearLedger() = withContext(Dispatchers.IO) {
        dao.clearLedger()
    }
}
