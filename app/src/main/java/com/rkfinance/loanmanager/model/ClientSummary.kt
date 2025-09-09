package com.rkfinance.loanmanager.model

import com.rkfinance.loanmanager.data.entities.Client

data class ClientSummary(
    val client: Client,
    val totalPrincipalLoaned: Double,
    val totalAmountPaid: Double, // Across active loans
    val currentOutstandingBalance: Double,
    val numberOfLoans: Int
)