package com.rkfinance.loanmanager.data.entities


import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.rkfinance.loanmanager.data.enums.Frequency

@Entity(
    tableName = "loans",
    foreignKeys = [
        ForeignKey(
            entity = Client::class,
            parentColumns = ["clientId"],
            childColumns = ["clientId"],
            onDelete = ForeignKey.CASCADE // Or RESTRICT depending on your business rule
        )
    ]
)
data class Loan(
    @PrimaryKey(autoGenerate = true)
    val loanId: Int = 0,
    val clientId: Int, // Foreign Key
    val principal: Double,
    val interestRate: Double, // Annual interest rate (e.g., 0.1 for 10%)
    val frequency: Frequency,
    val startDate: Long, // Unix timestamp
    val endDate: Long? = null // Unix timestamp, null if ongoing
)