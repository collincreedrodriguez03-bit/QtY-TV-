package com.example

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.example.qty.data.intake.MarketTick
import com.example.qty.data.integrity.IntegrityState
import com.example.qty.ledger.EvaluationTier
import com.example.qty.pricedynamics.trend.TrendDirection
import com.example.qty.pricedynamics.trend.TrendEngineOutput
import com.example.qty.pricedynamics.trend.TrendEvidence
import com.example.qty.pricedynamics.trend.TrendJudgeVerdict
import com.example.qty.temporal.TemporalState
import com.example.qty.ui.QtyTvUiState
import com.example.qty.ui.screens.QtyTvDashboard
import com.example.ui.theme.MyApplicationTheme
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.RoborazziOptions
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [36])
class GreetingScreenshotTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun dashboard_screenshot() {
    val sampleTemporal = TemporalState(currentTimestampMs = 1742540000000L, observationWindowSeconds = 60, targetHorizonSeconds = 30)
    val sampleTick = MarketTick(
        exchangeTimestampMs = 1742540000000L,
        serverSyncTimestampMs = null,
        price = 98450.25,
        volume = 1.45,
        sourceIdentity = "BINANCE_SPOT_BTCUSDT",
        sequenceId = 1001L
    )
    val samplePoints = listOf(
        Pair(1742539950000L, 98200.0),
        Pair(1742539960000L, 98250.0),
        Pair(1742539970000L, 98310.0),
        Pair(1742539980000L, 98390.0),
        Pair(1742539990000L, 98420.0),
        Pair(1742540000000L, 98450.25)
    )
    val sampleEvidence = TrendEvidence(
        timestampMs = 1742540000000L,
        observationWindowSeconds = 60,
        sampleCount = 6,
        sourceIdentity = "BINANCE_SPOT_BTCUSDT",
        latestPrice = 98450.25,
        slopePerSecond = 5.0,
        normalizedSlopeBps = 0.51,
        rSquared = 0.98,
        tStatistic = 14.2,
        emaFast = 98410.0,
        emaSlow = 98320.0,
        emaSpreadPercent = 0.091,
        cumulativeDriftBps = 25.4,
        channelPosition = 0.95,
        isIntegrityValid = true
    )
    val sampleVerdict = TrendJudgeVerdict(
        direction = TrendDirection.UP,
        judgeConfidence = 0.82,
        directionalAgreement = true,
        isFailClosed = false,
        justification = "Confirmed Upward Trend"
    )
    val sampleOutput = TrendEngineOutput(
        temporalState = sampleTemporal,
        evidence = sampleEvidence,
        verdict = sampleVerdict,
        windowTicks = samplePoints
    )
    val sampleState = QtyTvUiState(
        latestTick = sampleTick,
        integrityState = IntegrityState.Nominal,
        temporalState = sampleTemporal,
        trendOutput = sampleOutput,
        timeSeriesSize = 6,
        recentPriceSeries = samplePoints,
        totalLedgerCount = 42,
        evaluationTier = EvaluationTier.INSUFFICIENT,
        isStreamActive = true,
        statusMessage = "Authentic telemetry streaming nominal",
        isInitialBootstrapping = false
    )

    composeTestRule.setContent {
        MyApplicationTheme {
            QtyTvDashboard(
                uiState = sampleState,
                onToggleStream = {},
                onBootstrapRetry = {},
                onObservationWindowSelected = {},
                onTargetHorizonSelected = {},
                onCommitSnapshot = {},
                onClearLedger = {},
                enableAnimation = false
            )
        }
    }

    composeTestRule.waitForIdle()

    val roborazziOptions = RoborazziOptions(
        compareOptions = RoborazziOptions.CompareOptions(
            changeThreshold = 0.05f
        )
    )

    composeTestRule.onRoot().captureRoboImage(
        filePath = "src/test/screenshots/greeting.png",
        roborazziOptions = roborazziOptions
    )
  }
}
