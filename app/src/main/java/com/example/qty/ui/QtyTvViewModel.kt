package com.example.qty.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.qty.data.intake.AuthenticDataSource
import com.example.qty.data.intake.MarketTick
import com.example.qty.data.integrity.DataIntegrityVerifier
import com.example.qty.data.integrity.IntegrityState
import com.example.qty.ledger.EvaluationLedgerRepository
import com.example.qty.ledger.EvaluationTier
import com.example.qty.pricedynamics.trend.TrendEngine
import com.example.qty.temporal.TemporalState
import com.example.qty.temporal.TimeSeriesWindow
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * ViewModel orchestrating QtY TV (Phase 1 Trend Engine).
 *
 * Strict separation:
 * - All quantitative math and validation execute in domain layers.
 * - Manages authentic intake, integrity verification, temporal state,
 *   engine evaluation, and evaluation ledger audit flow.
 */
class QtyTvViewModel(
    private val dataSource: AuthenticDataSource,
    private val ledgerRepository: EvaluationLedgerRepository
) : ViewModel() {

    private val integrityVerifier = DataIntegrityVerifier()
    private val timeSeries = TimeSeriesWindow(maxCapacity = 1000)
    private val trendEngine = TrendEngine()

    private val _uiState = MutableStateFlow(QtyTvUiState())
    val uiState: StateFlow<QtyTvUiState> = _uiState.asStateFlow()

    private var streamJob: Job? = null

    init {
        observeLedger()
        bootstrapAndStart()
    }

    private fun observeLedger() {
        viewModelScope.launch {
            ledgerRepository.totalCount.collect { count ->
                _uiState.update { state ->
                    state.copy(
                        totalLedgerCount = count,
                        evaluationTier = EvaluationTier.fromSampleCount(count)
                    )
                }
            }
        }

        viewModelScope.launch {
            ledgerRepository.recentEntries.collect { entries ->
                _uiState.update { state ->
                    state.copy(recentLedgerEntries = entries)
                }
            }
        }
    }

    fun bootstrapAndStart() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isInitialBootstrapping = true,
                    statusMessage = "Bootstrapping authentic historical BTC trades from exchange..."
                )
            }

            val histResult = dataSource.fetchRecentHistoricalTicks()
            if (histResult.isSuccess) {
                val ticks = histResult.getOrThrow()
                var passCount = 0
                for (t in ticks) {
                    val integrity = integrityVerifier.verify(t)
                    if (integrity.isPassing) {
                        timeSeries.addTick(t)
                        passCount++
                    }
                }
                _uiState.update {
                    it.copy(
                        isInitialBootstrapping = false,
                        timeSeriesSize = timeSeries.size(),
                        statusMessage = "Bootstrapped $passCount verified authentic ticks (${ticks.firstOrNull()?.sourceIdentity ?: "EXCHANGE"})."
                    )
                }
                evaluateCurrentState()
                startTelemetryStream()
            } else {
                val err = histResult.exceptionOrNull()?.message ?: "Intake connection failure"
                _uiState.update {
                    it.copy(
                        isInitialBootstrapping = false,
                        integrityState = IntegrityState.FailedClosed("Initial authentic bootstrap failed: $err"),
                        statusMessage = "FAIL_CLOSED: Unable to fetch authentic initial data ($err)."
                    )
                }
            }
        }
    }

    fun startTelemetryStream() {
        if (streamJob?.isActive == true) return

        streamJob = viewModelScope.launch {
            _uiState.update { it.copy(isStreamActive = true) }

            while (isActive) {
                val tickResult = dataSource.fetchLatestTick()
                if (tickResult.isSuccess) {
                    val tick = tickResult.getOrThrow()
                    val integrity = integrityVerifier.verify(tick)

                    if (integrity.isPassing) {
                        timeSeries.addTick(tick)
                        _uiState.update {
                            it.copy(
                                latestTick = tick,
                                integrityState = integrity,
                                timeSeriesSize = timeSeries.size()
                            )
                        }
                        evaluateCurrentState(recordToLedger = true)
                    } else {
                        // Integrity violation - fail closed!
                        _uiState.update {
                            it.copy(
                                integrityState = integrity,
                                statusMessage = "FAIL_CLOSED: Integrity verification failed: $integrity"
                            )
                        }
                        evaluateCurrentState(recordToLedger = false)
                    }
                } else {
                    val ex = tickResult.exceptionOrNull()?.message ?: "Exchange network error"
                    val failClosed = IntegrityState.FailedClosed("Authentic data unavailable: $ex")
                    _uiState.update {
                        it.copy(
                            integrityState = failClosed,
                            statusMessage = "FAIL_CLOSED: $ex"
                        )
                    }
                    evaluateCurrentState(recordToLedger = false)
                }

                delay(1200L)
            }
        }
    }

    fun stopTelemetryStream() {
        streamJob?.cancel()
        streamJob = null
        _uiState.update { it.copy(isStreamActive = false, statusMessage = "Telemetry stream paused.") }
    }

    fun toggleStream() {
        if (_uiState.value.isStreamActive) {
            stopTelemetryStream()
        } else {
            startTelemetryStream()
        }
    }

    fun setObservationWindow(seconds: Int) {
        _uiState.update {
            it.copy(temporalState = it.temporalState.copy(observationWindowSeconds = seconds))
        }
        evaluateCurrentState(recordToLedger = false)
    }

    fun setTargetHorizon(seconds: Int) {
        _uiState.update {
            it.copy(temporalState = it.temporalState.copy(targetHorizonSeconds = seconds))
        }
        evaluateCurrentState(recordToLedger = false)
    }

    private fun evaluateCurrentState(recordToLedger: Boolean = false) {
        val currentState = _uiState.value
        val nowMs = currentState.latestTick?.timestampMs ?: System.currentTimeMillis()
        val updatedTemporal = currentState.temporalState.copy(currentTimestampMs = nowMs)

        val output = trendEngine.process(
            timeSeries = timeSeries,
            temporalState = updatedTemporal,
            integrityState = currentState.integrityState
        )

        val recentPoints = output.windowTicks

        _uiState.update {
            it.copy(
                temporalState = updatedTemporal,
                trendOutput = output,
                recentPriceSeries = recentPoints
            )
        }

        if (recordToLedger && output.evidence.isIntegrityValid) {
            viewModelScope.launch {
                ledgerRepository.recordSnapshot(output)
            }
        }
    }

    fun manualRecordSnapshot() {
        val output = _uiState.value.trendOutput ?: return
        viewModelScope.launch {
            ledgerRepository.recordSnapshot(output)
            _uiState.update { it.copy(statusMessage = "Manually committed Trend evidence snapshot to Ledger.") }
        }
    }

    fun clearLedger() {
        viewModelScope.launch {
            ledgerRepository.clearLedger()
            _uiState.update { it.copy(statusMessage = "Evaluation ledger cleared.") }
        }
    }

    class Factory(
        private val dataSource: AuthenticDataSource,
        private val ledgerRepository: EvaluationLedgerRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return QtyTvViewModel(dataSource, ledgerRepository) as T
        }
    }
}
