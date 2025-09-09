package com.rkfinance.loanmanager.ui.viewmodel

// com/rkfinance/loanmanager/ui/viewmodel/DashboardViewModel.kt

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.rkfinance.loanmanager.data.database.LoanManagerDatabase
import com.rkfinance.loanmanager.data.repository.LoanRepository
import com.rkfinance.loanmanager.model.AreaSummary
import com.rkfinance.loanmanager.model.ClientSummary
import com.rkfinance.loanmanager.model.LoanDetails
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.map

data class DashboardUiState(
    val totalPrincipalLoaned: Double = 0.0,
    val totalCurrentlyOutstanding: Double = 0.0,
    val totalRepaidOnActiveLoans: Double = 0.0, // This needs careful calculation based on what "repaid" means
    val activeLoansCount: Int = 0,
    val overdueLoansCount: Int = 0,
    val areaSummaries: List<AreaSummary> = emptyList(),
    val allLoanDetails: List<LoanDetails> = emptyList() // For detailed breakdown if needed
)

class DashboardViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: LoanRepository

    val dashboardUiState: StateFlow<DashboardUiState>

    init {
        val db = LoanManagerDatabase.getDatabase(application)
        repository = LoanRepository(db.clientDao(), db.loanDao(), db.paymentDao())

        // Combine multiple flows to build the dashboard state
        dashboardUiState = repository.getAllLoanDetailsFlow()
            .combine(repository.getAreaSummaries()) { allLoanDetails, areaSummaries ->
                var totalPrincipal = 0.0
                var totalOutstanding = 0.0
                var totalRepaid = 0.0 // Simplified
                var overdueCount = 0

                allLoanDetails.forEach { details ->
                    totalPrincipal += details.loan.principal
                    totalOutstanding += details.outstandingBalance
                    totalRepaid += details.totalPaid // Sum of all payments made
                    if (details.isOverdue) {
                        overdueCount++
                    }
                }

                DashboardUiState(
                    totalPrincipalLoaned = totalPrincipal,
                    totalCurrentlyOutstanding = totalOutstanding,
                    totalRepaidOnActiveLoans = totalRepaid, // This is total payments made on all loans
                    activeLoansCount = allLoanDetails.count { it.outstandingBalance > 0 },
                    overdueLoansCount = overdueCount,
                    areaSummaries = areaSummaries,
                    allLoanDetails = allLoanDetails
                )
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.Lazily,
                initialValue = DashboardUiState() // Initial empty state
            )
    }

    // You might want specific flows for charts if they need different transformations
    val areaSummariesForChart: Flow<List<AreaSummary>> = repository.getAreaSummaries()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val overdueLoans: Flow<List<LoanDetails>> = repository.getAllLoanDetailsFlow()
        .map { loans -> loans.filter { it.isOverdue } }
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
}