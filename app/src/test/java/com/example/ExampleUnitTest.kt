package com.example

import com.example.qty.data.intake.MarketTick
import com.example.qty.data.integrity.DataIntegrityVerifier
import com.example.qty.data.integrity.IntegrityState
import com.example.qty.ledger.EvaluationTier
import com.example.qty.math.MovingAveragesMath
import com.example.qty.math.RegressionMath
import com.example.qty.math.ReturnsMath
import com.example.qty.pricedynamics.trend.TrendDirection
import com.example.qty.pricedynamics.trend.TrendEngine
import com.example.qty.pricedynamics.trend.TrendJudge
import com.example.qty.temporal.TemporalState
import com.example.qty.temporal.TimeSeriesWindow
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {

  @Test
  fun testOlsRegressionLinearAscending() {
    // 5 points spaced by 10s with price climbing 20 $/sec
    val points = listOf(
        Pair(0L, 100.0),
        Pair(10_000L, 300.0),
        Pair(20_000L, 500.0),
        Pair(30_000L, 700.0),
        Pair(40_000L, 900.0)
    )
    val result = RegressionMath.computeOls(points)
    assertNotNull(result)
    assertEquals(20.0, result!!.slopePerSecond, 0.001)
    assertEquals(1.0, result.rSquared, 0.001) // Perfect linear fit
  }

  @Test
  fun testDataIntegrityChronologicalMonotonicity() {
    val verifier = DataIntegrityVerifier()
    val t1 = MarketTick(exchangeTimestampMs = 1000L, serverSyncTimestampMs = null, price = 50000.0, volume = 1.0, sourceIdentity = "BINANCE_SPOT_BTCUSDT", sequenceId = 1L)
    val state1 = verifier.verify(t1, currentLocalWallClockMs = 2000L)
    assertTrue(state1.isPassing)

    // Out of order tick (timestamp 500 < 1000)
    val t2 = MarketTick(exchangeTimestampMs = 500L, serverSyncTimestampMs = null, price = 50100.0, volume = 1.0, sourceIdentity = "BINANCE_SPOT_BTCUSDT", sequenceId = 2L)
    val state2 = verifier.verify(t2, currentLocalWallClockMs = 2000L)
    assertTrue(state2 is IntegrityState.ChronologicalViolation)
    assertTrue(state2.isFailClosed)
  }

  @Test
  fun testNoLookaheadWindowSlicing() {
    val window = TimeSeriesWindow()
    window.addTick(MarketTick(exchangeTimestampMs = 10_000L, serverSyncTimestampMs = null, price = 50000.0, volume = 1.0, sourceIdentity = "BINANCE_SPOT_BTCUSDT", sequenceId = 1L))
    window.addTick(MarketTick(exchangeTimestampMs = 20_000L, serverSyncTimestampMs = null, price = 50100.0, volume = 1.0, sourceIdentity = "BINANCE_SPOT_BTCUSDT", sequenceId = 2L))
    window.addTick(MarketTick(exchangeTimestampMs = 30_000L, serverSyncTimestampMs = null, price = 50200.0, volume = 1.0, sourceIdentity = "BINANCE_SPOT_BTCUSDT", sequenceId = 3L))
    window.addTick(MarketTick(exchangeTimestampMs = 40_000L, serverSyncTimestampMs = null, price = 50300.0, volume = 1.0, sourceIdentity = "BINANCE_SPOT_BTCUSDT", sequenceId = 4L))

    // Slice up to T = 25_000L with 30s window
    val slice = window.getObservationWindow(cutoffTimestampMs = 25_000L, windowDurationSeconds = 30)
    // Must only contain ticks at 10_000L and 20_000L, NEVER 30_000L or 40_000L!
    assertEquals(2, slice.size)
    assertEquals(10_000L, slice[0].timestampMs)
    assertEquals(20_000L, slice[1].timestampMs)
  }

  @Test
  fun testEvaluationTierSampleDepth() {
    assertEquals(EvaluationTier.INSUFFICIENT, EvaluationTier.fromSampleCount(0))
    assertEquals(EvaluationTier.INSUFFICIENT, EvaluationTier.fromSampleCount(99))
    assertEquals(EvaluationTier.PRELIMINARY, EvaluationTier.fromSampleCount(100))
    assertEquals(EvaluationTier.PRELIMINARY, EvaluationTier.fromSampleCount(299))
    assertEquals(EvaluationTier.CALIBRATION_READY, EvaluationTier.fromSampleCount(300))
    assertEquals(EvaluationTier.CALIBRATION_READY, EvaluationTier.fromSampleCount(1500))
  }

  @Test
  fun testTrendEngineFailClosedOnInvalidIntegrity() {
    val engine = TrendEngine()
    val window = TimeSeriesWindow()
    window.addTick(MarketTick(exchangeTimestampMs = 10_000L, serverSyncTimestampMs = null, price = 50000.0, volume = 1.0, sourceIdentity = "BINANCE_SPOT_BTCUSDT", sequenceId = 1L))
    val temporal = TemporalState(currentTimestampMs = 10_000L)

    val output = engine.process(
        timeSeries = window,
        temporalState = temporal,
        integrityState = IntegrityState.FailedClosed("Network dropped")
    )

    assertEquals(TrendDirection.FAIL_CLOSED, output.verdict.direction)
    assertEquals(0.0, output.verdict.judgeConfidence, 0.0001)
    assertTrue(output.verdict.isFailClosed)
  }

  @Test
  fun testTrendEngineUpwardConsensus() {
    val engine = TrendEngine()
    val window = TimeSeriesWindow()
    val baseTime = 1_000_000L
    // Add 15 ticks steadily climbing by 10.0 every second
    for (i in 0 until 15) {
      window.addTick(
          MarketTick(
              exchangeTimestampMs = baseTime + i * 1_000L,
              serverSyncTimestampMs = null,
              price = 60_000.0 + i * 10.0,
              volume = 1.5,
              sourceIdentity = "BINANCE_SPOT_BTCUSDT",
              sequenceId = (i + 1).toLong()
          )
      )
    }

    val temporal = TemporalState(
        currentTimestampMs = baseTime + 14 * 1_000L,
        observationWindowSeconds = 30
    )

    val output = engine.process(
        timeSeries = window,
        temporalState = temporal,
        integrityState = IntegrityState.Nominal
    )

    assertEquals(TrendDirection.UP, output.verdict.direction)
    assertTrue("Judge confidence should be positive for clear upward trend", output.verdict.judgeConfidence > 0.3)
    assertTrue("Slope should be positive", output.evidence.slopePerSecond > 0.0)
    assertTrue("Normalized slope bps should be positive", output.evidence.normalizedSlopeBps > 0.0)
    assertEquals(1.0, output.evidence.rSquared, 0.01) // Strict linear fit
    assertFalse(output.verdict.isFailClosed)
    assertNull(output.evidence.failClosedReason)
  }

  @Test
  fun testTrendEngineDownwardConsensus() {
    val engine = TrendEngine()
    val window = TimeSeriesWindow()
    val baseTime = 1_000_000L
    // Add 15 ticks falling by 15.0 every second
    for (i in 0 until 15) {
      window.addTick(
          MarketTick(
              exchangeTimestampMs = baseTime + i * 1_000L,
              serverSyncTimestampMs = null,
              price = 60_000.0 - i * 15.0,
              volume = 2.0,
              sourceIdentity = "BINANCE_SPOT_BTCUSDT",
              sequenceId = (i + 1).toLong()
          )
      )
    }

    val temporal = TemporalState(
        currentTimestampMs = baseTime + 14 * 1_000L,
        observationWindowSeconds = 30
    )

    val output = engine.process(
        timeSeries = window,
        temporalState = temporal,
        integrityState = IntegrityState.Nominal
    )

    assertEquals(TrendDirection.DOWN, output.verdict.direction)
    assertTrue("Judge confidence should be positive for clear downward trend", output.verdict.judgeConfidence > 0.3)
    assertTrue("Slope should be negative", output.evidence.slopePerSecond < 0.0)
    assertFalse(output.verdict.isFailClosed)
  }

  @Test
  fun testTrendEngineObservableEvidenceAuditability() {
    val engine = TrendEngine()
    val window = TimeSeriesWindow()
    val baseTime = 1_000_000L
    for (i in 0 until 20) {
      window.addTick(
          MarketTick(
              exchangeTimestampMs = baseTime + i * 1_000L,
              serverSyncTimestampMs = null,
              price = 65_000.0 + (i % 3) * 5.0,
              volume = 0.8,
              sourceIdentity = "BINANCE_SPOT_BTCUSDT",
              sequenceId = (i + 1).toLong()
          )
      )
    }

    val temporal = TemporalState(
        currentTimestampMs = baseTime + 19 * 1_000L,
        observationWindowSeconds = 30
    )

    val output = engine.process(
        timeSeries = window,
        temporalState = temporal,
        integrityState = IntegrityState.Nominal
    )

    val ev = output.evidence
    // Verify observable evidence is fully deterministic, finite, and audited
    assertFalse(ev.slopePerSecond.isNaN())
    assertFalse(ev.normalizedSlopeBps.isNaN())
    assertFalse(ev.rSquared.isNaN())
    assertFalse(ev.tStatistic.isNaN())
    assertEquals("BINANCE_SPOT_BTCUSDT", ev.sourceIdentity)
    assertEquals(20, ev.sampleCount)
    assertNotNull(ev.channelPosition)
    assertTrue(ev.channelPosition!! in 0.0..1.0)
  }


}
