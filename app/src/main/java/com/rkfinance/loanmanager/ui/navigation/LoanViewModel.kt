package com.rkfinance.loanmanager.ui.navigation

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.rkfinance.loanmanager.data.database.LoanManagerDatabase
import com.rkfinance.loanmanager.data.entities.Loan
import com.rkfinance.loanmanager.data.repository.LoanRepository
import com.rkfinance.loanmanager.model.LoanDetails
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class LoanViewModel(
    application: Application,
    private val savedStateHandle: SavedStateHandle
) : AndroidViewModel(application) {

    // Initialize repository immediately
    private val repository: LoanRepository = run {
        val db = LoanManagerDatabase.getDatabase(application)
        LoanRepository(db.clientDao(), db.loanDao(), db.paymentDao())
    }

    // StateFlow for selected client
    private val _clientId = MutableStateFlow<Int?>(savedStateHandle["clientId"])
    val clientId: StateFlow<Int?> = _clientId.asStateFlow()

    // Loans for a specific client
    val loansForClient: StateFlow<List<Loan>> = _clientId.flatMapLatest { id ->
        if (id != null) repository.getLoansByClientId(id)
        else emptyFlow()
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // Selected loan ID for viewing/editing details
    private val _selectedLoanId = MutableStateFlow<Int?>(null)
    val selectedLoanDetails: StateFlow<LoanDetails?> = _selectedLoanId.flatMapLatest { loanId ->
        loanId?.let { repository.getLoanDetails(it) } ?: flowOf(null)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    init {
        // Restore clientId from saved state handle
        savedStateHandle.get<Int>("clientId")?.let {
            setClientId(it)
        }
    }

    fun setClientId(clientId: Int) {
        _clientId.value = clientId
        savedStateHandle["clientId"] = clientId
    }

    fun setSelectedLoanId(loanId: Int?) {
        _selectedLoanId.value = loanId
    }

    fun insertLoan(loan: Loan) = viewModelScope.launch {
        repository.insertLoan(loan)
    }

    fun updateLoan(loan: Loan) = viewModelScope.launch {
        repository.updateLoan(loan)
    }

    fun deleteLoan(loan: Loan) = viewModelScope.launch {
        repository.deleteLoan(loan)
    }
}
