package com.rkfinance.loanmanager.data.daos


import androidx.room.*
import com.rkfinance.loanmanager.data.entities.Payment
import kotlinx.coroutines.flow.Flow

@Dao
interface PaymentDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayment(payment: Payment): Long

    @Delete // Usually payments are not updated, only inserted or deleted
    suspend fun deletePayment(payment: Payment)

    @Query("SELECT * FROM payments WHERE loanId = :loanId ORDER BY paymentDate DESC")
    fun getPaymentsByLoanId(loanId: Int): Flow<List<Payment>>
}