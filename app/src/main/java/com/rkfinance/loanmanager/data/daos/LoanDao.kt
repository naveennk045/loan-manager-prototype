package com.rkfinance.loanmanager.data.daos


import androidx.room.*
import com.rkfinance.loanmanager.data.entities.Loan
import kotlinx.coroutines.flow.Flow

@Dao
interface LoanDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLoan(loan: Loan): Long

    @Update
    suspend fun updateLoan(loan: Loan)

    @Delete
    suspend fun deleteLoan(loan: Loan)

    @Query("SELECT * FROM loans WHERE clientId = :clientId ORDER BY startDate DESC")
    fun getLoansByClientId(clientId: Int): Flow<List<Loan>>

    @Query("SELECT * FROM loans ORDER BY startDate DESC")
    fun getAllLoans(): Flow<List<Loan>>

    @Query("SELECT * FROM loans WHERE loanId = :id")
    fun getLoanById(id: Int): Flow<Loan?>
}