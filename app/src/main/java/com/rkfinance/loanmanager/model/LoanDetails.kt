package com.rkfinance.loanmanager.model

import com.rkfinance.loanmanager.data.entities.Loan
import com.rkfinance.loanmanager.data.entities.Payment

data class LoanDetails(
    val loan: Loan,
    val payments: List<Payment>,
    val totalPaid: Double,
    val outstandingBalance: Double,
    val isOverdue: Boolean
)