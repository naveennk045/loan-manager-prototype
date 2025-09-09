package com.rkfinance.loanmanager.data.repository

import com.rkfinance.loanmanager.data.daos.ClientDao
import com.rkfinance.loanmanager.data.daos.LoanDao
import com.rkfinance.loanmanager.data.daos.PaymentDao
import com.rkfinance.loanmanager.data.entities.Client
import com.rkfinance.loanmanager.data.entities.Loan
import com.rkfinance.loanmanager.data.entities.Payment
import com.rkfinance.loanmanager.data.enums.Area
import com.rkfinance.loanmanager.data.enums.Frequency
import com.rkfinance.loanmanager.model.AreaSummary
import com.rkfinance.loanmanager.model.ClientSummary
import com.rkfinance.loanmanager.model.LoanDetails
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import java.util.Calendar
import kotlin.math.pow

class LoanRepository(
    private val clientDao: ClientDao,
    private val loanDao: LoanDao,
    private val paymentDao: PaymentDao
) {

    // Client Operations
    fun getAllClients(): Flow<List<Client>> = clientDao.getAllClients()
    suspend fun insertClient(client: Client) = clientDao.insertClient(client)
    suspend fun updateClient(client: Client) = clientDao.updateClient(client)
    suspend fun deleteClient(client: Client) = clientDao.deleteClient(client)
    fun getClientById(clientId: Int): Flow<Client?> = clientDao.getClientById(clientId)

    // Loan Operations
    fun getLoansByClientId(clientId: Int): Flow<List<Loan>> = loanDao.getLoansByClientId(clientId)
    fun getAllLoans(): Flow<List<Loan>> = loanDao.getAllLoans()
    suspend fun insertLoan(loan: Loan) = loanDao.insertLoan(loan)
    suspend fun updateLoan(loan: Loan) = loanDao.updateLoan(loan)
    suspend fun deleteLoan(loan: Loan) = loanDao.deleteLoan(loan)
    fun getLoanById(loanId: Int): Flow<Loan?> = loanDao.getLoanById(loanId)

    // Payment Operations
    fun getPaymentsByLoanId(loanId: Int): Flow<List<Payment>> = paymentDao.getPaymentsByLoanId(loanId)
    suspend fun insertPayment(payment: Payment) = paymentDao.insertPayment(payment)
    suspend fun deletePayment(payment: Payment) = paymentDao.deletePayment(payment)


    // --- Business Logic ---

    fun getLoanDetails(loanId: Int): Flow<LoanDetails?> {
        return loanDao.getLoanById(loanId).combine(paymentDao.getPaymentsByLoanId(loanId)) { loan, payments ->
            loan?.let {
                val totalPaid = payments.sumOf { it.amount }
                val outstandingBalance = calculateOutstandingBalance(it, totalPaid)
                LoanDetails(
                    loan = it,
                    payments = payments,
                    totalPaid = totalPaid,
                    outstandingBalance = outstandingBalance,
                    isOverdue = isLoanOverdue(it, outstandingBalance)
                )
            }
        }
    }

    // Simplified interest calculation for example. Real-world might be more complex.
    private fun calculateTotalDue(loan: Loan): Double {
        val calendar = Calendar.getInstance()
        val startDate = Calendar.getInstance().apply { timeInMillis = loan.startDate }
        val today = Calendar.getInstance()

        var periods = 0
        when (loan.frequency) {
            Frequency.DAILY -> {
                periods = ((today.timeInMillis - startDate.timeInMillis) / (1000 * 60 * 60 * 24)).toInt()
            }
            Frequency.WEEKLY -> {
                periods = ((today.timeInMillis - startDate.timeInMillis) / (1000 * 60 * 60 * 24 * 7)).toInt()
            }
            Frequency.MONTHLY -> {
                var months = (today.get(Calendar.YEAR) - startDate.get(Calendar.YEAR)) * 12
                months -= startDate.get(Calendar.MONTH)
                months += today.get(Calendar.MONTH)
                periods = if (months <= 0) 0 else months
            }
        }

        // This is a simplified compound interest. Actual loan products have specific formulas.
        // P(1 + r/n)^(nt) where n is compounding frequency per year, t is years.
        // For simplicity, let's assume interest rate is per period defined by Frequency
        val periodicInterestRate = loan.interestRate / getPeriodsInYear(loan.frequency)
        return loan.principal * (1 + periodicInterestRate).pow(periods.toDouble())
    }

    private fun getPeriodsInYear(frequency: Frequency): Int {
        return when (frequency) {
            Frequency.DAILY -> 365
            Frequency.WEEKLY -> 52
            Frequency.MONTHLY -> 12
        }
    }


    fun calculateOutstandingBalance(loan: Loan, totalPaid: Double): Double {
        val totalDue = calculateTotalDue(loan) // This needs a proper interest calculation logic
        return (totalDue - totalPaid).coerceAtLeast(0.0)
    }

    fun isLoanOverdue(loan: Loan, outstandingBalance: Double): Boolean {
        if (outstandingBalance <= 0) return false
        val today = Calendar.getInstance().timeInMillis
        // Simple overdue check: if end date passed and balance > 0
        return loan.endDate?.let { it < today } ?: false
        // More complex logic: check if next payment date has passed without full payment for that period
    }


    fun getClientSummary(clientId: Int): Flow<ClientSummary?> {
        return clientDao.getClientById(clientId)
            .combine(loanDao.getLoansByClientId(clientId)) { client, loans ->
                client?.let {
                    var totalLoaned = 0.0
                    var totalOutstanding = 0.0
                    var totalPaidOnActiveLoans = 0.0 // Approximate

                    loans.forEach { loan ->
                        totalLoaned += loan.principal
                        val payments = paymentDao.getPaymentsByLoanId(loan.loanId).firstOrNull() ?: emptyList()
                        val paidForThisLoan = payments.sumOf { it.amount }
                        totalPaidOnActiveLoans += paidForThisLoan
                        totalOutstanding += calculateOutstandingBalance(loan, paidForThisLoan)
                    }
                    ClientSummary(
                        client = it,
                        totalPrincipalLoaned = totalLoaned,
                        totalAmountPaid = totalPaidOnActiveLoans, // This might not be total paid across all time if loans are deleted
                        currentOutstandingBalance = totalOutstanding,
                        numberOfLoans = loans.size
                    )
                }
            }
    }

    fun getAreaSummaries(): Flow<List<AreaSummary>> {
        return clientDao.getAllClients().combine(loanDao.getAllLoans()) { clients, allLoans ->
            val clientMap = clients.associateBy { it.clientId }
            val loansByArea = allLoans.groupBy { loan ->
                clientMap[loan.clientId]?.area // Group loans by the area of their client
            }

            Area.values().map { area ->
                val loansInArea = loansByArea[area] ?: emptyList()
                var totalExposure = 0.0
                var activeLoansCount = 0

                loansInArea.forEach { loan ->
                    val payments = paymentDao.getPaymentsByLoanId(loan.loanId).firstOrNull() ?: emptyList()
                    val paid = payments.sumOf { it.amount }
                    val outstanding = calculateOutstandingBalance(loan, paid)
                    if (outstanding > 0) {
                        totalExposure += outstanding
                        activeLoansCount++
                    }
                }
                AreaSummary(area, totalExposure, activeLoansCount, loansInArea.size)
            }
        }
    }

    fun getAllLoanDetailsFlow(): Flow<List<LoanDetails>> {
        return loanDao.getAllLoans().map { loans ->
            loans.map { loan ->
                val payments = paymentDao.getPaymentsByLoanId(loan.loanId).firstOrNull() ?: emptyList()
                val totalPaid = payments.sumOf { it.amount }
                val outstanding = calculateOutstandingBalance(loan, totalPaid)
                LoanDetails(
                    loan = loan,
                    payments = payments,
                    totalPaid = totalPaid,
                    outstandingBalance = outstanding,
                    isOverdue = isLoanOverdue(loan, outstanding)
                )
            }
        }
    }
}
