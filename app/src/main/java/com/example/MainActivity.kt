package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.qty.data.intake.AuthenticDataSource
import com.example.qty.ledger.EvaluationLedgerRepository
import com.example.qty.ledger.QtyDatabase
import com.example.qty.ui.QtyTvViewModel
import com.example.qty.ui.screens.QtyTvDashboard
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.TelemetryObsidian

class MainActivity : ComponentActivity() {

    private val viewModel: QtyTvViewModel by viewModels {
        val database = QtyDatabase.getInstance(applicationContext)
        val ledgerRepository = EvaluationLedgerRepository(database.trendLedgerDao())
        val dataSource = AuthenticDataSource()
        QtyTvViewModel.Factory(dataSource, ledgerRepository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = TelemetryObsidian
                ) {
                    QtyTvDashboard(
                        uiState = uiState,
                        onToggleStream = { viewModel.toggleStream() },
                        onBootstrapRetry = { viewModel.bootstrapAndStart() },
                        onObservationWindowSelected = { windowSec -> viewModel.setObservationWindow(windowSec) },
                        onTargetHorizonSelected = { horizonSec -> viewModel.setTargetHorizon(horizonSec) },
                        onCommitSnapshot = { viewModel.manualRecordSnapshot() },
                        onClearLedger = { viewModel.clearLedger() }
                    )
                }
            }
        }
    }
}
