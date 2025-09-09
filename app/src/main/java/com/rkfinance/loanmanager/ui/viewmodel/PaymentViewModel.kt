package com.rkfinance.loanmanager.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.rkfinance.loanmanager.data.database.LoanManagerDatabase
import com.rkfinance.loanmanager.data.entities.Payment
import com.rkfinance.loanmanager.data.repository.LoanRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class PaymentViewModel(
    application: Application,
    private val savedStateHandle: SavedStateHandle
) : AndroidViewModel(application) {

    // Initialize repository immediately
    private val repository: LoanRepository = run {
        val db = LoanManagerDatabase.getDatabase(application)
        LoanRepository(db.clientDao(), db.loanDao(), db.paymentDao())
    }

    // Track selected loan
    private val _loanId = MutableStateFlow<Int?>(savedStateHandle["loanId"])
    val loanId: StateFlow<Int?> = _loanId.asStateFlow()

    // Payments for the current loan
    val paymentsForLoan: StateFlow<List<Payment>> = _loanId.flatMapLatest { id ->
        if (id != null) repository.getPaymentsByLoanId(id)
        else emptyFlow()
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    init {
        // Restore loanId from saved state
        savedStateHandle.get<Int>("loanId")?.let {
            setLoanId(it)
        }
    }

    fun setLoanId(loanId: Int) {
        _loanId.value = loanId
        savedStateHandle["loanId"] = loanId
    }

    fun insertPayment(payment: Payment) = viewModelScope.launch {
        _loanId.value?.let { id ->
            repository.insertPayment(payment.copy(loanId = id))
        }
    }

    fun deletePayment(payment: Payment) = viewModelScope.launch {
        repository.deletePayment(payment)
    }
}
